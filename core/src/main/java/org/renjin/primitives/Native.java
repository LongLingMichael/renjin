package org.renjin.primitives;

import java.awt.Graphics;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.List;

import org.renjin.base.Base;
import org.renjin.eval.Context;
import org.renjin.eval.EvalException;
import org.renjin.gcc.runtime.DoublePtr;
import org.renjin.gcc.runtime.IntPtr;
import org.renjin.invoke.annotations.Builtin;
import org.renjin.invoke.reflection.FunctionBinding;
import org.renjin.methods.Methods;
import org.renjin.invoke.annotations.ArgumentList;
import org.renjin.invoke.annotations.Current;
import org.renjin.invoke.annotations.NamedFlag;
import org.renjin.sexp.*;
import org.renjin.sexp.ExternalPtr;

import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

public class Native {

  public static final boolean DEBUG = false;


  @Builtin(".C")
  public static SEXP dotC(@Current Context context,
                          @Current Environment rho,
                          SEXP methodExp,
                          @ArgumentList ListVector callArguments,
                          @NamedFlag("PACKAGE") String packageName,
                          @NamedFlag("NAOK") boolean naOk,
                          @NamedFlag("DUP") boolean dup,
                          @NamedFlag("ENCODING") boolean encoding) {

    Method method;

    if(methodExp instanceof StringVector) {
      String methodName = ((StringVector) methodExp).getElementAsString(0);


      if(packageName.equals("base")) {
        return delegateToJavaMethod(context, Base.class, methodName, callArguments);
      }

      method = Iterables.getOnlyElement(findMethod(getPackageClass(packageName), methodName));

    } else if(methodExp instanceof ExternalPtr && ((ExternalPtr) methodExp).getInstance() instanceof Method) {
      method = (Method) ((ExternalPtr) methodExp).getInstance();

    } else {
      throw new EvalException("Invalid method argument of type %s", methodExp.getTypeName());
    }

    Object[] nativeArguments = new Object[method.getParameterTypes().length];
    for(int i=0;i!=nativeArguments.length;++i) {
      Type type = method.getParameterTypes()[i];
      if(type.equals(IntPtr.class)) {
        nativeArguments[i] = intPtrFromVector(callArguments.get(i));
      } else if(type.equals(DoublePtr.class)) {
        nativeArguments[i] = doublePtrFromVector(callArguments.get(i));
      } else {
         throw new EvalException("Don't know how to marshall type " + callArguments.get(i).getClass().getName() +
                 " to for C argument " +  type + " in call to " + method.getName());
      }
    }

    try {
      method.invoke(null, nativeArguments);
    } catch (Exception e) {
      throw new EvalException(e);
    }

    ListVector.NamedBuilder builder = new ListVector.NamedBuilder();
    for(int i=0;i!=nativeArguments.length;++i) {
      if(DEBUG) {
        java.lang.System.out.println(callArguments.getName(i) + " = " + nativeArguments[i].toString());
      }
      builder.add(callArguments.getName(i), sexpFromPointer(nativeArguments[i]));
    }
    return builder.build();
  }

  private static void dumpCall(String methodName, String packageName, ListVector callArguments) {
    java.lang.System.out.print(".C('" + methodName + "', ");
    for(NamedValue arg : callArguments.namedValues()) {
      if(!Strings.isNullOrEmpty(arg.getName())) {
       java.lang.System.out.print(arg.getName() + " = ");
      }
      java.lang.System.out.println(Deparse.deparse(null, arg.getValue(), 80, false, 0, 0) + ", ");
    }
    java.lang.System.out.println("PACKAGE = '" + packageName + "')");
  }

  public static SEXP sexpFromPointer(Object ptr) {
    // Currently, our GCC bridge doesn't support storing values
    // to fields, so we can be confident that no other references
    // to these pointers exist
    if(ptr instanceof DoublePtr) {
      return DoubleArrayVector.unsafe(((DoublePtr) ptr).array);
    } else if(ptr instanceof IntPtr) {
      return new IntArrayVector(((IntPtr) ptr).array);
    } else {
      throw new UnsupportedOperationException(ptr.toString());
    }
  }

  public static DoublePtr doublePtrFromVector(SEXP sexp) {
    if(!(sexp instanceof AtomicVector)) {
      throw new EvalException("expected atomic vector");
    }
    return new DoublePtr(((AtomicVector) sexp).toDoubleArray());
  }

  public static IntPtr intPtrFromVector(SEXP sexp) {
    if(!(sexp instanceof AtomicVector)) {
      throw new EvalException("expected atomic vector");
    }
    AtomicVector vector = (AtomicVector)sexp;
    int[] array = new int[vector.length()];
    for(int i=0;i!=array.length;++i) {
      array[i] = vector.getElementAsInt(i);
    }
    return new IntPtr(array, 0);
  }

  /**
   * Invokes a method compiled to JVM byte code from Fortran, applying the correct calling
   * conventions, etc. This method differs from the
   *
   * @param context
   * @param rho
   * @param methodName
   * @param callArguments
   * @param packageName
   * @param naOk
   * @param dup
   * @param encoding
   * @return
   */
  @Builtin(".Fortran")
  public static SEXP dotFortran(@Current Context context,
                          @Current Environment rho,
                          SEXP methodExp,
                          @ArgumentList ListVector callArguments,
                          @NamedFlag("PACKAGE") String packageName,
                          @NamedFlag("CLASS") String className,
                          @NamedFlag("NAOK") boolean naOk,
                          @NamedFlag("DUP") boolean dup,
                          @NamedFlag("ENCODING") boolean encoding) {

    // quick spike: fortran functions in the "base" package are all
    // defined in libappl, so point us to that class.
    // TODO: map package names to implementation classes


    Method method;
    if(methodExp instanceof StringVector) {
      if("base".equals(packageName)) {
        className = "org.renjin.appl.Appl";
      }
      String methodName = ((StringVector) methodExp).getElementAsString(0);
      method = findFortranMethod(className, methodName);

    } else if(methodExp instanceof ExternalPtr && ((ExternalPtr) methodExp).getInstance() instanceof Method) {
      method = (Method) ((ExternalPtr) methodExp).getInstance();
    } else {
      throw new EvalException("Invalid argument type for method = %s", methodExp.getTypeName());
    }

    Class<?>[] fortranTypes = method.getParameterTypes();
    if(fortranTypes.length != callArguments.length()) {
      throw new EvalException("Invalid number of args");
    }

    Object[] fortranArgs = new Object[fortranTypes.length];
    ListVector.NamedBuilder returnValues = ListVector.newNamedBuilder();

    // For .Fortran() calls, we make a copy of the arguments, pass them by
    // reference to the fortran subroutine, and then return the modified arguments
    // as a ListVector.

    for(int i=0;i!=callArguments.length();++i) {
      AtomicVector vector = (AtomicVector) callArguments.get(i);
      if(fortranTypes[i].equals(DoublePtr.class)) {
        double[] array = vector.toDoubleArray();
        fortranArgs[i] = new DoublePtr(array, 0);
        returnValues.add(callArguments.getName(i), DoubleArrayVector.unsafe(array, vector.getAttributes()));

      } else if(fortranTypes[i].equals(IntPtr.class)) {
        int[] array = vector.toIntArray();
        fortranArgs[i] = new IntPtr(array, 0);
        returnValues.add(callArguments.getName(i), IntArrayVector.unsafe(array, vector.getAttributes()));
      } else {
        throw new UnsupportedOperationException("fortran type: " + fortranTypes[i]);
      }
    }

    try {
      method.invoke(null, fortranArgs);
    } catch (Exception e) {
      throw new EvalException("Exception thrown while executing " + method.getName(), e);
    }

    return returnValues.build();
  }


  private static Method findFortranMethod(String className, String methodName) {
    Class<?> declaringClass = null;
    try {
      declaringClass = Class.forName(className);
    } catch (ClassNotFoundException e) {
      throw new EvalException(String.format("Could not find class named %s", className), e);
    }

    String mangledName = methodName.toLowerCase() + "_";

    for(Method method : declaringClass.getMethods()) {
      if(method.getName().equals(mangledName) &&
          Modifier.isPublic(method.getModifiers()) &&
          Modifier.isStatic(method.getModifiers())) {
        return method;
      }
    }
    throw new EvalException("Could not find method %s in class %s", methodName, className);
  }

  @Builtin(".Call")
  public static SEXP dotCall(@Current Context context,
                             @Current Environment rho,
                             String methodName,
                             @ArgumentList ListVector callArguments,
                             @NamedFlag("PACKAGE") String packageName,
                             @NamedFlag("CLASS") String className) throws ClassNotFoundException {

    Class clazz;
    if(packageName != null) {
      clazz = getPackageClass(packageName);
    } else if(className != null) {
      clazz = Class.forName(className);
    } else {
      throw new EvalException("Either the PACKAGE or CLASS argument must be provided");
    }
    
    return delegateToJavaMethod(context, clazz, methodName, callArguments);
  }

  /**
   * Dispatches what were originally calls to "native" libraries (C/Fortran/etc)
   * to a Java class. The Calling convention (.C/.Fortran/.Call) are ignored.
   * @param className 
   *
   */
  public static SEXP delegateToJavaMethod(Context context,
                                          Class clazz,
                                          String methodName,
                                          ListVector arguments) {

    List<Method> overloads = findMethod(clazz, methodName);

    if(overloads.isEmpty()) {
      throw new EvalException("Method " + methodName + " not defined in " + clazz.getName());
    }

    FunctionBinding binding = new FunctionBinding(overloads);
    return binding.invoke(null, context, arguments);
  }

  public static List<Method> findMethod(Class packageClass, String methodName) {
    List<Method> overloads = Lists.newArrayList();
    for(Method method : packageClass.getMethods()) {
      if(method.getName().equals(methodName) &&
          (method.getModifiers() & (Modifier.STATIC | Modifier.PUBLIC)) != 0) {
        overloads.add(method);
      }
    }
    return overloads;
  }

  private static Class getPackageClass(String packageName) {
    if(packageName == null || packageName.equals("base")) {
      return Base.class;
    } else if(packageName.equals("methods")) {
      return Methods.class;
    } else if(packageName.equals("grDevices")) {
      return Graphics.class;
    } else {
      String packageClassName = "org.renjin." + packageName + "." +
          packageName;
      try {
        return Class.forName(packageClassName);
      } catch (ClassNotFoundException e) {
        throw new EvalException("Could not find class for 'native' methods for package '%s' (className='%s')",
            packageName, packageClassName);
      }
    }
  }
}
