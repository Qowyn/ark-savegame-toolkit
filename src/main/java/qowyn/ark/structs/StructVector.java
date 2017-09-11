package qowyn.ark.structs;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;

import qowyn.ark.ArkArchive;
import qowyn.ark.NameSizeCalculator;

public class StructVector extends StructBase {

  private float x;

  private float y;

  private float z;

  public StructVector() {}

  public StructVector(float x, float y, float z) {
    this.x = x;
    this.y = y;
    this.z = z;
  }

  public StructVector(ArkArchive archive) {
    x = archive.getFloat();
    y = archive.getFloat();
    z = archive.getFloat();
  }

  public StructVector(JsonNode node) {
    x = (float) node.path("x").asDouble();
    y = (float) node.path("y").asDouble();
    z = (float) node.path("z").asDouble();
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

    generator.writeEndObject();
  }

  @Override
  public void writeBinary(ArkArchive archive) {
    archive.putFloat(x);
    archive.putFloat(y);
    archive.putFloat(z);
  }

  @Override
  public int getSize(NameSizeCalculator nameSizer) {
    return Float.BYTES * 3;
  }

}
