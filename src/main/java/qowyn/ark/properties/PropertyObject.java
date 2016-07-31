package qowyn.ark.properties;

import java.util.Set;

import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

import qowyn.ark.ArkArchive;
import qowyn.ark.types.ObjectReference;

public class PropertyObject extends PropertyBase<ObjectReference> {

  private ObjectReference value;

  public PropertyObject(ArkArchive archive, PropertyArgs args) {
    super(archive, args);
    value = new ObjectReference(archive, dataSize);
  }

  public PropertyObject(JsonObject o) {
    super(o);
    value = new ObjectReference(o.getJsonObject("value"), dataSize);
  }

  @Override
  public Class<ObjectReference> getValueClass() {
    return ObjectReference.class;
  }

  @Override
  public ObjectReference getValue() {
    return value;
  }

  @Override
  public void setValue(ObjectReference value) {
    this.value = value;
  }

  @Override
  protected void serializeValue(JsonObjectBuilder job) {
    job.add("value", value.toJSON());
  }

  @Override
  protected void writeValue(ArkArchive archive) {
    value.write(archive);
  }

  @Override
  public int calculateDataSize(boolean nameTable) {
    return value.getSize(nameTable);
  }

  @Override
  protected boolean isDataSizeNeeded() {
    return true;
  }

  @Override
  public void collectNames(Set<String> nameTable) {
    super.collectNames(nameTable);
    value.collectNames(nameTable);
  }

  @Override
  public String toString() {
    return "PropertyObject [value=" + value + ", name=" + name + ", typeName=" + typeName + ", dataSize=" + dataSize + ", index=" + index + "]";
  }

}
