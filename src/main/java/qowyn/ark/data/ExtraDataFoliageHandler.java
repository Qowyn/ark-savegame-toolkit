package qowyn.ark.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonValue;
import javax.json.JsonValue.ValueType;

import qowyn.ark.ArkArchive;
import qowyn.ark.GameObject;
import qowyn.ark.properties.UnreadablePropertyException;
import qowyn.ark.structs.StructPropertyList;
import qowyn.ark.types.ArkName;

public class ExtraDataFoliageHandler implements ExtraDataHandler {

  private static final ArkName CLASS_NAME = ArkName.constantPlain("InstancedFoliageActor");

  @Override
  public boolean canHandle(GameObject object, int length) {
    return object.getClassName() == CLASS_NAME;
  }

  @Override
  public boolean canHandle(GameObject object, JsonValue value) {
    return object.getClassName() == CLASS_NAME && value.getValueType() == ValueType.ARRAY;
  }

  @Override
  public ExtraData read(GameObject object, ArkArchive archive, int length) throws UnexpectedDataException {
    int shouldBeZero = archive.getInt();
    if (shouldBeZero != 0) {
      throw new UnexpectedDataException("Expected int after properties to be 0 but found " + shouldBeZero + " at " + Integer.toHexString(archive.position() - 4));
    }

    int structMapCount = archive.getInt();

    List<Map<String, StructPropertyList>> structMapList = new ArrayList<>(structMapCount);

    try {
      for (int structMapIndex = 0; structMapIndex < structMapCount; structMapIndex++) {
        int structCount = archive.getInt();
        Map<String, StructPropertyList> structMap = new HashMap<>();

        for (int structIndex = 0; structIndex < structCount; structIndex++) {
          String structName = archive.getString();
          StructPropertyList properties = new StructPropertyList(archive);

          int shouldBeZero2 = archive.getInt();
          if (shouldBeZero2 != 0) {
            throw new UnexpectedDataException("Expected int after properties to be 0 but found " + shouldBeZero2 + " at " + Integer.toHexString(archive.position() - 4));
          }

          structMap.put(structName, properties);
        }

        structMapList.add(structMap);
      }
    } catch (UnreadablePropertyException upe) {
      throw new UnexpectedDataException(upe);
    }

    ExtraDataFoliage extraDataFoliage = new ExtraDataFoliage();
    extraDataFoliage.setStructMapList(structMapList);

    return extraDataFoliage;
  }

  @Override
  public ExtraData read(GameObject object, JsonValue value) {
    List<JsonObject> structMapArray = ((JsonArray) value).getValuesAs(JsonObject.class);
    int structMapCount = structMapArray.size();

    List<Map<String, StructPropertyList>> structMapList = new ArrayList<>(structMapCount);

    for (JsonObject structMapJson : structMapArray) {
      Map<String, StructPropertyList> structMap = new HashMap<>();

      for (Map.Entry<String, JsonValue> structs : structMapJson.entrySet()) {
        String key = structs.getKey().equals(ExtraDataFoliage.NULL_PLACEHOLDER) ? null : structs.getKey();
        structMap.put(key, new StructPropertyList(structs.getValue()));
      }

      structMapList.add(structMap);
    }

    ExtraDataFoliage extraDataFoliage = new ExtraDataFoliage();
    extraDataFoliage.setStructMapList(structMapList);

    return extraDataFoliage;
  }


}
