package qowyn.ark.data;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonGenerator;

import qowyn.ark.ArkArchive;
import qowyn.ark.NameCollector;
import qowyn.ark.NameContainer;
import qowyn.ark.NameSizeCalculator;
import qowyn.ark.structs.StructPropertyList;

public class ExtraDataFoliage implements ExtraData, NameContainer {

  static final String NULL_PLACEHOLDER = "/NULL_KEY";

  private List<Map<String, StructPropertyList>> structMapList;

  public List<Map<String, StructPropertyList>> getStructMapList() {
    return structMapList;
  }

  public void setStructMapList(List<Map<String, StructPropertyList>> structMapList) {
    this.structMapList = structMapList;
  }

  @Override
  public int calculateSize(NameSizeCalculator nameSizer) {
    int size = Integer.BYTES * 2;

    size += Integer.BYTES * structMapList.size();
    for (Map<String, StructPropertyList> structMap : structMapList) {
      for (Map.Entry<String, StructPropertyList> entry : structMap.entrySet()) {
        size += ArkArchive.getStringLength(entry.getKey());
        size += entry.getValue().getSize(nameSizer);
        size += Integer.BYTES;
      }
    }

    return size;
  }

  @Override
  public void writeJson(JsonGenerator generator) throws IOException {
    generator.writeStartArray(structMapList.size());

    for (Map<String, StructPropertyList> structMap : structMapList) {
      generator.writeStartObject();
      for (Map.Entry<String, StructPropertyList> entry : structMap.entrySet()) {
        generator.writeFieldName(entry.getKey() == null ? NULL_PLACEHOLDER : entry.getKey());
        entry.getValue().writeJson(generator);
      }
      generator.writeEndObject();
    }

    generator.writeEndArray();
  }

  @Override
  public void writeBinary(ArkArchive archive) {
    archive.putInt(0);
    archive.putInt(structMapList.size());

    for (Map<String, StructPropertyList> structMap : structMapList) {
      archive.putInt(structMap.size());
      for (Map.Entry<String, StructPropertyList> entry : structMap.entrySet()) {
        archive.putString(entry.getKey());
        entry.getValue().writeBinary(archive);
        archive.putInt(0);
      }
    }
  }

  @Override
  public void collectNames(NameCollector collector) {
    for (Map<String, StructPropertyList> structMap : structMapList) {
      for (Map.Entry<String, StructPropertyList> entry : structMap.entrySet()) {
        entry.getValue().collectNames(collector);
      }
    }
  }

}
