package qowyn.ark.structs;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonValue;

import qowyn.ark.ArkArchive;
import qowyn.ark.PropertyContainer;
import qowyn.ark.properties.Property;
import qowyn.ark.properties.PropertyReader;
import qowyn.ark.types.ArkName;

public class StructPropertyList extends StructBase implements PropertyContainer {

  private List<Property<?>> properties;

  public StructPropertyList(ArkArchive archive, ArkName structType) {
    super(structType);
    properties = new ArrayList<>();
    Property<?> property = PropertyReader.readProperty(archive);

    while (property != null) {
      properties.add(property);
      property = PropertyReader.readProperty(archive);
    }
  }

  public StructPropertyList(JsonValue v, ArkName structType) {
    super(structType);

    JsonArray a = (JsonArray) v;

    List<JsonObject> props = a.getValuesAs(JsonObject.class);
    properties = props.stream().map(PropertyReader::fromJSON).collect(Collectors.toList());
  }

  @Override
  public List<Property<?>> getProperties() {
    return properties;
  }

  @Override
  public void setProperties(List<Property<?>> properties) {
    this.properties = properties;
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

    archive.putName(Property.NONE_NAME);
  }

  @Override
  public int getSize(boolean nameTable) {
    int size = ArkArchive.getNameLength(Property.NONE_NAME, nameTable);

    size += properties.stream().mapToInt(p -> p.calculateSize(nameTable)).sum();

    return size;
  }

  @Override
  public void collectNames(Set<String> nameTable) {
    super.collectNames(nameTable);

    properties.forEach(p -> p.collectNames(nameTable));
  }

}
