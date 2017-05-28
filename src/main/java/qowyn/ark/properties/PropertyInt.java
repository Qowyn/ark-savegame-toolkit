package qowyn.ark.properties;

import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

import qowyn.ark.ArkArchive;
import qowyn.ark.types.ArkName;

public class PropertyInt extends PropertyBase<Integer> {

  public static final ArkName TYPE = ArkName.constantPlain("IntProperty");

  public PropertyInt(String name, int value) {
    super(ArkName.from(name), 0, value);
  }

  public PropertyInt(String name, int index, int value) {
    super(ArkName.from(name), index, value);
  }

  public PropertyInt(ArkArchive archive, ArkName name) {
    super(archive, name);
    value = archive.getInt();
  }

  public PropertyInt(JsonObject o) {
    super(o);
    value = o.getInt("value");
  }

  @Override
  public Class<Integer> getValueClass() {
    return Integer.class;
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
    archive.putInt(value);
  }

  @Override
  public int calculateDataSize(boolean nameTable) {
    return Integer.BYTES;
  }

}
