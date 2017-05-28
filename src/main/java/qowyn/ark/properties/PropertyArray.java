package qowyn.ark.properties;

import java.util.Set;

import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue.ValueType;

import qowyn.ark.ArkArchive;
import qowyn.ark.arrays.ArkArray;
import qowyn.ark.arrays.ArkArrayRegistry;
import qowyn.ark.arrays.ArkArrayStruct;
import qowyn.ark.arrays.ArkArrayUnknown;
import qowyn.ark.types.ArkName;

public class PropertyArray extends PropertyBase<ArkArray<?>> {

  public static final ArkName TYPE = ArkName.constantPlain("ArrayProperty");

  public PropertyArray(String name, ArkArray<?> value) {
    super(ArkName.from(name), 0, value);
  }

  public PropertyArray(String name, int index, ArkArray<?> value) {
    super(ArkName.from(name), index, value);
  }

  public PropertyArray(ArkArchive archive, ArkName name) {
    super(archive, name);
    ArkName arrayType = archive.getName();

    int position = archive.position();

    try {
      value = ArkArrayRegistry.read(archive, arrayType, this);

      if (value == null) {
        throw new UnreadablePropertyException("ArkArrayRegistry returned null");
      }
    } catch (UnreadablePropertyException upe) {
      archive.position(position);

      value = new ArkArrayUnknown(archive, dataSize, arrayType);

      archive.unknownNames();
      System.err.println("Reading ArrayProperty of type " + arrayType + " with name " + name + " as byte blob because:");
      upe.printStackTrace();
    }
  }

  public PropertyArray(JsonObject o) {
    super(o);
    ArkName arrayType = ArkName.from(o.getString("arrayType"));

    if (o.get("value").getValueType() == ValueType.STRING) {
      value = new ArkArrayUnknown(o.getString("value"), arrayType);
    } else {
      value = ArkArrayRegistry.read(o.getJsonArray("value"), arrayType, this);
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public Class<ArkArray<?>> getValueClass() {
    return (Class<ArkArray<?>>) (Class<?>) ArkArray.class;
  }

  @Override
  public ArkName getType() {
    return TYPE;
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
    job.add("arrayType", value.getType().toString());
    job.add("value", value.toJson());
  }

  @Override
  protected void writeValue(ArkArchive archive) {
    archive.putName(value.getType());
    value.write(archive);
  }

  @Override
  protected int calculateAdditionalSize(boolean nameTable) {
    return ArkArchive.getNameLength(value.getType(), nameTable);
  }

  @Override
  public int calculateDataSize(boolean nameTable) {
    return value.calculateSize(nameTable);
  }

  @Override
  public void collectNames(Set<String> nameTable) {
    super.collectNames(nameTable);
    nameTable.add(value.getType().getName());
    value.collectNames(nameTable);
  }

  @Override
  protected boolean isDataSizeNeeded() {
    return value instanceof ArkArrayStruct;
  }

}
