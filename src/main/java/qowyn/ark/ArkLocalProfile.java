package qowyn.ark;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonString;

import qowyn.ark.properties.Property;

public class ArkLocalProfile implements PropertyContainer, GameObjectContainer {

  private static final Base64.Encoder ENCODER = Base64.getEncoder();

  private static final Base64.Decoder DECODER = Base64.getDecoder();

  private static final int UNKNOWN_DATA_2_SIZE = 0xc;

  private int localProfileVersion;

  private byte[] unknownData;

  private byte[] unknownData2;

  private final ArrayList<GameObject> objects = new ArrayList<>();

  private GameObject localProfile;

  public ArkLocalProfile() {}

  public ArkLocalProfile(String fileName) throws FileNotFoundException, IOException {
    this(fileName, new ReadingOptions());
  }

  public ArkLocalProfile(String fileName, ReadingOptions options) throws FileNotFoundException, IOException {
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

  public ArkLocalProfile(JsonObject object) {
    readJson(object);
  }

  public void readBinary(ArkArchive archive) {
    localProfileVersion = archive.getInt();

    if (localProfileVersion != 1 && localProfileVersion != 3) {
      throw new UnsupportedOperationException("Unknown Profile Version " + localProfileVersion);
    }

    int unknownDataSize = archive.getInt();

    unknownData = archive.getBytes(unknownDataSize);

    if (localProfileVersion == 3) {
      unknownData2 = archive.getBytes(UNKNOWN_DATA_2_SIZE);
    }

    int objectCount = archive.getInt();

    for (int i = 0; i < objectCount; i++) {
      objects.add(new GameObject(archive));
    }

    for (int i = 0; i < objectCount; i++) {
      GameObject object = objects.get(i);
      if (object.getClassString().equals("PrimalLocalProfile")) {
        localProfile = object;
      }
      object.loadProperties(archive, i < objectCount - 1 ? objects.get(i + 1) : null, 0);
    }
  }

  public void writeBinary(String fileName) throws FileNotFoundException, IOException {
    writeBinary(fileName, WritingOptions.create());
  }

  public void writeBinary(String fileName, WritingOptions options) throws FileNotFoundException, IOException {
    if (localProfileVersion == 3) {
      if (unknownData2 == null) {
        unknownData2 = new byte[UNKNOWN_DATA_2_SIZE];
      } else if (unknownData2.length < UNKNOWN_DATA_2_SIZE) {
        byte[] temp = new byte[UNKNOWN_DATA_2_SIZE];
        System.arraycopy(unknownData2, 0, temp, 0, unknownData2.length);
        unknownData2 = temp;
      }
    }

    int size = Integer.BYTES * 3;

    size += unknownData.length;

    if (localProfileVersion == 3) {
      size += 12;
    }

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

      archive.putInt(localProfileVersion);

      archive.putInt(unknownData.length);
      archive.putBytes(unknownData);

      if (localProfileVersion == 3) {
        archive.putBytes(unknownData2, 0, UNKNOWN_DATA_2_SIZE);
      }

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
    localProfileVersion = object.getInt("localProfileVersion");
    objects.clear();

    JsonObject profile = object.getJsonObject("localProfile");
    if (profile != null) {
      setLocalProfile(new GameObject(profile));
    }

    JsonArray profileObjects = object.getJsonArray("objects");
    if (profileObjects != null) {
      for (JsonObject profileObject : profileObjects.getValuesAs(JsonObject.class)) {
        objects.add(new GameObject(profileObject));
      }
    }

    JsonString unknownDataString = object.getJsonString("unknownData");
    if (unknownDataString != null) {
      unknownData = DECODER.decode(unknownDataString.getString());
    }

    JsonString unknownData2String = object.getJsonString("unknownData2");
    if (unknownData2String != null) {
      unknownData2 = DECODER.decode(unknownData2String.getString());
    }
  }

  public JsonObject toJson() {
    JsonObjectBuilder job = Json.createObjectBuilder();

    job.add("localProfileVersion", localProfileVersion);
    job.add("localProfile", localProfile.toJson());

    if (objects.size() > 1) {
      JsonArrayBuilder additionalObjects = Json.createArrayBuilder();
      for (GameObject object : objects) {
        if (object == localProfile) {
          continue;
        }

        additionalObjects.add(object.toJson());
      }
      job.add("objects", additionalObjects.build());
    }

    job.add("unknownData", ENCODER.encodeToString(unknownData));
    if (unknownData2 != null) {
      job.add("unknownData2", ENCODER.encodeToString(unknownData2));
    }

    return job.build();
  }

  public int getLocalProfileVersion() {
    return localProfileVersion;
  }

  public void setLocalProfileVersion(int localProfileVersion) {
    this.localProfileVersion = localProfileVersion;
  }

  public ArrayList<GameObject> getObjects() {
    return objects;
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
