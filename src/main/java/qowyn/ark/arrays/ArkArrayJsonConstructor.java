package qowyn.ark.arrays;

import javax.json.JsonValue;

import qowyn.ark.types.ArkName;

@FunctionalInterface
public interface ArkArrayJsonConstructor {

  public ArkArray<?> apply(JsonValue v, int dataSize, ArkName propertyName);

}
