package qowyn.ark.structs;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;

import qowyn.ark.ArkArchive;
import qowyn.ark.NameSizeCalculator;

public class StructUniqueNetIdRepl extends StructBase {

  private int unk;

  private String netId;

  public StructUniqueNetIdRepl() {}

  public StructUniqueNetIdRepl(int unk, String netId) {
    this.unk = unk;
    this.netId = netId;
  }

  public StructUniqueNetIdRepl(ArkArchive archive) {
    unk = archive.getInt();
    netId = archive.getString();
  }

  public StructUniqueNetIdRepl(JsonNode node) {
    unk = node.path("unk").asInt();
    netId = node.path("netId").asText();
  }

  @Override
  public void writeJson(JsonGenerator generator) throws IOException {
    generator.writeStartObject();

    generator.writeNumberField("unk", unk);
    generator.writeStringField("netId", netId);

    generator.writeEndObject();
  }

  @Override
  public void writeBinary(ArkArchive archive) {
    archive.putInt(unk);
    archive.putString(netId);
  }

  @Override
  public int getSize(NameSizeCalculator nameSizer) {
    return Integer.BYTES + ArkArchive.getStringLength(netId);
  }

  public int getUnk() {
    return unk;
  }

  public void setUnk(int unk) {
    this.unk = unk;
  }

  public String getNetId() {
    return netId;
  }

  public void setNetId(String netId) {
    this.netId = netId;
  }

}
