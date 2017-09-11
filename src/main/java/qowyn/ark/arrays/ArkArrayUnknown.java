package qowyn.ark.arrays;

import java.io.IOException;
import java.util.AbstractList;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;

import qowyn.ark.ArkArchive;
import qowyn.ark.NameCollector;
import qowyn.ark.NameSizeCalculator;
import qowyn.ark.types.ArkName;

public class ArkArrayUnknown extends AbstractList<Byte> implements ArkArray<Byte> {

  private final byte[] value;

  private final ArkName type;

  public ArkArrayUnknown(ArkArchive archive, int size, ArkName type) {
    value = archive.getBytes(size);
    this.type = type;
  }

  public ArkArrayUnknown(JsonNode node, ArkName type) {
    try {
      value = node.binaryValue();
    } catch (IOException ex) {
      throw new RuntimeException(ex);
    }

    this.type = type;
  }

  @Override
  public Class<Byte> getValueClass() {
    return Byte.class;
  }

  @Override
  public ArkName getType() {
    return type;
  }

  @Override
  public int calculateSize(NameSizeCalculator nameSizer) {
    return value.length;
  }

  @Override
  public void writeJson(JsonGenerator generator) throws IOException {
    generator.writeBinary(value);
  }

  @Override
  public void writeBinary(ArkArchive archive) {
    archive.putBytes(value);
  }

  @Override
  public void collectNames(NameCollector collector) {}

  @Override
  public Byte get(int index) {
    if (index < 0 || index >= value.length) {
      throw new IndexOutOfBoundsException();
    }

    return Byte.valueOf(value[index]);
  }

  public byte getPrimitive(int index) {
    if (index < 0 || index >= value.length) {
      throw new IndexOutOfBoundsException();
    }

    return value[index];
  }

  @Override
  public int size() {
    return value.length;
  }

}
