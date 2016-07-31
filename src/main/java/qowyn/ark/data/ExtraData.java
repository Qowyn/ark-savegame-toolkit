package qowyn.ark.data;

import javax.json.JsonValue;

import qowyn.ark.ArkArchive;

public interface ExtraData {

  public int calculateSize(boolean nameTable);

  public JsonValue toJson();

  public void write(ArkArchive archive);

}
