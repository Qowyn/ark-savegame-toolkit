package qowyn.ark;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

import qowyn.ark.properties.Property;
import qowyn.ark.properties.PropertyRegistry;
import qowyn.ark.properties.UnreadablePropertyException;
import qowyn.ark.types.ArkName;

public class ArkSavFile implements PropertyContainer {

  private String className;

  private List<Property<?>> properties = new ArrayList<>();

  public ArkSavFile() {}

  public ArkSavFile(String fileName) throws FileNotFoundException, IOException {
    this(fileName, new ReadingOptions());
  }

  public ArkSavFile(String fileName, ReadingOptions options) throws FileNotFoundException, IOException {
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

  public ArkSavFile(JsonObject object) {
    readJson(object);
  }

  public void readBinary(ArkArchive archive) {
    className = archive.getString();

    properties.clear();
    try {
      Property<?> property = PropertyRegistry.readProperty(archive);

      while (property != null) {
        properties.add(property);
        property = PropertyRegistry.readProperty(archive);
      }
    } catch (UnreadablePropertyException upe) {
      upe.printStackTrace();
      return;
    }

    // TODO: verify 0 int at end
  }

  public void writeBinary(String fileName) throws FileNotFoundException, IOException {
    writeBinary(fileName, WritingOptions.create());
  }

  public void writeBinary(String fileName, WritingOptions options) throws FileNotFoundException, IOException {
    int size = Integer.BYTES + ArkArchive.getStringLength(className);

    size += ArkArchive.getNameLength(ArkName.NAME_NONE, false);

    size += properties.stream().mapToInt(p -> p.calculateSize(false)).sum();

    try (FileChannel fc = FileChannel.open(Paths.get(fileName), StandardOpenOption.CREATE, StandardOpenOption.READ, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING)) {
      ByteBuffer buffer;

      if (options.usesMemoryMapping()) {
        buffer = fc.map(FileChannel.MapMode.READ_WRITE, 0, size);
      } else {
        buffer = ByteBuffer.allocateDirect(size);
      }

      ArkArchive archive = new ArkArchive(buffer);

      archive.putString(className);

      if (properties != null) {
        properties.forEach(p -> p.write(archive));
      }

      archive.putName(ArkName.NAME_NONE);
      archive.putInt(0);

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
    className = object.getString("className");

    JsonArray propertiesArray = object.getJsonArray("properties");
    if (propertiesArray != null) {
      properties = propertiesArray.getValuesAs(JsonObject.class).parallelStream().map(PropertyRegistry::fromJSON).collect(Collectors.toList());
    } else {
      properties = new ArrayList<>();
    }
  }

  public JsonObject toJson() {
    JsonObjectBuilder job = Json.createObjectBuilder();

    job.add("className", className);

    if (properties != null && !properties.isEmpty()) {
      JsonArrayBuilder propsBuilder = Json.createArrayBuilder();
      properties.stream().map(Property::toJson).forEach(propsBuilder::add);

      job.add("properties", propsBuilder);
    }

    return job.build();
  }

  @Override
  public List<Property<?>> getProperties() {
    return properties;
  }

  @Override
  public void setProperties(List<Property<?>> properties) {
    this.properties = properties;
  }

}
