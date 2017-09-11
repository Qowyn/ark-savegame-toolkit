package qowyn.ark.structs;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;

import qowyn.ark.ArkArchive;
import qowyn.ark.NameCollector;
import qowyn.ark.NameSizeCalculator;
import qowyn.ark.PropertyContainer;
import qowyn.ark.properties.Property;
import qowyn.ark.properties.PropertyRegistry;
import qowyn.ark.types.ArkName;

public class StructPropertyList extends StructBase implements PropertyContainer {

  private List<Property<?>> properties;

  public StructPropertyList() {
    this.properties = new ArrayList<>();
  }

  public StructPropertyList(List<Property<?>> properties) {
    this.properties = Objects.requireNonNull(properties);
  }

  public StructPropertyList(ArkArchive archive) {
    properties = new ArrayList<>();
    Property<?> property = PropertyRegistry.readBinary(archive);

    while (property != null) {
      properties.add(property);
      property = PropertyRegistry.readBinary(archive);
    }
  }

  public StructPropertyList(JsonNode node) {
    properties = StreamSupport.stream(node.spliterator(), false).map(PropertyRegistry::readJson).collect(Collectors.toList());
  }

  @Override
  public List<Property<?>> getProperties() {
    return properties;
  }

  @Override
  public void setProperties(List<Property<?>> properties) {
    this.properties = Objects.requireNonNull(properties);
  }

  @Override
  public boolean isNative() {
    return false;
  }

  @Override
  public void writeJson(JsonGenerator generator) throws IOException {
    generator.writeStartArray(properties.size());

    for (Property<?> property: properties) {
      property.writeJson(generator);
    }

    generator.writeEndArray();
  }

  @Override
  public void writeBinary(ArkArchive archive) {
    properties.forEach(p -> p.writeBinary(archive));

    archive.putName(ArkName.NAME_NONE);
  }

  @Override
  public int getSize(NameSizeCalculator nameSizer) {
    int size = nameSizer.sizeOf(ArkName.NAME_NONE);

    size += properties.stream().mapToInt(p -> p.calculateSize(nameSizer)).sum();

    return size;
  }

  @Override
  public void collectNames(NameCollector collector) {
    properties.forEach(p -> p.collectNames(collector));
    collector.accept(ArkName.NAME_NONE);
  }

}
