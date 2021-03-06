package qowyn.ark.data;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;

import qowyn.ark.ArkArchive;
import qowyn.ark.NameSizeCalculator;

public class ExtraDataZero implements ExtraData {

  @Override
  public int calculateSize(NameSizeCalculator nameSizer) {
    return 4;
  }

  @Override
  public void writeJson(JsonGenerator generator) throws IOException {
    generator.writeNull();
  }

  @Override
  public void writeBinary(ArkArchive archive) {
    archive.putInt(0);
  }

}
