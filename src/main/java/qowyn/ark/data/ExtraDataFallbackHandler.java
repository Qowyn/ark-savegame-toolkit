package qowyn.ark.data;

import java.io.IOException;

import com.fasterxml.jackson.databind.JsonNode;

import qowyn.ark.ArkArchive;
import qowyn.ark.GameObject;
import qowyn.ark.LoggerHelper;

public class ExtraDataFallbackHandler implements ExtraDataHandler {

  @Override
  public boolean canHandle(GameObject object, int length) {
    return true;
  }

  @Override
  public boolean canHandle(GameObject object, JsonNode node) {
    return node.isBinary();
  }

  @Override
  public ExtraData readBinary(GameObject object, ArkArchive archive, int length) {
    ExtraDataBlob extraData = new ExtraDataBlob();

    archive.debugMessage(LoggerHelper.format("Unknown extended data for %s with length %d", object.getClassString(), length));
    extraData.setData(archive.getBytes(length));
    archive.unknownNames();

    return extraData;
  }

  @Override
  public ExtraData readJson(GameObject object, JsonNode node) {
    ExtraDataBlob extraData = new ExtraDataBlob();
    try {
      extraData.setData(node.binaryValue());
    } catch (IOException ex) {
      throw new RuntimeException(ex);
    }

    return extraData;
  }

}
