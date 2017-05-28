package qowyn.ark;

class LongLong {

  private final long high;

  private final long low;

  public LongLong(long high, long low) {
    this.high = high;
    this.low = low;
  }

  public long getHigh() {
    return high;
  }

  public long getLow() {
    return low;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + (int) (high ^ (high >>> 32));
    result = prime * result + (int) (low ^ (low >>> 32));
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    LongLong other = (LongLong) obj;
    if (high != other.high)
      return false;
    if (low != other.low)
      return false;
    return true;
  }

}
