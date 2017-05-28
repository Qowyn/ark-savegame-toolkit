package qowyn.ark.arrays;

import qowyn.ark.ArkArchive;
import qowyn.ark.properties.PropertyArray;

@FunctionalInterface
public interface ArkArrayBinaryConstructor {

  public ArkArray<?> apply(ArkArchive archive, PropertyArray property);

}
