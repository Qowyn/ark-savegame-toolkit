package qowyn.ark.properties;

import javax.json.JsonObject;

import qowyn.ark.ArkArchive;
import qowyn.ark.types.ArkName;

public class PropertyUInt64 extends PropertyInt64 {

  public static final ArkName TYPE = ArkName.constantPlain("UInt64Property");

  public PropertyUInt64(String name, long value) {
    super(name, value);
  }

  public PropertyUInt64(String name, int index, long value) {
    super(name, index, value);
  }

  public PropertyUInt64(ArkArchive archive, ArkName name) {
    super(archive, name);
  }

  public PropertyUInt64(JsonObject o) {
    super(o);
  }

  @Override
  public ArkName getType() {
    return TYPE;
  }

}
