package qowyn.ark.properties;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;

import qowyn.ark.ArkArchive;
import qowyn.ark.NameCollector;
import qowyn.ark.NameSizeCalculator;
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

  public PropertyName(JsonNode node) {
    super(node);
    value = ArkName.from(node.path("value").asText());
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
  protected void writeJsonValue(JsonGenerator generator) throws IOException {
    generator.writeStringField("value", value.toString());
  }

  @Override
  protected void writeBinaryValue(ArkArchive archive) {
    archive.putName(value);
  }

  @Override
  public int calculateDataSize(NameSizeCalculator nameSizer) {
    return nameSizer.sizeOf(value);
  }

  @Override
  public void collectNames(NameCollector collector) {
    super.collectNames(collector);
    collector.accept(value);
  }

}
