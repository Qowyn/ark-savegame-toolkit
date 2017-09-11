package qowyn.ark.properties;

import com.fasterxml.jackson.databind.JsonNode;

@FunctionalInterface
public interface PropertyJsonConstructor {

  public Property<?> apply(JsonNode node);

}
