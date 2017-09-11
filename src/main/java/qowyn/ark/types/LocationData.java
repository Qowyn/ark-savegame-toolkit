package qowyn.ark.types;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;

import qowyn.ark.ArkArchive;

public class LocationData {

  private float x;

  private float y;

  private float z;

  private float pitch;

  private float yaw;

  private float roll;

  public LocationData() {}

  public LocationData(ArkArchive archive) {
    readBinary(archive);
  }

  public LocationData(JsonNode node) {
    readJson(node);
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

  @Override
  public String toString() {
    return "LocationData [x=" + x + ", y=" + y + ", z=" + z + ", pitch=" + pitch + ", yaw=" + yaw + ", roll=" + roll + "]";
  }

  public void readJson(JsonNode node) {
    x = node.path("x").floatValue();
    y = node.path("y").floatValue();
    z = node.path("z").floatValue();
    pitch = node.path("pitch").floatValue();
    yaw = node.path("yaw").floatValue();
    roll = node.path("roll").floatValue();
  }

  public void writeJson(JsonGenerator generator) throws IOException {
    generator.writeStartObject();
    if (x != 0.0f) {
      generator.writeNumberField("x", x);
    }
    if (y != 0.0f) {
      generator.writeNumberField("y", y);
    }
    if (z != 0.0f) {
      generator.writeNumberField("z", z);
    }
    if (pitch != 0.0f) {
      generator.writeNumberField("pitch", pitch);
    }
    if (yaw != 0.0f) {
      generator.writeNumberField("yaw", yaw);
    }
    if (roll != 0.0f) {
      generator.writeNumberField("roll", roll);
    }
    generator.writeEndObject();
  }

  public long getSize() {
    return Float.BYTES * 6;
  }

  public void readBinary(ArkArchive archive) {
    x = archive.getFloat();
    y = archive.getFloat();
    z = archive.getFloat();
    pitch = archive.getFloat();
    yaw = archive.getFloat();
    roll = archive.getFloat();
  }

  public void writeBinary(ArkArchive archive) {
    archive.putFloat(x);
    archive.putFloat(y);
    archive.putFloat(z);
    archive.putFloat(pitch);
    archive.putFloat(yaw);
    archive.putFloat(roll);
  }

  public static void skip(ArkArchive archive) {
    archive.skipBytes(Float.BYTES * 6);
  }

}
