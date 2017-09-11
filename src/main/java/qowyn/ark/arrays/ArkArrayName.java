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

public class ArkArrayName extends ArrayList<ArkName> implements ArkArray<ArkName> {

  public static final ArkName TYPE = ArkName.constantPlain("NameProperty");

  private static final long serialVersionUID = 1L;

  public ArkArrayName() {}

  public ArkArrayName(ArkArchive archive, PropertyArray property) {
    int size = archive.getInt();

    for (int n = 0; n < size; n++) {
      add(archive.getName());
    }
  }

  public ArkArrayName(JsonNode node, PropertyArray property) {
    node.forEach(n -> this.add(ArkName.from(n.asText())));
  }

  @Override
  public Class<ArkName> getValueClass() {
    return ArkName.class;
  }

  @Override
  public ArkName getType() {
    return TYPE;
  }

  @Override
  public int calculateSize(NameSizeCalculator nameSizer) {
    int size = Integer.BYTES;

    size += stream().mapToInt(nameSizer::sizeOf).sum();

    return size;
  }

  @Override
  public void writeJson(JsonGenerator generator) throws IOException {
    generator.writeStartArray(size());

    for (ArkName value: this) {
      generator.writeString(value.toString());
    }

    generator.writeEndArray();
  }

  @Override
  public void writeBinary(ArkArchive archive) {
    archive.putInt(size());

    this.forEach(archive::putName);
  }

  @Override
  public void collectNames(NameCollector collector) {
    forEach(collector::accept);
  }

}
