package qowyn.ark.properties;

import java.util.Set;

import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

import qowyn.ark.ArkArchive;
import qowyn.ark.types.ArkName;

public class PropertyName extends PropertyBase<ArkName> {

  private ArkName value;

  public PropertyName(ArkArchive archive, PropertyArgs args) {
    super(archive, args);
    value = archive.getName();
  }

  public PropertyName(JsonObject o) {
    super(o);
    value = new ArkName(o.getString("value"));
  }

  @Override
  public Class<ArkName> getValueClass() {
    return ArkName.class;
  }

  public ArkName getValue() {
    return value;
  }

  @Override
  public void setValue(ArkName value) {
    this.value = value;
  }

  @Override
  protected void serializeValue(JsonObjectBuilder job) {
    job.add("value", value.toString());
  }

  @Override
  protected void writeValue(ArkArchive archive) {
    archive.putName(value);
  }

  @Override
  public int calculateDataSize(boolean nameTable) {
    return ArkArchive.getNameLength(value, nameTable);
  }

  @Override
  public void collectNames(Set<String> nameTable) {
    super.collectNames(nameTable);
    nameTable.add(value.getNameString());
  }

}
