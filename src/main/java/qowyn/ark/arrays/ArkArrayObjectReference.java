package qowyn.ark.arrays;

import java.io.IOException;
import java.util.ArrayList;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;

import qowyn.ark.ArkArchive;
import qowyn.ark.NameCollector;
import qowyn.ark.NameSizeCalculator;
import qowyn.ark.properties.PropertyArray;
import qowyn.ark.types.ArkName;
import qowyn.ark.types.ObjectReference;

public class ArkArrayObjectReference extends ArrayList<ObjectReference> implements ArkArray<ObjectReference> {

  public static final ArkName TYPE = ArkName.constantPlain("ObjectProperty");

  private static final long serialVersionUID = 1L;

  public ArkArrayObjectReference() {}

  public ArkArrayObjectReference(ArkArchive archive, PropertyArray property) {
    int size = archive.getInt();

    for (int n = 0; n < size; n++) {
      add(new ObjectReference(archive, 8)); // Fixed size?
    }
  }

  public ArkArrayObjectReference(JsonNode node, PropertyArray property) {
    node.forEach(o -> this.add(new ObjectReference(o, 8)));
  }

  @Override
  public Class<ObjectReference> getValueClass() {
    return ObjectReference.class;
  }

  @Override
  public ArkName getType() {
    return TYPE;
  }

  @Override
  public int calculateSize(NameSizeCalculator nameSizer) {
    int size = Integer.BYTES;

    size += this.stream().mapToInt(or -> or.getSize(nameSizer)).sum();

    return size;
  }

  @Override
  public void writeJson(JsonGenerator generator) throws IOException {
    generator.writeStartArray(size());

    for (ObjectReference value: this) {
      value.writeJson(generator);
    }

    generator.writeEndArray();
  }

  @Override
  public void writeBinary(ArkArchive archive) {
    archive.putInt(size());

    this.forEach(or -> or.writeBinary(archive));
  }

  @Override
  public void collectNames(NameCollector collector) {
    this.forEach(or -> or.collectNames(collector));
  }

}
