package qowyn.ark.properties;

import java.util.Base64;

import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

import qowyn.ark.ArkArchive;

public class PropertyText extends PropertyBase<String> {
  
  private static final Base64.Decoder DECODER = Base64.getDecoder();
  
  private static final Base64.Encoder ENCODER = Base64.getEncoder();

  public PropertyText(String name, String typeName, String value) {
    super(name, typeName, 0, value);
  }

  public PropertyText(String name, String typeName, int index, String value) {
    super(name, typeName, index, value);
  }

  public PropertyText(ArkArchive archive, PropertyArgs args) {
    super(archive, args);
    value = ENCODER.encodeToString(archive.getBytes(dataSize));
  }

  public PropertyText(JsonObject o) {
    super(o);
    value = o.getString("value");
  }

  @Override
  public Class<String> getValueClass() {
    return String.class;
  }

  @Override
  protected void serializeValue(JsonObjectBuilder job) {
    job.add("value", value);
  }

  @Override
  protected void writeValue(ArkArchive archive) {
    archive.putBytes(DECODER.decode(value));
  }

  @Override
  public int calculateDataSize(boolean nameTable) {
    return DECODER.decode(value).length;
  }

}
