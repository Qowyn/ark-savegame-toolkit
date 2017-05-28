package qowyn.ark.structs;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;

import qowyn.ark.ArkArchive;
import qowyn.ark.JsonHelper;

/**
 * Essentially FColor
 * 
 * @author Roland Firmont
 *
 */
public class StructColor extends StructBase {

  private byte b;

  private byte g;

  private byte r;

  private byte a;

  public StructColor() {}

  public StructColor(byte b, byte g, byte r, byte a) {
    this.b = b;
    this.g = g;
    this.r = r;
    this.a = a;
  }

  public StructColor(ArkArchive archive) {
    b = archive.getByte();
    g = archive.getByte();
    r = archive.getByte();
    a = archive.getByte();
  }

  public StructColor(JsonValue v) {
    JsonObject o = (JsonObject) v;

    b = (byte) o.getInt("b", 0);
    g = (byte) o.getInt("g", 0);
    r = (byte) o.getInt("r", 0);
    a = (byte) o.getInt("a", 0);
  }

  public byte getB() {
    return b;
  }

  public void setB(byte b) {
    this.b = b;
  }

  public byte getG() {
    return g;
  }

  public void setG(byte g) {
    this.g = g;
  }

  public byte getR() {
    return r;
  }

  public void setR(byte r) {
    this.r = r;
  }

  public byte getA() {
    return a;
  }

  public void setA(byte a) {
    this.a = a;
  }

  @Override
  public JsonObject toJson() {
    JsonObjectBuilder vectorBuilder = Json.createObjectBuilder();

    JsonHelper.addInt(vectorBuilder, "b", b);
    JsonHelper.addInt(vectorBuilder, "g", g);
    JsonHelper.addInt(vectorBuilder, "r", r);
    JsonHelper.addInt(vectorBuilder, "a", a);

    return vectorBuilder.build();
  }

  @Override
  public void write(ArkArchive archive) {
    archive.putByte(b);
    archive.putByte(g);
    archive.putByte(r);
    archive.putByte(a);
  }

  @Override
  public int getSize(boolean nameTable) {
    return Byte.BYTES * 4;
  }

}
