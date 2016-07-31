package qowyn.ark.structs;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;

import qowyn.ark.ArkArchive;
import qowyn.ark.JsonHelper;
import qowyn.ark.types.ArkName;

public class StructQuat extends StructBase {

  private float x;

  private float y;

  private float z;

  private float w;

  public StructQuat(ArkArchive archive, ArkName structType) {
    super(structType);

    x = archive.getFloat();
    y = archive.getFloat();
    z = archive.getFloat();
    w = archive.getFloat();
  }

  public StructQuat(JsonValue v, ArkName structType) {
    super(structType);

    JsonObject o = (JsonObject) v;

    x = JsonHelper.getFloat(o, "x");
    y = JsonHelper.getFloat(o, "y");
    z = JsonHelper.getFloat(o, "z");
    w = JsonHelper.getFloat(o, "w");
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

  public float getZ() {
    return z;
  }

  public void setZ(float z) {
    this.z = z;
  }

  public float getW() {
    return w;
  }

  public void setW(float w) {
    this.w = w;
  }

  @Override
  public JsonObject toJson() {
    JsonObjectBuilder vectorBuilder = Json.createObjectBuilder();

    JsonHelper.addFloat(vectorBuilder, "x", x);
    JsonHelper.addFloat(vectorBuilder, "y", y);
    JsonHelper.addFloat(vectorBuilder, "z", z);
    JsonHelper.addFloat(vectorBuilder, "w", w);

    return vectorBuilder.build();
  }

  @Override
  public void write(ArkArchive archive) {
    archive.putFloat(x);
    archive.putFloat(y);
    archive.putFloat(z);
    archive.putFloat(w);
  }

  @Override
  public int getSize(boolean nameTable) {
    return Float.BYTES * 4;
  }

}
