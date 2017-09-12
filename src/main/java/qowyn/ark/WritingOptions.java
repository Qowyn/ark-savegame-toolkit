package qowyn.ark;

/**
 * Options specific to write operations.
 * 
 * @author Roland Firmont
 */
public class WritingOptions extends BaseOptions {

  public static WritingOptions create() {
    return new WritingOptions();
  }

  @Override
  public WritingOptions parallel(boolean parallel) {
    super.parallel(parallel);
    return this;
  }

  @Override
  public WritingOptions withMemoryMapping(boolean memoryMapping) {
    super.withMemoryMapping(memoryMapping);
    return this;
  }

  @Override
  public WritingOptions withThreadCount(int threadCount) {
    super.withThreadCount(threadCount);
    return this;
  }

}
