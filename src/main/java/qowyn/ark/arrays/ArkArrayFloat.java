package qowyn.ark.arrays;

import java.util.ArrayList;
import java.util.Set;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonNumber;

import qowyn.ark.ArkArchive;

public class ArkArrayFloat extends ArrayList<Float> implements ArkArray<Float> {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  public ArkArrayFloat() {}

  public ArkArrayFloat(ArkArchive archive, int dataSize) {
    int size = archive.getInt();

    for (int n = 0; n < size; n++) {
      add(archive.getFloat());
    }
  }

  public ArkArrayFloat(JsonArray a, int dataSize) {
    a.getValuesAs(JsonNumber.class).forEach(n -> this.add(n.bigDecimalValue().floatValue()));
  }

  @Override
  public Class<Float> getValueClass() {
    return Float.class;
  }

  @Override
  public int calculateSize(boolean nameTable) {
    return Integer.BYTES + size() * Float.BYTES;
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

    this.forEach(archive::putFloat);
  }

  @Override
  public void collectNames(Set<String> nameTable) {}

}
