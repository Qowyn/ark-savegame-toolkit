package qowyn.ark.properties;

import java.util.Base64;

import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

import qowyn.ark.ArkArchive;

public class PropertyUnknown extends PropertyBase<byte[]> {

  private static final Base64.Decoder DECODER = Base64.getDecoder();

  private static final Base64.Encoder ENCODER = Base64.getEncoder();

  public PropertyUnknown(ArkArchive archive, PropertyArgs args) {
    super(archive, args);
    value = archive.getBytes(dataSize);
  }

  public PropertyUnknown(JsonObject o) {
    super(o);
    value = DECODER.decode(o.getString("value"));
  }

  @Override
  public Class<byte[]> getValueClass() {
    return byte[].class;
  }

  @Override
  public int calculateDataSize(boolean nameTable) {
    return value.length;
  }

  @Override
  protected void serializeValue(JsonObjectBuilder job) {
    job.add("value", ENCODER.encodeToString(value));
  }

  @Override
  protected void writeValue(ArkArchive archive) {
    archive.putBytes(value);
  }

  @Override
  public final void setValue(byte[] value) {
    throw new UnsupportedOperationException();
  }

}
