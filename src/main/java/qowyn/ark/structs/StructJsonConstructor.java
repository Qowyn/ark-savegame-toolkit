package qowyn.ark.structs;

import javax.json.JsonValue;

@FunctionalInterface
public interface StructJsonConstructor {

  public Struct apply(JsonValue value);

}
