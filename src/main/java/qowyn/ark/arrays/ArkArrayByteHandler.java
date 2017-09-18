package qowyn.ark.arrays;

import com.fasterxml.jackson.databind.JsonNode;

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
      return new ArkArrayUInt8(archive, property);
    }
  }

  public static ArkArray<?> create(JsonNode node, PropertyArray property) {
    // Enum version will have null as first element
    if (node.size() > 0 && node.get(0).isNull()) {
      return new ArkArrayByteValue(node, property);
    } else {
      return new ArkArrayUInt8(node, property);
    }
  }

}
