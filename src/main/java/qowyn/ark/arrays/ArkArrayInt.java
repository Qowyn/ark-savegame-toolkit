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

public class ArkArrayInt extends ArrayList<Integer> implements ArkArray<Integer> {

  public static final ArkName TYPE = ArkName.constantPlain("IntProperty");

  private static final long serialVersionUID = 1L;

  public ArkArrayInt() {}

  public ArkArrayInt(ArkArchive archive, PropertyArray property) {
    int size = archive.getInt();

    for (int n = 0; n < size; n++) {
      add(archive.getInt());
    }
  }

  public ArkArrayInt(JsonNode node, PropertyArray property) {
    node.forEach(n -> this.add(n.asInt()));
  }

  @Override
  public Class<Integer> getValueClass() {
    return Integer.class;
  }

  @Override
  public ArkName getType() {
    return TYPE;
  }

  @Override
  public int calculateSize(NameSizeCalculator nameSizer) {
    return Integer.BYTES + size() * Integer.BYTES;
  }

  @Override
  public void writeJson(JsonGenerator generator) throws IOException {
    generator.writeStartArray(size());

    for (int value: this) {
      generator.writeNumber(value);
    }

    generator.writeEndArray();
  }

  @Override
  public void writeBinary(ArkArchive archive) {
    archive.putInt(size());

    this.forEach(archive::putInt);
  }

  @Override
  public void collectNames(NameCollector collector) {}

}
