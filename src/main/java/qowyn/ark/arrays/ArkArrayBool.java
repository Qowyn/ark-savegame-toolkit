package qowyn.ark.arrays;

import java.util.ArrayList;
import java.util.Set;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonValue;

import qowyn.ark.ArkArchive;
import qowyn.ark.types.ArkName;

public class ArkArrayBool extends ArrayList<Boolean> implements ArkArray<Boolean> {

  private static final long serialVersionUID = 1L;

  public ArkArrayBool() {}

  public ArkArrayBool(ArkArchive archive, int dataSize, ArkName propertyName) {
    int size = archive.getInt();

    for (int n = 0; n < size; n++) {
      add(archive.getByte() != 0);
    }
  }

  public ArkArrayBool(JsonValue v, int dataSize, ArkName propertyName) {
    JsonArray a = (JsonArray) v;
    a.forEach(n -> this.add(n != JsonValue.FALSE));
  }

  @Override
  public Class<Boolean> getValueClass() {
    return Boolean.class;
  }

  @Override
  public int calculateSize(boolean nameTable) {
    return Integer.BYTES + size();
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

    this.forEach(b -> archive.putByte((byte) (b ? 1 : 0)));
  }

  @Override
  public void collectNames(Set<String> nameTable) {}

}
