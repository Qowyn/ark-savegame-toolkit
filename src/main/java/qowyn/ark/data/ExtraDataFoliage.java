package qowyn.ark.data;

import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;

import qowyn.ark.ArkArchive;
import qowyn.ark.NameContainer;
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
  public int calculateSize(boolean nameTable) {
    int size = Integer.BYTES * 2;

    size += Integer.BYTES * structMapList.size();
    for (Map<String, StructPropertyList> structMap : structMapList) {
      for (Map.Entry<String, StructPropertyList> entry : structMap.entrySet()) {
        size += ArkArchive.getStringLength(entry.getKey());
        size += entry.getValue().getSize(nameTable);
        size += Integer.BYTES;
      }
    }

    return size;
  }

  @Override
  public JsonValue toJson() {
    JsonArrayBuilder jab = Json.createArrayBuilder();

    for (Map<String, StructPropertyList> structMap : structMapList) {
      JsonObjectBuilder job = Json.createObjectBuilder();
      for (Map.Entry<String, StructPropertyList> entry : structMap.entrySet()) {
        job.add(entry.getKey() == null ? NULL_PLACEHOLDER : entry.getKey(), entry.getValue().toJson());
      }
      jab.add(job);
    }

    return jab.build();
  }

  @Override
  public void write(ArkArchive archive) {
    archive.putInt(0);
    archive.putInt(structMapList.size());

    for (Map<String, StructPropertyList> structMap : structMapList) {
      archive.putInt(structMap.size());
      for (Map.Entry<String, StructPropertyList> entry : structMap.entrySet()) {
        archive.putString(entry.getKey());
        entry.getValue().write(archive);
        archive.putInt(0);
      }
    }
  }

  @Override
  public void collectNames(Set<String> nameTable) {
    for (Map<String, StructPropertyList> structMap : structMapList) {
      for (Map.Entry<String, StructPropertyList> entry : structMap.entrySet()) {
        entry.getValue().collectNames(nameTable);
      }
    }
  }

}
