package qowyn.ark.structs;

import javax.json.Json;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;

import qowyn.ark.ArkArchive;
import qowyn.ark.types.ArkName;

public class StructUniqueNetIdRepl extends StructBase {

  private int unk;

  private String netId;

  public StructUniqueNetIdRepl(ArkArchive archive, ArkName structType) {
    super(structType);

    unk = archive.getInt();
    netId = archive.getString();
  }

  public StructUniqueNetIdRepl(JsonValue v, ArkName structType) {
    super(structType);
  }

  @Override
  public JsonValue toJson() {
    JsonObjectBuilder job = Json.createObjectBuilder();

    job.add("unk", unk);
    job.add("netId", netId);

    return job.build();
  }

  @Override
  public void write(ArkArchive archive) {
    archive.putInt(unk);
    archive.putString(netId);
  }

  @Override
  public int getSize(boolean nameTable) {
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
