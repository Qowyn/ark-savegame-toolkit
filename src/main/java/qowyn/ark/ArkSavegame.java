package qowyn.ark;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonString;
import javax.json.stream.JsonGenerator;

import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.YieldingWaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;

import qowyn.ark.disruptor.JsonObjectEvent;
import qowyn.ark.types.EmbeddedData;
import qowyn.ark.types.ObjectReference;

public class ArkSavegame implements GameObjectContainer {

  protected short saveVersion;

  protected int nameTableOffset;

  protected int propertiesBlockOffset;

  protected float gameTime;

  protected List<String> dataFiles = new ArrayList<>();

  protected List<EmbeddedData> embeddedData = new ArrayList<>();

  protected List<GameObject> objects = new ArrayList<>();

  public ArkSavegame() {}

  public ArkSavegame(String fileName) throws IOException {
    this(fileName, new ReadingOptions());
  }

  public ArkSavegame(String fileName, ReadingOptions options) throws IOException {
    try (FileChannel fc = FileChannel.open(Paths.get(fileName), StandardOpenOption.READ)) {
      if (fc.size() > Integer.MAX_VALUE) {
        throw new RuntimeException("Input file is too large.");
      }
      ByteBuffer buffer;
      if (options.usesMemoryMapping()) {
        buffer = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
      } else {
        buffer = ByteBuffer.allocateDirect((int) fc.size());
        int bytesRead = fc.read(buffer);
        int totalRead = bytesRead;
        while (bytesRead != -1 && totalRead < fc.size()) {
          bytesRead = fc.read(buffer);
          totalRead += bytesRead;
        }
        buffer.clear();
      }
      ArkArchive archive = new ArkArchive(buffer);
      readBinary(archive, options);
    }
  }

  public ArkSavegame(JsonObject object) {
    this(object, new ReadingOptions());
  }

  public ArkSavegame(JsonObject object, ReadingOptions options) {
    readJson(object, options);
  }

  public short getSaveVersion() {
    return saveVersion;
  }

  public void setSaveVersion(short saveVersion) {
    this.saveVersion = saveVersion;
  }

  public float getGameTime() {
    return gameTime;
  }

  public void setGameTime(float gameTime) {
    this.gameTime = gameTime;
  }

  public List<String> getDataFiles() {
    return dataFiles;
  }

  public void setDataFiles(List<String> dataFiles) {
    this.dataFiles = Objects.requireNonNull(dataFiles);
  }

  public List<EmbeddedData> getEmbeddedData() {
    return embeddedData;
  }

  public void setEmbeddedData(List<EmbeddedData> embeddedData) {
    this.embeddedData = Objects.requireNonNull(embeddedData);
  }

  @Override
  public List<GameObject> getObjects() {
    return objects;
  }

  public void setObjects(List<GameObject> objects) {
    this.objects = Objects.requireNonNull(objects);
  }

  public void readBinary(ArkArchive archive, ReadingOptions options) {
    readBinaryHeader(archive);

    if (saveVersion == 6) {
      // Name table is located after the objects block, but will be needed to read the objects block
      readBinaryNameTable(archive);
    }

    readBinaryDataFiles(archive, options);
    readBinaryEmbeddedData(archive, options);

    int unknownValue = archive.getInt();
    if (unknownValue != 0) {
      throw new UnsupportedOperationException("Found unexpected Value " + unknownValue + " at " + (archive.position() - 4));
    }

    readBinaryObjects(archive, options);
    readBinaryObjectProperties(archive, options);
  }

  protected void readBinaryHeader(ArkArchive archive) {
    saveVersion = archive.getShort();

    if (saveVersion == 5) {
      gameTime = archive.getFloat();

      propertiesBlockOffset = 0;
    } else if (saveVersion == 6) {
      nameTableOffset = archive.getInt();
      propertiesBlockOffset = archive.getInt();
      gameTime = archive.getFloat();
    } else {
      throw new UnsupportedOperationException("Found unknown Version " + saveVersion);
    }

  }

  protected void readBinaryNameTable(ArkArchive archive) {
    int position = archive.position();

    archive.position(nameTableOffset);

    int nameCount = archive.getInt();
    List<String> nameTable = new ArrayList<>(nameCount);
    for (int n = 0; n < nameCount; n++) {
      nameTable.add(archive.getString());
    }

    archive.setNameTable(nameTable);

    archive.position(position);
  }

  protected void readBinaryDataFiles(ArkArchive archive, ReadingOptions options) {
    int count = archive.getInt();

    if (options.getDataFiles()) {
      dataFiles.clear();
      for (int n = 0; n < count; n++) {
        dataFiles.add(archive.getString());
      }
    } else {
      for (int n = 0; n < count; n++) {
        archive.skipString();
      }
    }
  }

  protected void readBinaryEmbeddedData(ArkArchive archive, ReadingOptions options) {
    int count = archive.getInt();

    if (options.getEmbeddedData()) {
      embeddedData.clear();
      for (int n = 0; n < count; n++) {
        embeddedData.add(new EmbeddedData(archive));
      }
    } else {
      for (int n = 0; n < count; n++) {
        EmbeddedData.skip(archive);
      }
    }
  }

  protected void readBinaryObjects(ArkArchive archive, ReadingOptions options) {
    if (options.getGameObjects()) {
      int count = archive.getInt();

      objects.clear();
      for (int n = 0; n < count; n++) {
        GameObject gameObject = new GameObject(archive);
        gameObject.setId(n);
        objects.add(gameObject);
      }
    }
  }

  protected void readBinaryObjectProperties(ArkArchive archive, ReadingOptions options) {
    if (options.getGameObjects() && options.getGameObjectProperties()) {
      if (options.isParallel()) {
        ForkJoinPool pool = new ForkJoinPool(Runtime.getRuntime().availableProcessors(), ForkJoinPool.defaultForkJoinWorkerThreadFactory, null, true);

        pool.submit(() -> {
          IntStream stream = IntStream.range(0, objects.size()).parallel();

          if (options.getObjectFilter() != null) {
            stream = stream.filter(n -> options.getObjectFilter().test(objects.get(n)));
          }

          stream.forEach(n -> readBinaryObjectPropertiesImpl(n, new ArkArchive(archive)));
        }).join();

        pool.shutdown();
      } else {
        IntStream stream = IntStream.range(0, objects.size());

        if (options.getObjectFilter() != null) {
          stream = stream.filter(n -> options.getObjectFilter().test(objects.get(n)));
        }

        stream.forEach(n -> readBinaryObjectPropertiesImpl(n, archive));
      }
    }
  }

  protected void readBinaryObjectPropertiesImpl(int n, ArkArchive archive) {
    objects.get(n).loadProperties(archive, (n < objects.size() - 1) ? objects.get(n + 1) : null, propertiesBlockOffset);
  }

  public void writeBinary(String fileName) throws FileNotFoundException, IOException {
    writeBinary(fileName, new WritingOptions());
  }

  public void writeBinary(String fileName, WritingOptions options) throws FileNotFoundException, IOException {
    // calculateHeaderSize checks for valid known versions
    int size = calculateHeaderSize();
    size += calculateDataFilesSize();
    size += calculateEmbeddedDataSize();
    size += 4; // unknown field
    size += calculateObjectsSize(saveVersion == 6);

    Set<String> nameTable;

    if (saveVersion == 6) {
      nameTableOffset = size;

      nameTable = new LinkedHashSet<>();

      objects.forEach(o -> o.collectNames(nameTable));

      size += 4 + nameTable.stream().mapToInt(ArkArchive::getStringLength).sum();
    } else {
      nameTable = null;
    }

    propertiesBlockOffset = size;

    size += calculateObjectPropertiesSize(saveVersion == 6);

    try (FileChannel fc = FileChannel.open(Paths.get(fileName), StandardOpenOption.CREATE, StandardOpenOption.READ, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING)) {
      ByteBuffer buffer;

      if (options.usesMemoryMapping()) {
        buffer = fc.map(FileChannel.MapMode.READ_WRITE, 0, size);
      } else {
        buffer = ByteBuffer.allocateDirect(size);
      }

      ArkArchive archive = new ArkArchive(buffer);

      if (nameTable != null) {
        archive.setNameTable(new ArrayList<>(nameTable));
      }

      writeBinaryHeader(archive);
      writeBinaryDataFiles(archive);
      writeBinaryEmbeddedData(archive);
      archive.putInt(0); // Unknown Field
      writeBinaryObjects(archive);

      if (saveVersion == 6) {
        writeNameTable(archive);
      }

      writeBinaryProperties(archive, options);

      if (!options.usesMemoryMapping()) {
        buffer.clear();
        int bytesWritten = fc.write(buffer);
        int totalBytes = bytesWritten;
        while (totalBytes < buffer.capacity()) {
          bytesWritten = fc.write(buffer);
          totalBytes += bytesWritten;
        }
      }
    }

  }

  protected void writeBinaryHeader(ArkArchive archive) {
    archive.putShort(saveVersion);

    if (saveVersion == 6) {
      archive.putInt(nameTableOffset);
      archive.putInt(propertiesBlockOffset);
    }

    archive.putFloat(gameTime);
  }

  protected void writeBinaryDataFiles(ArkArchive archive) {
    archive.putInt(dataFiles.size());
    dataFiles.forEach(archive::putString);
  }

  protected void writeBinaryEmbeddedData(ArkArchive archive) {
    archive.putInt(embeddedData.size());
    embeddedData.forEach(ed -> ed.write(archive));
  }

  protected void writeBinaryObjects(ArkArchive archive) {
    archive.putInt(objects.size());

    int currentOffset;

    if (saveVersion == 5) {
      // Position of properties data is absolute
      currentOffset = propertiesBlockOffset;
    } else {
      // Position of properties data is relative to propertiesBlockOffset
      currentOffset = 0;
    }

    for (GameObject object : objects) {
      currentOffset = object.write(archive, currentOffset);
    }
  }

  protected void writeNameTable(ArkArchive archive) {
    List<String> nameTable = archive.getNameTable();

    archive.putInt(nameTable.size());
    nameTable.forEach(archive::putString);
  }

  protected void writeBinaryProperties(ArkArchive archive, WritingOptions options) {
    int offset;
    if (saveVersion == 5) {
      // Position of properties data is absolute
      offset = 0;
    } else {
      // Position of properties data is relative to propertiesBlockOffset
      offset = propertiesBlockOffset;
    }

    if (options.isParallel()) {
      ForkJoinPool pool = new ForkJoinPool(Runtime.getRuntime().availableProcessors(), ForkJoinPool.defaultForkJoinWorkerThreadFactory, null, true);

      final ThreadLocal<ArkArchive> sharedArchive = ThreadLocal.withInitial(() -> new ArkArchive(archive));

      pool.submit(() -> objects.parallelStream().forEach(o -> o.writeProperties(sharedArchive.get(), offset))).join();

      pool.shutdown();
    } else {
      objects.stream().forEach(o -> o.writeProperties(archive, offset));
    }
  }

  protected int calculateHeaderSize() {
    if (saveVersion == 5) {
      // saveVersion + gameTime
      return Short.BYTES + Float.BYTES;
    } else if (saveVersion == 6) {
      // saveVersion + nameTableOffset + propertiesBlockOffset + gameTime
      return Short.BYTES + Integer.BYTES * 2 + Float.BYTES;
    } else {
      throw new UnsupportedOperationException("Version " + saveVersion + " is unknown and cannot be written in binary form");
    }
  }

  protected int calculateDataFilesSize() {
    return 4 + dataFiles.parallelStream().mapToInt(ArkArchive::getStringLength).sum();
  }

  protected int calculateEmbeddedDataSize() {
    return 4 + embeddedData.parallelStream().mapToInt(EmbeddedData::getSize).sum();
  }

  protected int calculateObjectsSize(boolean nameTable) {
    return 4 + objects.parallelStream().mapToInt(o -> o.getSize(nameTable)).sum();
  }

  protected int calculateObjectPropertiesSize(boolean nameTable) {
    return objects.parallelStream().mapToInt(o -> o.getPropertiesSize(nameTable)).sum();
  }

  public void readJson(JsonObject object, ReadingOptions options) {
    readJsonHeader(object);
    readJsonDataFiles(object, options);
    readJsonEmbeddedData(object, options);
    readJsonObjects(object, options);
  }

  protected void readJsonHeader(JsonObject object) {
    saveVersion = (short) object.getInt("saveVersion");
    gameTime = object.getJsonNumber("gameTime").bigDecimalValue().floatValue();
  }

  protected void readJsonDataFiles(JsonObject object, ReadingOptions options) {
    if (options.getDataFiles()) {
      JsonArray dataFilesArray = object.getJsonArray("dataFiles");
      if (dataFilesArray != null) {
        dataFiles = new ArrayList<>(dataFilesArray.size());
        dataFilesArray.getValuesAs(JsonString.class).forEach(s -> dataFiles.add(s.getString()));
      }
    }
  }

  protected void readJsonEmbeddedData(JsonObject object, ReadingOptions options) {
    if (options.getEmbeddedData()) {
      JsonArray embeddedDataArray = object.getJsonArray("embeddedData");
      if (embeddedDataArray != null) {
        embeddedData = new ArrayList<>(embeddedDataArray.size());
        embeddedDataArray.getValuesAs(JsonObject.class).forEach(o -> embeddedData.add(new EmbeddedData(o)));
      }
    }
  }

  protected void readJsonObjects(JsonObject object, ReadingOptions options) {
    if (options.getGameObjects()) {
      JsonArray objectsArray = object.getJsonArray("objects");
      if (objectsArray != null) {
        objects = new ArrayList<>(objectsArray.size());
        objectsArray.getValuesAs(JsonObject.class).forEach(o -> objects.add(new GameObject(o, options.getGameObjectProperties())));

        for (int i = 0; i < objects.size(); i++) {
          objects.get(i).setId(i);
        }
      }
    }
  }

  /**
   * Writes this class as json using {@code generator}. This method is valid only in an array
   * context or in no context (see {@link JsonGenerator#writeStartObject()}. Requires the current
   * objects list to be correctly sorted, otherwise the written {@link ObjectReference
   * ObjectReferences} might be broken.
   * 
   * @param generator {@link JsonGenerator} to write with
   */
  @SuppressWarnings("unchecked")
  public void writeJson(JsonGenerator generator, WritingOptions options) {
    generator.writeStartObject();

    generator.write("saveVersion", saveVersion);
    generator.write("gameTime", gameTime);

    if (!dataFiles.isEmpty()) {
      generator.writeStartArray("dataFiles");

      dataFiles.forEach(generator::write);

      generator.writeEnd();
    }

    if (!embeddedData.isEmpty()) {
      generator.writeStartArray("embeddedData");

      embeddedData.forEach(ed -> generator.write(ed.toJson()));

      generator.writeEnd();
    }

    if (!objects.isEmpty()) {
      generator.writeStartArray("objects");

      if (options.isAsynchronous()) {
        int bufferSize = options.getAsyncBufferSize();

        Disruptor<JsonObjectEvent> disruptor = new Disruptor<>(JsonObjectEvent::new, bufferSize, Executors.defaultThreadFactory(), ProducerType.SINGLE, new YieldingWaitStrategy());

        disruptor.handleEventsWith((event, sequence, endOfBatch) -> generator.write(event.get()));

        disruptor.start();

        RingBuffer<JsonObjectEvent> ringBuffer = disruptor.getRingBuffer();

        objects.forEach(o -> ringBuffer.publishEvent((event, sequence) -> event.set(o.toJson(true))));

        disruptor.shutdown();
      } else {
        objects.forEach(o -> generator.write(o.toJson()));
      }

      generator.writeEnd();
    }

    generator.writeEnd();
  }

  public JsonObject toJson() {
    JsonObjectBuilder builder = Json.createObjectBuilder();

    builder.add("saveVersion", saveVersion);
    builder.add("gameTime", gameTime);

    if (!dataFiles.isEmpty()) {
      JsonArrayBuilder dataFilesBuilder = Json.createArrayBuilder();

      dataFiles.forEach(dataFilesBuilder::add);

      builder.add("dataFiles", dataFilesBuilder);
    }

    if (!embeddedData.isEmpty()) {
      JsonArrayBuilder embeddedDataBuilder = Json.createArrayBuilder();

      embeddedData.forEach(ed -> embeddedDataBuilder.add(ed.toJson()));

      builder.add("embeddedData", embeddedDataBuilder);
    }

    if (!objects.isEmpty()) {
      JsonArrayBuilder objectsBuilder = Json.createArrayBuilder();

      List<JsonObject> objectsList = objects.parallelStream()
          .sorted(Comparator.comparing(GameObject::getId))
          .map(GameObject::toJson)
          .collect(Collectors.toList());

      objectsList.forEach(objectsBuilder::add);

      builder.add("objects", objectsBuilder);
    }

    return builder.build();
  }

}
