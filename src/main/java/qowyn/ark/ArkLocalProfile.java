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

public class ArkLocalProfile extends FileFormatBase implements PropertyContainer, GameObjectContainerMixin {

  private static final int UNKNOWN_DATA_2_SIZE = 0xc;

  private int localProfileVersion;

  private byte[] unknownData;

  private byte[] unknownData2;

  private final ArrayList<GameObject> objects = new ArrayList<>();

  private final Map<Integer, Map<List<ArkName>, GameObject>> objectMap = new HashMap<>();

  private GameObject localProfile;

  public ArkLocalProfile() {}

  public ArkLocalProfile(Path filePath) throws IOException {
    readBinary(filePath);
  }

  public ArkLocalProfile(Path filePath, ReadingOptions options) throws IOException {
    readBinary(filePath, options);
  }

  public ArkLocalProfile(JsonNode node) {
    readJson(node);
  }

  public ArkLocalProfile(JsonNode node, ReadingOptions options) {
    readJson(node, options);
  }

  @Override
  public void readBinary(ArkArchive archive, ReadingOptions options) {
    localProfileVersion = archive.getInt();

    if (localProfileVersion != 1 && localProfileVersion != 3 && localProfileVersion != 4) {
      throw new UnsupportedOperationException("Unknown Profile Version " + localProfileVersion);
    }

    if (localProfileVersion < 4) {
      int unknownDataSize = archive.getInt();

      unknownData = archive.getBytes(unknownDataSize);

      if (localProfileVersion == 3) {
        unknownData2 = archive.getBytes(UNKNOWN_DATA_2_SIZE);
      }
    }

    int objectCount = archive.getInt();

    objects.clear();
    objectMap.clear();
    for (int i = 0; i < objectCount; i++) {
      addObject(new GameObject(archive), options.getBuildComponentTree());
    }

    for (int i = 0; i < objectCount; i++) {
      GameObject object = objects.get(i);
      if (object.getClassString().equals("PrimalLocalProfile")) {
        localProfile = object;
      }
      object.loadProperties(archive, i < objectCount - 1 ? objects.get(i + 1) : null, 0);
    }
  }

  @Override
  public void writeBinary(Path filePath, WritingOptions options) throws FileNotFoundException, IOException {
    int size;

    if (localProfileVersion > 3) {
      size = Integer.BYTES * 2;
    } else {
      size = Integer.BYTES * 3;

      if (localProfileVersion == 3) {
        if (unknownData2 == null) {
          unknownData2 = new byte[UNKNOWN_DATA_2_SIZE];
        } else if (unknownData2.length < UNKNOWN_DATA_2_SIZE) {
          byte[] temp = new byte[UNKNOWN_DATA_2_SIZE];
          System.arraycopy(unknownData2, 0, temp, 0, unknownData2.length);
          unknownData2 = temp;
        }
      }

      size += unknownData.length;

      if (localProfileVersion == 3) {
        size += UNKNOWN_DATA_2_SIZE;
      }
    }

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

      archive.putInt(localProfileVersion);

      if (localProfileVersion < 4) {
        archive.putInt(unknownData.length);
        archive.putBytes(unknownData);

        if (localProfileVersion == 3) {
          archive.putBytes(unknownData2, 0, UNKNOWN_DATA_2_SIZE);
        }
      }

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
    localProfileVersion = node.path("localProfileVersion").asInt();

    objects.clear();
    objectMap.clear();
    if (node.hasNonNull("localProfile")) {
      addObject(new GameObject(node.get("localProfile")), options.getBuildComponentTree());
      localProfile = objects.get(0);
    }

    if (node.hasNonNull("objects")) {
      for (JsonNode objectNode : node.get("objects")) {
        addObject(new GameObject(objectNode), options.getBuildComponentTree());
      }
    }

    JsonNode unknownDataString = node.path("unknownData");
    if (unknownDataString.isBinary()) {
      try {
        unknownData = unknownDataString.binaryValue();
      } catch (IOException ex) {
        throw new RuntimeException(ex);
      }
    }

    JsonNode unknownData2String = node.path("unknownData2");
    if (unknownData2String.isBinary()) {
      try {
        unknownData2 = unknownData2String.binaryValue();
      } catch (IOException ex) {
        throw new RuntimeException(ex);
      }
    }
  }

  @Override
  public void writeJson(JsonGenerator generator, WritingOptions options) throws IOException {
    generator.writeStartObject();

    generator.writeNumberField("localProfileVersion", localProfileVersion);
    generator.writeFieldName("localProfile");
    if (localProfile != null) {
      localProfile.writeJson(generator, true);
    } else {
      generator.writeNull();
    }

    if (objects.size() > (localProfile == null ? 0 : 1)) {
      generator.writeArrayFieldStart("objects");
      for (GameObject object : objects) {
        if (object == localProfile) {
          continue;
        }

        object.writeJson(generator, true);
      }
      generator.writeEndArray();
    }

    if (unknownData != null) {
      generator.writeBinaryField("unknownData", unknownData);
    }
    if (unknownData2 != null) {
      generator.writeBinaryField("unknownData2", unknownData2);
    }

    generator.writeEndObject();
  }

  public int getLocalProfileVersion() {
    return localProfileVersion;
  }

  public void setLocalProfileVersion(int localProfileVersion) {
    this.localProfileVersion = localProfileVersion;
  }

  @Override
  public ArrayList<GameObject> getObjects() {
    return objects;
  }

  @Override
  public Map<Integer, Map<List<ArkName>, GameObject>> getObjectMap() {
    return objectMap;
  }

  public GameObject getLocalProfile() {
    return localProfile;
  }

  public void setLocalProfile(GameObject localProfile) {
    if (this.localProfile != null) {
      int oldIndex = objects.indexOf(this.localProfile);
      if (oldIndex > -1) {
        objects.remove(oldIndex);
      }
    }
    this.localProfile = localProfile;
    if (localProfile != null && objects.indexOf(localProfile) == -1) {
      objects.add(0, localProfile);
    }
  }

  @Override
  public List<Property<?>> getProperties() {
    return localProfile.getProperties();
  }

  @Override
  public void setProperties(List<Property<?>> properties) {
    localProfile.setProperties(properties);
  }

}
