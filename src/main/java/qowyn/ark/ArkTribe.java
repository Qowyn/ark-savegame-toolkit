package qowyn.ark;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

public class ArkTribe {

  private int tribeVersion;

  private GameObject tribe;

  public ArkTribe() {}

  public ArkTribe(String fileName) throws FileNotFoundException, IOException {
    try (RandomAccessFile raf = new RandomAccessFile(fileName, "r")) {
      FileChannel fc = raf.getChannel();
      ArkArchive archive = new ArkArchive(fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size()));

      readBinary(archive);
    }
  }

  public ArkTribe(JsonObject object) {
    readJson(object);
  }

  public void readBinary(ArkArchive archive) {
    tribeVersion = archive.getInt();

    if (tribeVersion != 1) {
      throw new UnsupportedOperationException("Unknown Tribe Version " + tribeVersion);
    }

    int tribesCount = archive.getInt();

    if (tribesCount != 1) {
      throw new UnsupportedOperationException("Unsupported count of tribes " + tribesCount);
    }

    tribe = new GameObject(archive);
    tribe.loadProperties(archive, null, 0);
  }

  public void writeBinary(String fileName) throws FileNotFoundException, IOException {
    writeBinary(fileName, WritingOptions.create());
  }

  public void writeBinary(String fileName, WritingOptions options) throws FileNotFoundException, IOException {
    int size = Integer.BYTES * 2;

    size += tribe.getSize(false);

    int propertiesBlockOffset = size;

    size += tribe.getPropertiesSize(false);

    try (RandomAccessFile raf = new RandomAccessFile(fileName, "rw")) {
      raf.setLength(size);
      ByteBuffer buffer;

      if (options.usesMemoryMapping()) {
        FileChannel fc = raf.getChannel();
        buffer = fc.map(FileChannel.MapMode.READ_WRITE, 0, size);
      } else {
        buffer = ByteBuffer.allocate(size);
      }

      ArkArchive archive = new ArkArchive(buffer);

      archive.putInt(tribeVersion);
      archive.putInt(1);
      tribe.write(archive, propertiesBlockOffset);
      tribe.writeProperties(archive, 0);

      if (!options.usesMemoryMapping()) {
        raf.write(buffer.array());
      }
    }
  }

  public void readJson(JsonObject object) {
    tribeVersion = object.getInt("tribeVersion");
    tribe = new GameObject(object.getJsonObject("tribe"));
  }

  public JsonObject toJson() {
    JsonObjectBuilder job = Json.createObjectBuilder();

    job.add("tribeVersion", tribeVersion);
    job.add("tribe", tribe.toJson());

    return job.build();
  }

  public int getTribeVersion() {
    return tribeVersion;
  }

  public void setTribeVersion(int tribeVersion) {
    this.tribeVersion = tribeVersion;
  }

  public GameObject getTribe() {
    return tribe;
  }

  public void setTribe(GameObject tribe) {
    this.tribe = tribe;
  }

}
