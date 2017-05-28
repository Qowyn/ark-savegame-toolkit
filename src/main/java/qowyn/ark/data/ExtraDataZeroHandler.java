package qowyn.ark.data;

import javax.json.JsonValue;
import javax.json.JsonValue.ValueType;

import qowyn.ark.ArkArchive;
import qowyn.ark.GameObject;

public class ExtraDataZeroHandler implements ExtraDataHandler {

  private static final ExtraDataZero INSTANCE = new ExtraDataZero();

  @Override
  public boolean canHandle(GameObject object, int length) {
    return length == 4;
  }

  @Override
  public boolean canHandle(GameObject object, JsonValue value) {
    return value.getValueType() == ValueType.NULL;
  }

  @Override
  public ExtraData read(GameObject object, ArkArchive archive, int length) throws UnexpectedDataException {
    int shouldBeZero = archive.getInt();
    if (shouldBeZero != 0) {
      throw new UnexpectedDataException("Expected int after properties to be 0 but found " + shouldBeZero + " at " + Integer.toHexString(archive.position() - 4));
    }

    return INSTANCE;
  }

  @Override
  public ExtraData read(GameObject object, JsonValue value) {
    return INSTANCE;
  }

}
