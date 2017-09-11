package qowyn.ark.types;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;

import qowyn.ark.ArkArchive;
import qowyn.ark.NameCollector;
import qowyn.ark.NameContainer;
import qowyn.ark.NameSizeCalculator;

public final class ArkByteValue implements NameContainer {

  private byte byteValue;

  private ArkName nameValue;

  public ArkByteValue() {}

  public ArkByteValue(byte byteValue) {
    setByteValue(byteValue);
  }

  public ArkByteValue(ArkName nameValue) {
    this.nameValue = nameValue;
  }

  public ArkByteValue(ArkArchive archive, boolean name) {
    readBinary(archive, name);
  }

  public ArkByteValue(JsonNode node) {
    readJson(node);
  }

  public boolean isFromEnum() {
    return nameValue != null;
  }

  public byte getByteValue() {
    return byteValue;
  }

  public void setByteValue(byte byteValue) {
    this.nameValue = null;
    this.byteValue = byteValue;
  }

  public ArkName getNameValue() {
    return nameValue;
  }

  public void setNameValue(ArkName nameValue) {
    this.nameValue = nameValue;
  }

  public void readJson(JsonNode node) {
    if (node.isTextual()) {
      this.nameValue = ArkName.from(node.asText());
    } else {
      this.byteValue = (byte) node.asInt();
    }
  }

  public void writeJson(JsonGenerator generator) throws IOException {
    if (nameValue != null) {
      generator.writeString(nameValue.toString());
    } else {
      generator.writeNumber(byteValue);
    }
  }

  public int getSize(NameSizeCalculator nameSizer) {
    return nameValue != null ? nameSizer.sizeOf(nameValue) : 1;
  }

  public void readBinary(ArkArchive archive, boolean name) {
    if (name) {
      this.nameValue = archive.getName();
    } else {
      this.byteValue = archive.getByte();
    }
  }

  public void writeBinary(ArkArchive archive) {
    if (nameValue != null) {
      archive.putName(nameValue);
    } else {
      archive.putByte(byteValue);
    }
  }

  @Override
  public void collectNames(NameCollector collector) {
    if (nameValue != null) {
      collector.accept(nameValue);
    }
  }

  @Override
  public String toString() {
    return "ArkByteValue [byteValue=" + byteValue + ", nameValue=" + nameValue + "]";
  }

}
