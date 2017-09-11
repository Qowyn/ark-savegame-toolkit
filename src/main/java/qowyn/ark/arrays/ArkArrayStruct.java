package qowyn.ark.arrays;

import java.io.IOException;
import java.util.ArrayList;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;

import qowyn.ark.ArkArchive;
import qowyn.ark.NameCollector;
import qowyn.ark.NameSizeCalculator;
import qowyn.ark.properties.PropertyArray;
import qowyn.ark.structs.Struct;
import qowyn.ark.structs.StructRegistry;
import qowyn.ark.types.ArkName;

public class ArkArrayStruct extends ArrayList<Struct> implements ArkArray<Struct> {

  public static final ArkName TYPE = ArkName.constantPlain("StructProperty");

  private static final long serialVersionUID = 1L;

  private static final ArkName COLOR = ArkName.constantPlain("Color");

  private static final ArkName VECTOR = ArkName.constantPlain("Vector");

  private static final ArkName LINEAR_COLOR = ArkName.constantPlain("LinearColor");

  public ArkArrayStruct() {}

  public ArkArrayStruct(ArkArchive archive, PropertyArray property) {
    int size = archive.getInt();

    ArkName structType = StructRegistry.mapArrayNameToTypeName(property.getName());
    if (structType == null) {
      if (size * 4 + 4 == property.getDataSize()) {
        structType = COLOR;
      } else if (size * 12 + 4 == property.getDataSize()) {
        structType = VECTOR;
      } else if (size * 16 + 4 == property.getDataSize()) {
        structType = LINEAR_COLOR;
      }
    }

    for (int n = 0; n < size; n++) {
      add(StructRegistry.readBinary(archive, structType));
    }
  }

  public ArkArrayStruct(JsonNode node, PropertyArray property) {
    int size = property.getDataSize();

    ArkName structType = StructRegistry.mapArrayNameToTypeName(property.getName());
    if (structType == null) {
      if (size * 4 + 4 == property.getDataSize()) {
        structType = COLOR;
      } else if (size * 12 + 4 == property.getDataSize()) {
        structType = VECTOR;
      } else if (size * 16 + 4 == property.getDataSize()) {
        structType = LINEAR_COLOR;
      }
    }

    for (JsonNode v: node) {
      add(StructRegistry.readJson(v, structType));
    }
  }

  @Override
  public Class<Struct> getValueClass() {
    return Struct.class;
  }

  @Override
  public ArkName getType() {
    return TYPE;
  }

  @Override
  public int calculateSize(NameSizeCalculator nameSizer) {
    int size = Integer.BYTES;

    size += this.stream().mapToInt(s -> s.getSize(nameSizer)).sum();

    return size;
  }

  @Override
  public void writeJson(JsonGenerator generator) throws IOException {
    generator.writeStartArray(size());

    for (Struct value: this) {
      value.writeJson(generator);
    }

    generator.writeEndArray();
  }

  @Override
  public void writeBinary(ArkArchive archive) {
    archive.putInt(size());

    this.forEach(spl -> spl.writeBinary(archive));
  }

  @Override
  public void collectNames(NameCollector collector) {
    this.forEach(spl -> spl.collectNames(collector));
  }

}
