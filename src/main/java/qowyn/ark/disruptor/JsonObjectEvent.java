package qowyn.ark.disruptor;

import javax.json.JsonObject;

public class JsonObjectEvent {

  private JsonObject value;

  public void set(JsonObject value) {
    this.value = value;
  }

  public JsonObject get() {
    return value;
  }

}
