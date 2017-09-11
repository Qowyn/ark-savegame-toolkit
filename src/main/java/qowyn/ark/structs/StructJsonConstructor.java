package qowyn.ark.structs;

import com.fasterxml.jackson.databind.JsonNode;

@FunctionalInterface
public interface StructJsonConstructor {

  public Struct apply(JsonNode value);

}
