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

public class ArkTribe extends FileFormatBase implements PropertyContainer, GameObjectContainerMixin {

  private int tribeVersion;

  private final ArrayList<GameObject> objects = new ArrayList<>();

  private final Map<Integer, Map<List<ArkName>, GameObject>> objectMap = new HashMap<>();

  private GameObject tribe;

  public ArkTribe() {}

  public ArkTribe(Path filePath) throws IOException {
    readBinary(filePath);
  }

  public ArkTribe(Path filePath, ReadingOptions options) throws IOException {
    readBinary(filePath, options);
  }

  public ArkTribe(JsonNode node) {
    readJson(node);
  }

  public ArkTribe(JsonNode node, ReadingOptions options) {
    readJson(node, options);
  }

  @Override
  public void readBinary(ArkArchive archive, ReadingOptions options) {
    tribeVersion = archive.getInt();

    if (tribeVersion != 1) {
      throw new UnsupportedOperationException("Unknown Tribe Version " + tribeVersion);
    }

    int tribesCount = archive.getInt();

    objects.clear();
    objectMap.clear();
    for (int i = 0; i < tribesCount; i++) {
      addObject(new GameObject(archive), options.getBuildComponentTree());
    }

    for (int i = 0; i < tribesCount; i++) {
      GameObject object = objects.get(i);
      if (object.getClassString().equals("PrimalTribeData")) {
        tribe = object;
      }
      object.loadProperties(archive, i < tribesCount - 1 ? objects.get(i + 1) : null, 0);
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

      archive.putInt(tribeVersion);
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
    tribeVersion = node.path("tribeVersion").asInt();

    objects.clear();
    objectMap.clear();
    if (node.hasNonNull("tribe")) {
      addObject(new GameObject(node.get("tribe")), options.getBuildComponentTree());
      tribe = objects.get(0);
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

    generator.writeNumberField("tribeVersion", tribeVersion);
    generator.writeFieldName("tribe");
    if (tribe != null) {
      tribe.writeJson(generator, true);
    } else {
      generator.writeNull();
    }

    if (objects.size() > (tribe == null ? 0 : 1)) {
      generator.writeArrayFieldStart("objects");
      for (GameObject object : objects) {
        if (object == tribe) {
          continue;
        }

        object.writeJson(generator, true);
      }
      generator.writeEndArray();
    }

    generator.writeEndObject();
  }

  public int getTribeVersion() {
    return tribeVersion;
  }

  public void setTribeVersion(int tribeVersion) {
    this.tribeVersion = tribeVersion;
  }

  @Override
  public ArrayList<GameObject> getObjects() {
    return objects;
  }

  @Override
  public Map<Integer, Map<List<ArkName>, GameObject>> getObjectMap() {
    return objectMap;
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
