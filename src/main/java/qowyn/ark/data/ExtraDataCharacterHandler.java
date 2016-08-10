package qowyn.ark.data;

import javax.json.JsonValue;
import javax.json.JsonValue.ValueType;

import qowyn.ark.ArkArchive;
import qowyn.ark.GameObject;

public class ExtraDataCharacterHandler implements ExtraDataHandler {

  private static final ExtraDataCharacter INSTANCE = new ExtraDataCharacter();

  @Override
  public boolean canHandle(GameObject object, int length) {
    return object.getClassString().contains("_Character_");
  }

  @Override
  public boolean canHandle(GameObject object, JsonValue value) {
    return object.getClassString().contains("_Character_") && value.getValueType() == ValueType.NULL;
  }

  @Override
  public ExtraData read(GameObject object, ArkArchive archive, int length) {
    int shouldBeZero = archive.getInt();
    if (shouldBeZero != 0) {
      System.err.println("Expected int after properties to be 0 but found " + shouldBeZero + " at " + Integer.toHexString(archive.position() - 4));
    }

    int shouldBeOne = archive.getInt();
    if (shouldBeOne != 1) {
      System.err.println("Expected int after properties to be 1 but found " + shouldBeOne + " at " + Integer.toHexString(archive.position() - 4));
    }

    return INSTANCE;
  }

  @Override
  public ExtraData read(GameObject object, JsonValue value) {
    return INSTANCE;
  }

}
