package qowyn.ark.arrays;

import java.util.ArrayList;
import java.util.Set;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonNumber;

import qowyn.ark.ArkArchive;
import qowyn.ark.types.ArkName;

/**
 * This could backfire if ARK introduces an array of enum values for a property.
 *
 * TODO check available size and compare with number of elements
 *
 * @author Roland Firmont
 *
 */
public class ArkArrayByte extends ArrayList<Byte> implements ArkArray<Byte> {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  public ArkArrayByte() {}

  public ArkArrayByte(ArkArchive archive, ArkName propertyName) {
    int size = archive.getInt();

    for (int n = 0; n < size; n++) {
      add(archive.getByte());
    }
  }

  public ArkArrayByte(JsonArray a, ArkName propertyName) {
    a.getValuesAs(JsonNumber.class).forEach(n -> this.add((byte) n.intValueExact()));
  }

  @Override
  public Class<Byte> getValueClass() {
    return Byte.class;
  }

  @Override
  public int calculateSize(boolean nameTable) {
    return Integer.BYTES + size() * Byte.BYTES;
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

    this.forEach(archive::putByte);
  }

  @Override
  public void collectNames(Set<String> nameTable) {}

}
