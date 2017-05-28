package qowyn.ark.properties;

import java.util.Set;

import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

import qowyn.ark.ArkArchive;
import qowyn.ark.types.ArkName;

public class PropertyName extends PropertyBase<ArkName> {

  public static final ArkName TYPE = ArkName.constantPlain("NameProperty");

  public PropertyName(String name, ArkName value) {
    super(ArkName.from(name), 0, value);
  }

  public PropertyName(String name, int index, ArkName value) {
    super(ArkName.from(name), index, value);
  }

  public PropertyName(ArkArchive archive, ArkName name) {
    super(archive, name);
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
  public ArkName getType() {
    return TYPE;
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
