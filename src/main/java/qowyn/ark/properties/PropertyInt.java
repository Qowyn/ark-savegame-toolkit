package qowyn.ark.properties;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;

import qowyn.ark.ArkArchive;
import qowyn.ark.NameSizeCalculator;
import qowyn.ark.types.ArkName;

public class PropertyInt extends PropertyBase<Integer> {

  public static final ArkName TYPE = ArkName.constantPlain("IntProperty");

  public PropertyInt(String name, int value) {
    super(ArkName.from(name), 0, value);
  }

  public PropertyInt(String name, int index, int value) {
    super(ArkName.from(name), index, value);
  }

  public PropertyInt(ArkArchive archive, ArkName name) {
    super(archive, name);
    value = archive.getInt();
  }

  public PropertyInt(JsonNode node) {
    super(node);
    value = node.path("value").asInt();
  }

  @Override
  public Class<Integer> getValueClass() {
    return Integer.class;
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
    archive.putInt(value);
  }

  @Override
  public int calculateDataSize(NameSizeCalculator nameSizer) {
    return Integer.BYTES;
  }

}
