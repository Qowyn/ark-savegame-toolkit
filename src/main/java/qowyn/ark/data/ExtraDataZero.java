package qowyn.ark.data;

import javax.json.JsonValue;

import qowyn.ark.ArkArchive;

public class ExtraDataZero implements ExtraData {

  @Override
  public int calculateSize(boolean nameTable) {
    return 4;
  }

  @Override
  public JsonValue toJson() {
    return JsonValue.NULL;
  }

  @Override
  public void write(ArkArchive archive) {
    archive.putInt(0);
  }

}
