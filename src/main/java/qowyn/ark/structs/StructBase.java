package qowyn.ark.structs;

import java.util.Set;

public abstract class StructBase implements Struct {

  @Override
  public boolean isNative() {
    return true;
  }

  @Override
  public void collectNames(Set<String> nameTable) {}

}
