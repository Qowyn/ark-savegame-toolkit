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

public class ArkArrayInt16 extends ArrayList<Short> implements ArkArray<Short> {

  public static final ArkName TYPE = ArkName.constantPlain("Int16Property");

  private static final long serialVersionUID = 1L;

  public ArkArrayInt16() {}

  public ArkArrayInt16(ArkArchive archive, PropertyArray property) {
    int size = archive.getInt();

    for (int n = 0; n < size; n++) {
      add(archive.getShort());
    }
  }

  public ArkArrayInt16(JsonNode node, PropertyArray property) {
    node.forEach(n -> this.add((short) n.asInt()));
  }

  @Override
  public Class<Short> getValueClass() {
    return Short.class;
  }

  @Override
  public ArkName getType() {
    return TYPE;
  }

  @Override
  public int calculateSize(NameSizeCalculator nameSizer) {
    return Integer.BYTES + size() * Short.BYTES;
  }

  @Override
  public void writeJson(JsonGenerator generator) throws IOException {
    generator.writeStartArray(size());

    for (short value: this) {
      generator.writeNumber(value);
    }

    generator.writeEndArray();
  }

  @Override
  public void writeBinary(ArkArchive archive) {
    archive.putInt(size());

    this.forEach(archive::putShort);
  }

  @Override
  public void collectNames(NameCollector collector) {}

}
