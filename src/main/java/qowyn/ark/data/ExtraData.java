package qowyn.ark.data;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;

import qowyn.ark.ArkArchive;
import qowyn.ark.NameSizeCalculator;

public interface ExtraData {

  public int calculateSize(NameSizeCalculator nameSizer);

  public void writeJson(JsonGenerator generator) throws IOException;

  public void writeBinary(ArkArchive archive);

}
