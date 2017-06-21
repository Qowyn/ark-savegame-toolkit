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

public class ArkCloudInventory implements PropertyContainer, GameObjectContainer {

  private int inventoryVersion;

  private final ArrayList<GameObject> objects = new ArrayList<>();

  private GameObject inventoryData;

  public ArkCloudInventory() {}

  public ArkCloudInventory(String fileName) throws FileNotFoundException, IOException {
    this(fileName, new ReadingOptions());
  }

  public ArkCloudInventory(String fileName, ReadingOptions options) throws FileNotFoundException, IOException {
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

  public ArkCloudInventory(JsonObject object) {
    readJson(object);
  }

  public void readBinary(ArkArchive archive) {
    inventoryVersion = archive.getInt();

    if (inventoryVersion != 1 && inventoryVersion != 3) {
      throw new UnsupportedOperationException("Unknown Cloud Inventory Version " + inventoryVersion);
    }

    int objectCount = archive.getInt();

    for (int i = 0; i < objectCount; i++) {
      objects.add(new GameObject(archive));
    }

    for (int i = 0; i < objectCount; i++) {
      GameObject object = objects.get(i);
      if (object.getClassString().equals("ArkCloudInventoryData")) {
        inventoryData = object;
      }
      object.loadProperties(archive, i < objectCount - 1 ? objects.get(i + 1) : null, 0);
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

      archive.putInt(inventoryVersion);
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
    inventoryVersion = object.getInt("inventoryVersion");
    objects.clear();

    JsonObject inventoryData = object.getJsonObject("inventoryData");
    if (inventoryData != null) {
      setInventoryData(new GameObject(inventoryData));
    }

    JsonArray profileObjects = object.getJsonArray("objects");
    if (profileObjects != null) {
      for (JsonObject profileObject : profileObjects.getValuesAs(JsonObject.class)) {
        objects.add(new GameObject(profileObject));
      }
    }
  }

  public JsonObject toJson() {
    JsonObjectBuilder job = Json.createObjectBuilder();

    job.add("inventoryVersion", inventoryVersion);
    job.add("inventoryData", inventoryData.toJson());

    if (objects.size() > 1) {
      JsonArrayBuilder additionalObjects = Json.createArrayBuilder();
      for (GameObject object : objects) {
        if (object == inventoryData) {
          continue;
        }

        additionalObjects.add(object.toJson());
      }
      job.add("objects", additionalObjects.build());
    }

    return job.build();
  }

  public int getInventoryVersion() {
    return inventoryVersion;
  }

  public void setInventoryVersion(int inventoryVersion) {
    this.inventoryVersion = inventoryVersion;
  }

  public ArrayList<GameObject> getObjects() {
    return objects;
  }

  public GameObject getInventoryData() {
    return inventoryData;
  }

  public void setInventoryData(GameObject inventoryData) {
    if (this.inventoryData != null) {
      int oldIndex = objects.indexOf(this.inventoryData);
      if (oldIndex > -1) {
        objects.remove(oldIndex);
      }
    }
    this.inventoryData = inventoryData;
    if (inventoryData != null && objects.indexOf(inventoryData) == -1) {
      objects.add(0, inventoryData);
    }
  }

  @Override
  public List<Property<?>> getProperties() {
    return inventoryData.getProperties();
  }

  @Override
  public void setProperties(List<Property<?>> properties) {
    inventoryData.setProperties(properties);
  }

}
