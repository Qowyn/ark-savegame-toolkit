package qowyn.ark.properties;

import qowyn.ark.types.ArkName;

public final class PropertyArgs {

  private final ArkName name;

  private final ArkName typeName;

  public PropertyArgs(ArkName name, ArkName typeName) {
    this.name = name;
    this.typeName = typeName;
  }

  public ArkName getName() {
    return name;
  }

  public ArkName getTypeName() {
    return typeName;
  }

}
