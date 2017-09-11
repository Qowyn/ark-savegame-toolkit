package qowyn.ark.structs;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;

import qowyn.ark.ArkArchive;
import qowyn.ark.NameSizeCalculator;

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

  public StructVector2D(JsonNode node) {
    x = (float) node.path("x").asDouble();
    y = (float) node.path("y").asDouble();
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
  public void writeJson(JsonGenerator generator) throws IOException {
    generator.writeStartObject();

    if (x != 0.0f) {
      generator.writeNumberField("x", x);
    }
    if (y != 0.0f) {
      generator.writeNumberField("y", y);
    }

    generator.writeEndObject();
  }

  @Override
  public void writeBinary(ArkArchive archive) {
    archive.putFloat(x);
    archive.putFloat(y);
  }

  @Override
  public int getSize(NameSizeCalculator nameSizer) {
    return Float.BYTES * 2;
  }

}
