package qowyn.ark.structs;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;

import qowyn.ark.ArkArchive;
import qowyn.ark.NameSizeCalculator;

public class StructLinearColor extends StructBase {

  private float r;

  private float g;

  private float b;

  private float a;

  public StructLinearColor() {}

  public StructLinearColor(float r, float g, float b, float a) {
    this.r = r;
    this.g = g;
    this.b = b;
    this.a = a;
  }

  public StructLinearColor(ArkArchive archive) {
    r = archive.getFloat();
    g = archive.getFloat();
    b = archive.getFloat();
    a = archive.getFloat();
  }

  public StructLinearColor(JsonNode node) {
    r = (float) node.path("r").asDouble();
    g = (float) node.path("g").asDouble();
    b = (float) node.path("b").asDouble();
    a = (float) node.path("a").asDouble();
  }

  public float getR() {
    return r;
  }

  public void setR(float r) {
    this.r = r;
  }

  public float getG() {
    return g;
  }

  public void setG(float g) {
    this.g = g;
  }

  public float getA() {
    return a;
  }

  public void setA(float a) {
    this.a = a;
  }

  public float getB() {
    return b;
  }

  public void setB(float b) {
    this.b = b;
  }

  @Override
  public void writeJson(JsonGenerator generator) throws IOException {
    generator.writeStartObject();

    if (r != 0.0f) {
      generator.writeNumberField("r", r);
    }
    if (g != 0.0f) {
      generator.writeNumberField("g", g);
    }
    if (b != 0.0f) {
      generator.writeNumberField("b", b);
    }
    if (a != 0.0f) {
      generator.writeNumberField("a", a);
    }

    generator.writeEndObject();
  }

  @Override
  public void writeBinary(ArkArchive archive) {
    archive.putFloat(r);
    archive.putFloat(g);
    archive.putFloat(b);
    archive.putFloat(a);
  }

  @Override
  public int getSize(NameSizeCalculator nameSizer) {
    return Float.BYTES * 4;
  }

}
