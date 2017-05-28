package qowyn.ark.arrays;

import java.util.ArrayList;
import java.util.Set;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonString;

import qowyn.ark.ArkArchive;
import qowyn.ark.properties.PropertyArray;
import qowyn.ark.types.ArkName;

public class ArkArrayString extends ArrayList<String> implements ArkArray<String> {

  public static final ArkName TYPE = ArkName.constantPlain("StrProperty");

  private static final long serialVersionUID = 1L;

  public ArkArrayString() {}

  public ArkArrayString(ArkArchive archive, PropertyArray property) {
    int size = archive.getInt();

    for (int n = 0; n < size; n++) {
      add(archive.getString());
    }
  }

  public ArkArrayString(JsonArray a, PropertyArray property) {
    a.getValuesAs(JsonString.class).forEach(s -> this.add(s.getString()));
  }

  @Override
  public void collectNames(Set<String> nameTable) {}

  @Override
  public Class<String> getValueClass() {
    return String.class;
  }

  @Override
  public ArkName getType() {
    return TYPE;
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
