package qowyn.ark.arrays;

import java.util.List;

import javax.json.JsonArray;

import qowyn.ark.ArkArchive;
import qowyn.ark.NameContainer;

public interface ArkArray<T> extends List<T>, NameContainer {

  public Class<T> getValueClass();

  public int calculateSize(boolean nameTable);

  public JsonArray toJson();

  public void write(ArkArchive archive);

}
