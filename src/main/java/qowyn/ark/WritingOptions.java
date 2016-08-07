package qowyn.ark;

/**
 * Options specific to write operations.
 * 
 * @author Roland Firmont
 */
public class WritingOptions extends BaseOptions {

  private int asyncBufferSize = 4096;

  public static WritingOptions create() {
    return new WritingOptions();
  }

  /**
   * How many objects the buffer for asynchronous operations can hold.
   * 
   * @see #asyncBufferSize(int)
   * @return how many objects the buffer for asynchronous operations can hold
   */
  public int getAsyncBufferSize() {
    return asyncBufferSize;
  }

  /**
   * How many objects the buffer for asynchronous operations can hold. Higher values increase memory
   * consumption but may decrease total processing time if I/O operations are slow.
   * 
   * @param asyncBufferSize how many objects the buffer for asynchronous operations can hold
   * @return self, to continue building options using a fluent interface
   */
  public WritingOptions asyncBufferSize(int asyncBufferSize) {
    this.asyncBufferSize = asyncBufferSize;
    return this;
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
  public WritingOptions asynchronous(boolean asynchronous) {
    super.asynchronous(asynchronous);
    return this;
  }

}
