package qowyn.ark.data;

import javax.json.JsonValue;

import qowyn.ark.ArkArchive;
import qowyn.ark.NameSizeCalculator;

public class ExtraDataZero implements ExtraData {

  @Override
  public int calculateSize(NameSizeCalculator nameSizer) {
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
