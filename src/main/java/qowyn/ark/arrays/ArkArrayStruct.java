package qowyn.ark.arrays;

import java.util.ArrayList;
import java.util.Set;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonValue;

import qowyn.ark.ArkArchive;
import qowyn.ark.structs.Struct;
import qowyn.ark.structs.StructPropertyList;
import qowyn.ark.structs.StructRegistry;
import qowyn.ark.types.ArkName;

public class ArkArrayStruct extends ArrayList<Struct> implements ArkArray<Struct> {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  public ArkArrayStruct() {}

  public ArkArrayStruct(ArkArchive archive, int dataSize, ArkName propertyName) {
    int size = archive.getInt();
    
    ArkName structType = StructRegistry.mapArrayNameToTypeName(propertyName);
    if (structType == null) {
      if (size * 4 + 4 == dataSize) {
        structType = ArkName.from("Color");
      } else if (size * 12 + 4 == dataSize) {
        structType = ArkName.from("Vector");
      } else if (size * 16 + 4 == dataSize) {
        structType = ArkName.from("LinearColor");
      }
    }

    for (int n = 0; n < size; n++) {
      add(StructRegistry.read(archive, structType));
    }
  }

  public ArkArrayStruct(JsonValue v, int dataSize, ArkName propertyName) {
    JsonArray a = (JsonArray) v;
    int size = a.size();

    ArkName structType;
    if (size * 4 + 4 == dataSize) {
      structType = ArkName.from("Color");
    } else if (size * 12 + 4 == dataSize) {
      structType = ArkName.from("Vector");
    } else if (size * 16 + 4 == dataSize) {
      structType = ArkName.from("LinearColor");
    } else {
      structType = null;
    }

    if (structType != null) {
      a.forEach(o -> this.add(StructRegistry.read(o, structType)));
    } else {
      a.forEach(o -> this.add(new StructPropertyList(o)));
    }
  }

  @Override
  public Class<Struct> getValueClass() {
    return Struct.class;
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
