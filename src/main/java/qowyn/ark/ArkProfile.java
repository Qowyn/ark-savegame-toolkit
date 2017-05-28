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
import javax.json.JsonObjectBuilder;

import qowyn.ark.properties.Property;

public class ArkProfile implements PropertyContainer, GameObjectContainer {

  private int profileVersion;

  private final ArrayList<GameObject> objects = new ArrayList<>();

  private GameObject profile;

  public ArkProfile() {}

  public ArkProfile(String fileName) throws FileNotFoundException, IOException {
    this(fileName, new ReadingOptions());
  }

  public ArkProfile(String fileName, ReadingOptions options) throws FileNotFoundException, IOException {
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

  public ArkProfile(JsonObject object) {
    readJson(object);
  }

  public void readBinary(ArkArchive archive) {
    profileVersion = archive.getInt();

    if (profileVersion != 1) {
      throw new UnsupportedOperationException("Unknown Profile Version " + profileVersion);
    }

    int profilesCount = archive.getInt();

    for (int i = 0; i < profilesCount; i++) {
      objects.add(new GameObject(archive));
    }

    for (int i = 0; i < profilesCount; i++) {
      GameObject object = objects.get(i);
      if (object.getClassString().equals("PrimalPlayerData") || object.getClassString().equals("PrimalPlayerDataBP_C")) {
        profile = object;
      }
      object.loadProperties(archive, i < profilesCount - 1 ? objects.get(i + 1) : null, 0);
    }
  }

  public void writeBinary(String fileName) throws FileNotFoundException, IOException {
    writeBinary(fileName, WritingOptions.create());
  }

  public void writeBinary(String fileName, WritingOptions options) throws FileNotFoundException, IOException {
    int size = Integer.BYTES * 2;

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

      archive.putInt(profileVersion);
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
    profileVersion = object.getInt("profileVersion");
    objects.clear();

    JsonObject profile = object.getJsonObject("profile");
    if (profile != null) {
      setProfile(new GameObject(profile));
    }

    JsonArray profileObjects = object.getJsonArray("objects");
    if (profileObjects != null) {
      for (JsonObject profileObject : profileObjects.getValuesAs(JsonObject.class)) {
        objects.add(new GameObject(profileObject));
      }
    }
  }

  public JsonObject toJson() {
    JsonObjectBuilder job = Json.createObjectBuilder();

    job.add("profileVersion", profileVersion);
    job.add("profile", profile.toJson());

    if (objects.size() > 1) {
      JsonArrayBuilder additionalObjects = Json.createArrayBuilder();
      for (GameObject object : objects) {
        if (object == profile) {
          continue;
        }

        additionalObjects.add(object.toJson());
      }
      job.add("objects", additionalObjects.build());
    }

    return job.build();
  }

  public int getProfileVersion() {
    return profileVersion;
  }

  public void setProfileVersion(int profileVersion) {
    this.profileVersion = profileVersion;
  }

  public ArrayList<GameObject> getObjects() {
    return objects;
  }

  public GameObject getProfile() {
    return profile;
  }

  public void setProfile(GameObject profile) {
    if (this.profile != null) {
      int oldIndex = objects.indexOf(this.profile);
      if (oldIndex > -1) {
        objects.remove(oldIndex);
      }
    }
    this.profile = profile;
    if (profile != null && objects.indexOf(profile) == -1) {
      objects.add(0, profile);
    }
  }

  @Override
  public List<Property<?>> getProperties() {
    return profile.getProperties();
  }

  @Override
  public void setProperties(List<Property<?>> properties) {
    profile.setProperties(properties);
  }

}
