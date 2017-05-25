package qowyn.ark.properties;

import java.util.Set;

import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

import qowyn.ark.ArkArchive;
import qowyn.ark.types.ArkName;

public class PropertyName extends PropertyBase<ArkName> {

  public PropertyName(String name, String typeName, ArkName value) {
    super(name, typeName, 0, value);
  }

  public PropertyName(String name, String typeName, int index, ArkName value) {
    super(name, typeName, index, value);
  }

  public PropertyName(ArkArchive archive, PropertyArgs args) {
    super(archive, args);
    value = archive.getName();
  }

  public PropertyName(JsonObject o) {
    super(o);
    value = ArkName.from(o.getString("value"));
  }

  @Override
  public Class<ArkName> getValueClass() {
    return ArkName.class;
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
    nameTable.add(value.getName());
  }

}
