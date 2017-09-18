package qowyn.ark;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;

import qowyn.ark.data.ExtraData;
import qowyn.ark.data.ExtraDataBlob;
import qowyn.ark.data.ExtraDataRegistry;
import qowyn.ark.properties.Property;
import qowyn.ark.properties.PropertyRegistry;
import qowyn.ark.properties.UnreadablePropertyException;
import qowyn.ark.types.ArkName;
import qowyn.ark.types.LocationData;

public class GameObject implements PropertyContainer, NameContainer {

  private static final String NULL_UUID_STRING = "00000000-0000-0000-0000-000000000000";

  private static final UUID NULL_UUID = UUID.fromString(NULL_UUID_STRING);

  private static final Map<UUID, UUID> uuidCache = new ConcurrentHashMap<>();

  private int id;

  private UUID uuid;

  private ArkName className;

  private boolean item;

  private List<ArkName> names = new ArrayList<>();

  private boolean fromDataFile;

  private int dataFileIndex;

  private LocationData locationData;

  /**
   * Cached propertiesSize, avoids calculating the size of properties twice
   */
  protected int propertiesSize;

  protected int propertiesOffset;

  protected List<Property<?>> properties = new ArrayList<>();

  protected ExtraData extraData;

  protected GameObject parent;

  protected Map<ArkName, GameObject> components = new LinkedHashMap<>();

  public GameObject() {}

  public GameObject(ArkArchive archive) {
    readBinary(archive);
  }

  public GameObject(JsonNode node) {
    this(node, true);
  }

  public GameObject(JsonNode node, boolean loadProperties) {
    readJson(node, loadProperties);
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public UUID getUuid() {
    return uuid;
  }

  public void setUuid(UUID uuid) {
    this.uuid = uuid;
  }

  public ArkName getClassName() {
    return className;
  }

  public void setClassName(ArkName className) {
    this.className = className;
  }

  public String getClassString() {
    return className != null ? className.toString() : null;
  }

  public void setClassString(String classString) {
    className = ArkName.from(classString);
  }

  public boolean isItem() {
    return item;
  }

  public void setItem(boolean item) {
    this.item = item;
  }

  public List<ArkName> getNames() {
    return names;
  }

  public void setNames(List<ArkName> names) {
    this.names = names;
  }

  public boolean isFromDataFile() {
    return fromDataFile;
  }

  public void setFromDataFile(boolean fromDataFile) {
    this.fromDataFile = fromDataFile;
  }

  public int getDataFileIndex() {
    return dataFileIndex;
  }

  public void setDataFileIndex(int dataFileIndex) {
    this.dataFileIndex = dataFileIndex;
  }

  public LocationData getLocation() {
    return locationData;
  }

  public void setLocation(LocationData location) {
    this.locationData = location;
  }

  public List<Property<?>> getProperties() {
    return properties;
  }

  @Override
  public void setProperties(List<Property<?>> properties) {
    this.properties = properties;
  }

  public ExtraData getExtraData() {
    return extraData;
  }

  public void setExtraData(ExtraData extraData) {
    this.extraData = extraData;
  }

  public void writeProperties(ArkArchive archive, int propertiesBlockOffset) {
    archive.position(propertiesBlockOffset + propertiesOffset);

    if (properties != null) {
      properties.forEach(p -> p.writeBinary(archive));
    }

    archive.putName(ArkName.NAME_NONE);

    if (extraData != null) {
      extraData.writeBinary(archive);
    } else {
      throw new UnsupportedOperationException("Cannot write binary data without known extra data");
    }
  }

  public void readJson(JsonNode node, boolean loadProperties) {
    
    uuid = uuidCache.computeIfAbsent(UUID.fromString(node.path("uuid").asText(NULL_UUID_STRING)), uuid -> uuid);
    className = ArkName.from(node.path("class").asText());
    item = node.path("item").asBoolean();

    names.clear();
    if (node.hasNonNull("names")) {
      for (JsonNode nameNode: node.get("names")) {
        names.add(ArkName.from(nameNode.asText()));
      }
    }

    fromDataFile = node.path("fromDataFile").asBoolean();
    dataFileIndex = node.path("dataFileIndex").asInt();

    if (node.hasNonNull("location")) {
      locationData = new LocationData(node.get("location"));
    } else {
      locationData = null;
    }

    properties.clear();
    if (loadProperties) {
      if (node.hasNonNull("properties")) {
        for (JsonNode propertyNode: node.get("properties")) {
          properties.add(PropertyRegistry.readJson(propertyNode));
        }
      }

      if (node.has("extra")) {
        extraData = ExtraDataRegistry.getExtraData(this, node.get("extra"));
      } else {
        extraData = null;
      }
    } else {
      extraData = null;
    }
  }

  public void writeJson(JsonGenerator generator) throws IOException {
    writeJson(generator, false);
  }

  public void writeJson(JsonGenerator generator, boolean withId) throws IOException {
    generator.writeStartObject();

    if (withId) {
      generator.writeNumberField("id", id);
    }

    if (uuid != null && !uuid.equals(NULL_UUID)) {
      generator.writeStringField("uuid", uuid.toString());
    }

    if (className != null) {
      generator.writeStringField("class", className.toString());
    }

    if (item) {
      generator.writeBooleanField("item", item);
    }

    if (names != null && names.size() > 0) {
      generator.writeArrayFieldStart("names");

      for (ArkName name: names) {
        generator.writeString(name.toString());
      }

      generator.writeEndArray();
    }

    if (fromDataFile) {
      generator.writeBooleanField("fromDataFile", fromDataFile);
    }

    if (dataFileIndex != 0) {
      generator.writeNumberField("dataFileIndex", dataFileIndex);
    }

    if (locationData != null) {
      generator.writeFieldName("location");
      locationData.writeJson(generator);
    }

    if (properties != null && !properties.isEmpty()) {
      generator.writeArrayFieldStart("properties");

      for (Property<?> property: properties) {
        property.writeJson(generator);
      }

      generator.writeEndArray();
    }

    if (extraData != null) {
      generator.writeFieldName("extra");
      extraData.writeJson(generator);
    }

    generator.writeEndObject();
  }

  public int getSize(NameSizeCalculator nameSizer) {
    // UUID item names.size() unkBool unkIndex (locationData!=null) propertiesOffset unkInt
    int size = 16 + Integer.BYTES * 7;

    size += nameSizer.sizeOf(className);

    if (names != null) {
      size += names.stream().mapToInt(nameSizer::sizeOf).sum();
    }

    if (locationData != null) {
      size += locationData.getSize();
    }

    return size;
  }

  /**
   * Calculates the size of the property block and caches the resulting value
   * @param nameSizer
   * @return
   */
  public int getPropertiesSize(NameSizeCalculator nameSizer) {
    int size = nameSizer.sizeOf(ArkName.NAME_NONE);

    size += properties.stream().mapToInt(p -> p.calculateSize(nameSizer)).sum();

    if (extraData != null) {
      size += extraData.calculateSize(nameSizer);
    } else {
      throw new UnsupportedOperationException("Cannot write binary data without known extra data");
    }

    propertiesSize = size;
    return size;
  }

  public void readBinary(ArkArchive archive) {
    long highOfHigh = archive.getInt();
    long lowOfHigh = archive.getInt();
    long high = (highOfHigh << 32) + lowOfHigh;
    long highOfLow = archive.getInt();
    long lowOfLow = archive.getInt();
    long low = (highOfLow << 32) + lowOfLow;

    uuid = uuidCache.computeIfAbsent(new UUID(high, low), uuid -> uuid);

    className = archive.getName();

    item = archive.getBoolean();

    int nameCount = archive.getInt();

    names.clear();
    for (int nameIndex = 0; nameIndex < nameCount; nameIndex++) {
      names.add(archive.getName());
    }

    fromDataFile = archive.getBoolean();
    dataFileIndex = archive.getInt();

    boolean hasLocationData = archive.getBoolean();

    if (hasLocationData) {
      locationData = new LocationData(archive);
    }

    propertiesOffset = archive.getInt();

    int shouldBeZero = archive.getInt();
    if (shouldBeZero != 0) {
      System.err.println("Expected int after propertiesOffset to be 0 but found " + shouldBeZero + " at " + Integer.toHexString(archive.position() - 4));
      archive.unknownData();
    }
  }

  public void loadProperties(ArkArchive archive, GameObject next, int propertiesBlockOffset) {
    int offset = propertiesBlockOffset + propertiesOffset;
    int nextOffset = (next != null) ? propertiesBlockOffset + next.propertiesOffset : archive.limit();

    archive.position(offset);
    int position = offset;

    properties.clear();
    try {
      Property<?> property = PropertyRegistry.readBinary(archive);

      while (property != null) {
        position = archive.position();
        properties.add(property);
        property = PropertyRegistry.readBinary(archive);
      }
    } catch (UnreadablePropertyException upe) {
      archive.unknownNames();

      archive.position(position);
      ExtraDataBlob blob = new ExtraDataBlob();
      blob.setData(archive.getBytes(nextOffset - position));
      extraData = blob;

      System.err.println("Error while reading property at " + Integer.toHexString(position) + " from GameObject " + id + " caused by:");
      upe.printStackTrace();
      return;
    }

    int distance = nextOffset - archive.position();

    if (distance > 0) {
      extraData = ExtraDataRegistry.getExtraData(this, archive, distance);
    } else {
      extraData = null;
    }
  }

  public int writeBinary(ArkArchive archive, int offset) {
    if (uuid != null) {
      archive.putLong(uuid.getMostSignificantBits());
      archive.putLong(uuid.getLeastSignificantBits());
    } else {
      archive.putLong(0);
      archive.putLong(0);
    }

    archive.putName(className);
    archive.putBoolean(item);

    if (names != null) {
      archive.putInt(names.size());
      names.forEach(archive::putName);
    } else {
      archive.putInt(0);
    }

    archive.putBoolean(fromDataFile);
    archive.putInt(dataFileIndex);

    if (locationData != null) {
      archive.putBoolean(true);
      locationData.writeBinary(archive);
    } else {
      archive.putBoolean(false);
    }

    propertiesOffset = offset;
    archive.putInt(propertiesOffset);
    archive.putInt(0);

    return offset + propertiesSize;
  }

  @Override
  public void collectNames(NameCollector collector) {
    collector.accept(className);

    if (names != null) {
      names.forEach(name -> collector.accept(name));
    }

    properties.forEach(property -> property.collectNames(collector));
    collector.accept(ArkName.NAME_NONE);

    if (extraData instanceof NameContainer) {
      ((NameContainer) extraData).collectNames(collector);
    }
  }

  public void collectBaseNames(NameCollector collector) {
    collector.accept(className);

    if (names != null) {
      names.forEach(name -> collector.accept(name));
    }
  }

  public void collectPropertyNames(NameCollector collector) {
    properties.forEach(property -> property.collectNames(collector));
    collector.accept(ArkName.NAME_NONE);

    if (extraData instanceof NameContainer) {
      ((NameContainer) extraData).collectNames(collector);
    }
  }

  public GameObject getParent() {
    return parent;
  }

  public void setParent(GameObject parent) {
    this.parent = parent;
  }

  public Map<ArkName, GameObject> getComponents() {
    return components;
  }

  public void setComponents(Map<ArkName, GameObject> components) {
    this.components = components;
  }

  public void addComponent(GameObject component) {
    this.components.put(component.getNames().get(0), component);
  }

  public boolean hasParentNames() {
    return names.size() > 1;
  }

  public List<ArkName> getParentNames() {
    return names.subList(1, names.size());
  }

  public static void clearUUIDCache() {
    uuidCache.clear();
  }

}
