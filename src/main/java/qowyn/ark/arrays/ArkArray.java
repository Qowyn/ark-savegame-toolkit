package qowyn.ark.arrays;

import java.util.List;

import javax.json.JsonValue;

import qowyn.ark.ArkArchive;
import qowyn.ark.NameContainer;
import qowyn.ark.types.ArkName;

public interface ArkArray<T> extends List<T>, NameContainer {

  public Class<T> getValueClass();

  public ArkName getType();

  public int calculateSize(boolean nameTable);

  public JsonValue toJson();

  public void write(ArkArchive archive);

}
