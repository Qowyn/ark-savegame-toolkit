package qowyn.ark;

/**
 * Basic Options for read and write operations
 * 
 * @author Roland Firmont
 */
public class BaseOptions {

  private boolean memoryMapping = true;

  private boolean parallel = false;

  private boolean asynchronous = Runtime.getRuntime().availableProcessors() > 1;

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
   * Should I/O be handled asynchronously where possible?
   * 
   * @return whether I/O will be handled asynchronously
   */
  public boolean isAsynchronous() {
    return asynchronous;
  }

  /**
   * Sets whether I/O will be handled asynchronously.
   * 
   * @param asynchronous whether I/O will be handled asynchronously
   * @return self, to continue building options using a fluent interface
   */
  public BaseOptions asynchronous(boolean asynchronous) {
    this.asynchronous = asynchronous;
    return this;
  }

}
