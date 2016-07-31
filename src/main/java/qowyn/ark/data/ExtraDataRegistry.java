package qowyn.ark.data;

import java.util.ArrayList;
import java.util.List;

import javax.json.JsonValue;

import qowyn.ark.ArkArchive;
import qowyn.ark.GameObject;

public class ExtraDataRegistry {

  /**
   * Contains ExtraDataHandler in reverse Order
   */
  public static final List<ExtraDataHandler> EXTRA_DATA_HANDLERS = new ArrayList<>();

  static {
    EXTRA_DATA_HANDLERS.add(new ExtraDataFallbackHandler());
    EXTRA_DATA_HANDLERS.add(new ExtraDataZeroHandler());
    EXTRA_DATA_HANDLERS.add(new ExtraDataCharacterHandler());
    EXTRA_DATA_HANDLERS.add(new ExtraDataFoliageHandler());
  }

  /**
   * Searches {@link #EXTRA_DATA_HANDLERS} in reverse Order and terminates on the first
   * {@link ExtraDataHandler} which can handle given {@link GameObject} {@code object}
   * 
   * @param object The GameObject
   * @param archive The source archive of object
   * @param length Amount of bytes of extra data
   * @return
   */
  public static ExtraData getExtraData(GameObject object, ArkArchive archive, int length) {
    for (int i = EXTRA_DATA_HANDLERS.size() - 1; i >= 0; i--) {
      ExtraDataHandler handler = EXTRA_DATA_HANDLERS.get(i);
      if (handler.canHandle(object, length)) {
        return handler.read(object, archive, length);
      }
    }

    return null;
  }

  /**
   * Searches {@link #EXTRA_DATA_HANDLERS} in reverse Order and terminates on the first
   * {@link ExtraDataHandler} which can handle given {@link GameObject} {@code object}
   * 
   * @param object The GameObject
   * @param value The JsonValue
   * @return
   */
  public static ExtraData getExtraData(GameObject object, JsonValue value) {
    for (int i = EXTRA_DATA_HANDLERS.size() - 1; i >= 0; i--) {
      ExtraDataHandler handler = EXTRA_DATA_HANDLERS.get(i);
      if (handler.canHandle(object, value)) {
        return handler.read(object, value);
      }
    }

    return null;
  }
}
