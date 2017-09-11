package qowyn.ark.properties;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;

import qowyn.ark.ArkArchive;
import qowyn.ark.NameSizeCalculator;
import qowyn.ark.types.ArkName;

public class PropertyInt8 extends PropertyBase<Byte> {

  public static final ArkName TYPE = ArkName.constantPlain("Int8Property");

  public PropertyInt8(String name, byte value) {
    super(ArkName.from(name), 0, value);
  }

  public PropertyInt8(String name, int index, byte value) {
    super(ArkName.from(name), index, value);
  }

  public PropertyInt8(ArkArchive archive, ArkName name) {
    super(archive, name);
    value = archive.getByte();
  }

  public PropertyInt8(JsonNode node) {
    super(node);
    value = (byte) node.path("value").asInt();
  }

  @Override
  public Class<Byte> getValueClass() {
    return Byte.class;
  }

  @Override
  public ArkName getType() {
    return TYPE;
  }

  @Override
  protected void writeJsonValue(JsonGenerator generator) throws IOException {
    generator.writeNumberField("value", value);
  }

  @Override
  protected void writeBinaryValue(ArkArchive archive) {
    archive.putByte(value);
  }

  @Override
  public int calculateDataSize(NameSizeCalculator nameSizer) {
    return Byte.BYTES;
  }

}
