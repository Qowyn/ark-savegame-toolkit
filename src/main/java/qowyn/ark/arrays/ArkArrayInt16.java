package qowyn.ark.arrays;

import java.util.ArrayList;
import java.util.Set;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonNumber;

import qowyn.ark.ArkArchive;

public class ArkArrayInt16 extends ArrayList<Short> implements ArkArray<Short> {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  public ArkArrayInt16() {}

  public ArkArrayInt16(ArkArchive archive, int dataSize) {
    int size = archive.getInt();

    for (int n = 0; n < size; n++) {
      add(archive.getShort());
    }
  }

  public ArkArrayInt16(JsonArray a, int dataSize) {
    a.getValuesAs(JsonNumber.class).forEach(n -> this.add((short)n.intValue()));
  }

  @Override
  public Class<Short> getValueClass() {
    return Short.class;
  }

  @Override
  public int calculateSize(boolean nameTable) {
    return Integer.BYTES + size() * Short.BYTES;
  }

  @Override
  public JsonArray toJson() {
    JsonArrayBuilder jab = Json.createArrayBuilder();

    this.forEach(n -> jab.add(n));

    return jab.build();
  }

  @Override
  public void write(ArkArchive archive) {
    archive.putInt(size());

    this.forEach(archive::putShort);
  }

  @Override
  public void collectNames(Set<String> nameTable) {}

}
