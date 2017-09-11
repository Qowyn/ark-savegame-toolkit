package qowyn.ark.arrays;

import com.fasterxml.jackson.databind.JsonNode;

import qowyn.ark.ArkArchive;
import qowyn.ark.properties.PropertyArray;
import qowyn.ark.types.ArkName;

public class ArkArrayUInt64 extends ArkArrayInt64 {

  public static final ArkName TYPE = ArkName.constantPlain("UInt64Property");

  private static final long serialVersionUID = 1L;
  
  public ArkArrayUInt64() {}

  public ArkArrayUInt64(ArkArchive archive, PropertyArray property) {
    super(archive, property);
  }

  public ArkArrayUInt64(JsonNode node, PropertyArray property) {
    super(node, property);
  }

  @Override
  public ArkName getType() {
    return TYPE;
  }

}
