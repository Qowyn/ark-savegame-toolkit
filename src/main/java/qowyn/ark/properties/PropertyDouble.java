package qowyn.ark.properties;

import qowyn.ark.ArkArchive;
import qowyn.ark.NameSizeCalculator;
import qowyn.ark.types.ArkName;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;

public class PropertyDouble extends PropertyBase<Double> {

  public static final ArkName TYPE = ArkName.constantPlain("DoubleProperty");

  public PropertyDouble(String name, double value) {
    super(ArkName.from(name), 0, value);
  }

  public PropertyDouble(String name, int index, double value) {
    super(ArkName.from(name), index, value);
  }

  public PropertyDouble(ArkArchive archive, ArkName name) {
    super(archive, name);
    value = archive.getDouble();
  }

  public PropertyDouble(JsonNode node) {
    super(node);
    value = node.path("value").asDouble();
  }

  @Override
  public Class<Double> getValueClass() {
    return Double.class;
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
    archive.putDouble(value);
  }

  @Override
  public int calculateDataSize(NameSizeCalculator nameSizer) {
    return Double.BYTES;
  }

}
