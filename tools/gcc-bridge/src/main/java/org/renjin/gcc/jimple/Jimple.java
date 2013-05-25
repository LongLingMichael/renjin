package org.renjin.gcc.jimple;

import org.renjin.gcc.gimple.GimpleParameter;
import org.renjin.gcc.gimple.expr.GimpleVariableRef;

public class Jimple {

  public static String id(GimpleParameter param) {
    return validIdentifier(param.getName());
  }

  public static String id(String varName) {
    return validIdentifier(varName);
  }

  private static String validIdentifier(String name) {
    return name.replace('.', '$');
  }

  public static String id(GimpleVariableRef rhs) {
    return id(rhs.getName());
  }

  public static String constant(Object value) {
    if (value instanceof Number) {
      return value.toString();
    } else if (value instanceof Boolean) {
      return value.toString();
    } else if (value instanceof String) {
      return "\"" + value + "\"";
    } else {
      throw new UnsupportedOperationException("constant: " + value);
    }
  }

  public static String type(Class<?> type) {
    return type.toString();
  }
}
