package qowyn.ark;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

import qowyn.ark.properties.Property;

public class ArkTribe implements PropertyContainer, GameObjectContainer {

  private int tribeVersion;

  private final ArrayList<GameObject> objects = new ArrayList<>();

  private GameObject tribe;

  public ArkTribe() {}

  public ArkTribe(String fileName) throws FileNotFoundException, IOException {
    this(fileName, new ReadingOptions());
  }

  public ArkTribe(String fileName, ReadingOptions options) throws FileNotFoundException, IOException {
    try (FileChannel fc = FileChannel.open(Paths.get(fileName), StandardOpenOption.READ)) {
      if (fc.size() > Integer.MAX_VALUE) {
        throw new RuntimeException("Input file is too large.");
      }
      ByteBuffer buffer;
      if (options.usesMemoryMapping()) {
        buffer = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
      } else {
        buffer = ByteBuffer.allocateDirect((int) fc.size());
        int bytesRead = fc.read(buffer);
        int totalRead = bytesRead;
        while (bytesRead != -1 && totalRead < fc.size()) {
          bytesRead = fc.read(buffer);
          totalRead += bytesRead;
        }
        buffer.clear();
      }
      ArkArchive archive = new ArkArchive(buffer);
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

    for (int i = 0; i < tribesCount; i++) {
      objects.add(new GameObject(archive));
    }

    for (int i = 0; i < tribesCount; i++) {
      GameObject object = objects.get(i);
      if (object.getClassString().equals("PrimalTribeData")) {
        tribe = object;
      }
      object.loadProperties(archive, i < tribesCount - 1 ? objects.get(i + 1) : null, 0);
    }
  }

  public void writeBinary(String fileName) throws FileNotFoundException, IOException {
    writeBinary(fileName, WritingOptions.create());
  }

  public void writeBinary(String fileName, WritingOptions options) throws FileNotFoundException, IOException {
    int size = Integer.BYTES * 2;

    size += objects.stream().mapToInt(object -> object.getSize(false)).sum();

    int propertiesBlockOffset = size;

    size += objects.stream().mapToInt(object -> object.getPropertiesSize(false)).sum();

    try (FileChannel fc = FileChannel.open(Paths.get(fileName), StandardOpenOption.CREATE, StandardOpenOption.READ, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING)) {
      ByteBuffer buffer;

      if (options.usesMemoryMapping()) {
        buffer = fc.map(FileChannel.MapMode.READ_WRITE, 0, size);
      } else {
        buffer = ByteBuffer.allocateDirect(size);
      }

      ArkArchive archive = new ArkArchive(buffer);

      archive.putInt(tribeVersion);
      archive.putInt(objects.size());

      for (GameObject object : objects) {
        propertiesBlockOffset = object.write(archive, propertiesBlockOffset);
      }

      for (GameObject object : objects) {
        object.writeProperties(archive, 0);
      }

      if (!options.usesMemoryMapping()) {
        buffer.clear();
        int bytesWritten = fc.write(buffer);
        int totalBytes = bytesWritten;
        while (totalBytes < buffer.capacity()) {
          bytesWritten = fc.write(buffer);
          totalBytes += bytesWritten;
        }
      }
    }
  }

  public void readJson(JsonObject object) {
    tribeVersion = object.getInt("tribeVersion");
    objects.clear();

    JsonObject tribe = object.getJsonObject("tribe");
    if (tribe != null) {
      setTribe(new GameObject(tribe));
    }

    JsonArray tribeObjects = object.getJsonArray("objects");
    if (tribeObjects != null) {
      for (JsonObject tribeObject : tribeObjects.getValuesAs(JsonObject.class)) {
        objects.add(new GameObject(tribeObject));
      }
    }
  }

  public JsonObject toJson() {
    JsonObjectBuilder job = Json.createObjectBuilder();

    job.add("tribeVersion", tribeVersion);
    job.add("tribe", tribe.toJson());

    if (objects.size() > 1) {
      JsonArrayBuilder additionalObjects = Json.createArrayBuilder();
      for (GameObject object : objects) {
        if (object == tribe) {
          continue;
        }

        additionalObjects.add(object.toJson());
      }
      job.add("objects", additionalObjects.build());
    }

    return job.build();
  }

  public int getTribeVersion() {
    return tribeVersion;
  }

  public void setTribeVersion(int tribeVersion) {
    this.tribeVersion = tribeVersion;
  }

  public ArrayList<GameObject> getObjects() {
    return objects;
  }

  public GameObject getTribe() {
    return tribe;
  }

  public void setTribe(GameObject tribe) {
    this.tribe = tribe;
  }

  @Override
  public List<Property<?>> getProperties() {
    return tribe.getProperties();
  }

  @Override
  public void setProperties(List<Property<?>> properties) {
    tribe.setProperties(properties);
  }

}
