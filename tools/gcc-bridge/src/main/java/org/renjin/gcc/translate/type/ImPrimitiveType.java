package org.renjin.gcc.translate.type;

import org.renjin.gcc.gimple.type.*;
import org.renjin.gcc.jimple.JimpleExpr;
import org.renjin.gcc.jimple.JimpleType;
import org.renjin.gcc.jimple.RealJimpleType;
import org.renjin.gcc.translate.FunctionContext;
import org.renjin.gcc.translate.VarUsage;
import org.renjin.gcc.translate.var.PrimitiveHeapVar;
import org.renjin.gcc.translate.var.PrimitiveStackVar;
import org.renjin.gcc.translate.var.Variable;

import java.lang.reflect.Array;


public enum ImPrimitiveType implements ImType {

  DOUBLE {
    @Override
    public Class getPrimitiveClass() {
      return double.class;
    }

    @Override
    public JimpleExpr literalExpr(Object value) {
      return JimpleExpr.doubleConstant(((Number)value).doubleValue());
    }

    @Override
    public int getStorageSizeInBytes() {
      return 8;
    }
  },
  FLOAT {
    @Override
    public Class getPrimitiveClass() {
      return float.class;
    }

    @Override
    public JimpleExpr literalExpr(Object value) {
      return JimpleExpr.floatConstant(((Number)value).floatValue());
    }

    @Override
    public int getStorageSizeInBytes() {
      return 4;
    }
  },
  INT {
    @Override
    public Class getPrimitiveClass() {
      return int.class;
    }

    @Override
    public int getStorageSizeInBytes() {
      return 4;
    }

    @Override
    public JimpleExpr literalExpr(Object value) {
      return JimpleExpr.integerConstant(((Number)value).intValue());
    }
  },
  LONG {
    @Override
    public Class getPrimitiveClass() {
      return long.class;
    }

    @Override
    public int getStorageSizeInBytes() {
      return 8;
    }

    @Override
    public JimpleExpr literalExpr(Object value) {
      return JimpleExpr.integerConstant(((Number)value).intValue());
    }
  },
  BOOLEAN {
    @Override
    public Class getPrimitiveClass() {
      return boolean.class;
    }

    @Override
    public int getStorageSizeInBytes() {
      throw new UnsupportedOperationException("to check");
    }

    @Override
    public JimpleExpr literalExpr(Object value) {
      if(value instanceof Boolean) {
        return JimpleExpr.integerConstant( ((Boolean)value) ? 1 : 0 );
      } else {
        return literalExpr( ((Number)value).intValue() != 0 );
      }
    }
  },
  CHAR {
    @Override
    public Class getPrimitiveClass() {
      return char.class;
    }

    @Override
    public JimpleExpr literalExpr(Object value) {
      return literalExpr( ((Number)value).intValue());
    }

    @Override
    public int getStorageSizeInBytes() {
      return 1;
    }
  };

  @Override
  public JimpleType paramType() {
    return asJimple();
  }

  @Override
  public JimpleType returnType() {
    return asJimple();
  }

  public JimpleType asJimple() {
    return new RealJimpleType(getPrimitiveClass());
  }

  public abstract Class getPrimitiveClass();

  public Class getArrayClass() {
    return Array.newInstance(getPrimitiveClass(), 0).getClass();
  }

  @Override
  public ImPrimitivePtrType pointerType() {
    return new ImPrimitivePtrType(this);
  }

  public abstract JimpleExpr literalExpr(Object value);

  @Override
  public Variable createLocalVariable(FunctionContext functionContext, String gimpleName, VarUsage usage) {
    if (usage.isAddressed()) {
      return new PrimitiveHeapVar(functionContext, this, gimpleName);
    } else {
      return new PrimitiveStackVar(functionContext, this, gimpleName);
    }
  }

  /**
   * @return  the storage size of the type, as understood by GCC. This is used
   * to convert pointer offsets, provided to us in bytes, to array index offsets.
   */
  public abstract int getStorageSizeInBytes();

  public JimpleExpr castIfNeeded(JimpleExpr expr, ImPrimitiveType type) {
    if(type != this) {
      return JimpleExpr.cast(expr, type.asJimple());
    } else {
      return expr;
    }
  }

  public static ImPrimitiveType valueOf(GimpleType type) {
    if (type instanceof GimpleRealType) {
      switch(((GimpleRealType) type).getPrecision()) {
        case 32:
          return FLOAT;
        case 64:
          return DOUBLE;
      }
    } else if (type instanceof GimpleIntegerType) {
      int precision = ((GimpleIntegerType) type).getPrecision();
      switch(precision) {
        case 8:
          return CHAR;
        case 32:
          return INT;
        case 64:
          return LONG;
      }
    } else if (type instanceof GimpleBooleanType) {
      return BOOLEAN;
    }
    throw new UnsupportedOperationException("type:" + type);

  }

  public static ImPrimitiveType valueOf(JimpleType type) {
    if(type.equals(JimpleType.DOUBLE)) {
      return DOUBLE;
    } else if(type.equals(JimpleType.FLOAT)) {
      return FLOAT;
    } else if(type.equals(JimpleType.BOOLEAN)) {
      return BOOLEAN;
    } else if(type.equals(JimpleType.INT)) {
      return INT;
    } else if(type.equals(JimpleType.CHAR)) {
      return CHAR;
    } else if(type.equals(JimpleType.LONG)) {
      return LONG;
    }
    throw new UnsupportedOperationException(type.toString());
  }
}
