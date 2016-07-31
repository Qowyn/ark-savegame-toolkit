package qowyn.ark;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;

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
