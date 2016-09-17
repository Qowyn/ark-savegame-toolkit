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

import qowyn.ark.arrays.ArkArrayByte;

public class ArkContainer implements GameObjectContainer {

  private final List<GameObject> objects = new ArrayList<>();

  public ArkContainer() {}

  public ArkContainer(String fileName) throws FileNotFoundException, IOException {
    this(fileName, new ReadingOptions());
  }

  public ArkContainer(String fileName, ReadingOptions options) throws FileNotFoundException, IOException {
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

  public ArkContainer(ArkArrayByte source) {
    ByteBuffer buffer = ByteBuffer.allocateDirect(source.size());

    source.forEach(buffer::put);

    buffer.clear();

    ArkArchive archive = new ArkArchive(buffer);
    readBinary(archive);
  }

  public ArkContainer(JsonArray jsonObjects) {
    readJson(jsonObjects);
  }

  public void readBinary(ArkArchive archive) {
    int objectCount = archive.getInt();

    for (int i = 0; i < objectCount; i++) {
      objects.add(new GameObject(archive));
    }

    for (int i = 0; i < objectCount; i++) {
      objects.get(i).loadProperties(archive, i < objectCount - 1 ? objects.get(i + 1) : null, 0);
    }
  }

  public void writeBinary(String fileName) throws FileNotFoundException, IOException {
    writeBinary(fileName, WritingOptions.create());
  }

  public void writeBinary(String fileName, WritingOptions options) throws FileNotFoundException, IOException {
    int size = Integer.BYTES;

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

  public void readJson(JsonArray jsonObjects) {
    objects.clear();

    for (JsonObject jsonObject : jsonObjects.getValuesAs(JsonObject.class)) {
      objects.add(new GameObject(jsonObject));
    }
  }

  public JsonArray toJson() {
    JsonArrayBuilder jab = Json.createArrayBuilder();

    for (GameObject object : objects) {
      jab.add(object.toJson());
    }

    return jab.build();
  }

  public List<GameObject> getObjects() {
    return objects;
  }

  public ArkArrayByte toByteArray() {
    int size = Integer.BYTES;

    size += objects.stream().mapToInt(object -> object.getSize(false)).sum();

    int propertiesBlockOffset = size;

    size += objects.stream().mapToInt(object -> object.getPropertiesSize(false)).sum();

    ByteBuffer buffer = ByteBuffer.allocateDirect(size);
    ArkArchive archive = new ArkArchive(buffer);

    archive.putInt(objects.size());

    for (GameObject object : objects) {
      propertiesBlockOffset = object.write(archive, propertiesBlockOffset);
    }

    for (GameObject object : objects) {
      object.writeProperties(archive, 0);
    }

    ArkArrayByte result = new ArkArrayByte();

    buffer.clear();

    for (int n = 0; n < size; n++) {
      result.add(buffer.get());
    }

    return result;
  }

}
