package org.renjin.gcc.translate;

import org.renjin.gcc.gimple.GimpleOp;
import org.renjin.gcc.gimple.type.GimpleBooleanType;
import org.renjin.gcc.gimple.type.GimpleIntegerType;
import org.renjin.gcc.gimple.type.GimpleRealType;
import org.renjin.gcc.jimple.JimpleExpr;
import org.renjin.gcc.jimple.JimpleType;
import org.renjin.gcc.translate.expr.ImExpr;
import org.renjin.gcc.translate.type.ImPrimitiveType;
import org.renjin.gcc.translate.type.PrimitiveType;

public class Comparison {
  private GimpleOp op;
  private ImExpr a;
  private ImExpr b;
  private ImPrimitiveType type;


  public Comparison(GimpleOp op, ImExpr a, ImExpr b) {
    super();
    this.op = op;
    this.a = a;
    this.b = b;
    TypeChecker.assertSameType(a,b);
    this.type = (ImPrimitiveType) a.type();
  }


  public JimpleExpr toCondition(FunctionContext context) {
    switch(type) {
    case FLOAT:
    case DOUBLE:
      switch (op) {
      case NE_EXPR:
        return floatComparison(context, "cmpl", "!=", 0);
      case EQ_EXPR:
        return floatComparison(context, "cmpl", "==", 0);
      case LE_EXPR:
        return floatComparison(context, "cmpg", "<=", 0);
      case LT_EXPR:
        return floatComparison(context, "cmpg", "<", 0);
      case GT_EXPR:
        return floatComparison(context, "cmpl", ">", 0);
      case GE_EXPR:
        return floatComparison(context, "cmpl", ">=", 0);
      }
      break;
    case INT:
    case LONG:
    case BOOLEAN:

      switch (op) {
      case NE_EXPR:
        return intComparison(context, "!=");
      case EQ_EXPR:
        return intComparison(context, "==");
      case LE_EXPR:
        return intComparison(context, "<=");
      case LT_EXPR:
        return intComparison(context, "<");
      case GT_EXPR:
        return intComparison(context, ">");
      case GE_EXPR:
        return intComparison(context, ">=");
      }
    }
    throw new UnsupportedOperationException(" don't know how to compare expressions of type " + a.type());
  }

  private JimpleExpr floatComparison(FunctionContext context, String operator, String condition, int operand) {
    String cmp = context.declareTemp(JimpleType.INT);
    context.getBuilder().addStatement(String.format("%s = %s %s %s",
        cmp, 
        a.translateToPrimitive(context, type),
        operator, 
        b.translateToPrimitive(context, type)));

    return new JimpleExpr(cmp + " " + condition + " " + operand);
  }
  
  private JimpleExpr intComparison(FunctionContext context, String operator) {
    return JimpleExpr.binaryInfix(operator,
        a.translateToPrimitive(context, ImPrimitiveType.INT),
        b.translateToPrimitive(context, ImPrimitiveType.INT));
    
  }
}
