package qowyn.ark.structs;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;

import qowyn.ark.ArkArchive;
import qowyn.ark.JsonHelper;
import qowyn.ark.types.ArkName;

public class StructVector extends StructBase {

  private float x;

  private float y;

  private float z;

  public StructVector(ArkName structType) {
    super(structType);
  }

  public StructVector(ArkName structType, float x, float y, float z) {
    super(structType);
    this.x = x;
    this.y = y;
    this.z = z;
  }

  public StructVector(ArkArchive archive, ArkName structType) {
    super(structType);

    x = archive.getFloat();
    y = archive.getFloat();
    z = archive.getFloat();
  }

  public StructVector(JsonValue v, ArkName structType) {
    super(structType);

    JsonObject o = (JsonObject) v;

    x = JsonHelper.getFloat(o, "x");
    y = JsonHelper.getFloat(o, "y");
    z = JsonHelper.getFloat(o, "z");
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

  @Override
  public JsonObject toJson() {
    JsonObjectBuilder vectorBuilder = Json.createObjectBuilder();

    JsonHelper.addFloat(vectorBuilder, "x", x);
    JsonHelper.addFloat(vectorBuilder, "y", y);
    JsonHelper.addFloat(vectorBuilder, "z", z);

    return vectorBuilder.build();
  }

  @Override
  public void write(ArkArchive archive) {
    archive.putFloat(x);
    archive.putFloat(y);
    archive.putFloat(z);
  }

  @Override
  public int getSize(boolean nameTable) {
    return Float.BYTES * 3;
  }

}
