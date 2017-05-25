package qowyn.ark.structs;

import qowyn.ark.ArkArchive;

@FunctionalInterface
public interface StructBinaryConstructor {

  public Struct apply(ArkArchive archive);

}
