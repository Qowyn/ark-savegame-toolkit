package qowyn.ark;

import qowyn.ark.types.ArkName;

@FunctionalInterface
public interface NameSizeCalculator {

  public int sizeOf(ArkName name);

}
