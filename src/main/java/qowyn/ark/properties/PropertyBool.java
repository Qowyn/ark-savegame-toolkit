package qowyn.ark.properties;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;

import qowyn.ark.ArkArchive;
import qowyn.ark.NameSizeCalculator;
import qowyn.ark.types.ArkName;

public class PropertyBool extends PropertyBase<Boolean> {

  public static final ArkName TYPE = ArkName.constantPlain("BoolProperty");

  public PropertyBool(String name, boolean value) {
    super(ArkName.from(name), 0, value);
  }

  public PropertyBool(String name, int index, boolean value) {
    super(ArkName.from(name), index, value);
  }

  public PropertyBool(ArkArchive archive, ArkName name) {
    super(archive, name);
    value = archive.getByte() != 0;
  }

  public PropertyBool(JsonNode node) {
    super(node);
    value = node.path("value").asBoolean();
  }

  @Override
  public Class<Boolean> getValueClass() {
    return Boolean.class;
  }

  @Override
  public ArkName getType() {
    return TYPE;
  }

  @Override
  protected void writeJsonValue(JsonGenerator generator) throws IOException {
    generator.writeBooleanField("value", value);
  }
  @Override
  protected void writeBinaryValue(ArkArchive archive) {
    archive.putByte((byte) (value ? 1 : 0));
  }

  @Override
  protected int calculateAdditionalSize(NameSizeCalculator nameSizer) {
    return 1; // Special case: value of PropertyBool is not considered "data"
  }

  @Override
  public int calculateDataSize(NameSizeCalculator nameSizer) {
    return 0;
  }

}
