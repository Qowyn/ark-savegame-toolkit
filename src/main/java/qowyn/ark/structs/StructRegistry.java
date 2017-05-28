package qowyn.ark.structs;

import java.util.HashMap;
import java.util.Map;

import javax.json.JsonValue;

import qowyn.ark.ArkArchive;
import qowyn.ark.properties.UnreadablePropertyException;
import qowyn.ark.types.ArkName;

public class StructRegistry {

  public static final Map<ArkName, StructBinaryConstructor> TYPE_MAP = new HashMap<>();

  public static final Map<ArkName, StructJsonConstructor> TYPE_JSON_MAP = new HashMap<>();

  public static final Map<ArkName, ArkName> NAME_TYPE_MAP = new HashMap<>();

  public static void addStruct(String name, StructBinaryConstructor binary, StructJsonConstructor json) {
    TYPE_MAP.put(ArkName.constantPlain(name), binary);
    TYPE_JSON_MAP.put(ArkName.constantPlain(name), json);
  }

  static {
    addStruct("Vector", StructVector::new, StructVector::new);
    addStruct("Vector2D", StructVector2D::new, StructVector2D::new);
    addStruct("Quat", StructQuat::new, StructQuat::new);
    addStruct("Color", StructColor::new, StructColor::new);
    addStruct("LinearColor", StructLinearColor::new, StructLinearColor::new);
    addStruct("Rotator", StructVector::new, StructVector::new);
    addStruct("UniqueNetIdRepl", StructUniqueNetIdRepl::new, StructUniqueNetIdRepl::new);

    NAME_TYPE_MAP.put(ArkName.constantPlain("CustomColors"), ArkName.constantPlain("Color"));
    NAME_TYPE_MAP.put(ArkName.constantPlain("CustomColours_60_7D3267C846B277953C0C41AEBD54FBCB"), ArkName.constantPlain("LinearColor"));
  }

  public static ArkName mapArrayNameToTypeName(ArkName arrayName) {
    return NAME_TYPE_MAP.getOrDefault(arrayName, null);
  }

  public static Struct read(ArkArchive archive, ArkName structType) {
    if (TYPE_MAP.containsKey(structType)) {
      return TYPE_MAP.get(structType).apply(archive);
    } else {
      try {
        return new StructPropertyList(archive);
      } catch (UnreadablePropertyException upe) {
        throw new UnreadablePropertyException("Unknown Struct Type " + structType + " at " + Integer.toHexString(archive.position()) + " failed to read as StructPropertyList", upe);
      }
    }
  }

  public static Struct read(JsonValue o, ArkName structType) {
    if (TYPE_JSON_MAP.containsKey(structType)) {
      return TYPE_JSON_MAP.get(structType).apply(o);
    } else {
      try {
        return new StructPropertyList(o);
      } catch (UnreadablePropertyException upe) {
        throw new UnreadablePropertyException("Unknown Struct Type " + structType + " failed to read as StructPropertyList", upe);
      }
    }
  }

}
