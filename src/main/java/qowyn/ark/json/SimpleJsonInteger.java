package qowyn.ark.json;

import java.math.BigDecimal;
import java.math.BigInteger;

import javax.json.JsonNumber;

public final class SimpleJsonInteger implements JsonNumber {

  private final int value;

  public SimpleJsonInteger(int value) {
    this.value = value;
  }

  @Override
  public ValueType getValueType() {
    return ValueType.NUMBER;
  }

  @Override
  public boolean isIntegral() {
    return true;
  }

  @Override
  public int intValue() {
    return value;
  }

  @Override
  public int intValueExact() {
    return value;
  }

  @Override
  public long longValue() {
    return value;
  }

  @Override
  public long longValueExact() {
    return value;
  }

  @Override
  public BigInteger bigIntegerValue() {
    return BigInteger.valueOf(value);
  }

  @Override
  public BigInteger bigIntegerValueExact() {
    return BigInteger.valueOf(value);
  }

  @Override
  public double doubleValue() {
    return value;
  }

  @Override
  public BigDecimal bigDecimalValue() {
    return BigDecimal.valueOf(value);
  }

  @Override
  public String toString() {
    return Integer.toString(value);
  }

}
