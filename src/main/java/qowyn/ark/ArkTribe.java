package qowyn.ark;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;

public class ArkTribe {

  private int tribeVersion;

  private GameObject tribe;

  public ArkTribe() {}

  public ArkTribe(String fileName) throws FileNotFoundException, IOException {
    try (RandomAccessFile raf = new RandomAccessFile(fileName, "r")) {
      FileChannel fc = raf.getChannel();
      ArkArchive archive = new ArkArchive(fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size()));

      readBinary(archive);
    }
  }

  public void readBinary(ArkArchive archive) {
    tribeVersion = archive.getInt();

    if (tribeVersion != 1) {
      throw new UnsupportedOperationException("Unknown Tribe Version " + tribeVersion);
    }

    int tribesCount = archive.getInt();

    if (tribesCount != 1) {
      throw new UnsupportedOperationException("Unsupported count of tribes " + tribesCount);
    }

    tribe = new GameObject(archive);
    tribe.loadProperties(archive, null, 0);
  }

  public int getTribeVersion() {
    return tribeVersion;
  }

  public void setTribeVersion(int tribeVersion) {
    this.tribeVersion = tribeVersion;
  }

  public GameObject getTribe() {
    return tribe;
  }

  public void setTribe(GameObject tribe) {
    this.tribe = tribe;
  }

}
