package qowyn.ark.properties;

import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

import qowyn.ark.ArkArchive;
import qowyn.ark.NameCollector;
import qowyn.ark.NameSizeCalculator;
import qowyn.ark.structs.Struct;
import qowyn.ark.structs.StructRegistry;
import qowyn.ark.structs.StructUnknown;
import qowyn.ark.types.ArkName;

public class PropertyStruct extends PropertyBase<Struct> {

  public static final ArkName TYPE = ArkName.constantPlain("StructProperty");

  private ArkName structType;

  public PropertyStruct(String name, Struct value, ArkName structType) {
    super(ArkName.from(name), 0, value);
    this.structType = structType;
  }

  public PropertyStruct(String name, int index, Struct value, ArkName structType) {
    super(ArkName.from(name), index, value);
    this.structType = structType;
  }

  public PropertyStruct(ArkArchive archive, ArkName name) {
    super(archive, name);
    structType = archive.getName();

    int position = archive.position();
    try {
      value = StructRegistry.read(archive, structType);

      if (value == null) {
        throw new UnreadablePropertyException("StructRegistry returned null");
      }
    } catch (UnreadablePropertyException upe) {
      archive.position(position);

      value = new StructUnknown(archive, dataSize);

      archive.unknownNames();
      System.err.println("Reading StructProperty of type " + structType + " with name " + name + " as byte blob because:");
      upe.printStackTrace();
    }
  }

  public PropertyStruct(JsonObject o) {
    super(o);
    structType = ArkName.from(o.getString("structType"));

    if (o.containsKey("unknown")) {
      value = new StructUnknown(o.get("unknown"));
    } else {
      value = StructRegistry.read(o.get("value"), structType);
    }
  }

  @Override
  public Class<Struct> getValueClass() {
    return Struct.class;
  }

  @Override
  public ArkName getType() {
    return TYPE;
  }

  @Override
  protected void serializeValue(JsonObjectBuilder job) {
    job.add("structType", structType.toString());
    if (value instanceof StructUnknown) {
      job.add("unknown", value.toJson());
    } else {
      job.add("value", value.toJson());
    }
  }

  @Override
  protected void writeValue(ArkArchive archive) {
    archive.putName(structType);
    value.write(archive);
  }

  @Override
  protected int calculateAdditionalSize(NameSizeCalculator nameSizer) {
    return nameSizer.sizeOf(structType);
  }

  @Override
  public int calculateDataSize(NameSizeCalculator nameSizer) {
    return value.getSize(nameSizer);
  }

  @Override
  public void collectNames(NameCollector collector) {
    super.collectNames(collector);
    collector.accept(structType);
    value.collectNames(collector);
  }

  public ArkName getStructType() {
    return structType;
  }
  
  public void setStructType(ArkName structType) {
    this.structType = structType;
  }

}
