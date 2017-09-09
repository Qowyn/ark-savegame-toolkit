package qowyn.ark;

import java.util.ArrayList;
import java.util.List;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonValue;

public class HibernatedObject {

  public float x;

  public float y;

  public float z;

  public byte unkByte;

  public float unkFloat;

  public final ArrayList<GameObject> objects = new ArrayList<>();

  public int unkInt1;

  public int classIndex;

  public HibernatedObject() {
  }

  public HibernatedObject(ArkArchive archive) {
    readBinary(archive);
  }

  public void readBinary(ArkArchive archive) {
    x = archive.getFloat();
    y = archive.getFloat();
    z = archive.getFloat();
    unkByte = archive.getByte();
    unkFloat = archive.getFloat();

    ArkArchive nameArchive = archive.slice(archive.getInt());
    List<String> nameTable = readBinaryNameTable(nameArchive);

    ArkArchive objectArchive = archive.slice(archive.getInt());
    readBinaryObjects(objectArchive, nameTable);

    unkInt1 = archive.getInt();
    classIndex = archive.getInt();
  }

  protected List<String> readBinaryNameTable(ArkArchive archive) {
    int version = archive.getInt();
    if (version != 3) {
      archive.debugMessage(LoggerHelper.format("Found unknown Version %d", version), -4);
      throw new UnsupportedOperationException();
    }

    int count = archive.getInt();
    List<String> result = new ArrayList<>(count);

    for (int index = 0; index < count; index++) {
      result.add(archive.getString());
    }

    return result;
  }

  protected void readBinaryObjects(ArkArchive archive, List<String> nameTable) {
    int count = archive.getInt();

    objects.clear();
    objects.ensureCapacity(count);
    for (int index = 0; index < count; index++) {
      objects.add(new GameObject(archive));
    }

    archive.setNameTable(nameTable, 0, true);

    for (int index = 0; index < count; index++) {
      objects.get(index).loadProperties(archive, index + 1 < count ? objects.get(index + 1) : null, 0);
    }
  }

  public JsonValue toJson() {
    JsonArrayBuilder jab = Json.createArrayBuilder();

    objects.forEach(o -> jab.add(o.toJson(false)));

    return Json.createObjectBuilder()
        .add("x", x)
        .add("y", y)
        .add("z", z)
        .add("unkByte", unkByte)
        .add("unkFloat", unkFloat)
        .add("objects", jab)
        .add("unkInt1", unkInt1)
        .add("classIndex", classIndex)
        .build();
  }

}
