package qowyn.ark.structs;

import qowyn.ark.NameCollector;

public abstract class StructBase implements Struct {

  @Override
  public boolean isNative() {
    return true;
  }

  @Override
  public void collectNames(NameCollector collector) {}

}
