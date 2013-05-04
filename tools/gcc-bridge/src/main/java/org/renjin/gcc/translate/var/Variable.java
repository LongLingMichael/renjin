package org.renjin.gcc.translate.var;


import org.renjin.gcc.gimple.GimpleOp;
import org.renjin.gcc.gimple.expr.GimpleExpr;
import org.renjin.gcc.gimple.type.GimpleType;
import org.renjin.gcc.jimple.JimpleExpr;
import org.renjin.gcc.jimple.JimpleType;

import java.util.List;

public abstract class Variable {

  public abstract void assign(GimpleOp op, List<GimpleExpr> operands);

  public GimpleType getGimpleType() {
	  throw new UnsupportedOperationException();
  }
  
  public JimpleExpr asPrimitiveExpr(JimpleType type) {
    throw new UnsupportedOperationException(this + " does not have a primitive representation");
  }

  public JimpleType getPrimitiveType() {
    throw new UnsupportedOperationException(this + " does not have a primitive representation");
  }
  
  public void initFromParameter() {

  }
  
  public void initFromConstant(Object value) {
    throw new UnsupportedOperationException();
  }

  public void assignMember(String member, GimpleOp operator, List<GimpleExpr> operands) {
    throw new UnsupportedOperationException(this + " does not support member assignment");
  }

  public JimpleExpr memberRef(String member, JimpleType jimpleType) {
    throw new UnsupportedOperationException(this + " does not support member assignment");
  }

  public JimpleExpr wrapPointer() {
    throw new UnsupportedOperationException(this + " is not addressable");
  }

  public boolean isReal() {
    throw new UnsupportedOperationException(this + " does not have a numeric representation");
  }

  public JimpleExpr asPrimitiveArrayRef(JimpleExpr index) {
    throw new UnsupportedOperationException(this + " cannot be referenced as a primitive array");
  }

  public void assignIndirect(GimpleOp operator, List<GimpleExpr> operands) {
    throw new UnsupportedOperationException();
  }
  
  public abstract JimpleExpr returnExpr();
}
