package qowyn.ark.structs;

import javax.json.JsonValue;

import qowyn.ark.ArkArchive;
import qowyn.ark.NameContainer;
import qowyn.ark.types.ArkName;

public interface Struct extends NameContainer {

  /**
   * May be null if struct is contained in ArrayProperty
   * 
   * @return
   */
  public ArkName getStructType();

  public void setStructType(ArkName structType);

  public boolean isNative();

  public JsonValue toJson();

  public void write(ArkArchive archive);

  public int getSize(boolean nameTable);

}
