package qowyn.ark.types;

import javax.json.JsonNumber;
import javax.json.JsonString;
import javax.json.JsonValue;

import qowyn.ark.ArkArchive;
import qowyn.ark.NameCollector;
import qowyn.ark.NameContainer;
import qowyn.ark.NameSizeCalculator;
import qowyn.ark.json.SimpleJsonInteger;
import qowyn.ark.json.SimpleJsonString;

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
    read(archive, name);
  }

  public ArkByteValue(JsonValue v) {
    fromJson(v);
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

  public void fromJson(JsonValue v) {
    if (v instanceof JsonString) {
      this.nameValue = ArkName.from(((JsonString) v).getString());
    } else {
      this.byteValue = (byte) ((JsonNumber) v).intValue();
    }
  }

  public JsonValue toJson() {
    if (nameValue != null) {
      return new SimpleJsonString(nameValue.toString());
    } else {
      return new SimpleJsonInteger(byteValue);
    }
  }

  public int getSize(NameSizeCalculator nameSizer) {
    return nameValue != null ? nameSizer.sizeOf(nameValue) : 1;
  }

  public void read(ArkArchive archive, boolean name) {
    if (name) {
      this.nameValue = archive.getName();
    } else {
      this.byteValue = archive.getByte();
    }
  }

  public void write(ArkArchive archive) {
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
