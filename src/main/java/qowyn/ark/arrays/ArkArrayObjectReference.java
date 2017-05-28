package qowyn.ark.arrays;

import java.util.ArrayList;
import java.util.Set;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;

import qowyn.ark.ArkArchive;
import qowyn.ark.properties.PropertyArray;
import qowyn.ark.types.ArkName;
import qowyn.ark.types.ObjectReference;

public class ArkArrayObjectReference extends ArrayList<ObjectReference> implements ArkArray<ObjectReference> {

  public static final ArkName TYPE = ArkName.constantPlain("ObjectProperty");

  private static final long serialVersionUID = 1L;

  public ArkArrayObjectReference() {}

  public ArkArrayObjectReference(ArkArchive archive, PropertyArray property) {
    int size = archive.getInt();

    for (int n = 0; n < size; n++) {
      add(new ObjectReference(archive, 8)); // Fixed size?
    }
  }

  public ArkArrayObjectReference(JsonArray a, PropertyArray property) {
    a.getValuesAs(JsonObject.class).forEach(o -> this.add(new ObjectReference(o, 8)));
  }

  @Override
  public Class<ObjectReference> getValueClass() {
    return ObjectReference.class;
  }

  @Override
  public ArkName getType() {
    return TYPE;
  }

  @Override
  public int calculateSize(boolean nameTable) {
    int size = Integer.BYTES;

    size += this.stream().mapToInt(or -> or.getSize(nameTable)).sum();

    return size;
  }

  @Override
  public JsonArray toJson() {
    JsonArrayBuilder jab = Json.createArrayBuilder();

    this.forEach(or -> jab.add(or.toJSON()));

    return jab.build();
  }

  @Override
  public void write(ArkArchive archive) {
    archive.putInt(size());

    this.forEach(or -> or.write(archive));
  }

  @Override
  public void collectNames(Set<String> nameTable) {
    this.forEach(or -> or.collectNames(nameTable));
  }

}
