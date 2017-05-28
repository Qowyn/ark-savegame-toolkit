package qowyn.ark.data;

import java.util.Base64;

import javax.json.JsonString;
import javax.json.JsonValue;
import javax.json.JsonValue.ValueType;

import qowyn.ark.ArkArchive;
import qowyn.ark.GameObject;

public class ExtraDataFallbackHandler implements ExtraDataHandler {

  private static final Base64.Decoder DECODER = Base64.getDecoder();

  @Override
  public boolean canHandle(GameObject object, int length) {
    return true;
  }

  @Override
  public boolean canHandle(GameObject object, JsonValue value) {
    return value != null && value.getValueType() == ValueType.STRING;
  }

  @Override
  public ExtraData read(GameObject object, ArkArchive archive, int length) {
    ExtraDataBlob extraData = new ExtraDataBlob();

    extraData.setData(archive.getBytes(length));
    archive.unknownNames();

    return extraData;
  }

  @Override
  public ExtraData read(GameObject object, JsonValue value) {
    JsonString valueString = (JsonString) value;

    ExtraDataBlob extraData = new ExtraDataBlob();
    extraData.setData(DECODER.decode(valueString.getString()));

    return extraData;
  }

}
