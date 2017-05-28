package qowyn.ark.properties;

import java.util.Base64;

import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

import qowyn.ark.ArkArchive;
import qowyn.ark.types.ArkName;

public class PropertyText extends PropertyBase<String> {

  public static final ArkName TYPE = ArkName.constantPlain("TextProperty");

  private static final Base64.Decoder DECODER = Base64.getDecoder();

  private static final Base64.Encoder ENCODER = Base64.getEncoder();

  public PropertyText(String name, String value) {
    super(ArkName.from(name), 0, value);
  }

  public PropertyText(String name, int index, String value) {
    super(ArkName.from(name), index, value);
  }

  public PropertyText(ArkArchive archive, ArkName name) {
    super(archive, name);
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
  public ArkName getType() {
    return TYPE;
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
