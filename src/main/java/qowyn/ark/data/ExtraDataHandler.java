package qowyn.ark.data;

import javax.json.JsonValue;

import qowyn.ark.ArkArchive;
import qowyn.ark.GameObject;

public interface ExtraDataHandler {

  public boolean canHandle(GameObject object, int length);

  public boolean canHandle(GameObject object, JsonValue value);

  public ExtraData read(GameObject object, ArkArchive archive, int length) throws UnexpectedDataException;

  public ExtraData read(GameObject object, JsonValue value);

}
