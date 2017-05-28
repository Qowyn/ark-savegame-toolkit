package qowyn.ark.arrays;

import java.util.ArrayList;
import java.util.Set;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonNumber;

import qowyn.ark.ArkArchive;
import qowyn.ark.properties.PropertyArray;
import qowyn.ark.types.ArkName;

public class ArkArrayInt64 extends ArrayList<Long> implements ArkArray<Long> {

  public static final ArkName TYPE = ArkName.constantPlain("Int64Property");

  private static final long serialVersionUID = 1L;

  public ArkArrayInt64() {}

  public ArkArrayInt64(ArkArchive archive, PropertyArray property) {
    int size = archive.getInt();

    for (int n = 0; n < size; n++) {
      add(archive.getLong());
    }
  }

  public ArkArrayInt64(JsonArray a, PropertyArray property) {
    a.getValuesAs(JsonNumber.class).forEach(n -> this.add(n.longValue()));
  }

  @Override
  public Class<Long> getValueClass() {
    return Long.class;
  }

  @Override
  public ArkName getType() {
    return TYPE;
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
