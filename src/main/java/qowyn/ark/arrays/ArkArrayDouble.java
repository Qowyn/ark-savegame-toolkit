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

public class ArkArrayDouble extends ArrayList<Double> implements ArkArray<Double> {

  public static final ArkName TYPE = ArkName.constantPlain("DoubleProperty");

  private static final long serialVersionUID = 1L;

  public ArkArrayDouble() {}

  public ArkArrayDouble(ArkArchive archive, PropertyArray property) {
    int size = archive.getInt();

    for (int n = 0; n < size; n++) {
      add(archive.getDouble());
    }
  }

  public ArkArrayDouble(JsonNode node, PropertyArray property) {
    node.forEach(n -> this.add(n.asDouble()));
  }

  @Override
  public Class<Double> getValueClass() {
    return Double.class;
  }

  @Override
  public ArkName getType() {
    return TYPE;
  }

  @Override
  public int calculateSize(NameSizeCalculator nameSizer) {
    return Integer.BYTES + size() * Double.BYTES;
  }

  @Override
  public void writeJson(JsonGenerator generator) throws IOException {
    generator.writeStartArray(size());

    for (double value: this) {
      generator.writeNumber(value);
    }

    generator.writeEndArray();
  }

  @Override
  public void writeBinary(ArkArchive archive) {
    archive.putInt(size());

    this.forEach(archive::putDouble);
  }

  @Override
  public void collectNames(NameCollector collector) {}

}
