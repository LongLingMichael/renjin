package org.renjin.gcc.translate.marshall;


import org.renjin.gcc.jimple.JimpleExpr;
import org.renjin.gcc.jimple.JimpleType;
import org.renjin.gcc.translate.FunctionContext;
import org.renjin.gcc.translate.expr.Expr;

import java.util.List;

public class Marshallers {
  
  public static Marshaller forType(JimpleType type) {
    if(type.isPrimitive()) {
      return new PrimitiveMarshaller(type);
    } else if(type.isPointerWrapper()) {
      return new PointerWrapperMarshaller();
    } else if(type.isFunctionPointer()) {
      return new FunPtrMarshaller();
    } else {
      throw new UnsupportedOperationException(type.toString());
    }
  }

  public static JimpleExpr marshallReturnValue(FunctionContext context, Expr returnValue) {
    JimpleType returnType = context.getBuilder().getReturnType();
    return marshall(context, returnValue, returnType);
  }

  public static JimpleExpr marshall(FunctionContext context, Expr returnValue, JimpleType type) {
    return forType(type).marshall(context, returnValue);
  }
}
