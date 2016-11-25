package qowyn.ark.properties;

import java.util.Set;

import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

import qowyn.ark.ArkArchive;
import qowyn.ark.arrays.ArkArray;
import qowyn.ark.arrays.ArkArrayRegistry;
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
      value = ArkArrayRegistry.read(archive, arrayType, dataSize);

      if (value == null) {
        throw new UnreadablePropertyException();
      }
    } catch (UnreadablePropertyException upe) {
      archive.position(position + dataSize);
      System.err.println("Warning: Unreadable ArrayProperty with name " + name + ", skipping.");
      throw new UnreadablePropertyException();
    }
  }

  public PropertyArray(JsonObject o) {
    super(o);
    arrayType = new ArkName(o.getString("arrayType"));

    value = ArkArrayRegistry.read(o.getJsonArray("value"), arrayType, dataSize);
  }

  @SuppressWarnings("unchecked")
  @Override
  public Class<ArkArray<?>> getValueClass() {
    return (Class<ArkArray<?>>) (Class<?>) ArkArray.class;
  }

  @Override
  public ArkArray<?> getValue() {
    return value;
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
  public void setValue(ArkArray<?> value) {
    this.value = value;
  }

  @Override
  protected void serializeValue(JsonObjectBuilder job) {
    job.add("arrayType", arrayType.toString());
    job.add("value", value.toJson());
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
    nameTable.add(arrayType.getNameString());
    value.collectNames(nameTable);
  }

}
