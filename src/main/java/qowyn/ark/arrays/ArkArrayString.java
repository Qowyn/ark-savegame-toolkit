package qowyn.ark.arrays;

import java.util.ArrayList;
import java.util.Set;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonString;
import javax.json.JsonValue;

import qowyn.ark.ArkArchive;
import qowyn.ark.types.ArkName;

public class ArkArrayString extends ArrayList<String> implements ArkArray<String> {

  private static final long serialVersionUID = 1L;

  public ArkArrayString() {}

  public ArkArrayString(ArkArchive archive, int dataSize, ArkName propertyName) {
    int size = archive.getInt();

    for (int n = 0; n < size; n++) {
      add(archive.getString());
    }
  }

  public ArkArrayString(JsonValue v, int dataSize, ArkName propertyName) {
    JsonArray a = (JsonArray) v;
    a.getValuesAs(JsonString.class).forEach(s -> this.add(s.getString()));
  }

  @Override
  public void collectNames(Set<String> nameTable) {}

  @Override
  public Class<String> getValueClass() {
    return String.class;
  }

  @Override
  public int calculateSize(boolean nameTable) {
    int size = Integer.BYTES;

    size += stream().mapToInt(ArkArchive::getStringLength).sum();

    return size;
  }

  @Override
  public JsonArray toJson() {
    JsonArrayBuilder jab = Json.createArrayBuilder();

    this.forEach(jab::add);

    return jab.build();
  }

  @Override
  public void write(ArkArchive archive) {
    archive.putInt(size());

    this.forEach(archive::putString);
  }

}
