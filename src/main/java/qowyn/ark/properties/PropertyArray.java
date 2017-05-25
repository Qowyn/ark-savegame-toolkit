package qowyn.ark.properties;

import java.util.Set;

import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

import qowyn.ark.ArkArchive;
import qowyn.ark.arrays.ArkArray;
import qowyn.ark.arrays.ArkArrayRegistry;
import qowyn.ark.arrays.ArkArrayStruct;
import qowyn.ark.arrays.ArkArrayUnknown;
import qowyn.ark.types.ArkName;

public class PropertyArray extends PropertyBase<ArkArray<?>> {

  private ArkName arrayType;

  public PropertyArray(String name, String typeName, ArkArray<?> value, ArkName arrayType) {
    super(name, typeName, 0, value);
    this.arrayType = arrayType;
  }

  public PropertyArray(String name, String typeName, int index, ArkArray<?> value, ArkName arrayType) {
    super(name, typeName, index, value);
    this.arrayType = arrayType;
  }

  public PropertyArray(ArkArchive archive, PropertyArgs args) {
    super(archive, args);
    arrayType = archive.getName();

    int position = archive.position();

    try {
      value = ArkArrayRegistry.read(archive, arrayType, dataSize, name);

      if (value == null) {
        throw new UnreadablePropertyException("ArkArrayRegistry returned null");
      }
    } catch (UnreadablePropertyException upe) {
      archive.position(position);

      value = new ArkArrayUnknown(archive, dataSize);

      archive.unknownNames();
      System.err.println("Reading ArrayProperty of type " + arrayType + " with name " + name + " as byte blob because:");
      upe.printStackTrace();
    }
  }

  public PropertyArray(JsonObject o) {
    super(o);
    arrayType = ArkName.from(o.getString("arrayType"));

    if (o.containsKey("unknown")) {
      value = new ArkArrayUnknown(o.get("unknown"));
    } else {
      value = ArkArrayRegistry.read(o.get("value"), arrayType, dataSize, name);
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public Class<ArkArray<?>> getValueClass() {
    return (Class<ArkArray<?>>) (Class<?>) ArkArray.class;
  }

  @SuppressWarnings("unchecked")
  public <T> ArkArray<T> getTypedValue() {
    return (ArkArray<T>) value;
  }

  @SuppressWarnings("unchecked")
  public <T> ArkArray<T> getTypedValue(Class<T> clazz) {
    return value != null && value.getValueClass().isAssignableFrom(clazz) ? (ArkArray<T>) value : null;
  }

  @Override
  protected void serializeValue(JsonObjectBuilder job) {
    job.add("arrayType", arrayType.toString());
    if (value instanceof ArkArrayUnknown) {
      job.add("unknown", value.toJson());
    } else {
      job.add("value", value.toJson());
    }
  }

  @Override
  protected void writeValue(ArkArchive archive) {
    archive.putName(arrayType);
    value.write(archive);
  }

  @Override
  protected int calculateAdditionalSize(boolean nameTable) {
    return ArkArchive.getNameLength(arrayType, nameTable);
  }

  @Override
  public int calculateDataSize(boolean nameTable) {
    return value.calculateSize(nameTable);
  }

  @Override
  public void collectNames(Set<String> nameTable) {
    super.collectNames(nameTable);
    nameTable.add(arrayType.getName());
    value.collectNames(nameTable);
  }

  @Override
  protected boolean isDataSizeNeeded() {
    return value instanceof ArkArrayStruct;
  }

}
