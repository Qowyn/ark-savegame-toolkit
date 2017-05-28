package qowyn.ark.arrays;

import javax.json.JsonArray;

import qowyn.ark.properties.PropertyArray;

@FunctionalInterface
public interface ArkArrayJsonConstructor {

  public ArkArray<?> apply(JsonArray a, PropertyArray property);

}
