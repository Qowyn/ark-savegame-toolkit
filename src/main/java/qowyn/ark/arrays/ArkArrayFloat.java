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

public class ArkArrayFloat extends ArrayList<Float> implements ArkArray<Float> {

  public static final ArkName TYPE = ArkName.constantPlain("FloatProperty");

  private static final long serialVersionUID = 1L;

  public ArkArrayFloat() {}

  public ArkArrayFloat(ArkArchive archive, PropertyArray property) {
    int size = archive.getInt();

    for (int n = 0; n < size; n++) {
      add(archive.getFloat());
    }
  }

  public ArkArrayFloat(JsonArray a, PropertyArray property) {
    a.getValuesAs(JsonNumber.class).forEach(n -> this.add(n.bigDecimalValue().floatValue()));
  }

  @Override
  public Class<Float> getValueClass() {
    return Float.class;
  }

  @Override
  public ArkName getType() {
    return TYPE;
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
