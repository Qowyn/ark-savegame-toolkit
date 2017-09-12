package qowyn.ark;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;

import qowyn.ark.properties.Property;
import qowyn.ark.properties.PropertyRegistry;
import qowyn.ark.properties.UnreadablePropertyException;
import qowyn.ark.types.ArkName;

public class ArkSavFile extends FileFormatBase implements PropertyContainer {

  private String className;

  private List<Property<?>> properties = new ArrayList<>();

  public ArkSavFile() {}

  public ArkSavFile(Path filePath) throws IOException {
    readBinary(filePath);
  }

  public ArkSavFile(Path filePath, ReadingOptions options) throws IOException {
    readBinary(filePath, options);
  }

  public ArkSavFile(JsonNode node) {
    readJson(node);
  }

  public ArkSavFile(JsonNode node, ReadingOptions options) {
    readJson(node, options);
  }

  @Override
  public void readBinary(ArkArchive archive, ReadingOptions options) {
    className = archive.getString();

    properties.clear();
    try {
      Property<?> property = PropertyRegistry.readBinary(archive);

      while (property != null) {
        properties.add(property);
        property = PropertyRegistry.readBinary(archive);
      }
    } catch (UnreadablePropertyException upe) {
      upe.printStackTrace();
      return;
    }

    // TODO: verify 0 int at end
  }

  @Override
  public void writeBinary(Path filePath, WritingOptions options) throws FileNotFoundException, IOException {
    int size = Integer.BYTES + ArkArchive.getStringLength(className);

    NameSizeCalculator nameSizer = ArkArchive.getNameSizer(false);

    size += nameSizer.sizeOf(ArkName.NAME_NONE);

    size += properties.stream().mapToInt(p -> p.calculateSize(nameSizer)).sum();

    try (FileChannel fc = FileChannel.open(filePath, StandardOpenOption.CREATE, StandardOpenOption.READ, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING)) {
      ByteBuffer buffer;

      if (options.usesMemoryMapping()) {
        buffer = fc.map(FileChannel.MapMode.READ_WRITE, 0, size);
      } else {
        buffer = ByteBuffer.allocateDirect(size);
      }

      ArkArchive archive = new ArkArchive(buffer, filePath);

      archive.putString(className);

      if (properties != null) {
        properties.forEach(p -> p.writeBinary(archive));
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

  @Override
  public void readJson(JsonNode node, ReadingOptions options) {
    className = node.path("className").asText();

    properties.clear();
    if (node.hasNonNull("properties")) {
      for (JsonNode propertyNode: node.get("properties")) {
        properties.add(PropertyRegistry.readJson(propertyNode));
      }
    }
  }

  @Override
  public void writeJson(JsonGenerator generator, WritingOptions options) throws IOException {
    generator.writeStartObject();

    generator.writeStringField("className", className);

    if (!properties.isEmpty()) {
      generator.writeArrayFieldStart("properties");

      for (Property<?> property: properties) {
        property.writeJson(generator);
      }

      generator.writeEndArray();
    }

    generator.writeEndObject();
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
