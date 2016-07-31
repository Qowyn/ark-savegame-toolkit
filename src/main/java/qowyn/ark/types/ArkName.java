package qowyn.ark.types;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ArkName {

  private static final Pattern NAME_INDEX_PATTERN = Pattern.compile("^(.*)_([0-9]+)$");

  private final String nameString;

  private final int nameIndex;

  private final boolean zeroIndexVisible;

  public ArkName(String name) {
    Matcher matcher = NAME_INDEX_PATTERN.matcher(name);
    if (matcher.matches()) {
      nameString = matcher.group(1);
      nameIndex = Integer.parseInt(matcher.group(2));
      zeroIndexVisible = true;
    } else {
      nameString = name;
      nameIndex = 0;
      zeroIndexVisible = false;
    }
  }

  public ArkName(String nameString, int nameIndex) {
    this.nameString = nameString;
    this.nameIndex = nameIndex;
    this.zeroIndexVisible = false;
  }

  public String getNameString() {
    return nameString;
  }

  public int getNameIndex() {
    return nameIndex;
  }

  public boolean isZeroIndexVisible() {
    return zeroIndexVisible;
  }

  @Override
  public String toString() {
    if (nameIndex == 0 && !zeroIndexVisible) {
      return nameString;
    } else {
      return nameString + "_" + nameIndex;
    }
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + nameIndex;
    result = prime * result + ((nameString == null) ? 0 : nameString.hashCode());
    result = prime * result + (zeroIndexVisible ? 1231 : 1237);
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (obj instanceof String) {
      return toString().equals(obj);
    }
    if (getClass() != obj.getClass())
      return false;
    ArkName other = (ArkName) obj;
    if (nameIndex != other.nameIndex)
      return false;
    if (nameString == null) {
      if (other.nameString != null)
        return false;
    } else if (!nameString.equals(other.nameString))
      return false;
    if (zeroIndexVisible != other.zeroIndexVisible)
      return false;
    return true;
  }

}
