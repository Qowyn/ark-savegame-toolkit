package qowyn.ark;

public class WritingOptions {

  private boolean parallelWriting = false;

  private boolean memoryMapping = true;

  public static WritingOptions create() {
    return new WritingOptions();
  }

  public boolean getParallelWriting() {
    return parallelWriting;
  }

  public WritingOptions withParallelWriting(boolean parallelWriting) {
    this.parallelWriting = parallelWriting;
    return this;
  }

  public boolean getMemoryMapping() {
    return memoryMapping;
  }

  public WritingOptions withMemoryMapping(boolean memoryMapping) {
    this.memoryMapping = memoryMapping;
    return this;
  }

}
