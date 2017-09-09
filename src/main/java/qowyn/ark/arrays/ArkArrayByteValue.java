package qowyn.ark.arrays;

import java.util.ArrayList;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonString;
import javax.json.JsonValue;
import javax.json.JsonValue.ValueType;

import qowyn.ark.ArkArchive;
import qowyn.ark.NameCollector;
import qowyn.ark.NameSizeCalculator;
import qowyn.ark.properties.PropertyArray;
import qowyn.ark.types.ArkByteValue;
import qowyn.ark.types.ArkName;

/**
 *
 * @author Roland Firmont
 *
 */
public class ArkArrayByteValue extends ArrayList<ArkByteValue> implements ArkArray<ArkByteValue> {

  private static final long serialVersionUID = 1L;

  public ArkArrayByteValue() {}

  public ArkArrayByteValue(ArkArchive archive, PropertyArray property) {
    int size = archive.getInt();

    for (int n = 0; n < size; n++) {
      add(new ArkByteValue(archive.getName()));
    }
  }

  public ArkArrayByteValue(JsonArray a, PropertyArray property) {
    for (JsonValue v : a) {
      if (v.getValueType() != ValueType.NULL) {
        add(new ArkByteValue(ArkName.from(((JsonString) v).getString())));
      }
    }
  }

  @Override
  public Class<ArkByteValue> getValueClass() {
    return ArkByteValue.class;
  }

  @Override
  public ArkName getType() {
    return ArkArrayByteHandler.TYPE;
  }

  @Override
  public int calculateSize(NameSizeCalculator nameSizer) {
    return Integer.BYTES + stream().mapToInt(bv -> nameSizer.sizeOf(bv.getNameValue())).sum();
  }

  @Override
  public JsonArray toJson() {
    JsonArrayBuilder jab = Json.createArrayBuilder();

    // Marker
    jab.addNull();
    this.forEach(bv -> jab.add(bv.getNameValue().toString()));

    return jab.build();
  }

  @Override
  public void write(ArkArchive archive) {
    archive.putInt(size());

    this.forEach(bv -> archive.putName(bv.getNameValue()));
  }

  @Override
  public void collectNames(NameCollector collector) {
    this.forEach(bv -> collector.accept(bv.getNameValue()));
  }

}
