package qowyn.ark.arrays;

import java.util.ArrayList;
import java.util.Set;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonString;

import qowyn.ark.ArkArchive;
import qowyn.ark.types.ArkName;

public class ArkArrayName extends ArrayList<ArkName> implements ArkArray<ArkName> {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  public ArkArrayName() {}

  public ArkArrayName(ArkArchive archive, int dataSize) {
    int size = archive.getInt();

    for (int n = 0; n < size; n++) {
      add(archive.getName());
    }
  }

  public ArkArrayName(JsonArray a, int dataSize) {
    a.getValuesAs(JsonString.class).forEach(s -> this.add(new ArkName(s.getString())));
  }

  @Override
  public Class<ArkName> getValueClass() {
    return ArkName.class;
  }

  @Override
  public int calculateSize(boolean nameTable) {
    int size = Integer.BYTES;

    size += stream().mapToInt(n -> ArkArchive.getNameLength(n, nameTable)).sum();

    return size;
  }

  @Override
  public JsonArray toJson() {
    JsonArrayBuilder jab = Json.createArrayBuilder();

    this.forEach(n -> jab.add(n.toString()));

    return jab.build();
  }

  @Override
  public void write(ArkArchive archive) {
    archive.putInt(size());

    this.forEach(archive::putName);
  }

  @Override
  public void collectNames(Set<String> nameTable) {
    forEach(n -> nameTable.add(n.getNameString()));
  }

}
