package qowyn.ark.structs;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;

import qowyn.ark.ArkArchive;
import qowyn.ark.NameSizeCalculator;

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

  public StructColor(JsonNode node) {
    b = (byte) node.path("b").asInt();
    g = (byte) node.path("g").asInt();
    r = (byte) node.path("r").asInt();
    a = (byte) node.path("a").asInt();
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
  public void writeJson(JsonGenerator generator) throws IOException {
    generator.writeStartObject();

    if (b != 0) {
      generator.writeNumberField("b", b);
    }
    if (g != 0) {
      generator.writeNumberField("g", g);
    }
    if (r != 0) {
      generator.writeNumberField("r", r);
    }
    if (a != 0) {
      generator.writeNumberField("a", a);
    }

    generator.writeEndObject();
  }

  @Override
  public void writeBinary(ArkArchive archive) {
    archive.putByte(b);
    archive.putByte(g);
    archive.putByte(r);
    archive.putByte(a);
  }

  @Override
  public int getSize(NameSizeCalculator nameSizer) {
    return Byte.BYTES * 4;
  }

}
