package qowyn.ark.structs;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;

import qowyn.ark.ArkArchive;
import qowyn.ark.NameSizeCalculator;
import qowyn.ark.properties.UnreadablePropertyException;

public class StructUnknown extends StructBase {

  private final byte[] value;

  public StructUnknown(ArkArchive archive, int dataSize) {
    this.value = archive.getBytes(dataSize);
  }

  public StructUnknown(JsonNode node) {
    try {
      value = node.binaryValue();
    } catch (IOException ex) {
      throw new UnreadablePropertyException(ex);
    }
  }

  @Override
  public void writeJson(JsonGenerator generator) throws IOException {
    generator.writeBinary(value);
  }

  @Override
  public void writeBinary(ArkArchive archive) {
    archive.putBytes(value);
  }

  @Override
  public int getSize(NameSizeCalculator nameSizer) {
    return value.length;
  }

}
