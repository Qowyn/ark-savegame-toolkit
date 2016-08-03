package qowyn.ark;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonString;
import javax.json.JsonValue;
import javax.json.JsonValue.ValueType;

import qowyn.ark.data.ExtraData;
import qowyn.ark.data.ExtraDataRegistry;
import qowyn.ark.properties.Property;
import qowyn.ark.properties.PropertyReader;
import qowyn.ark.properties.UnreadablePropertyException;
import qowyn.ark.types.ArkName;
import qowyn.ark.types.LocationData;

public class GameObject implements PropertyContainer, NameContainer {

  private int id;

  private UUID uuid;

  private ArkName className;

  private boolean item;

  private List<ArkName> names;

  private boolean unkBool;

  private int unkIndex; // Related to unkBool

  private LocationData locationData;

  protected int propertiesOffset;

  protected List<Property<?>> properties = new ArrayList<>();

  protected ExtraData extraData;

  public GameObject() {}

  public GameObject(ArkArchive archive) {
    read(archive);
  }

  public GameObject(JsonObject o) {
    this(o, true);
  }

  public GameObject(JsonObject o, boolean loadProperties) {
    fromJson(o, loadProperties);
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
    className = new ArkName(classString);
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

  public boolean isUnkBool() {
    return unkBool;
  }

  public void setUnkBool(boolean unkBool) {
    this.unkBool = unkBool;
  }

  public int getUnkIndex() {
    return unkIndex;
  }

  public void setUnkIndex(int unkIndex) {
    this.unkIndex = unkIndex;
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

  public void setProperties(List<Property<?>> properties) {
    this.properties = Objects.requireNonNull(properties);
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
      properties.forEach(p -> p.write(archive));
    }

    archive.putName(Property.NONE_NAME);

    if (extraData != null) {
      extraData.write(archive);
    }
  }

  private static final String NULL_UUID_STRING = "00000000-0000-0000-0000-000000000000";

  private static final UUID NULL_UUID = UUID.fromString(NULL_UUID_STRING);

  public void fromJson(JsonObject o, boolean loadProperties) {
    uuid = UUID.fromString(o.getString("uuid", NULL_UUID_STRING));
    className = new ArkName(o.getString("class", ""));
    item = o.getBoolean("item", false);

    JsonArray nameArray = o.getJsonArray("names");
    if (nameArray != null) {
      names = nameArray.getValuesAs(JsonString.class).stream().map(s -> new ArkName(s.getString())).collect(Collectors.toList());
    }

    unkBool = o.getBoolean("unkBool", false);
    unkIndex = o.getInt("unkIndex", 0);

    JsonObject locData = o.getJsonObject("location");
    if (locData != null) {
      locationData = new LocationData(locData);
    }

    if (loadProperties) {
      JsonArray propertiesArray = o.getJsonArray("properties");
      if (propertiesArray != null) {
        properties = propertiesArray.getValuesAs(JsonObject.class).parallelStream().map(PropertyReader::fromJSON).collect(Collectors.toList());
      }

      JsonValue extra = o.get("extra");
      if (extra != null && extra.getValueType() != ValueType.NULL) {
        extraData = ExtraDataRegistry.getExtraData(this, extra);
      }
    }
  }

  public JsonObject toJson() {
    return toJson(false);
  }

  public JsonObject toJson(boolean withId) {
    JsonObjectBuilder job = Json.createObjectBuilder();

    if (withId) {
      job.add("id", id);
    }

    if (uuid != null && !uuid.equals(NULL_UUID)) {
      job.add("uuid", uuid.toString());
    }

    if (className != null) {
      job.add("class", className.toString());
    }

    if (item) {
      job.add("item", item);
    }

    if (names != null && names.size() > 0) {
      JsonArrayBuilder namesArray = Json.createArrayBuilder();
      names.stream().map(ArkName::toString).forEachOrdered(namesArray::add);

      job.add("names", namesArray);
    }

    if (unkBool) {
      job.add("unkBool", unkBool);
    }

    if (unkIndex != 0) {
      job.add("unkIndex", unkIndex);
    }

    if (locationData != null) {
      job.add("location", locationData.toJson());
    }

    if (properties != null && !properties.isEmpty()) {
      JsonArrayBuilder propsBuilder = Json.createArrayBuilder();
      properties.stream().map(Property::toJson).forEach(propsBuilder::add);

      job.add("properties", propsBuilder);
    }

    if (extraData != null) {
      job.add("extra", extraData.toJson());
    }

    return job.build();
  }

  public int getSize(boolean nameTable) {
    // UUID item names.size() unkBool unkIndex (locationData!=null) propertiesOffset unkInt
    int size = 16 + Integer.BYTES * 7;

    size += ArkArchive.getNameLength(className, nameTable);

    if (names != null) {
      size += names.stream().mapToInt(n -> ArkArchive.getNameLength(n, nameTable)).sum();
    }

    if (locationData != null) {
      size += locationData.getSize();
    }

    return size;
  }

  public int getPropertiesSize(boolean nameTable) {
    int size = ArkArchive.getNameLength(Property.NONE_NAME, nameTable);

    size += properties.stream().mapToInt(p -> p.calculateSize(nameTable)).sum();

    if (extraData != null) {
      size += extraData.calculateSize(nameTable);
    }

    return size;
  }

  public void read(ArkArchive archive) {
    long uuidMostSig = archive.getLong();
    long uuidLeastSig = archive.getLong();

    uuid = new UUID(uuidMostSig, uuidLeastSig);

    className = archive.getName();

    item = archive.getBoolean();

    int countNames = archive.getInt();
    names = new ArrayList<>();

    for (int nameIndex = 0; nameIndex < countNames; nameIndex++) {
      names.add(archive.getName());
    }

    unkBool = archive.getBoolean();
    unkIndex = archive.getInt();

    int countLocationData = archive.getInt();

    if (countLocationData > 1) {
      System.err.print("countLocationData > 1 at " + Integer.toHexString(archive.position() - 4));
    }

    if (countLocationData != 0) {
      locationData = new LocationData(archive);
    }

    propertiesOffset = archive.getInt();

    int shouldBeZero = archive.getInt();
    if (shouldBeZero != 0) {
      System.err.println("Expected int after propertiesOffset to be 0 but found " + shouldBeZero + " at " + Integer.toHexString(archive.position() - 4));
    }
  }

  public void loadProperties(ArkArchive archive, GameObject next, int propertiesBlockOffset) {
    int offset = propertiesBlockOffset + propertiesOffset;
    int nextOffset = (next != null) ? propertiesBlockOffset + next.propertiesOffset : archive.limit();

    archive.position(offset);

    properties.clear();
    try {
      Property<?> property = PropertyReader.readProperty(archive);

      while (property != null) {
        properties.add(property);
        property = PropertyReader.readProperty(archive);
      }
    } catch (UnreadablePropertyException upe) {
      // Stop reading and ignore possible extra data for now, needs a new field in ExtraDataHandler
      return;
    }

    int distance = nextOffset - archive.position();

    if (distance > 0) {
      extraData = ExtraDataRegistry.getExtraData(this, archive, distance);
    } else {
      extraData = null;
    }
  }

  public int write(ArkArchive archive, int offset) {
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

    archive.putBoolean(unkBool);
    archive.putInt(unkIndex);

    if (locationData != null) {
      archive.putInt(1);
      locationData.write(archive);
    } else {
      archive.putInt(0);
    }

    propertiesOffset = offset;
    archive.putInt(propertiesOffset);
    archive.putInt(0);

    return offset + getPropertiesSize(archive.hasNameTable());
  }

  public void collectNames(Set<String> nameTable) {
    nameTable.add(className.getNameString());

    if (names != null) {
      names.forEach(name -> nameTable.add(name.getNameString()));
    }

    properties.forEach(property -> property.collectNames(nameTable));

    if (extraData instanceof NameContainer) {
      ((NameContainer) extraData).collectNames(nameTable);
    }
  }

}
