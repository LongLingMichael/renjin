package org.renjin.gcc.gimple.type;

public class AbstractGimpleType implements GimpleType {
  private int size;

  public int getSize() {
    return size;
  }

  public void setSize(int size) {
    this.size = size;
  }

  @Override
  public boolean isPointerTo(Class<? extends GimpleType> clazz) {
    return false;
  }

  @Override
  public <X extends GimpleType> X getBaseType() {
    throw new UnsupportedOperationException("this is not pointer type (" + getClass().getSimpleName() + ")");
  }
}
