package org.renjin.compiler.ir.tac.expressions;


import com.sun.org.apache.bcel.internal.generic.GETSTATIC;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.renjin.compiler.emit.EmitContext;
import org.renjin.sexp.*;

public class SexpConstant extends Constant {

  public static final SexpConstant NULL = new SexpConstant(Null.INSTANCE);

  private final SEXP value;

  private SexpConstant(SEXP value) {
    this.value = value;
  }

  public static Constant valueOf(SEXP value) {
    if(value == Null.INSTANCE) {
      return NULL;
    } else if(value instanceof DoubleVector && value.length()==1) {
      return new DoubleScalarConstant(((DoubleVector) value).getElementAsDouble(0));
    } else if(value instanceof LogicalVector && value.length()==1) {

    }
    return new SexpConstant(value);
  }

  @Override
  public Object getValue() {
    return value;
  }

  @Override
  public void emitPush(EmitContext emitContext, MethodVisitor mv) {
    if(value == Null.INSTANCE) {
      mv.visitFieldInsn(Opcodes.GETSTATIC, "org/renjin/sexp/Null", "INSTANCE", "Lorg/renjin/sexp/Null;");
    } else {
      throw new UnsupportedOperationException("sexp: " + value);
    }
  }

  @Override
  public Class inferType() {
    return value.getClass();
  }
}
