package qowyn.ark.structs;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

import javax.json.JsonValue;

import qowyn.ark.ArkArchive;
import qowyn.ark.types.ArkName;

public class StructRegistry {

  public static final Map<ArkName, BiFunction<ArkArchive, ArkName, Struct>> TYPE_MAP = new HashMap<>();

  public static final Map<ArkName, BiFunction<JsonValue, ArkName, Struct>> TYPE_JSON_MAP = new HashMap<>();

  public static final Map<ArkName, ArkName> NAME_TYPE_MAP = new HashMap<>();

  public static void addStruct(String name, BiFunction<ArkArchive, ArkName, Struct> binary, BiFunction<JsonValue, ArkName, Struct> json) {
    TYPE_MAP.put(new ArkName(name), binary);
    TYPE_JSON_MAP.put(new ArkName(name), json);
  }

  static {
    addStruct("ItemNetID", StructPropertyList::new, StructPropertyList::new);
    addStruct("Transform", StructPropertyList::new, StructPropertyList::new);
    addStruct("PrimalPlayerDataStruct", StructPropertyList::new, StructPropertyList::new);
    addStruct("PrimalPlayerCharacterConfigStruct", StructPropertyList::new, StructPropertyList::new);
    addStruct("PrimalPersistentCharacterStatsStruct", StructPropertyList::new, StructPropertyList::new);
    addStruct("TribeData", StructPropertyList::new, StructPropertyList::new);
    addStruct("TribeGovernment", StructPropertyList::new, StructPropertyList::new);
    addStruct("TerrainInfo", StructPropertyList::new, StructPropertyList::new);
    addStruct("Vector", StructVector::new, StructVector::new);
    addStruct("Vector2D", StructVector2D::new, StructVector2D::new);
    addStruct("Quat", StructQuat::new, StructQuat::new);
    addStruct("Color", StructColor::new, StructColor::new);
    addStruct("LinearColor", StructLinearColor::new, StructLinearColor::new);
    addStruct("Rotator", StructVector::new, StructVector::new);
    addStruct("UniqueNetIdRepl", StructUniqueNetIdRepl::new, StructUniqueNetIdRepl::new);

    NAME_TYPE_MAP.put(new ArkName("CustomColors"), new ArkName("Color"));
    NAME_TYPE_MAP.put(new ArkName("CustomColours_60_7D3267C846B277953C0C41AEBD54FBCB"), new ArkName("LinearColor"));
  }

  public static ArkName mapArrayNameToTypeName(ArkName arrayName) {
    ArkName typeName = NAME_TYPE_MAP.getOrDefault(arrayName, null);
    if (typeName == null) {
      return null;
    } else {
      return typeName;
    }
  }

  public static Struct read(ArkArchive archive, ArkName structType) {
    if (TYPE_MAP.containsKey(structType)) {
      return TYPE_MAP.get(structType).apply(archive, structType);
    } else {
      System.err.println("Warning: Unknown Struct Type " + structType + " at " + Integer.toHexString(archive.position()) + " trying to read as StructPropertyList");
      return new StructPropertyList(archive, structType);
    }
  }

  public static Struct read(JsonValue o, ArkName structType) {
    if (TYPE_JSON_MAP.containsKey(structType)) {
      return TYPE_JSON_MAP.get(structType).apply(o, structType);
    } else {
      return new StructPropertyList(o, structType);
    }
  }

}
