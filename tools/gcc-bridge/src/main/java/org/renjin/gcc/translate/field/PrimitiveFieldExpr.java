package org.renjin.gcc.translate.field;

import com.google.common.base.Preconditions;
import org.renjin.gcc.jimple.JimpleExpr;
import org.renjin.gcc.jimple.JimpleType;
import org.renjin.gcc.jimple.RealJimpleType;
import org.renjin.gcc.translate.FunctionContext;
import org.renjin.gcc.translate.expr.AbstractImExpr;
import org.renjin.gcc.translate.expr.ImExpr;
import org.renjin.gcc.translate.expr.ImLValue;
import org.renjin.gcc.translate.type.ImPrimitiveType;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 *
 */
public class PrimitiveFieldExpr extends AbstractImExpr implements ImLValue {
  private final String instanceName;
  private final JimpleType declaringClass;
  private final String member;
  private final ImPrimitiveType memberType;

  public PrimitiveFieldExpr(String instanceName, JimpleType declaringClass,
                            String member, ImPrimitiveType memberType) {
    this.instanceName = instanceName;
    this.declaringClass = declaringClass;
    this.member = member;
    this.memberType = memberType;

    Preconditions.checkNotNull("memberType", this.memberType);
  }

  public PrimitiveFieldExpr(Field field) {
    if(!Modifier.isStatic(field.getModifiers())) {
      throw new UnsupportedOperationException("field is not static, but no instance name provided");
    }
    this.instanceName = null;
    this.declaringClass =  new RealJimpleType(field.getDeclaringClass());
    this.member = field.getName();
    this.memberType = ImPrimitiveType.valueOf(field.getType());

    Preconditions.checkNotNull("memberType", memberType);

  }

  @Override
  public ImPrimitiveType type() {
    return memberType;
  }

  private String fieldSignature() {
    return String.format("<%s: %s %s>", declaringClass, memberType, member);
  }

  @Override
  public JimpleExpr translateToPrimitive(FunctionContext context, ImPrimitiveType type) {
    if(instanceName == null) {
      return new JimpleExpr(fieldSignature());
    } else {
      return new JimpleExpr(instanceName + "." + fieldSignature());
    }
  }

  @Override
  public void writeAssignment(FunctionContext context, ImExpr rhs) {

    JimpleExpr rhsExpr = rhs.translateToPrimitive(context, memberType);
    String lhs;
    if(instanceName == null) {
      lhs = fieldSignature();
    } else {
      lhs = instanceName + "." + fieldSignature();
    }
    context.getBuilder().addAssignment(lhs, rhsExpr);
  }

  @Override
  public String toString() {
    return instanceName + "." + member;
  }
}
