package qowyn.ark.arrays;

import javax.json.JsonArray;
import javax.json.JsonValue.ValueType;

import qowyn.ark.ArkArchive;
import qowyn.ark.properties.PropertyArray;
import qowyn.ark.properties.UnreadablePropertyException;
import qowyn.ark.types.ArkName;

public class ArkArrayByteHandler {

  public static final ArkName TYPE = ArkName.constantPlain("ByteProperty");

  public static ArkArray<?> create(ArkArchive archive, PropertyArray property) {
    int size = archive.getInt();

    if (property.getDataSize() < size + 4) {
      throw new UnreadablePropertyException("Found Array of ByteProperty with unexpected size.");
    }

    archive.position(archive.position() - 4);

    if (property.getDataSize() > size + 4) {
      return new ArkArrayByteValue(archive, property);
    } else {
      return new ArkArrayInt8(archive, property);
    }
  }

  public static ArkArray<?> create(JsonArray a, PropertyArray property) {
    // Enum version will have null as first element
    if (a.size() > 0 && a.get(0).getValueType() == ValueType.NULL) {
      return new ArkArrayByteValue(a, property);
    } else {
      return new ArkArrayInt8(a, property);
    }
  }

}
