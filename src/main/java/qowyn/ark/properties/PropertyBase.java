package qowyn.ark.properties;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;

import qowyn.ark.ArkArchive;
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

  public PropertyBase(JsonNode node) {
    name = ArkName.from(node.get("name").asText());
    dataSize = node.path("size").asInt();
    index = node.path("index").asInt();
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

  protected abstract void writeBinaryValue(ArkArchive archive);

  @Override
  public void writeBinary(ArkArchive archive) {
    archive.putName(name);
    archive.putName(getType());
    archive.putInt(dataSize);
    archive.putInt(index);

    writeBinaryValue(archive);
  }

  protected abstract void writeJsonValue(JsonGenerator generator) throws IOException;

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
  public void writeJson(JsonGenerator generator) throws IOException {
    generator.writeStartObject();

    generator.writeStringField("name", name.toString());
    generator.writeStringField("type", getType().toString());
    if (isDataSizeNeeded() && dataSize != 0) {
      generator.writeNumberField("size", dataSize);
    }
    if (index != 0) {
      generator.writeNumberField("index", index);
    }

    writeJsonValue(generator);

    generator.writeEndObject();
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
