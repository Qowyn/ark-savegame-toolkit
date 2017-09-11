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

public class ArkArrayFloat extends ArrayList<Float> implements ArkArray<Float> {

  public static final ArkName TYPE = ArkName.constantPlain("FloatProperty");

  private static final long serialVersionUID = 1L;

  public ArkArrayFloat() {}

  public ArkArrayFloat(ArkArchive archive, PropertyArray property) {
    int size = archive.getInt();

    for (int n = 0; n < size; n++) {
      add(archive.getFloat());
    }
  }

  public ArkArrayFloat(JsonNode node, PropertyArray property) {
    node.forEach(n -> this.add((float) n.asDouble()));
  }

  @Override
  public Class<Float> getValueClass() {
    return Float.class;
  }

  @Override
  public ArkName getType() {
    return TYPE;
  }

  @Override
  public int calculateSize(NameSizeCalculator nameSizer) {
    return Integer.BYTES + size() * Float.BYTES;
  }

  @Override
  public void writeJson(JsonGenerator generator) throws IOException {
    generator.writeStartArray(size());

    for (float value: this) {
      generator.writeNumber(value);
    }

    generator.writeEndArray();
  }

  @Override
  public void writeBinary(ArkArchive archive) {
    archive.putInt(size());

    this.forEach(archive::putFloat);
  }

  @Override
  public void collectNames(NameCollector collector) {}

}
