/*
 * R : A Computer Language for Statistical Data Analysis
 * Copyright (C) 1995, 1996  Robert Gentleman and Ross Ihaka
 * Copyright (C) 1997--2008  The R Development Core Team
 * Copyright (C) 2003, 2004  The R Foundation
 * Copyright (C) 2010 bedatadriven
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.renjin.primitives.subset;

import com.google.common.base.Strings;
import org.renjin.eval.Context;
import org.renjin.eval.EvalException;
import org.renjin.invoke.annotations.*;
import org.renjin.iterator.IntIterator;
import org.renjin.methods.MethodDispatch;
import org.renjin.sexp.*;
import org.renjin.util.NamesBuilder;

public class Subsetting {

  private Subsetting() {

  }

  @Builtin("$")
  public static SEXP getElementByName(PairList list, @Unevaluated SEXP nameExp) {
    String name = asString(nameExp);
    SEXP match = null;
    int matchCount = 0;

    for (PairList.Node node : list.nodes()) {
      if (node.hasTag()) {
        if (node.getTag().getPrintName().startsWith(name)) {
          match = node.getValue();
          matchCount++;
        }
      }
    }
    return matchCount == 1 ? match : Null.INSTANCE;
  }

  private static Symbol asSymbol(SEXP nameExp) {
    if(nameExp instanceof Symbol) {
      return (Symbol) nameExp;
    } else if(nameExp instanceof StringVector && nameExp.length() == 1) {
      return Symbol.get( ((StringVector) nameExp).getElementAsString(0) );
    } else {
      throw new EvalException("illegal argument: " + nameExp);
    }
  }

  private static String asString(SEXP nameExp) {
    if(nameExp instanceof Symbol) {
      return ((Symbol) nameExp).getPrintName();
    } else if(nameExp instanceof StringVector && nameExp.length() == 1) {
      return ((StringVector) nameExp).getElementAsString(0);
    } else {
      throw new EvalException("illegal argument: " + nameExp);
    }
  }

  @Builtin("$")
  public static SEXP getElementByName(Environment env, @Unevaluated SEXP nameExp) {
    String name = asString(nameExp);
    SEXP value = env.getVariable(name);
    if (value == Symbol.UNBOUND_VALUE) {
      return Null.INSTANCE;
    }
    return value;
  }

  @Builtin("$")
  public static SEXP getMemberByName(ExternalPtr<?> externalPtr, @Unevaluated SEXP nameExp) {
    return externalPtr.getMember(asSymbol(nameExp));
  }

  @Builtin("$<-")
  public static SEXP setElementByName(ExternalPtr<?> externalPtr, @Unevaluated SEXP nameExp, SEXP value) {
    externalPtr.setMember(asSymbol(nameExp), value);
    return externalPtr;
  }

  @Builtin("@")
  public static SEXP getSlotValue(@Current Context context, @Current MethodDispatch methods, SEXP object,
                                  @Unevaluated Symbol slotName) {
    if(slotName.getPrintName().equals(".Data")) {
      return context.evaluate(FunctionCall.newCall(Symbol.get("getDataPart"), object), methods.getMethodsNamespace());
    }
    
    SEXP value = object.getAttribute(slotName);
    if(value == Symbols.S4_NULL) {
      return Null.INSTANCE;
    } else {
      return value;
    }
  }

  @Builtin("$")
  public static SEXP getElementByName(ListVector list,
      @Unevaluated SEXP nameExp) {
    String name = asString(nameExp);
    SEXP match = null;
    int matchCount = 0;

    for (int i = 0; i != list.length(); ++i) {
      String elementName = list.getName(i);
      if (!StringVector.isNA(elementName)) {
        if (elementName.equals(name)) {
          return list.getElementAsSEXP(i);
        } else if (elementName.startsWith(name)) {
          match = list.get(i);
          matchCount++;
        }
      }
    }
    return matchCount == 1 ? match : Null.INSTANCE;
  }

  @Builtin("$<-")
  public static SEXP setElementByName(ListVector list,
      @Unevaluated Symbol name, SEXP value) {
    return setSingleElement(list.newCopyNamedBuilder(), name.getPrintName(), value);

  }

  @Builtin("$<-")
  public static SEXP setElementByName(PairList.Node pairList,
      @Unevaluated Symbol name, SEXP value) {
    return setSingleElement(pairList.newCopyBuilder(), name.getPrintName(), value);
  }

  @Builtin("$<-")
  public static SEXP setElementByName(Environment env,
      @Unevaluated Symbol name, SEXP value) {
    env.setVariable(name, value);
    return env;
  }

  /**
   * Same as "[" but not generic
   */
  @Builtin(".subset")
  public static SEXP subset(SEXP source, @ArgumentList ListVector arguments,
      @NamedFlag("drop") @DefaultValue(true) boolean drop) {
    Vector vector;
    if(source instanceof Vector) {
      vector = (Vector)source;
    } else if(source instanceof PairList) {
      vector = ((PairList) source).toVector();
    } else {
      throw new EvalException(source.getClass().getName());
    }
    return getSubset(vector, arguments, drop);
  }

  @Generic
  @Builtin("[")
  public static SEXP getSubset(SEXP source, @ArgumentList ListVector subscripts,
      @NamedFlag("drop") @DefaultValue(true) boolean drop) {

    if (source == Null.INSTANCE) {
      // handle an exceptional case: if source is NULL,
      // the result is always null
      return Null.INSTANCE;
    
    } else if(source instanceof FunctionCall) {
      return getCallSubset((FunctionCall) source, subscripts);

    } else if(source instanceof PairList.Node) {
      return getSubset(((PairList.Node) source).toVector(), subscripts, drop);
      
    } else if(source instanceof Vector) {
      return getSubset((Vector)source, subscripts, drop);
      
    } else {
      throw new EvalException("invalid source");
    }
  }

  private static SEXP getCallSubset(FunctionCall source, ListVector subscripts) {
    Selection selection = new VectorIndexSelection(source, subscripts.get(0));
    FunctionCall.Builder call = FunctionCall.newBuilder();
    call.withAttributes(source.getAttributes());

    IntIterator it = selection.intIterator();
    while(it.hasNext()) {
      int sourceIndex = it.nextInt();
      call.addCopy(source.getNode(sourceIndex));
    }
    return call.build();
  }
  
  private static SEXP getSubset(Vector source, ListVector subscripts, boolean drop) {
    return new SubscriptOperation()
      .setSource(source, subscripts)
      .setDrop(drop)
      .extract();
  }

  
  @Generic
  @Builtin("[<-")
  public static SEXP setSubset(SEXP source, @ArgumentList ListVector arguments) {
    
    SEXP replacement = arguments.getElementAsSEXP(arguments.length() - 1);
   
    return new SubscriptOperation()
        .setSource(source, arguments, 0, 1)
        .replace(replacement);
  }
  
  @Generic
  @Builtin("[[<-")
  public static SEXP setSingleElement(AtomicVector source, Vector index, Vector replacement) {
    // When applied to atomic vectors, [[<- works exactly like [<-
    // EXCEPT when the vector is zero-length, and then we create a new list
    if(source.length() == 0) {
      return setSingleElement(new ListVector.NamedBuilder(), 
          index.getElementAsInt(0), 
          replacement);
    } else {
      return new SubscriptOperation()
      .setSource(source, new ListVector(index), 0, 0)
      .replace(replacement); 
    }
  }
    
  
  @Generic
  @Builtin("[[<-")
  public static Environment setSingleElement(Environment environment, String name, SEXP replacement) {
     environment.setVariable(name, replacement);
     return environment; 
  }
  
  @Generic
  @Builtin("[[<-")
  public static SEXP setSingleElement(PairList.Node pairList, int indexToReplace, SEXP replacement) {
    return setSingleElement(pairList.newCopyBuilder(), indexToReplace, replacement);
  }  
  
  @Generic
  @Builtin("[[<-")
  public static SEXP setSingleElement(PairList.Node pairList, String nameToReplace, SEXP replacement) {
     return setSingleElement(pairList.newCopyBuilder(), nameToReplace, replacement);
  }

  @Generic
  @Builtin("[[<-")
  public static SEXP setSingleElement(ListVector list, int indexToReplace, SEXP replacement) {
    return setSingleElement(list.newCopyNamedBuilder(), indexToReplace, replacement);
  }
  
  @Generic
  @Builtin("[[<-")
  public static SEXP setSingleElement(ListVector list, String nameToReplace, SEXP replacement) {
    return setSingleElement(list.newCopyNamedBuilder(), nameToReplace, replacement);
  }

  private static SEXP setSingleElement(ListBuilder result,
      int indexToReplace, SEXP replacement) {
    if(replacement == Null.INSTANCE) {
      // REMOVE element
      if(indexToReplace < result.length()) {
        result.remove(indexToReplace - 1);
      }
    } else if(indexToReplace <= result.length()) {
      // REPLACE element
      result.set(indexToReplace - 1, replacement);
    } else {
      // ADD new elements 
      int newLength = indexToReplace;
      while(result.length() < newLength-1) {
        result.add(Null.INSTANCE);
      }
      result.add(replacement);
    }
    return result.build();
  }

  private static SEXP setSingleElement(ListBuilder builder, String nameToReplace, SEXP replacement) {
    int index = builder.getIndexByName(nameToReplace);
    if(replacement == Null.INSTANCE) {
      if(index != -1) {
        builder.remove(index);
      }
    } else {
      if(index == -1) {
        builder.add(nameToReplace, replacement);
      } else {
        builder.set(index, replacement);
      }
    }
    return builder.build();
  }
    
  @Generic
  @Builtin("[[")
  public static SEXP getSingleElement(Vector vector, int index) {
    if (vector.length() == 0) {
      return Null.INSTANCE;
    }

    EvalException.check(index >= 0, "attempt to select more than one element");
    EvalException.check(index != 0, "attempt to select less than one element");
    EvalException.check(index <= vector.length(), "subscript out of bounds");

    return vector.getElementAsSEXP(index - 1);
  }

  @Generic
  @Builtin("[[")
  public static SEXP getSingleElement(PairList.Node pairlist, int index) {
    if (index > pairlist.length()) {
      throw new EvalException("subscript out of bounds");
    } else {
      return pairlist.getElementAsSEXP(index - 1);
    }
  }

  /**
   * Same as [[ but not marked as @Generic
   */
  @Builtin(".subset2")
  public static SEXP getSingleElementDefault(Vector vector, int index) {
    return getSingleElement(vector, index);
  }

  @Builtin(".subset2")
  public static SEXP getSingleElementDefault(Vector vector, int index, boolean exact) {
    return getSingleElement(vector, index);
  }


  @Builtin(".subset2")
  public static SEXP getSingleElementDefaultByExactName(Vector vector,
      String name) {
    return getSingleElementByExactName(vector, name);
  }


  @Builtin(".subset2")
  public static SEXP getSingleElementDefaultByExactName(Vector vector,
      String name, boolean exact) {

    return getSingleElementByName(vector, name, exact);
  }

  @Generic
  @Builtin("[[")
  public static SEXP getSingleElementByExactName(Vector vector, String subscript) {
    int index = vector.getIndexByName(subscript);
    return index == -1 ? Null.INSTANCE : vector.getElementAsSEXP(index);
  }

  @Generic
  @Builtin("[[")
  public static SEXP getSingleElementByExactName(PairList.Node pairlist,
      String subscript) {
    return getSingleElementByExactName(pairlist.toVector(), subscript);
  }

  @Generic
  @Builtin("[[")
  public static SEXP getSingleElementByExactName(Environment env,
      String subscript) {
    SEXP value = env.getVariable(subscript);
    if (value == Symbol.UNBOUND_VALUE)
      return Null.INSTANCE;
    return value;
  }

  @Generic
  @Builtin("[[")
  public static SEXP getSingleElementByName(Vector vector, String subscript,
      boolean exact) {
    if (exact) {
      return getSingleElementByExactName(vector, subscript);
    } else {
      int matchCount = 0;
      SEXP match = Null.INSTANCE;

      for (int i = 0; i != vector.length(); ++i) {
        if (Strings.nullToEmpty(vector.getName(i)).startsWith(subscript)) {
          match = vector.getElementAsSEXP(i);
          matchCount++;
        }
      }

      return matchCount == 1 ? match : Null.INSTANCE;
    }
  }

  @Generic
  @Builtin("[[")
  public static SEXP getSingleElementByName(PairList.Node pairlist,
      String subscript, boolean exact) {
    return getSingleElementByName(pairlist.toVector(), subscript, exact);
  }
}
