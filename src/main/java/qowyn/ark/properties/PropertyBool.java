package qowyn.ark.properties;

import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

import qowyn.ark.ArkArchive;
import qowyn.ark.types.ArkName;

public class PropertyBool extends PropertyBase<Boolean> {

  public static final ArkName TYPE = ArkName.constantPlain("BoolProperty");

  public PropertyBool(String name, boolean value) {
    super(ArkName.from(name), 0, value);
  }

  public PropertyBool(String name, int index, boolean value) {
    super(ArkName.from(name), index, value);
  }

  public PropertyBool(ArkArchive archive, ArkName name) {
    super(archive, name);
    value = archive.getByte() != 0;
  }

  public PropertyBool(JsonObject o) {
    super(o);
    value = o.getBoolean("value");
  }

  @Override
  public Class<Boolean> getValueClass() {
    return Boolean.class;
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
    archive.putByte((byte) (value ? 1 : 0));
  }

  @Override
  protected int calculateAdditionalSize(boolean nameTable) {
    return 1; // Special case: value of PropertyBool is not considered "data"
  }

  @Override
  public int calculateDataSize(boolean nameTable) {
    return 0;
  }

}
