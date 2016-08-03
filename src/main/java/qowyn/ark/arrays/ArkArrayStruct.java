package qowyn.ark.arrays;

import java.util.ArrayList;
import java.util.Set;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;

import qowyn.ark.ArkArchive;
import qowyn.ark.structs.Struct;
import qowyn.ark.structs.StructPropertyList;

public class ArkArrayStruct extends ArrayList<Struct> implements ArkArray<Struct> {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  public ArkArrayStruct() {}

  public ArkArrayStruct(ArkArchive archive) {
    int size = archive.getInt();

    for (int n = 0; n < size; n++) {
      add(new StructPropertyList(archive, null));
    }
  }

  public ArkArrayStruct(JsonArray a) {
    a.forEach(o -> this.add(new StructPropertyList(o, null)));
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
