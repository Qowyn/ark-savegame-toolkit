package qowyn.ark.properties;

import com.fasterxml.jackson.databind.JsonNode;

import qowyn.ark.ArkArchive;
import qowyn.ark.types.ArkName;

public class PropertyUInt16 extends PropertyInt16 {

  public static final ArkName TYPE = ArkName.constantPlain("UInt16Property");

  public PropertyUInt16(String name, short value) {
    super(name, value);
  }

  public PropertyUInt16(String name, int index, short value) {
    super(name, index, value);
  }
  
  public PropertyUInt16(ArkArchive archive, ArkName name) {
    super(archive, name);
  }

  public PropertyUInt16(JsonNode node) {
    super(node);
  }
  
  @Override
  public ArkName getType() {
    return TYPE;
  }

}
