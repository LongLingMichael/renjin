package org.renjin.gcc.translate;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Map;

import org.renjin.gcc.gimple.GimpleCall;
import org.renjin.gcc.gimple.GimpleFunction;
import org.renjin.gcc.gimple.GimpleParameter;
import org.renjin.gcc.gimple.GimpleVarDecl;
import org.renjin.gcc.gimple.expr.GimpleArrayRef;
import org.renjin.gcc.gimple.expr.GimpleConstant;
import org.renjin.gcc.gimple.expr.GimpleExpr;
import org.renjin.gcc.gimple.expr.GimpleExternal;
import org.renjin.gcc.gimple.expr.GimpleVariableRef;
import org.renjin.gcc.gimple.type.GimpleType;
import org.renjin.gcc.jimple.JimpleExpr;
import org.renjin.gcc.jimple.JimpleMethodBuilder;
import org.renjin.gcc.jimple.JimpleType;
import org.renjin.gcc.translate.call.MethodRef;
import org.renjin.gcc.translate.types.TypeTranslator;
import org.renjin.gcc.translate.var.Variable;
import org.renjin.gcc.CallingConvention;

import com.google.common.collect.Maps;

public class FunctionContext {

  private TranslationContext translationContext;
  private GimpleFunction gimpleFunction;
  private JimpleMethodBuilder builder;
  private Map<String, Variable> variables = Maps.newHashMap();

  private int nextTempId = 0;
  private int nextLabelId = 1000;

  public FunctionContext(TranslationContext translationContext,
                         GimpleFunction gimpleFunction, JimpleMethodBuilder builder) {
    this.gimpleFunction = gimpleFunction;
    this.translationContext = translationContext;
    this.builder = builder;
    
    VarUsageInspector varUsage = new VarUsageInspector(gimpleFunction);

    for(GimpleVarDecl decl : gimpleFunction.getVariableDeclarations()) {
      Variable localVariable = translationContext
          .resolveType(decl.getType())
          .createLocalVariable(this, decl.getName(), varUsage.getUsage(decl));
      
      variables.put(decl.getName(), localVariable);

      
      if(decl.getConstantValue() != null) {
        localVariable.initFromConstant(decl.getConstantValue());
      }
    }

    for(GimpleParameter param : gimpleFunction.getParameters()) {
      TypeTranslator type = translationContext.resolveType(param.getType());
      builder.addParameter(type.paramType(), param.getName());
      Variable variable = type.createLocalVariable(this, param.getName(), varUsage.getUsage(param));
      variable.initFromParameter();
      variables.put(param.getName(), variable);
    }
  }


  public MethodRef resolveMethod(GimpleCall call) {
    return translationContext.resolveMethod(call, getCallingConvention());
  }
  
  public CallingConvention getCallingConvention() {
    return gimpleFunction.getCallingConvention();
  }
  
  public String declareTemp(JimpleType type) {
    String name = "_tmp" + (nextTempId++);
    builder.addVarDecl(type, name);
    return name;
  }

  public JimpleMethodBuilder getBuilder() {
    return builder;
  }

  public TranslationContext getTranslationContext() {
    return translationContext;
  }

  public String newLabel() {
    return "trlabel" + (nextLabelId++) + "__";
  }

  public Variable lookupVar(GimpleVariableRef var) {
    return lookupVar(var.getName());
  }

  private Variable lookupVar(String name) {
    Variable variable = variables.get(name);
    if(variable == null) {
      throw new IllegalArgumentException("No such variable " + name);
    }
    return variable;
  }


  public Variable lookupVar(GimpleExpr gimpleExpr) {
    if(gimpleExpr instanceof GimpleVariableRef) {
      return lookupVar((GimpleVariableRef)gimpleExpr);
    } else {
      throw new UnsupportedOperationException("Expected GimpleVar, got: " + gimpleExpr + " [" + gimpleExpr.getClass().getSimpleName() + "]");
    }
  }

  public Collection<Variable> getVariables() {
    return variables.values();
  }

  public JimpleExpr asNumericExpr(GimpleExpr gimpleExpr, JimpleType type) {
    if(gimpleExpr instanceof GimpleVariableRef) {
      Variable variable = lookupVar((GimpleVariableRef)gimpleExpr);
      return variable.asPrimitiveExpr(type);
    } else if (gimpleExpr instanceof GimpleConstant) {
      return asConstant(((GimpleConstant) gimpleExpr).getValue(), type);
    } else if (gimpleExpr instanceof GimpleExternal) {
      return fromField((GimpleExternal) gimpleExpr);
    } else if (gimpleExpr instanceof GimpleArrayRef) {
      return numericFromArrayRef((GimpleArrayRef) gimpleExpr);
    } else {
      throw new UnsupportedOperationException(gimpleExpr.toString());
    }
  }

  private JimpleExpr numericFromArrayRef(GimpleArrayRef ref) {
    JimpleExpr index = asNumericExpr(ref.getIndex(), JimpleType.INT);
    return lookupVar(ref.getArray()).asPrimitiveArrayRef(index);
  }

  private JimpleExpr fromField(GimpleExternal external) {
    Field field = translationContext.findField(external);
    return JimpleExpr.staticFieldReference(field);
  }

  private JimpleExpr asConstant(Object value, JimpleType type) {
    if(!(value instanceof Number)) {
      throw new UnsupportedOperationException("value: " + value + " (" + value.getClass() + ")");
    }
    Number number = (Number)value;
    if(type.equals(JimpleType.DOUBLE)) {
      return JimpleExpr.doubleConstant(number.doubleValue());
      
    } else if(type.equals(JimpleType.FLOAT)) {
        return JimpleExpr.floatConstant(number.floatValue());
     
    } else if(type.equals(JimpleType.INT) ||
        type.equals(JimpleType.BOOLEAN)) {
      return JimpleExpr.integerConstant(number.intValue());
      
    } else if(type.equals(JimpleType.LONG)) {
      return JimpleExpr.longConstant(number.longValue());
    
    } else {
      throw new UnsupportedOperationException("type: " + type);
    }
  }

  public GimpleType getGimpleVariableType(GimpleExpr expr) {
    return gimpleFunction.getType(expr);
  }
}
