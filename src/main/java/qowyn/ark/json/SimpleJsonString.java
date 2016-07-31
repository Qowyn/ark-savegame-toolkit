package qowyn.ark.json;

import javax.json.JsonString;

/**
 * 
 */
public final class SimpleJsonString implements JsonString {

  private final String string;

  public SimpleJsonString(String string) {
    this.string = string;
  }

  @Override
  public ValueType getValueType() {
    return ValueType.STRING;
  }

  @Override
  public String getString() {
    return string;
  }

  @Override
  public CharSequence getChars() {
    return string;
  }

  @Override
  public int hashCode() {
    return string.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (!(obj instanceof JsonString)) {
      return false;
    }
    JsonString other = (JsonString) obj;
    return string.equals(other.getString());
  }

}
