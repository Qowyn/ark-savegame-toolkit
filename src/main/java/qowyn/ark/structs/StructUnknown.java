package qowyn.ark.structs;

import java.util.Base64;

import javax.json.JsonString;
import javax.json.JsonValue;

import qowyn.ark.ArkArchive;
import qowyn.ark.json.SimpleJsonString;

public class StructUnknown extends StructBase {

  private static final Base64.Decoder DECODER = Base64.getDecoder();

  private static final Base64.Encoder ENCODER = Base64.getEncoder();

  private final byte[] value;

  public StructUnknown(ArkArchive archive, int dataSize) {
    this.value = archive.getBytes(dataSize);
  }

  public StructUnknown(JsonValue v) {
    JsonString s = (JsonString) v;
    value = DECODER.decode(s.getString());
  }

  @Override
  public JsonValue toJson() {
    return new SimpleJsonString(ENCODER.encodeToString(value));
  }

  @Override
  public void write(ArkArchive archive) {
    archive.putBytes(value);
  }

  @Override
  public int getSize(boolean nameTable) {
    // TODO Auto-generated method stub
    return 0;
  }

}
