package qowyn.ark.data;

import com.fasterxml.jackson.databind.JsonNode;

import qowyn.ark.ArkArchive;
import qowyn.ark.GameObject;

public class ExtraDataCharacterHandler implements ExtraDataHandler {

  private static final ExtraDataCharacter INSTANCE = new ExtraDataCharacter();

  @Override
  public boolean canHandle(GameObject object, int length) {
    return (object.getClassString().contains("_Character_") || object.getClassString().startsWith("PlayerPawnTest_")) && length == 8;
  }

  @Override
  public boolean canHandle(GameObject object, JsonNode node) {
    return (object.getClassString().contains("_Character_") || object.getClassString().startsWith("PlayerPawnTest_")) && node.isNull();
  }

  @Override
  public ExtraData readBinary(GameObject object, ArkArchive archive, int length) throws UnexpectedDataException {
    int shouldBeZero = archive.getInt();
    if (shouldBeZero != 0) {
      throw new UnexpectedDataException("Expected int after properties to be 0 but found " + shouldBeZero + " at " + Integer.toHexString(archive.position() - 4));
    }

    int shouldBeOne = archive.getInt();
    if (shouldBeOne != 1) {
      throw new UnexpectedDataException("Expected int after properties to be 1 but found " + shouldBeOne + " at " + Integer.toHexString(archive.position() - 4));
    }

    return INSTANCE;
  }

  @Override
  public ExtraData readJson(GameObject object, JsonNode node) {
    return INSTANCE;
  }

}
