package qowyn.ark.structs;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonValue;

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
    Property<?> property = PropertyRegistry.readProperty(archive);

    while (property != null) {
      properties.add(property);
      property = PropertyRegistry.readProperty(archive);
    }
  }

  public StructPropertyList(JsonValue v) {
    JsonArray a = (JsonArray) v;

    List<JsonObject> props = a.getValuesAs(JsonObject.class);
    properties = props.stream().map(PropertyRegistry::fromJSON).collect(Collectors.toList());
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
  public JsonArray toJson() {
    JsonArrayBuilder propsBuilder = Json.createArrayBuilder();
    properties.stream().map(Property::toJson).forEach(propsBuilder::add);

    return propsBuilder.build();
  }

  @Override
  public void write(ArkArchive archive) {
    properties.forEach(p -> p.write(archive));

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
