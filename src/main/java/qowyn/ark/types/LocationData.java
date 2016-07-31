package qowyn.ark.types;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

import qowyn.ark.ArkArchive;
import qowyn.ark.JsonHelper;

public class LocationData {

  private float x;

  private float y;

  private float z;

  private float pitch;

  private float yaw;

  private float roll;

  public LocationData() {}

  public LocationData(ArkArchive archive) {
    read(archive);
  }

  public LocationData(JsonObject o) {
    fromJson(o);
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

  public float getPitch() {
    return pitch;
  }

  public void setPitch(float pitch) {
    this.pitch = pitch;
  }

  public float getYaw() {
    return yaw;
  }

  public void setYaw(float yaw) {
    this.yaw = yaw;
  }

  public float getRoll() {
    return roll;
  }

  public void setRoll(float roll) {
    this.roll = roll;
  }

  public float getLat() {
    return getLat(8000);
  }

  public float getLat(int height) {
    return 50 + y / height;
  }

  public float getLon() {
    return getLon(8000);
  }

  public float getLon(int width) {
    return 50 + x / width;
  }

  @Override
  public String toString() {
    return "LocationData [x=" + x + ", y=" + y + ", z=" + z + ", pitch=" + pitch + ", yaw=" + yaw + ", roll=" + roll + "]";
  }

  public void fromJson(JsonObject o) {
    x = JsonHelper.getFloat(o, "x");
    y = JsonHelper.getFloat(o, "y");
    z = JsonHelper.getFloat(o, "z");
    pitch = JsonHelper.getFloat(o, "pitch");
    yaw = JsonHelper.getFloat(o, "yaw");
    roll = JsonHelper.getFloat(o, "roll");
  }

  public JsonObject toJson() {
    JsonObjectBuilder builder = Json.createObjectBuilder();

    JsonHelper.addFloat(builder, "x", x);
    JsonHelper.addFloat(builder, "y", y);
    JsonHelper.addFloat(builder, "z", z);
    JsonHelper.addFloat(builder, "pitch", pitch);
    JsonHelper.addFloat(builder, "yaw", yaw);
    JsonHelper.addFloat(builder, "roll", roll);

    return builder.build();
  }

  public long getSize() {
    return Float.BYTES * 6;
  }

  public void read(ArkArchive archive) {
    x = archive.getFloat();
    y = archive.getFloat();
    z = archive.getFloat();
    pitch = archive.getFloat();
    yaw = archive.getFloat();
    roll = archive.getFloat();
  }

  public void write(ArkArchive archive) {
    archive.putFloat(x);
    archive.putFloat(y);
    archive.putFloat(z);
    archive.putFloat(pitch);
    archive.putFloat(yaw);
    archive.putFloat(roll);
  }

  public static void skip(ArkArchive archive) {
    archive.position(archive.position() + Float.BYTES * 6);
  }

}
