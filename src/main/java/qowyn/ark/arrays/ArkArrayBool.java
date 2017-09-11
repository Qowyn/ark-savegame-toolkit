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

public class ArkArrayBool extends ArrayList<Boolean> implements ArkArray<Boolean> {

  public static final ArkName TYPE = ArkName.constantPlain("BoolProperty");

  private static final long serialVersionUID = 1L;

  public ArkArrayBool() {}

  public ArkArrayBool(ArkArchive archive, PropertyArray property) {
    int size = archive.getInt();

    for (int n = 0; n < size; n++) {
      add(archive.getByte() != 0);
    }
  }

  public ArkArrayBool(JsonNode node, PropertyArray property) {
    node.forEach(n -> this.add(n.asBoolean()));
  }

  @Override
  public Class<Boolean> getValueClass() {
    return Boolean.class;
  }

  @Override
  public ArkName getType() {
    return TYPE;
  }

  @Override
  public int calculateSize(NameSizeCalculator nameSizer) {
    return Integer.BYTES + size();
  }

  @Override
  public void writeJson(JsonGenerator generator) throws IOException {
    generator.writeStartArray(size());

    for (boolean value: this) {
      generator.writeBoolean(value);
    }

    generator.writeEndArray();
  }

  @Override
  public void writeBinary(ArkArchive archive) {
    archive.putInt(size());

    this.forEach(b -> archive.putByte((byte) (b ? 1 : 0)));
  }

  @Override
  public void collectNames(NameCollector collector) {}

}
