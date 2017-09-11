package qowyn.ark.structs;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;

import qowyn.ark.ArkArchive;
import qowyn.ark.NameContainer;
import qowyn.ark.NameSizeCalculator;

public interface Struct extends NameContainer {

  public boolean isNative();

  public void writeJson(JsonGenerator generator) throws IOException;

  public void writeBinary(ArkArchive archive);

  public int getSize(NameSizeCalculator nameSizer);

}
