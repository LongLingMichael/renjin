package org.renjin.gcc.translate.var;

import org.renjin.gcc.gimple.type.GimpleType;
import org.renjin.gcc.gimple.type.RecordType;
import org.renjin.gcc.jimple.Jimple;
import org.renjin.gcc.translate.FunctionContext;
import org.renjin.gcc.translate.expr.Expr;
import org.renjin.gcc.translate.struct.Struct;

public class StructPtrVar extends Variable {

  private RecordType type;
  private Struct struct;
  private String gimpleName;
  private String jimpleName;
  private FunctionContext context;

  public StructPtrVar(FunctionContext context, String gimpleName, Struct struct) {
    this.struct = struct;
    this.gimpleName = gimpleName;
    this.jimpleName = Jimple.id(gimpleName);
    this.context = context;

    context.getBuilder().addVarDecl(struct.getJimpleType(), jimpleName);
  }

  @Override
  public void writeAssignment(FunctionContext context, Expr rhs) {
    throw new UnsupportedOperationException();
  }

  @Override
  public GimpleType type() {
    return type;
  }
}
