package qowyn.ark.properties;

import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

import qowyn.ark.ArkArchive;
import qowyn.ark.types.ArkName;

public class PropertyStr extends PropertyBase<String> {

  public static final ArkName TYPE = ArkName.constantPlain("StrProperty");

  public PropertyStr(String name, String value) {
    super(ArkName.from(name), 0, value);
  }

  public PropertyStr(String name, int index, String value) {
    super(ArkName.from(name), index, value);
  }

  public PropertyStr(ArkArchive archive, ArkName name) {
    super(archive, name);
    value = archive.getString();
  }

  public PropertyStr(JsonObject o) {
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
    archive.putString(value);
  }

  @Override
  public int calculateDataSize(boolean nameTable) {
    return ArkArchive.getStringLength(value);
  }

}
