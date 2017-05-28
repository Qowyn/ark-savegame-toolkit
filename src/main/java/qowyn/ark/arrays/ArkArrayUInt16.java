package qowyn.ark.arrays;

import javax.json.JsonArray;

import qowyn.ark.ArkArchive;
import qowyn.ark.properties.PropertyArray;
import qowyn.ark.types.ArkName;

public class ArkArrayUInt16 extends ArkArrayInt16 {

  public static final ArkName TYPE = ArkName.constantPlain("UInt16Property");

  private static final long serialVersionUID = 1L;

  public ArkArrayUInt16() {}

  public ArkArrayUInt16(ArkArchive archive, PropertyArray property) {
    super(archive, property);
  }

  public ArkArrayUInt16(JsonArray a, PropertyArray property) {
    super(a, property);
  }

  @Override
  public ArkName getType() {
    return TYPE;
  }

}
