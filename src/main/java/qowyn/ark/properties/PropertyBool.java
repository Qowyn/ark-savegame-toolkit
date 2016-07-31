package qowyn.ark.properties;

import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

import qowyn.ark.ArkArchive;

public class PropertyBool extends PropertyBase<Boolean> {

  private boolean value;

  public PropertyBool(ArkArchive archive, PropertyArgs args) {
    super(archive, args);
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
  public Boolean getValue() {
    return value;
  }

  @Override
  public void setValue(Boolean value) {
    this.value = value;
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
