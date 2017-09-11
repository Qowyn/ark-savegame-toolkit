package qowyn.ark.arrays;

import java.io.IOException;
import java.util.List;

import com.fasterxml.jackson.core.JsonGenerator;

import qowyn.ark.ArkArchive;
import qowyn.ark.NameContainer;
import qowyn.ark.NameSizeCalculator;
import qowyn.ark.types.ArkName;

public interface ArkArray<T> extends List<T>, NameContainer {

  public Class<T> getValueClass();

  public ArkName getType();

  public int calculateSize(NameSizeCalculator nameSizer);

  public void writeJson(JsonGenerator generator) throws IOException;

  public void writeBinary(ArkArchive archive);

}
