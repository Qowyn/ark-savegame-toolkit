package qowyn.ark.properties;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;

import qowyn.ark.ArkArchive;
import qowyn.ark.NameSizeCalculator;
import qowyn.ark.types.ArkName;

public class PropertyInt64 extends PropertyBase<Long> {

  public static final ArkName TYPE = ArkName.constantPlain("Int64Property");

  public PropertyInt64(String name, long value) {
    super(ArkName.from(name), 0, value);
  }

  public PropertyInt64(String name, int index, long value) {
    super(ArkName.from(name), index, value);
  }

  public PropertyInt64(ArkArchive archive, ArkName name) {
    super(archive, name);
    value = archive.getLong();
  }

  public PropertyInt64(JsonNode node) {
    super(node);
    value = node.path("value").asLong();
  }

  @Override
  public Class<Long> getValueClass() {
    return Long.class;
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
    archive.putLong(value);
  }

  @Override
  public int calculateDataSize(NameSizeCalculator nameSizer) {
    return Long.BYTES;
  }

}
