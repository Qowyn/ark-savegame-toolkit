package qowyn.ark;

import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class ConcurrencyUtil {

  public static <T> Stream<List<T>> splitToChunks(List<T> list, int length) {
    int size = list.size();
    int fullChunks = (size - 1) / length;

    return IntStream.rangeClosed(0, fullChunks)
        .mapToObj(n -> list.subList(n * length, n == fullChunks ? size : (n + 1) * length));
  }

}
