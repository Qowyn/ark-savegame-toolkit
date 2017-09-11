package qowyn.ark;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;

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
    Path filePath = Paths.get(fileName);
    try (FileChannel fc = FileChannel.open(filePath, StandardOpenOption.READ)) {
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
      ArkArchive archive = new ArkArchive(buffer, filePath);
      readBinary(archive);
    }
  }

  public ArkTribe(JsonNode node) {
    readJson(node);
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

    NameSizeCalculator nameSizer = ArkArchive.getNameSizer(false);

    size += objects.stream().mapToInt(object -> object.getSize(nameSizer)).sum();

    int propertiesBlockOffset = size;

    size += objects.stream().mapToInt(object -> object.getPropertiesSize(nameSizer)).sum();

    Path filePath = Paths.get(fileName);
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

  public void readJson(JsonNode node) {
    tribeVersion = node.path("tribeVersion").asInt();
    objects.clear();

    if (node.hasNonNull("tribe")) {
      setTribe(new GameObject(node.get("tribe")));
    }

    if (node.hasNonNull("objects")) {
      for (JsonNode tribeObject : node.get("objects")) {
        objects.add(new GameObject(tribeObject));
      }
    }
  }

  public void writeJson(JsonGenerator generator) throws IOException {
    generator.writeStartObject();

    generator.writeNumberField("tribeVersion", tribeVersion);
    generator.writeFieldName("tribe");
    if (tribe != null) {
      tribe.writeJson(generator, true);
    } else {
      generator.writeNull();
    }

    if (objects.size() > 1) {
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
