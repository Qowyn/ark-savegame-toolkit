package qowyn.ark.properties;

import qowyn.ark.ArkArchive;
import qowyn.ark.types.ArkName;

@FunctionalInterface
public interface PropertyBinaryConstructor {

  public Property<?> apply(ArkArchive archive, ArkName name);

}
