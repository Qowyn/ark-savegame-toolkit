package qowyn.ark.data;

import java.util.Base64;

import javax.json.JsonValue;

import qowyn.ark.ArkArchive;
import qowyn.ark.NameSizeCalculator;
import qowyn.ark.json.SimpleJsonString;

public class ExtraDataBlob implements ExtraData {

  private static final Base64.Encoder ENCODER = Base64.getEncoder();

  private byte[] data;

  public byte[] getData() {
    return data;
  }

  public void setData(byte[] data) {
    this.data = data;
  }

  @Override
  public int calculateSize(NameSizeCalculator nameSizer) {
    return data != null ? data.length : 0;
  }

  @Override
  public JsonValue toJson() {
    return data != null ? new SimpleJsonString(ENCODER.encodeToString(data)) : JsonValue.NULL;
  }

  @Override
  public void write(ArkArchive archive) {
    if (data != null) {
      archive.putBytes(data);
    }
  }



}
