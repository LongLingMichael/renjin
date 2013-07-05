package org.renjin.gcc.translate.type;


import org.renjin.gcc.jimple.*;
import org.renjin.gcc.translate.FunctionContext;
import org.renjin.gcc.translate.VarUsage;
import org.renjin.gcc.translate.expr.ImExpr;
import org.renjin.gcc.translate.var.PrimitiveArrayVar;
import org.renjin.gcc.translate.var.Variable;

public class ImPrimitiveArrayType implements ImType {

  private ImPrimitiveType componentType;
  private Integer lowerBound;
  private Integer upperBound;

  public ImPrimitiveArrayType(ImPrimitiveType componentType, Integer lowerBound, Integer upperBound) {
    this.componentType = componentType;
    this.lowerBound = lowerBound;
    this.upperBound = upperBound;
  }

  @Override
  public JimpleType paramType() {
    throw new UnsupportedOperationException("arrays as parameters not supported");
  }

  public JimpleType asJimple() {
    return new RealJimpleType(componentType.getArrayClass());
  }

  @Override
  public JimpleType returnType() {
    return componentType().jimpleArrayType();
  }

  @Override
  public void defineField(JimpleClassBuilder classBuilder, String memberName, boolean isStatic) {
    throw new UnsupportedOperationException("arrays as field members not yet implemented");
  }

  @Override
  public Variable createLocalVariable(FunctionContext functionContext, String gimpleName, VarUsage varUsage) {
    return new PrimitiveArrayVar(functionContext, Jimple.id(gimpleName), this);
  }

  @Override
  public ImExpr createFieldExpr(String instanceExpr, JimpleType classType, String memberName) {
    throw new UnsupportedOperationException();
  }

  @Override
  public ImPrimitiveArrayPtrType pointerType() {
    return new ImPrimitiveArrayPtrType(this);
  }

  @Override
  public ImType arrayType(Integer lowerBound, Integer upperBound) {
    throw new UnsupportedOperationException(this.toString());
  }

  public ImPrimitiveType componentType() {
    return componentType;
  }

  public int getLength() {
    return upperBound - lowerBound + 1;
  }

  public int getLowerBound() {
    return lowerBound;
  }
}
