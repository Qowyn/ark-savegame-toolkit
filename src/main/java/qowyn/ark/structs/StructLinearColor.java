package qowyn.ark.structs;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;

import qowyn.ark.ArkArchive;
import qowyn.ark.JsonHelper;

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

  public StructLinearColor(JsonValue v) {
    JsonObject o = (JsonObject) v;

    r = JsonHelper.getFloat(o, "r");
    g = JsonHelper.getFloat(o, "g");
    b = JsonHelper.getFloat(o, "b");
    a = JsonHelper.getFloat(o, "a");
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
  public JsonObject toJson() {
    JsonObjectBuilder vectorBuilder = Json.createObjectBuilder();

    JsonHelper.addFloat(vectorBuilder, "r", r);
    JsonHelper.addFloat(vectorBuilder, "g", g);
    JsonHelper.addFloat(vectorBuilder, "b", b);
    JsonHelper.addFloat(vectorBuilder, "a", a);

    return vectorBuilder.build();
  }

  @Override
  public void write(ArkArchive archive) {
    archive.putFloat(r);
    archive.putFloat(g);
    archive.putFloat(b);
    archive.putFloat(a);
  }

  @Override
  public int getSize(boolean nameTable) {
    return Float.BYTES * 4;
  }

}
