package qowyn.ark.structs;

import javax.json.JsonValue;

import qowyn.ark.ArkArchive;
import qowyn.ark.NameContainer;

public interface Struct extends NameContainer {

  public boolean isNative();

  public JsonValue toJson();

  public void write(ArkArchive archive);

  public int getSize(boolean nameTable);

}
