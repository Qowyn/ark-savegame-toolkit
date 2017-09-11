package qowyn.ark.data;

import com.fasterxml.jackson.databind.JsonNode;

import qowyn.ark.ArkArchive;
import qowyn.ark.GameObject;

public interface ExtraDataHandler {

  public boolean canHandle(GameObject object, int length);

  public boolean canHandle(GameObject object, JsonNode node);

  public ExtraData readBinary(GameObject object, ArkArchive archive, int length) throws UnexpectedDataException;

  public ExtraData readJson(GameObject object, JsonNode node);

}
