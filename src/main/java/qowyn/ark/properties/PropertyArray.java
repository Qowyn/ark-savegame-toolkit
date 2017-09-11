package qowyn.ark.properties;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;

import qowyn.ark.ArkArchive;
import qowyn.ark.NameCollector;
import qowyn.ark.NameSizeCalculator;
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
      value = ArkArrayRegistry.readBinary(archive, arrayType, this);

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

  public PropertyArray(JsonNode node) {
    super(node);
    ArkName arrayType = ArkName.from(node.path("arrayType").asText());

    if (node.path("value").isBinary()) {
      value = new ArkArrayUnknown(node.path("value"), arrayType);
    } else {
      value = ArkArrayRegistry.readJson(node.path("value"), arrayType, this);
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
  protected void writeBinaryValue(ArkArchive archive) {
    archive.putName(value.getType());
    value.writeBinary(archive);
  }

  @Override
  protected void writeJsonValue(JsonGenerator generator) throws IOException {
    generator.writeStringField("arrayType", value.getType().toString());
    generator.writeFieldName("value");
    value.writeJson(generator);
  }

  @Override
  protected int calculateAdditionalSize(NameSizeCalculator nameSizer) {
    return nameSizer.sizeOf(value.getType());
  }

  @Override
  public int calculateDataSize(NameSizeCalculator nameSizer) {
    return value.calculateSize(nameSizer);
  }

  @Override
  public void collectNames(NameCollector collector) {
    super.collectNames(collector);
    collector.accept(value.getType());
    value.collectNames(collector);
  }

  @Override
  protected boolean isDataSizeNeeded() {
    return value instanceof ArkArrayStruct;
  }

}
