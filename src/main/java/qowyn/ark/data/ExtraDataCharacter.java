package qowyn.ark.data;

import javax.json.JsonValue;

import qowyn.ark.ArkArchive;

public class ExtraDataCharacter implements ExtraData {

  @Override
  public int calculateSize(boolean nameTable) {
    return 8;
  }

  @Override
  public JsonValue toJson() {
    return JsonValue.NULL;
  }

  @Override
  public void write(ArkArchive archive) {
    archive.putInt(0);
    archive.putInt(1);
  }

}
