package qowyn.ark.structs;

import java.util.Set;

import qowyn.ark.types.ArkName;

public abstract class StructBase implements Struct {

  protected ArkName structType;

  public StructBase(ArkName structType) {
    this.structType = structType;
  }

  @Override
  public ArkName getStructType() {
    return structType;
  }

  @Override
  public void setStructType(ArkName structType) {
    this.structType = structType;
  }

  @Override
  public boolean isNative() {
    return true;
  }

  @Override
  public void collectNames(Set<String> nameTable) {
    if (structType != null) {
      nameTable.add(structType.getNameString());
    }
  }

}
