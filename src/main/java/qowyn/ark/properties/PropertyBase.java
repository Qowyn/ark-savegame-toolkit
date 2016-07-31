package qowyn.ark.properties;

import java.util.Set;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

import qowyn.ark.ArkArchive;
import qowyn.ark.JsonHelper;
import qowyn.ark.types.ArkName;

public abstract class PropertyBase<T> implements Property<T> {

  protected ArkName name;

  protected ArkName typeName;

  protected int dataSize;

  protected int index;

  public PropertyBase(ArkArchive archive, PropertyArgs args) {
    name = args.getName();
    typeName = args.getTypeName();
    dataSize = archive.getInt();
    index = archive.getInt();
  }

  public PropertyBase(JsonObject o) {
    name = new ArkName(o.getString("name"));
    typeName = new ArkName(o.getString("type"));
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
    name = new ArkName(nameString);
  }

  @Override
  public ArkName getTypeName() {
    return typeName;
  }

  @Override
  public void setTypeName(ArkName typeName) {
    this.typeName = typeName;
  }

  @Override
  public String getTypeString() {
    return typeName.toString();
  }

  @Override
  public void setTypeString(String typeString) {
    this.typeName = new ArkName(typeString);
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
  protected int calculateAdditionalSize(boolean nameTable) {
    return 0;
  }

  @Override
  public int calculateSize(boolean nameTable) {
    // dataSize index
    int size = Integer.BYTES * 2;

    size += ArkArchive.getNameLength(name, nameTable);
    size += ArkArchive.getNameLength(typeName, nameTable);
    size += calculateAdditionalSize(nameTable);
    size += calculateDataSize(nameTable);

    return size;
  }

  protected abstract void serializeValue(JsonObjectBuilder job);

  /**
   * Determines if the dataSize cannot be calculated and thus needs to be recorded.
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
    job.add("type", typeName.toString());
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
    archive.putName(typeName);
    archive.putInt(calculateDataSize(archive.hasNameTable()));
    archive.putInt(index);

    writeValue(archive);
  }

  public void collectNames(Set<String> nameTable) {
    nameTable.add(name.getNameString());
    nameTable.add(typeName.getNameString());
  }

}
