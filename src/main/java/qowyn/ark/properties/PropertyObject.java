package qowyn.ark.properties;

import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

import qowyn.ark.ArkArchive;
import qowyn.ark.NameCollector;
import qowyn.ark.NameSizeCalculator;
import qowyn.ark.types.ArkName;
import qowyn.ark.types.ObjectReference;

public class PropertyObject extends PropertyBase<ObjectReference> {

  public static final ArkName TYPE = ArkName.constantPlain("ObjectProperty");

  public PropertyObject(String name, ObjectReference value) {
    super(ArkName.from(name), 0, value);
  }

  public PropertyObject(String name, int index, ObjectReference value) {
    super(ArkName.from(name), index, value);
  }

  public PropertyObject(ArkArchive archive, ArkName name) {
    super(archive, name);
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
  public ArkName getType() {
    return TYPE;
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
  public int calculateDataSize(NameSizeCalculator nameSizer) {
    return value.getSize(nameSizer);
  }

  @Override
  protected boolean isDataSizeNeeded() {
    return true;
  }

  @Override
  public void collectNames(NameCollector collector) {
    super.collectNames(collector);
    value.collectNames(collector);
  }

}
