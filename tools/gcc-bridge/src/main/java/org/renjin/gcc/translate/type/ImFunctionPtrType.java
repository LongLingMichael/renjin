package org.renjin.gcc.translate.type;

import org.renjin.gcc.gimple.type.GimpleFunctionType;
import org.renjin.gcc.jimple.JimpleType;
import org.renjin.gcc.jimple.SyntheticJimpleType;
import org.renjin.gcc.translate.FunctionContext;
import org.renjin.gcc.translate.TranslationContext;
import org.renjin.gcc.translate.VarUsage;
import org.renjin.gcc.translate.var.FunPtrVar;
import org.renjin.gcc.translate.var.Variable;

public class ImFunctionPtrType implements ImType {

  private ImFunctionType baseType;
  private String interfaceName;

  public ImFunctionPtrType(ImFunctionType type) {
    this.baseType = type;
  }

  @Override
  public JimpleType returnType() {
    return jimpleType();
  }

  private JimpleType jimpleType() {
    return new SyntheticJimpleType(baseType.interfaceName());
  }

  @Override
  public JimpleType paramType() {
    return jimpleType();
  }

  @Override
  public Variable createLocalVariable(FunctionContext functionContext, String gimpleName, VarUsage usage) {
    return new FunPtrVar(functionContext, gimpleName, this);
  }

  @Override
  public ImType pointerType() {
    throw new UnsupportedOperationException();
  }

  public JimpleType interfaceType() {
    return new SyntheticJimpleType(baseType.interfaceName());
  }
}
