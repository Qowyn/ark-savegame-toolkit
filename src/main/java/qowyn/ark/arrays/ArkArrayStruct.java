package qowyn.ark.arrays;

import java.util.ArrayList;
import java.util.Set;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonValue;

import qowyn.ark.ArkArchive;
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
      add(StructRegistry.read(archive, structType));
    }
  }

  public ArkArrayStruct(JsonArray a, PropertyArray property) {
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

    for (JsonValue v : a) {
      add(StructRegistry.read(v, structType));
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
  public int calculateSize(boolean nameTable) {
    int size = Integer.BYTES;

    size += this.stream().mapToInt(s -> s.getSize(nameTable)).sum();

    return size;
  }

  @Override
  public JsonArray toJson() {
    JsonArrayBuilder jab = Json.createArrayBuilder();

    this.forEach(spl -> jab.add(spl.toJson()));

    return jab.build();
  }

  @Override
  public void write(ArkArchive archive) {
    archive.putInt(size());

    this.forEach(spl -> spl.write(archive));
  }

  @Override
  public void collectNames(Set<String> nameTable) {
    this.forEach(spl -> spl.collectNames(nameTable));
  }

}
