package qowyn.ark.properties;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

import qowyn.ark.ArkArchive;
import qowyn.ark.JsonHelper;
import qowyn.ark.NameCollector;
import qowyn.ark.NameSizeCalculator;
import qowyn.ark.types.ArkName;

public abstract class PropertyBase<T> implements Property<T> {

  protected ArkName name;

  protected int dataSize;

  protected int index;

  protected T value;

  public PropertyBase(ArkName name, int index, T value) {
    this.name = name;
    this.index = index;
    this.value = value;
  }

  public PropertyBase(ArkArchive archive, ArkName name) {
    this.name = name;
    dataSize = archive.getInt();
    index = archive.getInt();
  }

  public PropertyBase(JsonObject o) {
    name = ArkName.from(o.getString("name"));
    dataSize = o.getInt("size", 0);
    index = o.getInt("index", 0);
  }

  @Override
  public ArkName getName() {
    return name;
  }

  @Override
  public void setName(ArkName name) {
    this.name = name;
  }

  @Override
  public String getNameString() {
    return name.toString();
  }

  @Override
  public void setNameString(String nameString) {
    name = ArkName.from(nameString);
  }

  @Override
  public String getTypeString() {
    return getType().toString();
  }

  @Override
  public int getDataSize() {
    return dataSize;
  }

  @Override
  public void setDataSize(int dataSize) {
    this.dataSize = dataSize;
  }

  @Override
  public int getIndex() {
    return index;
  }

  @Override
  public void setIndex(int index) {
    this.index = index;
  }

  /**
   * Calculates additional space required to serialize fields of this property.
   * 
   * @param nameTable
   * @return
   */
  protected int calculateAdditionalSize(NameSizeCalculator nameSizer) {
    return 0;
  }

  /**
   * Side-effect: calling this function will change the value of the dataSize field.
   * This makes sure that the value can be used by the write function without having to calculate it twice
   * @param nameSizer
   * @return
   */
  @Override
  public int calculateSize(NameSizeCalculator nameSizer) {
    // dataSize index
    int size = Integer.BYTES * 2;
    dataSize = calculateDataSize(nameSizer);

    size += nameSizer.sizeOf(name);
    size += nameSizer.sizeOf(getType());
    size += calculateAdditionalSize(nameSizer);
    size += dataSize;

    return size;
  }

  protected abstract void serializeValue(JsonObjectBuilder job);

  /**
   * Determines if the dataSize cannot be calculated and thus needs to be recorded.
   * Used when writing the JSON representation of the property
   * 
   * @return <tt>true</tt> if dataSize needs to be recorded
   */
  protected boolean isDataSizeNeeded() {
    return false;
  }

  @Override
  public JsonObject toJson() {
    JsonObjectBuilder job = Json.createObjectBuilder();

    job.add("name", name.toString());
    job.add("type", getType().toString());
    if (isDataSizeNeeded()) {
      JsonHelper.addInt(job, "size", dataSize);
    }
    JsonHelper.addInt(job, "index", index);

    serializeValue(job);

    return job.build();
  }

  protected abstract void writeValue(ArkArchive archive);

  @Override
  public void write(ArkArchive archive) {
    archive.putName(name);
    archive.putName(getType());
    archive.putInt(dataSize);
    archive.putInt(index);

    writeValue(archive);
  }

  @Override
  public void collectNames(NameCollector collector) {
    collector.accept(name);
    collector.accept(getType());
  }

  @Override
  public T getValue() {
    return value;
  }

  @Override
  public void setValue(T value) {
    this.value = value;
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + " [value=" + value + ", name=" + name + ", dataSize=" + dataSize + ", index=" + index + "]";
  }

}
