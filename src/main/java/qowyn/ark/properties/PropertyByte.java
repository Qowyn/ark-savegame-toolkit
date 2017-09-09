package qowyn.ark.properties;

import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

import qowyn.ark.ArkArchive;
import qowyn.ark.JsonHelper;
import qowyn.ark.NameCollector;
import qowyn.ark.NameSizeCalculator;
import qowyn.ark.types.ArkByteValue;
import qowyn.ark.types.ArkName;

public class PropertyByte extends PropertyBase<ArkByteValue> {

  public static final ArkName TYPE = ArkName.constantPlain("ByteProperty");

  private ArkName enumType;

  public PropertyByte(String name, ArkName value, ArkName enumType) {
    super(ArkName.from(name), 0, new ArkByteValue(value));
    this.enumType = enumType;
  }

  public PropertyByte(String name, int index, ArkName value, ArkName enumType) {
    super(ArkName.from(name), index, new ArkByteValue(value));
    this.enumType = enumType;
  }

  public PropertyByte(String name, byte value) {
    super(ArkName.from(name), 0, new ArkByteValue(value));
    this.enumType = ArkName.NAME_NONE;
  }

  public PropertyByte(String name, int index, byte value) {
    super(ArkName.from(name), index, new ArkByteValue(value));
    this.enumType = ArkName.NAME_NONE;
  }

  public PropertyByte(ArkArchive archive, ArkName name) {
    super(archive, name);
    enumType = archive.getName();
    value = new ArkByteValue(archive, !enumType.equals(ArkName.NAME_NONE));
  }

  public PropertyByte(JsonObject o) {
    super(o);
    enumType = ArkName.from(o.getString("enum", ArkName.NAME_NONE.toString()));
    value = new ArkByteValue(o.get("value"));
  }

  @Override
  public Class<ArkByteValue> getValueClass() {
    return ArkByteValue.class;
  }

  @Override
  public ArkName getType() {
    return TYPE;
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
  protected int calculateAdditionalSize(NameSizeCalculator nameSizer) {
    return nameSizer.sizeOf(enumType);
  }

  @Override
  public int calculateDataSize(NameSizeCalculator nameSizer) {
    return value.getSize(nameSizer);
  }

  @Override
  public void collectNames(NameCollector collector) {
    super.collectNames(collector);
    collector.accept(enumType);
    value.collectNames(collector);
  }

  public ArkName getEnumType() {
    return enumType;
  }

  public void setEnumType(ArkName enumType) {
    this.enumType = enumType;
  }

}
