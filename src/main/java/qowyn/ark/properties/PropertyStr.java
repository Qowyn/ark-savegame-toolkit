package qowyn.ark.properties;

import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

import qowyn.ark.ArkArchive;

public class PropertyStr extends PropertyBase<String> {

  private String value;

  public PropertyStr(ArkArchive archive, PropertyArgs args) {
    super(archive, args);
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

  public String getValue() {
    return value;
  }

  @Override
  public void setValue(String value) {
    this.value = value;
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
