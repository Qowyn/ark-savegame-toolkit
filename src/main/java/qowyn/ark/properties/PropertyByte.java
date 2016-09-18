package qowyn.ark.properties;

import java.util.Set;

import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

import qowyn.ark.ArkArchive;
import qowyn.ark.JsonHelper;
import qowyn.ark.types.ArkByteValue;
import qowyn.ark.types.ArkName;

public class PropertyByte extends PropertyBase<ArkByteValue> {

  public PropertyByte(String name, String typeName, ArkByteValue value) {
    super(name, typeName, 0, value);
  }

  public PropertyByte(String name, String typeName, int index, ArkByteValue value) {
    super(name, typeName, index, value);
  }

  public PropertyByte(ArkArchive archive, PropertyArgs args) {
    super(archive, args);
    ArkName enumName = archive.getName();
    value = new ArkByteValue(archive, enumName);
  }

  public PropertyByte(JsonObject o) {
    super(o);
    ArkName enumName = new ArkName(o.getString("enum", ArkByteValue.NONE));
    value = new ArkByteValue(o, enumName);
  }

  @Override
  public Class<ArkByteValue> getValueClass() {
    return ArkByteValue.class;
  }

  @Override
  public ArkByteValue getValue() {
    return value;
  }

  @Override
  public void setValue(ArkByteValue value) {
    this.value = value;
  }

  @Override
  protected void serializeValue(JsonObjectBuilder job) {
    JsonHelper.addString(job, "enum", value.getEnumName().toString(), ArkByteValue.NONE);
    job.add("value", value.toJson());
  }

  @Override
  protected void writeValue(ArkArchive archive) {
    archive.putName(value.getEnumName());
    value.write(archive);
  }

  @Override
  protected int calculateAdditionalSize(boolean nameTable) {
    return ArkArchive.getNameLength(value.getEnumName(), nameTable);
  }

  @Override
  public int calculateDataSize(boolean nameTable) {
    return value.getSize(nameTable);
  }

  @Override
  public void collectNames(Set<String> nameTable) {
    super.collectNames(nameTable);
    value.collectNames(nameTable);
  }

}
