package qowyn.ark.arrays;

import java.util.ArrayList;
import java.util.Set;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonNumber;
import javax.json.JsonValue;

import qowyn.ark.ArkArchive;
import qowyn.ark.types.ArkName;

public class ArkArrayLong extends ArrayList<Long> implements ArkArray<Long> {

  private static final long serialVersionUID = 1L;

  public ArkArrayLong() {}

  public ArkArrayLong(ArkArchive archive, int dataSize, ArkName propertyName) {
    int size = archive.getInt();

    for (int n = 0; n < size; n++) {
      add(archive.getLong());
    }
  }

  public ArkArrayLong(JsonValue v, int dataSize, ArkName propertyName) {
    JsonArray a = (JsonArray) v;
    a.getValuesAs(JsonNumber.class).forEach(n -> this.add(n.longValue()));
  }

  @Override
  public Class<Long> getValueClass() {
    return Long.class;
  }

  @Override
  public int calculateSize(boolean nameTable) {
    return Integer.BYTES + size() * Long.BYTES;
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

    this.forEach(archive::putLong);
  }

  @Override
  public void collectNames(Set<String> nameTable) {}

}
