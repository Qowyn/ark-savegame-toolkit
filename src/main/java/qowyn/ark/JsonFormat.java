package qowyn.ark;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;

public interface JsonFormat {

  public default void readJson(JsonNode node) {
    readJson(node, ReadingOptions.create());
  }

  public void readJson(JsonNode node, ReadingOptions options);

  public default void writeJson(JsonGenerator generator) throws IOException {
    writeJson(generator, WritingOptions.create());
  }

  public void writeJson(JsonGenerator generator, WritingOptions options) throws IOException;

}
