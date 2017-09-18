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

import qowyn.ark.arrays.ArkArrayInt8;
import qowyn.ark.arrays.ArkArrayUInt8;
import qowyn.ark.types.ArkName;

public class ArkContainer extends FileFormatBase implements GameObjectContainerMixin {

  private final ArrayList<GameObject> objects = new ArrayList<>();

  private final Map<Integer, Map<List<ArkName>, GameObject>> objectMap = new HashMap<>();

  public ArkContainer() {}

  public ArkContainer(Path filePath) throws IOException {
    readBinary(filePath);
  }

  public ArkContainer(Path filePath, ReadingOptions options) throws IOException {
    readBinary(filePath, options);
  }

  public ArkContainer(JsonNode node) {
    readJson(node);
  }

  public ArkContainer(JsonNode node, ReadingOptions options) {
    readJson(node, options);
  }

  public ArkContainer(ArkArrayUInt8 source) {
    ByteBuffer buffer = ByteBuffer.allocateDirect(source.size());

    source.forEach(buffer::put);

    buffer.clear();

    ArkArchive archive = new ArkArchive(buffer);
    readBinary(archive, new ReadingOptions());
  }

  public ArkContainer(ArkArrayInt8 source) {
    ByteBuffer buffer = ByteBuffer.allocateDirect(source.size());

    source.forEach(buffer::put);

    buffer.clear();

    ArkArchive archive = new ArkArchive(buffer);
    readBinary(archive, new ReadingOptions());
  }

  @Override
  public void readBinary(ArkArchive archive, ReadingOptions options) {
    int objectCount = archive.getInt();

    objects.clear();
    objectMap.clear();
    for (int i = 0; i < objectCount; i++) {
      addObject(new GameObject(archive), options.getBuildComponentTree());
    }

    for (int i = 0; i < objectCount; i++) {
      objects.get(i).loadProperties(archive, i < objectCount - 1 ? objects.get(i + 1) : null, 0);
    }
  }

  @Override
  public void writeBinary(Path filePath, WritingOptions options) throws FileNotFoundException, IOException {
    int size = Integer.BYTES;

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

    objects.clear();
    objectMap.clear();
    if (node.isArray()) {
      for (JsonNode jsonObject : node) {
        addObject(new GameObject(jsonObject), options.getBuildComponentTree());
      }
    } else {
      if (node.hasNonNull("objects")) {
        for (JsonNode jsonObject : node.get("objects")) {
          addObject(new GameObject(jsonObject), options.getBuildComponentTree());
        }
      }
    }
  }

  @Override
  public void writeJson(JsonGenerator generator, WritingOptions options) throws IOException {
    generator.writeStartArray();

    for (GameObject object : objects) {
      object.writeJson(generator, true);
    }

    generator.writeEndArray();
  }

  @Override
  public ArrayList<GameObject> getObjects() {
    return objects;
  }

  @Override
  public Map<Integer, Map<List<ArkName>, GameObject>> getObjectMap() {
    return objectMap;
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
