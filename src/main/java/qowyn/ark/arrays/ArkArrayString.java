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

public class ArkArrayString extends ArrayList<String> implements ArkArray<String> {

  public static final ArkName TYPE = ArkName.constantPlain("StrProperty");

  private static final long serialVersionUID = 1L;

  public ArkArrayString() {}

  public ArkArrayString(ArkArchive archive, PropertyArray property) {
    int size = archive.getInt();

    for (int n = 0; n < size; n++) {
      add(archive.getString());
    }
  }

  public ArkArrayString(JsonNode node, PropertyArray property) {
    node.forEach(n -> this.add(n.textValue()));
  }

  @Override
  public void collectNames(NameCollector collector) {}

  @Override
  public Class<String> getValueClass() {
    return String.class;
  }

  @Override
  public ArkName getType() {
    return TYPE;
  }

  @Override
  public int calculateSize(NameSizeCalculator nameSizer) {
    int size = Integer.BYTES;

    size += stream().mapToInt(ArkArchive::getStringLength).sum();

    return size;
  }

  @Override
  public void writeJson(JsonGenerator generator) throws IOException {
    generator.writeStartArray(size());

    for (String value: this) {
      generator.writeString(value);
    }

    generator.writeEndArray();
  }

  @Override
  public void writeBinary(ArkArchive archive) {
    archive.putInt(size());

    this.forEach(archive::putString);
  }

}
