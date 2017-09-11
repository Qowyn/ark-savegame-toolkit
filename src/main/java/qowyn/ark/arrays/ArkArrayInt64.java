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

public class ArkArrayInt64 extends ArrayList<Long> implements ArkArray<Long> {

  public static final ArkName TYPE = ArkName.constantPlain("Int64Property");

  private static final long serialVersionUID = 1L;

  public ArkArrayInt64() {}

  public ArkArrayInt64(ArkArchive archive, PropertyArray property) {
    int size = archive.getInt();

    for (int n = 0; n < size; n++) {
      add(archive.getLong());
    }
  }

  public ArkArrayInt64(JsonNode node, PropertyArray property) {
    node.forEach(n -> this.add(n.asLong()));
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
  public int calculateSize(NameSizeCalculator nameSizer) {
    return Integer.BYTES + size() * Long.BYTES;
  }

  @Override
  public void writeJson(JsonGenerator generator) throws IOException {
    generator.writeStartArray(size());

    for (long value: this) {
      generator.writeNumber(value);
    }

    generator.writeEndArray();
  }

  @Override
  public void writeBinary(ArkArchive archive) {
    archive.putInt(size());

    this.forEach(archive::putLong);
  }

  @Override
  public void collectNames(NameCollector collector) {}

}
