package qowyn.ark.arrays;

import java.io.IOException;
import java.util.ArrayList;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;

import qowyn.ark.ArkArchive;
import qowyn.ark.NameCollector;
import qowyn.ark.NameSizeCalculator;
import qowyn.ark.properties.PropertyArray;
import qowyn.ark.types.ArkName;

public class ArkArrayInt8 extends ArrayList<Byte> implements ArkArray<Byte> {

  public static final ArkName TYPE = ArkName.constantPlain("Int8Property");

  private static final long serialVersionUID = 1L;

  public ArkArrayInt8() {}

  public ArkArrayInt8(ArkArchive archive, PropertyArray property) {
    int size = archive.getInt();

    for (int n = 0; n < size; n++) {
      add(archive.getByte());
    }
  }

  public ArkArrayInt8(JsonNode node, PropertyArray property) {
    node.forEach(n -> this.add((byte) n.asInt()));
  }

  @Override
  public Class<Byte> getValueClass() {
    return Byte.class;
  }

  @Override
  public ArkName getType() {
    return TYPE;
  }

  @Override
  public int calculateSize(NameSizeCalculator nameSizer) {
    return Integer.BYTES + size() * Byte.BYTES;
  }

  @Override
  public void writeJson(JsonGenerator generator) throws IOException {
    generator.writeStartArray(size());

    for (byte value: this) {
      generator.writeNumber(value);
    }

    generator.writeEndArray();
  }

  @Override
  public void writeBinary(ArkArchive archive) {
    archive.putInt(size());

    this.forEach(archive::putByte);
  }

  @Override
  public void collectNames(NameCollector collector) {}

}
