package qowyn.ark.arrays;

import com.fasterxml.jackson.databind.JsonNode;

import qowyn.ark.ArkArchive;
import qowyn.ark.properties.PropertyArray;
import qowyn.ark.types.ArkName;

public class ArkArrayUInt8 extends ArkArrayInt8 {

  private static final long serialVersionUID = 1L;

  public ArkArrayUInt8() {}

  public ArkArrayUInt8(ArkArchive archive, PropertyArray property) {
    super(archive, property);
  }

  public ArkArrayUInt8(JsonNode node, PropertyArray property) {
    super(node, property);
  }

  @Override
  public ArkName getType() {
    return ArkArrayByteHandler.TYPE;
  }

}
