package qowyn.ark.properties;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;

import qowyn.ark.ArkArchive;
import qowyn.ark.NameSizeCalculator;
import qowyn.ark.types.ArkName;

public class PropertyStr extends PropertyBase<String> {

  public static final ArkName TYPE = ArkName.constantPlain("StrProperty");

  public PropertyStr(String name, String value) {
    super(ArkName.from(name), 0, value);
  }

  public PropertyStr(String name, int index, String value) {
    super(ArkName.from(name), index, value);
  }

  public PropertyStr(ArkArchive archive, ArkName name) {
    super(archive, name);
    value = archive.getString();
  }

  public PropertyStr(JsonNode node) {
    super(node);
    value = node.path("value").textValue();
  }

  @Override
  public Class<String> getValueClass() {
    return String.class;
  }

  @Override
  public ArkName getType() {
    return TYPE;
  }

  @Override
  protected void writeBinaryValue(ArkArchive archive) {
    archive.putString(value);
  }

  @Override
  protected void writeJsonValue(JsonGenerator generator) throws IOException {
    generator.writeStringField("value", value);
  }

  @Override
  public int calculateDataSize(NameSizeCalculator nameSizer) {
    return ArkArchive.getStringLength(value);
  }

}
