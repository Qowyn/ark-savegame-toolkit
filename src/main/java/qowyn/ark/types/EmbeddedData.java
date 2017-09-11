package qowyn.ark.types;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;

import qowyn.ark.ArkArchive;

public class EmbeddedData {

  private String path;

  private byte[][][] data;

  public EmbeddedData() {}

  public EmbeddedData(ArkArchive archive) {
    readBinary(archive);
  }

  public EmbeddedData(JsonNode node) {
    readJson(node);
  }

  public byte[][][] getData() {
    return data;
  }

  public void setData(byte[][][] data) {
    this.data = data;
  }

  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }

  public void readJson(JsonNode node) {
    path = node.path("path").asText();

    JsonNode dataValue = node.get("data");

    try {
      if (dataValue != null) {
        data = new byte[dataValue.size()][][];
        for (int part = 0; part < dataValue.size(); part++) {
          JsonNode partArray = dataValue.get(part);
          data[part] = new byte[partArray.size()][];
          for (int blob = 0; blob < partArray.size(); blob++) {
            data[part][blob] = partArray.get(blob).binaryValue();
          }
        }
      }
    } catch (IOException ex) {
      throw new RuntimeException(ex);
    }
  }

  public void writeJson(JsonGenerator generator) throws IOException {
    generator.writeStartObject();

    generator.writeStringField("path", path);

    generator.writeArrayFieldStart("data");
    for (int part = 0; part < data.length; part++) {
      generator.writeStartArray();
      for (int blob = 0; blob < data[part].length; blob++) {
        generator.writeBinary(data[part][blob]);
      }
      generator.writeEndArray();
    }
    generator.writeEndArray();

    generator.writeEndObject();
  }

  public int getSize() {
    int size = ArkArchive.getStringLength(path) + 4;

    if (data != null) {
      size += data.length * 4;
      for (byte[][] partData : data) {
        if (partData != null) {
          size += partData.length * 4;
          for (byte[] blobData : partData) {
            size += blobData.length;
          }
        }
      }
    }

    return size;
  }

  public void readBinary(ArkArchive archive) {
    path = archive.getString();

    int partCount = archive.getInt();

    data = new byte[partCount][][];
    for (int part = 0; part < partCount; part++) {
      int blobCount = archive.getInt();
      byte[][] partData = new byte[blobCount][];

      for (int blob = 0; blob < blobCount; blob++) {
        int blobSize = archive.getInt() * 4; // Array of 32 bit values
        partData[blob] = archive.getBytes(blobSize);
      }

      data[part] = partData;
    }
  }

  public void writeBinary(ArkArchive archive) {
    archive.putString(path);

    if (data != null) {
      archive.putInt(data.length);
      for (byte[][] partData : data) {
        archive.putInt(partData.length);
        for (byte[] blobData : partData) {
          archive.putInt(blobData.length / 4);
          archive.putBytes(blobData);
        }
      }
    } else {
      archive.putInt(0);
    }
  }

  public static void skip(ArkArchive archive) {
    archive.skipString();

    int partCount = archive.getInt();
    for (int part = 0; part < partCount; part++) {
      int blobCount = archive.getInt();
      for (int blob = 0; blob < blobCount; blob++) {
        int blobSize = archive.getInt() * 4;
        archive.position(archive.position() + blobSize);
      }
    }
  }

}
