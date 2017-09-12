package qowyn.ark;

/**
 * Basic Options for read and write operations
 * 
 * @author Roland Firmont
 */
public class BaseOptions {

  private boolean memoryMapping = true;

  private boolean parallel = false;

  private int threadCount = Runtime.getRuntime().availableProcessors();

  public static BaseOptions create() {
    return new BaseOptions();
  }

  /**
   * Should {@link #withMemoryMapping(boolean) memory mapping} be used when processing binary data?
   * 
   * @return whether memory mapping will be used when processing binary data
   */
  public boolean usesMemoryMapping() {
    return memoryMapping;
  }

  /**
   * Sets whether memory mapping will be used when processing binary data.
   * 
   * @param memoryMapping whether memory mapping will be used when processing binary data
   * @return self, to continue building options using a fluent interface
   */
  public BaseOptions withMemoryMapping(boolean memoryMapping) {
    this.memoryMapping = memoryMapping;
    return this;
  }

  /**
   * Should data be processed in {@link #parallel(boolean) parallel}?
   * 
   * @return whether data will be processed in parallel
   */
  public boolean isParallel() {
    return parallel;
  }

  /**
   * Sets where data will be processed in parallel.
   * 
   * @param parallel whether data will be processed in parallel
   * @return self, to continue building options using a fluent interface
   */
  public BaseOptions parallel(boolean parallel) {
    this.parallel = parallel;
    return this;
  }

  /**
   * The amount of thread to use for parallel operations
   * 
   * @return amount of threads to use
   */
  public int getThreadCount() {
    return threadCount;
  }

  /**
   * Sets the amount of threads to use
   * 
   * @param threadCount amount of threads to use
   * @return self, to continue building options using a fluent interface
   */
  public BaseOptions withThreadCount(int threadCount) {
    if (threadCount < 1) {
      throw new IllegalArgumentException("ThreadCount must not be lower than 1");
    }
    this.threadCount = threadCount;
    return this;
  }

}
