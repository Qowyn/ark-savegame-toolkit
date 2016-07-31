package qowyn.ark.types;

import java.util.Base64;
import java.util.List;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

import qowyn.ark.ArkArchive;

public class EmbeddedData {

  private static final Base64.Encoder ENCODER = Base64.getEncoder();

  private static final Base64.Decoder DECODER = Base64.getDecoder();

  private String path;

  private byte[][][] data;

  public EmbeddedData() {}

  public EmbeddedData(ArkArchive archive) {
    read(archive);
  }

  public EmbeddedData(JsonObject o) {
    fromJson(o);
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

  public void fromJson(JsonObject o) {
    path = o.getString("path", "");

    JsonArray dataValue = o.getJsonArray("data");

    if (dataValue != null) {
      List<JsonArray> dataArray = dataValue.getValuesAs(JsonArray.class);
      data = new byte[dataArray.size()][][];
      for (int part = 0; part < dataArray.size(); part++) {
        JsonArray partArray = dataArray.get(part);
        data[part] = new byte[partArray.size()][];
        for (int blob = 0; blob < partArray.size(); blob++) {
          data[part][blob] = DECODER.decode(partArray.getString(blob));
        }
      }
    }
  }

  public JsonObject toJson() {
    JsonObjectBuilder job = Json.createObjectBuilder();

    job.add("path", path);

    JsonArrayBuilder dataBuilder = Json.createArrayBuilder();
    for (int part = 0; part < data.length; part++) {
      JsonArrayBuilder blobBuilder = Json.createArrayBuilder();
      for (int blob = 0; blob < data[part].length; blob++) {
        blobBuilder.add(ENCODER.encodeToString(data[part][blob]));
      }
      dataBuilder.add(blobBuilder);
    }

    job.add("data", dataBuilder);

    return job.build();
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

  public void read(ArkArchive archive) {
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

  public void write(ArkArchive archive) {
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
