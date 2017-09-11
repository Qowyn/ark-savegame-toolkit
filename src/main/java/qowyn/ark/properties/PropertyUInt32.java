package qowyn.ark.properties;

import com.fasterxml.jackson.databind.JsonNode;

import qowyn.ark.ArkArchive;
import qowyn.ark.types.ArkName;

public class PropertyUInt32 extends PropertyInt {

  public static final ArkName TYPE = ArkName.constantPlain("UInt32Property");

  public PropertyUInt32(String name, int value) {
    super(name, value);
  }

  public PropertyUInt32(String name, int index, int value) {
    super(name, index, value);
  }

  public PropertyUInt32(ArkArchive archive, ArkName name) {
    super(archive, name);
  }

  public PropertyUInt32(JsonNode node) {
    super(node);
  }

  @Override
  public ArkName getType() {
    return TYPE;
  }

}
