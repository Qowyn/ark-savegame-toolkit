package qowyn.ark;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;

import qowyn.ark.arrays.ArkArrayInt8;
import qowyn.ark.arrays.ArkArrayUInt8;

public class ArkContainer implements GameObjectContainer {

  private final ArrayList<GameObject> objects = new ArrayList<>();

  public ArkContainer() {}

  public ArkContainer(String fileName) throws FileNotFoundException, IOException {
    this(fileName, new ReadingOptions());
  }

  public ArkContainer(String fileName, ReadingOptions options) throws FileNotFoundException, IOException {
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

  public ArkContainer(ArkArrayUInt8 source) {
    ByteBuffer buffer = ByteBuffer.allocateDirect(source.size());

    source.forEach(buffer::put);

    buffer.clear();

    ArkArchive archive = new ArkArchive(buffer);
    readBinary(archive);
  }

  public ArkContainer(ArkArrayInt8 source) {
    ByteBuffer buffer = ByteBuffer.allocateDirect(source.size());

    source.forEach(buffer::put);

    buffer.clear();

    ArkArchive archive = new ArkArchive(buffer);
    readBinary(archive);
  }

  public ArkContainer(JsonNode node) {
    readJson(node);
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
    objects.clear();

    if (node.isArray()) {
      int id = 0;
      for (JsonNode jsonObject : node) {
        objects.add(new GameObject(jsonObject));
        objects.get(id).setId(id++); // Set id and increase afterwards
      }
    } else {
      if (node.hasNonNull("objects")) {
        int id = 0;
        for (JsonNode jsonObject : node.get("objects")) {
          objects.add(new GameObject(jsonObject));
          objects.get(id).setId(id++); // Set id and increase afterwards
        }
      }
    }
  }

  public void writeJson(JsonGenerator generator) throws IOException {
    generator.writeStartArray();

    for (GameObject object : objects) {
      object.writeJson(generator, true);
    }

    generator.writeEndArray();
  }

  public ArrayList<GameObject> getObjects() {
    return objects;
  }

  private ByteBuffer toBuffer() {
    int size = Integer.BYTES;

    NameSizeCalculator nameSizer = ArkArchive.getNameSizer(false);

    size += objects.stream().mapToInt(object -> object.getSize(nameSizer)).sum();

    int propertiesBlockOffset = size;

    size += objects.stream().mapToInt(object -> object.getPropertiesSize(nameSizer)).sum();

    ByteBuffer buffer = ByteBuffer.allocateDirect(size);
    ArkArchive archive = new ArkArchive(buffer);

    archive.putInt(objects.size());

    for (GameObject object : objects) {
      propertiesBlockOffset = object.writeBinary(archive, propertiesBlockOffset);
    }

    for (GameObject object : objects) {
      object.writeProperties(archive, 0);
    }

    return buffer;
    
  }

  public ArkArrayUInt8 toByteArray() {
    ByteBuffer buffer = toBuffer();

    ArkArrayUInt8 result = new ArkArrayUInt8();

    buffer.clear();

    for (int n = 0; n < buffer.capacity(); n++) {
      result.add(buffer.get());
    }

    return result;
  }

  public ArkArrayInt8 toSignedByteArray() {
    ByteBuffer buffer = toBuffer();

    ArkArrayInt8 result = new ArkArrayInt8();

    buffer.clear();

    for (int n = 0; n < buffer.capacity(); n++) {
      result.add(buffer.get());
    }

    return result;
  }

}
