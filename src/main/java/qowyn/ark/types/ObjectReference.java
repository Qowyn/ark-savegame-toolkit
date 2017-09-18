package qowyn.ark.types;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;

import qowyn.ark.ArkArchive;
import qowyn.ark.GameObject;
import qowyn.ark.GameObjectContainer;
import qowyn.ark.NameCollector;
import qowyn.ark.NameContainer;
import qowyn.ark.NameSizeCalculator;

public class ObjectReference implements NameContainer {

  public static final int TYPE_ID = 0;

  public static final int TYPE_PATH = 1;

  // Temporary, to support path references in save files
  public static final int TYPE_PATH_NO_TYPE = 2;

  private int length;

  private int objectType;

  private int objectId;

  private ArkName objectString;

  public ObjectReference() {}

  public ObjectReference(int length, int objectId) {
    this.length = length;
    this.objectId = objectId;
    this.objectType = TYPE_ID;
  }

  public ObjectReference(ArkName objectString) {
    this.objectString = objectString;
    this.objectType = TYPE_PATH;
  }

  public ObjectReference(ArkArchive archive, int length) {
    this.length = length;
    readBinary(archive);
  }

  public ObjectReference(JsonNode node, int length) {
    this.length = length;
    readJson(node);
  }

  public int getLength() {
    return length;
  }

  public void setLength(int length) {
    this.length = length;
  }

  public int getObjectId() {
    return objectId;
  }

  public void setObjectId(int objectId) {
    this.objectId = objectId;
  }

  public ArkName getObjectString() {
    return objectString;
  }

  public void setObjectString(ArkName objectString) {
    this.objectString = objectString;
  }

  public int getObjectType() {
    return objectType;
  }

  public void setObjectType(int objectType) {
    this.objectType = objectType;
  }

  @Override
  public String toString() {
    return "ObjectReference [objectType=" + objectType + ", objectId=" + objectId + ", objectString=" + objectString + ", length=" + length + "]";
  }

  public GameObject getObject(GameObjectContainer objectContainer) {

    if (objectType == TYPE_ID && objectId > -1 && objectId < objectContainer.getObjects().size()) {
      return objectContainer.getObjects().get(objectId);
    }

    return null;
  }

  public void readJson(JsonNode node) {
    if (node.isNumber()) {
      objectId = node.asInt();
      objectType = TYPE_ID;
    } else if (node.isTextual()) {
      objectString = ArkName.from(node.textValue());
      objectType = TYPE_PATH;
    } else {
      objectString = ArkName.from(node.path("value").asText());
      objectType = node.path("short").asBoolean(false) ? TYPE_PATH_NO_TYPE : TYPE_PATH;
    }
  }

  public void writeJson(JsonGenerator generator) throws IOException {
    if (objectType == TYPE_ID) {
      generator.writeNumber(objectId);
    } else if (objectType == TYPE_PATH) {
      generator.writeString(objectString.toString());
    } else if (objectType == TYPE_PATH_NO_TYPE) {
      generator.writeStartObject();
      generator.writeStringField("value", objectString.toString());
      generator.writeBooleanField("short", true);
      generator.writeEndObject();
    }
  }

  public int getSize(NameSizeCalculator nameSizer) {
    if (objectType == TYPE_ID) {
      return length;
    } else if (objectType == TYPE_PATH) {
      return Integer.BYTES + nameSizer.sizeOf(objectString);
    } else if (objectType == TYPE_PATH_NO_TYPE) {
      return nameSizer.sizeOf(objectString);
    } else {
      return length;
    }
  }

  public void readBinary(ArkArchive archive) {
    if (length >= 8) {
      objectType = archive.getInt();
      if (objectType == TYPE_ID) {
        objectId = archive.getInt();
      } else if (objectType == TYPE_PATH) {
        objectString = archive.getName();
      } else {
        archive.position(archive.position() - 4);
        objectType = TYPE_PATH_NO_TYPE;
        objectString = archive.getName();
      }
    } else if (length == 4) { // Only seems to happen in Version 5
      objectType = TYPE_ID;
      objectId = archive.getInt();
    } else {
      System.err.println("Warning: ObjectReference with length value " + length + " at " + Integer.toHexString(archive.position()));
      archive.position(archive.position() + length);
    }
  }

  public void writeBinary(ArkArchive archive) {
    if (objectType == TYPE_PATH || length >= 8 && objectType != TYPE_PATH_NO_TYPE) {
      archive.putInt(objectType);
    }

    if (objectType == TYPE_ID) {
      archive.putInt(objectId);
    } else if (objectType == TYPE_PATH || objectType == TYPE_PATH_NO_TYPE) {
      archive.putName(objectString);
    }
  }

  @Override
  public void collectNames(NameCollector collector) {
    if (objectType == TYPE_PATH) {
      collector.accept(objectString);
    }
  }

  public boolean isId() {
    return objectType == TYPE_ID;
  }

  public boolean isPath() {
    return objectType != TYPE_ID;
  }

}
