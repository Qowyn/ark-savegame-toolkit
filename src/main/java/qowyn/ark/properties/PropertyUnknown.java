package qowyn.ark.properties;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;

import qowyn.ark.ArkArchive;
import qowyn.ark.NameSizeCalculator;
import qowyn.ark.types.ArkName;

public class PropertyUnknown extends PropertyBase<byte[]> {

  private final ArkName type;

  public PropertyUnknown(ArkArchive archive, ArkName name, ArkName type) {
    super(archive, name);
    this.type = type;
    value = archive.getBytes(dataSize);
  }

  public PropertyUnknown(JsonNode node) {
    super(node);
    this.type = ArkName.from(node.path("type").asText());
    try {
      value = node.path("value").binaryValue();
    } catch (IOException ex) {
      throw new UnreadablePropertyException(ex);
    }
  }

  @Override
  public Class<byte[]> getValueClass() {
    return byte[].class;
  }

  @Override
  public ArkName getType() {
    return type;
  }

  @Override
  public int calculateDataSize(NameSizeCalculator nameSizer) {
    return value.length;
  }

  @Override
  protected void writeBinaryValue(ArkArchive archive) {
    archive.putBytes(value);
  }

  @Override
  protected void writeJsonValue(JsonGenerator generator) throws IOException {
    generator.writeBinaryField("value", value);
  }

  @Override
  public final void setValue(byte[] value) {
    throw new UnsupportedOperationException();
  }

}
