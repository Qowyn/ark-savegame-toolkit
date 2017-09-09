package qowyn.ark.data;

import javax.json.JsonValue;

import qowyn.ark.ArkArchive;
import qowyn.ark.NameSizeCalculator;

public interface ExtraData {

  public int calculateSize(NameSizeCalculator nameSizer);

  public JsonValue toJson();

  public void write(ArkArchive archive);

}
