package qowyn.ark;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

public class ArkProfile {

  private int profileVersion;

  private GameObject profile;

  public ArkProfile() {}

  public ArkProfile(String fileName) throws FileNotFoundException, IOException {
    try (RandomAccessFile raf = new RandomAccessFile(fileName, "r")) {
      FileChannel fc = raf.getChannel();
      ArkArchive archive = new ArkArchive(fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size()));

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

    if (profilesCount != 1) {
      throw new UnsupportedOperationException("Unsupported count of profiles " + profilesCount);
    }

    profile = new GameObject(archive);
    profile.loadProperties(archive, null, 0);
  }

  public void writeBinary(String fileName) throws FileNotFoundException, IOException {
    writeBinary(fileName, WritingOptions.create());
  }

  public void writeBinary(String fileName, WritingOptions options) throws FileNotFoundException, IOException {
    int size = Integer.BYTES * 2;

    size += profile.getSize(false);

    int propertiesBlockOffset = size;

    size += profile.getPropertiesSize(false);

    try (RandomAccessFile raf = new RandomAccessFile(fileName, "rw")) {
      raf.setLength(size);
      ByteBuffer buffer;

      if (options.getMemoryMapping()) {
        FileChannel fc = raf.getChannel();
        buffer = fc.map(FileChannel.MapMode.READ_WRITE, 0, size);
      } else {
        buffer = ByteBuffer.allocate(size);
      }

      ArkArchive archive = new ArkArchive(buffer);

      archive.putInt(profileVersion);
      archive.putInt(1);
      profile.write(archive, propertiesBlockOffset);
      profile.writeProperties(archive, 0);

      if (!options.getMemoryMapping()) {
        raf.write(buffer.array());
      }
    }
  }

  public void readJson(JsonObject object) {
    profileVersion = object.getInt("profileVersion");
    profile = new GameObject(object.getJsonObject("profile"));
  }

  public JsonObject toJson() {
    JsonObjectBuilder job = Json.createObjectBuilder();

    job.add("profileVersion", profileVersion);
    job.add("profile", profile.toJson());

    return job.build();
  }

  public int getProfileVersion() {
    return profileVersion;
  }

  public void setProfileVersion(int profileVersion) {
    this.profileVersion = profileVersion;
  }

  public GameObject getProfile() {
    return profile;
  }

  public void setProfile(GameObject profile) {
    this.profile = profile;
  }

}
