package qowyn.ark;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;

import qowyn.ark.properties.Property;
import qowyn.ark.types.ArkName;

public class ArkCloudInventory extends FileFormatBase implements PropertyContainer, GameObjectContainerMixin {

  private int inventoryVersion;

  private final ArrayList<GameObject> objects = new ArrayList<>();

  private final Map<Integer, Map<List<ArkName>, GameObject>> objectMap = new HashMap<>();

  private GameObject inventoryData;

  public ArkCloudInventory() {}

  public ArkCloudInventory(Path filePath) throws IOException {
    readBinary(filePath);
  }

  public ArkCloudInventory(Path filePath, ReadingOptions options) throws IOException {
    readBinary(filePath, options);
  }

  public ArkCloudInventory(JsonNode node) {
    readJson(node);
  }

  public ArkCloudInventory(JsonNode node, ReadingOptions options) {
    readJson(node, options);
  }

  @Override
  public void readBinary(ArkArchive archive, ReadingOptions options) {
    inventoryVersion = archive.getInt();

    if (inventoryVersion < 1 || inventoryVersion > 4) {
      throw new UnsupportedOperationException("Unknown Cloud Inventory Version " + inventoryVersion);
    }

    int objectCount = archive.getInt();

    objects.clear();
    objectMap.clear();
    for (int i = 0; i < objectCount; i++) {
      addObject(new GameObject(archive), options.getBuildComponentTree());
    }

    for (int i = 0; i < objectCount; i++) {
      GameObject object = objects.get(i);
      if (object.getClassString().equals("ArkCloudInventoryData")) {
        inventoryData = object;
      }
      object.loadProperties(archive, i < objectCount - 1 ? objects.get(i + 1) : null, 0);
    }
  }

  @Override
  public void writeBinary(Path filePath, WritingOptions options) throws FileNotFoundException, IOException {
    int size = Integer.BYTES * 2;

    NameSizeCalculator nameSizer = ArkArchive.getNameSizer(false);

    size += objects.stream().mapToInt(object -> object.getSize(nameSizer)).sum();

    int propertiesBlockOffset = size;

    size += objects.stream().mapToInt(object -> object.getPropertiesSize(nameSizer)).sum();

    try (FileChannel fc = FileChannel.open(filePath, StandardOpenOption.CREATE, StandardOpenOption.READ, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING)) {
      ByteBuffer buffer;

      if (options.usesMemoryMapping()) {
        buffer = fc.map(FileChannel.MapMode.READ_WRITE, 0, size);
      } else {
        buffer = ByteBuffer.allocateDirect(size);
      }

      ArkArchive archive = new ArkArchive(buffer, filePath);

      archive.putInt(inventoryVersion);
      archive.putInt(objects.size());

      for (GameObject object : objects) {
        propertiesBlockOffset = object.writeBinary(archive, propertiesBlockOffset);
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

  @Override
  public void readJson(JsonNode node, ReadingOptions options) {
    inventoryVersion = node.path("inventoryVersion").asInt();

    objects.clear();
    objectMap.clear();
    if (node.hasNonNull("inventoryData")) {
      addObject(new GameObject(node.get("inventoryData")), options.getBuildComponentTree());
      inventoryData = objects.get(0);
    }

    if (node.hasNonNull("objects")) {
      for (JsonNode objectNode : node.get("objects")) {
        addObject(new GameObject(objectNode), options.getBuildComponentTree());
      }
    }
  }

  @Override
  public void writeJson(JsonGenerator generator, WritingOptions options) throws IOException {
    generator.writeStartObject();

    generator.writeNumberField("inventoryVersion", inventoryVersion);
    generator.writeFieldName("inventoryData");
    if (inventoryData != null) {
      inventoryData.writeJson(generator, true);
    } else {
      generator.writeNull();
    }

    if (objects.size() > (inventoryData == null ? 0 : 1)) {
      generator.writeArrayFieldStart("objects");

      for (GameObject object : objects) {
        if (object == inventoryData) {
          continue;
        }

        object.writeJson(generator, true);
      }

      generator.writeEndArray();
    }

    generator.writeEndObject();
  }

  public int getInventoryVersion() {
    return inventoryVersion;
  }

  public void setInventoryVersion(int inventoryVersion) {
    this.inventoryVersion = inventoryVersion;
  }

  @Override
  public ArrayList<GameObject> getObjects() {
    return objects;
  }

  @Override
  public Map<Integer, Map<List<ArkName>, GameObject>> getObjectMap() {
    return objectMap;
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
