package qowyn.ark.arrays;

import com.fasterxml.jackson.databind.JsonNode;

import qowyn.ark.properties.PropertyArray;

@FunctionalInterface
public interface ArkArrayJsonConstructor {

  public ArkArray<?> apply(JsonNode node, PropertyArray property);

}
