package org.renjin.gcc.gimple.type;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({ 
    @Type(value = GimpleIntegerType.class, name = "integer_type"),
    @Type(value = GimpleRealType.class, name = "real_type"),
    @Type(value = GimplePointerType.class, name = "pointer_type"),
    @Type(value = GimpleReferenceType.class, name = "reference_type"),
    @Type(value = GimpleArrayType.class, name = "array_type"),
    @Type(value = GimpleBooleanType.class, name = "boolean_type"),
    @Type(value = GimpleFunctionType.class, name = "function_type"),
    @Type(value = GimpleRecordType.class, name = "record_type"),
    @Type(value = GimpleVoidType.class, name = "void_type") })
public interface GimpleType {

  boolean isPointerTo(Class<? extends GimpleType> clazz);
  
  /**
   * @return the base type if this is a pointer type
   * @throws UnsupportedOperationException if this is not a pointer type
   */
  <X extends GimpleType> X getBaseType();
}
