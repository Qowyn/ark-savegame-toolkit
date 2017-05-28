package qowyn.ark.properties;

import javax.json.JsonObject;

@FunctionalInterface
public interface PropertyJsonConstructor {

  public Property<?> apply(JsonObject o);

}
