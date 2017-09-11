package qowyn.ark.properties;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;

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

  public PropertyObject(JsonNode node) {
    super(node);
    value = new ObjectReference(node.path("value"), dataSize);
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
  protected void writeJsonValue(JsonGenerator generator) throws IOException {
    generator.writeFieldName("value");
    value.writeJson(generator);
  }

  @Override
  protected void writeBinaryValue(ArkArchive archive) {
    value.writeBinary(archive);
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
