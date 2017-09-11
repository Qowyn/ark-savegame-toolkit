package qowyn.ark.properties;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;

import qowyn.ark.ArkArchive;
import qowyn.ark.NameSizeCalculator;
import qowyn.ark.types.ArkName;

public class PropertyInt16 extends PropertyBase<Short> {

  public static final ArkName TYPE = ArkName.constantPlain("Int16Property");

  public PropertyInt16(String name, short value) {
    super(ArkName.from(name), 0, value);
  }

  public PropertyInt16(String name, int index, short value) {
    super(ArkName.from(name), index, value);
  }

  public PropertyInt16(ArkArchive archive, ArkName name) {
    super(archive, name);
    value = archive.getShort();
  }

  public PropertyInt16(JsonNode node) {
    super(node);
    value = (short) node.path("value").asInt();
  }

  @Override
  public Class<Short> getValueClass() {
    return Short.class;
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
    archive.putShort(value);
  }

  @Override
  public int calculateDataSize(NameSizeCalculator nameSizer) {
    return Short.BYTES;
  }

}
