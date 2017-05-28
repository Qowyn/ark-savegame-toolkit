package qowyn.ark.properties;

import java.util.Set;

import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

import qowyn.ark.ArkArchive;
import qowyn.ark.JsonHelper;
import qowyn.ark.types.ArkByteValue;
import qowyn.ark.types.ArkName;

public class PropertyByte extends PropertyBase<ArkByteValue> {

  public static final ArkName TYPE = ArkName.constantPlain("ByteProperty");

  private ArkName enumType;

  public PropertyByte(String name, ArkName typeName, ArkByteValue value, ArkName enumType) {
    super(ArkName.from(name), typeName, 0, value);
    this.enumType = enumType;
  }

  public PropertyByte(String name, ArkName typeName, int index, ArkByteValue value, ArkName enumType) {
    super(ArkName.from(name), typeName, index, value);
    this.enumType = enumType;
  }

  public PropertyByte(ArkArchive archive, PropertyArgs args) {
    super(archive, args);
    enumType = archive.getName();
    value = new ArkByteValue(archive, !enumType.equals(ArkName.NAME_NONE));
  }

  public PropertyByte(JsonObject o) {
    super(o);
    enumType = ArkName.from(o.getString("enum", ArkName.NAME_NONE.toString()));
    value = new ArkByteValue(o);
  }

  @Override
  public Class<ArkByteValue> getValueClass() {
    return ArkByteValue.class;
  }

  @Override
  protected void serializeValue(JsonObjectBuilder job) {
    JsonHelper.addString(job, "enum", enumType.toString(), ArkName.NAME_NONE.toString());
    job.add("value", value.toJson());
  }

  @Override
  protected void writeValue(ArkArchive archive) {
    archive.putName(enumType);
    value.write(archive);
  }

  @Override
  protected int calculateAdditionalSize(boolean nameTable) {
    return ArkArchive.getNameLength(enumType, nameTable);
  }

  @Override
  public int calculateDataSize(boolean nameTable) {
    return value.getSize(nameTable);
  }

  @Override
  public void collectNames(Set<String> nameTable) {
    super.collectNames(nameTable);
    nameTable.add(enumType.toString());
    value.collectNames(nameTable);
  }

  public ArkName getEnumType() {
    return enumType;
  }

  public void setEnumType(ArkName enumType) {
    this.enumType = enumType;
  }

}
