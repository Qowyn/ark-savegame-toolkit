package qowyn.ark.structs;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;

import qowyn.ark.ArkArchive;
import qowyn.ark.JsonHelper;

public class StructVector2D extends StructBase {

  private float x;

  private float y;

  public StructVector2D() {}

  public StructVector2D(float x, float y) {
    this.x = x;
    this.y = y;
  }

  public StructVector2D(ArkArchive archive) {
    x = archive.getFloat();
    y = archive.getFloat();
  }

  public StructVector2D(JsonValue v) {
    JsonObject o = (JsonObject) v;

    x = JsonHelper.getFloat(o, "x");
    y = JsonHelper.getFloat(o, "y");
  }

  public float getX() {
    return x;
  }

  public void setX(float x) {
    this.x = x;
  }

  public float getY() {
    return y;
  }

  public void setY(float y) {
    this.y = y;
  }

  @Override
  public JsonObject toJson() {
    JsonObjectBuilder vectorBuilder = Json.createObjectBuilder();

    JsonHelper.addFloat(vectorBuilder, "x", x);
    JsonHelper.addFloat(vectorBuilder, "y", y);

    return vectorBuilder.build();
  }

  @Override
  public void write(ArkArchive archive) {
    archive.putFloat(x);
    archive.putFloat(y);
  }

  @Override
  public int getSize(boolean nameTable) {
    return Float.BYTES * 2;
  }

}
