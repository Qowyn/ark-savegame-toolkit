package qowyn.ark;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;

import qowyn.ark.types.ArkName;
import qowyn.ark.types.ObjectReference;

public class HibernationEntry implements GameObjectContainerMixin {

  private float x;

  private float y;

  private float z;

  private byte unkByte;

  private float unkFloat;

  private final ArrayList<ArkName> zoneVolumes = new ArrayList<>();

  private final ArrayList<GameObject> objects = new ArrayList<>();

  private final Map<Integer, Map<List<ArkName>, GameObject>> objectMap = new HashMap<>();

  private int unkInt1;

  private int classIndex;

  private List<String> nameTable;

  private int nameTableSize;

  private int objectsSize;

  private int propertiesStart;

  public HibernationEntry() {
  }

  public HibernationEntry(ArkArchive archive, ReadingOptions options) {
    readBinary(archive, options);
  }

  public HibernationEntry(JsonNode node, ReadingOptions options) {
    readJson(node, options);
  }

  public void readBinary(ArkArchive archive, ReadingOptions options) {
    x = archive.getFloat();
    y = archive.getFloat();
    z = archive.getFloat();
    unkByte = archive.getByte();
    unkFloat = archive.getFloat();

    if (options.getHibernationObjectProperties()) {
      ArkArchive nameArchive = archive.slice(archive.getInt());
      readBinaryNameTable(nameArchive);
    } else {
      archive.skipBytes(archive.getInt());
      nameTable = null;

      // Unknown data since the missed names are unrelated to the main nameTable
      archive.unknownData();
    }

    ArkArchive objectArchive = archive.slice(archive.getInt());
    readBinaryObjects(objectArchive, options);

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

  protected void readBinaryObjects(ArkArchive archive, ReadingOptions options) {
    int count = archive.getInt();

    objects.clear();
    objects.ensureCapacity(count);
    objectMap.clear();
    for (int index = 0; index < count; index++) {
      addObject(new GameObject(archive), options.getBuildComponentTree());
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
      currentOffset = object.writeBinary(objectArchive, currentOffset);
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

  public void readJson(JsonNode node, ReadingOptions options) {
    x = (float) node.path("x").asDouble();
    y = (float) node.path("y").asDouble();
    z = (float) node.path("z").asDouble();
    unkByte = (byte) node.path("unkByte").asInt();
    unkFloat = (float) node.path("unkFloat").asDouble();

    zoneVolumes.clear();
    if (node.hasNonNull("zones")) {
      for (JsonNode zone: node.path("zones")) {
        zoneVolumes.add(ArkName.from(zone.asText()));
      }
    }

    objects.clear();
    objectMap.clear();
    if (node.hasNonNull("objects")) {
      for (JsonNode object: node.path("objects")) {
        addObject(new GameObject(object, options.getHibernationObjectProperties()), options.getBuildComponentTree());
      }
    }

    unkInt1 = node.path("unkInt1").asInt();
    classIndex = node.path("classIndex").asInt();
  }

  public void writeJson(JsonGenerator generator) throws IOException {
    generator.writeStartObject();

    generator.writeNumberField("x", x);
    generator.writeNumberField("y", y);
    generator.writeNumberField("z", z);
    generator.writeNumberField("unkByte", unkByte);
    generator.writeNumberField("unkFloat", unkFloat);

    generator.writeArrayFieldStart("zones");
    for (ArkName zone: zoneVolumes) {
      generator.writeString(zone.toString());
    }
    generator.writeEndArray();

    generator.writeArrayFieldStart("objects");
    for (GameObject object: objects) {
      object.writeJson(generator, true);
    }
    generator.writeEndArray();

    generator.writeNumberField("unkInt1", unkInt1);
    generator.writeNumberField("classIndex", classIndex);

    generator.writeEndObject();
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

  @Override
  public ArrayList<GameObject> getObjects() {
    return objects;
  }

  @Override
  public Map<Integer, Map<List<ArkName>, GameObject>> getObjectMap() {
    return objectMap;
  }

  @Override
  public GameObject getObject(ObjectReference reference) {
    if (reference == null || !reference.isId()) {
      return null;
    }

    if (reference.getObjectId() > 0 && reference.getObjectId() <= getObjects().size()) {
      return getObjects().get(reference.getObjectId() - 1);
    } else {
      return null;
    }
  }

}
