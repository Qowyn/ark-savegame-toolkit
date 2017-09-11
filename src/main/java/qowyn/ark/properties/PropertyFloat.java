package qowyn.ark.properties;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;

import qowyn.ark.ArkArchive;
import qowyn.ark.NameSizeCalculator;
import qowyn.ark.types.ArkName;

public class PropertyFloat extends PropertyBase<Float> {

  public static final ArkName TYPE = ArkName.constantPlain("FloatProperty");

  public PropertyFloat(String name, float value) {
    super(ArkName.from(name), 0, value);
  }

  public PropertyFloat(String name, int index, float value) {
    super(ArkName.from(name), index, value);
  }

  public PropertyFloat(ArkArchive archive, ArkName name) {
    super(archive, name);
    value = archive.getFloat();
  }

  public PropertyFloat(JsonNode node) {
    super(node);
    value = (float) node.path("value").asDouble();
  }

  @Override
  public Class<Float> getValueClass() {
    return Float.class;
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
    archive.putFloat(value);
  }

  @Override
  public int calculateDataSize(NameSizeCalculator nameSizer) {
    return Float.BYTES;
  }

}
