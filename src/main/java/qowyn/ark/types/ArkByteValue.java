package qowyn.ark.types;

import java.util.Set;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

import qowyn.ark.ArkArchive;
import qowyn.ark.NameContainer;

public class ArkByteValue implements NameContainer {

  public static final String NONE = "None";

  public static final ArkName NONE_NAME = new ArkName(NONE);

  private boolean fromEnum;

  private byte byteValue;

  private ArkName enumName;

  private ArkName nameValue;

  public ArkByteValue() {}

  public ArkByteValue(byte byteValue) {
    this.fromEnum = false;
    this.enumName = NONE_NAME;
    this.byteValue = byteValue;
  }

  public ArkByteValue(ArkName enumName, ArkName nameValue) {
    this.fromEnum = true;
    this.enumName = enumName;
    this.nameValue = nameValue;
  }

  public ArkByteValue(ArkArchive archive, ArkName enumName) {
    read(archive, enumName);
  }

  public ArkByteValue(JsonObject o, ArkName enumName) {
    fromJson(o, enumName);
  }

  public boolean isFromEnum() {
    return fromEnum;
  }

  public byte getByteValue() {
    return byteValue;
  }

  public void setByteValue(byte byteValue) {
    this.fromEnum = false;
    this.enumName = NONE_NAME;
    this.byteValue = byteValue;
  }

  public ArkName getEnumName() {
    return enumName;
  }

  public ArkName getNameValue() {
    return nameValue;
  }

  public void setEnumValue(ArkName enumName, ArkName nameValue) {
    this.fromEnum = true;
    this.enumName = enumName;
    this.nameValue = nameValue;
  }

  public void fromJson(JsonObject o, ArkName enumName) {
    this.enumName = enumName;
    this.fromEnum = !enumName.equals(NONE_NAME);
    JsonObject value = o.getJsonObject("value");
    if (fromEnum) {
      this.nameValue = new ArkName(value.getString("value"));
    } else {
      this.byteValue = (byte) value.getInt("value");
    }
  }

  public JsonObject toJson() {
    JsonObjectBuilder job = Json.createObjectBuilder();

    if (fromEnum) {
      job.add("value", nameValue.toString());
    } else {
      job.add("value", byteValue);
    }

    return job.build();
  }

  public int getSize(boolean nameTable) {
    return fromEnum ? ArkArchive.getNameLength(nameValue, nameTable) : 1;
  }

  public void read(ArkArchive archive, ArkName enumName) {
    this.enumName = enumName;
    this.fromEnum = !enumName.equals(NONE_NAME);
    if (fromEnum) {
      this.nameValue = archive.getName();
    } else {
      this.byteValue = archive.getByte();
    }
  }

  public void write(ArkArchive archive) {
    if (fromEnum) {
      archive.putName(nameValue);
    } else {
      archive.putByte(byteValue);
    }
  }

  @Override
  public void collectNames(Set<String> nameTable) {
    nameTable.add(enumName.getNameString());
    if (fromEnum) {
      nameTable.add(nameValue.getNameString());
    }
  }

}
