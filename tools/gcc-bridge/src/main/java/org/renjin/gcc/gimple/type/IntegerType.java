package org.renjin.gcc.gimple.type;

public class IntegerType extends PrimitiveType {
  private int precision;
  private boolean unsigned;
  
  public IntegerType() {
    
  }
  
  public IntegerType(int precision) {
    this.precision = precision;
    setSize(precision);
  }

  /**
   * 
   * @return The number of bits of precision
   */
  public int getPrecision() {
    return precision;
  }

  public void setPrecision(int precision) {
    this.precision = precision;
  }

  public boolean isUnsigned() {
    return unsigned;
  }

  public void setUnsigned(boolean unsigned) {
    this.unsigned = unsigned;
  }

  @Override
  public String toString() {
    StringBuilder s = new StringBuilder();
    if (unsigned) {
      s.append("unsigned ");
    }
    s.append("int" + precision);
    return s.toString();
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + precision;
    result = prime * result + (unsigned ? 1231 : 1237);
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    IntegerType other = (IntegerType) obj;
    if (precision != other.precision)
      return false;
    if (unsigned != other.unsigned)
      return false;
    return true;
  }
}
