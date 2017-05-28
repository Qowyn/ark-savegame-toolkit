package qowyn.ark.arrays;

import javax.json.JsonArray;

import qowyn.ark.ArkArchive;
import qowyn.ark.properties.PropertyArray;
import qowyn.ark.types.ArkName;

public class ArkArrayUInt8 extends ArkArrayInt8 {

  private static final long serialVersionUID = 1L;

  public ArkArrayUInt8() {}

  public ArkArrayUInt8(ArkArchive archive, PropertyArray property) {
    super(archive, property);
  }

  public ArkArrayUInt8(JsonArray a, PropertyArray property) {
    super(a, property);
  }

  @Override
  public ArkName getType() {
    return ArkArrayByteHandler.TYPE;
  }

}
