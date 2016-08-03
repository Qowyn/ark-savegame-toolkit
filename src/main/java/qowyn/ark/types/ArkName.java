package qowyn.ark.types;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ArkName {

  private static final Pattern NAME_INDEX_PATTERN = Pattern.compile("^(.*)_([0-9]+)$");

  private final String nameString;

  private final int nameIndex;

  private final String string;

  public ArkName(String name) {
    Matcher matcher = NAME_INDEX_PATTERN.matcher(name);
    if (matcher.matches()) {
      nameString = matcher.group(1);
      nameIndex = Integer.parseInt(matcher.group(2));
    } else {
      nameString = name;
      nameIndex = 0;
    }
    string = name;
  }

  public ArkName(String nameString, int nameIndex) {
    this.nameString = nameString;
    this.nameIndex = nameIndex;
    if (nameIndex == 0) {
      string = nameString;
    } else {
      string = nameString + "_" + nameIndex;
    }
  }

  public String getNameString() {
    return nameString;
  }

  public int getNameIndex() {
    return nameIndex;
  }

  @Override
  public String toString() {
    return string;
  }

  @Override
  public int hashCode() {
    return string.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (!(obj instanceof ArkName))
      return false;
    return string.equals(((ArkName) obj).string);
  }

}
