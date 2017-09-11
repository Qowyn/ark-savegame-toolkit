package qowyn.ark.data;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;

import qowyn.ark.ArkArchive;
import qowyn.ark.NameSizeCalculator;

public class ExtraDataBlob implements ExtraData {

  private byte[] data;

  public byte[] getData() {
    return data;
  }

  public void setData(byte[] data) {
    this.data = data;
  }

  @Override
  public int calculateSize(NameSizeCalculator nameSizer) {
    return data != null ? data.length : 0;
  }

  @Override
  public void writeJson(JsonGenerator generator) throws IOException {
    generator.writeBinary(data);
  }

  @Override
  public void writeBinary(ArkArchive archive) {
    if (data != null) {
      archive.putBytes(data);
    }
  }



}
