package qowyn.ark.arrays;

import javax.json.JsonArray;

import qowyn.ark.ArkArchive;
import qowyn.ark.properties.PropertyArray;
import qowyn.ark.types.ArkName;

public class ArkArrayUInt32 extends ArkArrayInt {

  public static final ArkName TYPE = ArkName.constantPlain("UInt32Property");

  private static final long serialVersionUID = 1L;

  public ArkArrayUInt32() {}

  public ArkArrayUInt32(ArkArchive archive, PropertyArray property) {
    super(archive, property);
  }

  public ArkArrayUInt32(JsonArray a, PropertyArray property) {
    super(a, property);
  }

  @Override
  public ArkName getType() {
    return TYPE;
  }

}
