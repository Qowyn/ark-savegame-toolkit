package qowyn.ark;

import java.io.IOException;
import java.nio.file.Path;

public interface BinaryFormat {
  
  public default void readBinary(Path filePath) throws IOException {
    readBinary(filePath, ReadingOptions.create());
  }
  
  public void readBinary(Path filePath, ReadingOptions options) throws IOException;

  public default void writeBinary(Path filePath) throws IOException {
    writeBinary(filePath, WritingOptions.create());
  }

  public void writeBinary(Path filePath, WritingOptions options) throws IOException;

}
