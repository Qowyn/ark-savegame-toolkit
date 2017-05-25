package qowyn.ark.arrays;

import qowyn.ark.ArkArchive;
import qowyn.ark.types.ArkName;

@FunctionalInterface
public interface ArkArrayBinaryConstructor {

  public ArkArray<?> apply(ArkArchive archive, int dataSize, ArkName propertyName);
  
}
