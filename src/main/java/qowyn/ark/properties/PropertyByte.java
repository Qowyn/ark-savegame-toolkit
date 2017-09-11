package qowyn.ark.properties;
import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;

import qowyn.ark.ArkArchive;
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

  public PropertyByte(JsonNode node) {
    super(node);
    enumType = ArkName.from(node.path("enum").asText(ArkName.NAME_NONE.toString()));
    value = new ArkByteValue(node.path("value"));
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
  protected void writeJsonValue(JsonGenerator generator) throws IOException {
    if (enumType != ArkName.NAME_NONE) {
      generator.writeStringField("enum", enumType.toString());
    }

    generator.writeFieldName("value");
    value.writeJson(generator);
  }

  @Override
  protected void writeBinaryValue(ArkArchive archive) {
    archive.putName(enumType);
    value.writeBinary(archive);
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
