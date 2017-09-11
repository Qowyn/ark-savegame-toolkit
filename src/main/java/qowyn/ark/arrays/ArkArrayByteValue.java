package qowyn.ark.arrays;

import java.io.IOException;
import java.util.ArrayList;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;

import qowyn.ark.ArkArchive;
import qowyn.ark.NameCollector;
import qowyn.ark.NameSizeCalculator;
import qowyn.ark.properties.PropertyArray;
import qowyn.ark.types.ArkByteValue;
import qowyn.ark.types.ArkName;

/**
 *
 * @author Roland Firmont
 *
 */
public class ArkArrayByteValue extends ArrayList<ArkByteValue> implements ArkArray<ArkByteValue> {

  private static final long serialVersionUID = 1L;

  public ArkArrayByteValue() {}

  public ArkArrayByteValue(ArkArchive archive, PropertyArray property) {
    int size = archive.getInt();

    for (int n = 0; n < size; n++) {
      add(new ArkByteValue(archive.getName()));
    }
  }

  public ArkArrayByteValue(JsonNode node, PropertyArray property) {
    node.forEach(n -> this.add(new ArkByteValue(ArkName.from(n.asText()))));
  }

  @Override
  public Class<ArkByteValue> getValueClass() {
    return ArkByteValue.class;
  }

  @Override
  public ArkName getType() {
    return ArkArrayByteHandler.TYPE;
  }

  @Override
  public int calculateSize(NameSizeCalculator nameSizer) {
    return Integer.BYTES + stream().mapToInt(bv -> nameSizer.sizeOf(bv.getNameValue())).sum();
  }

  @Override
  public void writeJson(JsonGenerator generator) throws IOException {
    generator.writeStartArray(size() + 1);

    // Marker
    generator.writeNull();
    for (ArkByteValue value: this) {
      generator.writeString(value.getNameValue().toString());
    }

    generator.writeEndArray();
  }

  @Override
  public void writeBinary(ArkArchive archive) {
    archive.putInt(size());

    this.forEach(bv -> archive.putName(bv.getNameValue()));
  }

  @Override
  public void collectNames(NameCollector collector) {
    this.forEach(bv -> collector.accept(bv.getNameValue()));
  }

}
