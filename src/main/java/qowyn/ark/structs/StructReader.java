package qowyn.ark.structs;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

import javax.json.JsonValue;

import qowyn.ark.ArkArchive;
import qowyn.ark.types.ArkName;

public class StructReader {

  public static final Map<String, BiFunction<ArkArchive, ArkName, Struct>> TYPE_MAP = new HashMap<>();

  public static final Map<String, BiFunction<JsonValue, ArkName, Struct>> TYPE_JSON_MAP = new HashMap<>();

  public static void addStruct(String name, BiFunction<ArkArchive, ArkName, Struct> binary, BiFunction<JsonValue, ArkName, Struct> json) {
    TYPE_MAP.put(name, binary);
    TYPE_JSON_MAP.put(name, json);
  }

  static {
    addStruct("ItemNetID", StructPropertyList::new, StructPropertyList::new);
    addStruct("Transform", StructPropertyList::new, StructPropertyList::new);
    addStruct("PrimalPlayerDataStruct", StructPropertyList::new, StructPropertyList::new);
    addStruct("PrimalPlayerCharacterConfigStruct", StructPropertyList::new, StructPropertyList::new);
    addStruct("PrimalPersistentCharacterStatsStruct", StructPropertyList::new, StructPropertyList::new);
    addStruct("TribeData", StructPropertyList::new, StructPropertyList::new);
    addStruct("Vector", StructVector::new, StructVector::new);
    addStruct("Quat", StructQuat::new, StructQuat::new);
    addStruct("LinearColor", StructLinearColor::new, StructLinearColor::new);
    addStruct("Rotator", StructVector::new, StructVector::new);
    addStruct("UniqueNetIdRepl", StructUniqueNetIdRepl::new, StructUniqueNetIdRepl::new);
  }

  public static Struct read(ArkArchive archive, ArkName structType, int size) {
    String structTypeString = structType.toString();

    if (TYPE_MAP.containsKey(structTypeString)) {
      return TYPE_MAP.get(structTypeString).apply(archive, structType);
    } else {
      System.err.println("Warning: Unknown Struct Type " + structType + " at " + Integer.toHexString(archive.position()) + " trying to read as StructPropertyList");
      return new StructPropertyList(archive, structType);
    }
  }

  public static Struct read(JsonValue o, ArkName structType) {
    String structTypeString = structType.toString();

    if (TYPE_JSON_MAP.containsKey(structTypeString)) {
      return TYPE_JSON_MAP.get(structTypeString).apply(o, structType);
    } else {
      return new StructPropertyList(o, structType);
    }
  }

}
