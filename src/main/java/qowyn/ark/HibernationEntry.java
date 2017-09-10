package qowyn.ark;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonString;
import javax.json.JsonValue;

import qowyn.ark.types.ArkName;

public class HibernationEntry {

  private float x;

  private float y;

  private float z;

  private byte unkByte;

  private float unkFloat;

  private final ArrayList<ArkName> zoneVolumes = new ArrayList<>();

  private final ArrayList<GameObject> objects = new ArrayList<>();

  private int unkInt1;

  private int classIndex;

  private List<String> nameTable;

  private int nameTableSize;

  private int objectsSize;

  private int propertiesStart;

  public HibernationEntry() {
  }

  public HibernationEntry(ArkArchive archive, boolean loadProperties) {
    readBinary(archive, loadProperties);
  }

  public HibernationEntry(JsonObject object, boolean loadProperties) {
    readJson(object, loadProperties);
  }

  public void readBinary(ArkArchive archive, boolean loadProperties) {
    x = archive.getFloat();
    y = archive.getFloat();
    z = archive.getFloat();
    unkByte = archive.getByte();
    unkFloat = archive.getFloat();

    if (loadProperties) {
      ArkArchive nameArchive = archive.slice(archive.getInt());
      readBinaryNameTable(nameArchive);
    } else {
      archive.skipBytes(archive.getInt());
      nameTable = null;

      // Unknown data since the missed names are unrelated to the main nameTable
      archive.unknownData();
    }

    ArkArchive objectArchive = archive.slice(archive.getInt());
    readBinaryObjects(objectArchive);

    unkInt1 = archive.getInt();
    classIndex = archive.getInt();
  }

  protected void readBinaryNameTable(ArkArchive archive) {
    int version = archive.getInt();
    if (version != 3) {
      archive.debugMessage(LoggerHelper.format("Found unknown Version %d", version), -4);
      throw new UnsupportedOperationException();
    }

    int count = archive.getInt();
    nameTable = new ArrayList<>(count);

    for (int index = 0; index < count; index++) {
      nameTable.add(archive.getString());
    }

    int zoneCount = archive.getInt();

    for (int index = 0; index < zoneCount; index++) {
      zoneVolumes.add(archive.getName());
    }
  }

  protected void readBinaryObjects(ArkArchive archive) {
    int count = archive.getInt();

    objects.clear();
    objects.ensureCapacity(count);
    for (int index = 0; index < count; index++) {
      objects.add(new GameObject(archive));
    }

    if (nameTable != null) {
      archive.setNameTable(nameTable, 0, true);
  
      for (int index = 0; index < count; index++) {
        objects.get(index).loadProperties(archive, index + 1 < count ? objects.get(index + 1) : null, 0);
      }
    }
  }

  public void writeBinary(ArkArchive archive) {
    archive.putFloat(x);
    archive.putFloat(y);
    archive.putFloat(z);
    archive.putByte(unkByte);
    archive.putFloat(unkFloat);

    archive.putInt(nameTableSize);
    ArkArchive nameArchive = archive.slice(nameTableSize);
    nameArchive.putInt(3);

    nameArchive.putInt(nameTable.size());
    nameTable.forEach(nameArchive::putString);

    nameArchive.putInt(zoneVolumes.size());
    zoneVolumes.forEach(nameArchive::putName);

    archive.putInt(objectsSize);
    ArkArchive objectArchive = archive.slice(objectsSize);
    objectArchive.putInt(objects.size());

    int currentOffset = propertiesStart;
    for (GameObject object: objects) {
      currentOffset = object.write(objectArchive, currentOffset);
    }

    objectArchive.setNameTable(nameTable, 0, true);
    for (GameObject object: objects) {
      object.writeProperties(objectArchive, 0);
    }

    archive.putInt(unkInt1);
    archive.putInt(classIndex);
  }

  public int getSizeAndCollectNames() {
    // x y z unkFloat, unkByte, unkInt1 classIndex nameTableSize objectsSize
    int size = Float.BYTES * 4 + 1 + Integer.BYTES * 4;

    Set<String> names = new LinkedHashSet<>();
    for (GameObject object: objects) {
      object.collectPropertyNames(name -> names.add(name.toString()));
    }

    NameSizeCalculator objectSizer = ArkArchive.getNameSizer(false);
    NameSizeCalculator propertiesSizer = ArkArchive.getNameSizer(true, true);

    nameTableSize = Integer.BYTES * 3;
    nameTable = new ArrayList<>(names);

    nameTableSize += nameTable.stream().mapToInt(ArkArchive::getStringLength).sum();
    nameTableSize += zoneVolumes.stream().mapToInt(objectSizer::sizeOf).sum();

    objectsSize = Integer.BYTES;

    objectsSize += objects.stream().mapToInt(go -> go.getSize(objectSizer)).sum();

    propertiesStart = objectsSize;

    objectsSize += objects.stream().mapToInt(go -> go.getPropertiesSize(propertiesSizer)).sum();

    return size + nameTableSize + objectsSize;
  }

  public void readJson(JsonObject hibernatedObject, boolean loadProperties) {
    x = hibernatedObject.getJsonNumber("x").bigDecimalValue().floatValue();
    y = hibernatedObject.getJsonNumber("y").bigDecimalValue().floatValue();
    z = hibernatedObject.getJsonNumber("z").bigDecimalValue().floatValue();
    unkByte = hibernatedObject.getJsonNumber("unkByte").bigIntegerValue().byteValue();
    unkFloat = hibernatedObject.getJsonNumber("unkFloat").bigDecimalValue().floatValue();

    JsonArray zoneArray = hibernatedObject.getJsonArray("zones");
    zoneVolumes.clear();
    if (zoneArray != null) {
      zoneVolumes.ensureCapacity(zoneArray.size());
      for (JsonString zone: zoneArray.getValuesAs(JsonString.class)) {
        zoneVolumes.add(ArkName.from(zone.getString()));
      }
    }

    JsonArray objectArray = hibernatedObject.getJsonArray("objects");
    objects.clear();
    if (objectArray != null) {
      objects.ensureCapacity(objectArray.size());
      for (JsonObject object: objectArray.getValuesAs(JsonObject.class)) {
        objects.add(new GameObject(object, loadProperties));
      }
    }

    unkInt1 = hibernatedObject.getInt("unkInt1");
    classIndex = hibernatedObject.getInt("classIndex");
  }

  public JsonValue toJson() {
    JsonArrayBuilder zoneArray = Json.createArrayBuilder();

    zoneVolumes.forEach(z -> zoneArray.add(z.toString()));

    JsonArrayBuilder objectArray = Json.createArrayBuilder();

    objects.forEach(o -> objectArray.add(o.toJson(false)));

    return Json.createObjectBuilder()
        .add("x", x)
        .add("y", y)
        .add("z", z)
        .add("unkByte", unkByte)
        .add("unkFloat", unkFloat)
        .add("zones", zoneArray)
        .add("objects", objectArray)
        .add("unkInt1", unkInt1)
        .add("classIndex", classIndex)
        .build();
  }

  public float getX() {
    return x;
  }

  public void setX(float x) {
    this.x = x;
  }

  public float getY() {
    return y;
  }

  public void setY(float y) {
    this.y = y;
  }

  public float getZ() {
    return z;
  }

  public void setZ(float z) {
    this.z = z;
  }

  public byte getUnkByte() {
    return unkByte;
  }

  public void setUnkByte(byte unkByte) {
    this.unkByte = unkByte;
  }

  public float getUnkFloat() {
    return unkFloat;
  }

  public void setUnkFloat(float unkFloat) {
    this.unkFloat = unkFloat;
  }

  public int getUnkInt1() {
    return unkInt1;
  }

  public void setUnkInt1(int unkInt1) {
    this.unkInt1 = unkInt1;
  }

  public int getClassIndex() {
    return classIndex;
  }

  public void setClassIndex(int classIndex) {
    this.classIndex = classIndex;
  }

  public ArrayList<ArkName> getZoneVolumes() {
    return zoneVolumes;
  }

  public ArrayList<GameObject> getObjects() {
    return objects;
  }

}
