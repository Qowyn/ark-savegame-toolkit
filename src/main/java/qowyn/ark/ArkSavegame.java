package qowyn.ark;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.IntStream;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;

import qowyn.ark.types.ArkName;
import qowyn.ark.types.EmbeddedData;
import qowyn.ark.types.ListAppendingSet;
import qowyn.ark.types.ObjectReference;

public class ArkSavegame extends FileFormatBase implements GameObjectContainerMixin {

  protected short saveVersion;

  protected int hibernationOffset;

  protected int nameTableOffset;

  protected int propertiesBlockOffset;

  /**
   * How often has this map-save been written
   */
  protected int saveCount;

  /**
   * How long has this map been running
   */
  protected float gameTime;

  protected final ArrayList<String> dataFiles = new ArrayList<>();

  protected final ArrayList<EmbeddedData> embeddedData = new ArrayList<>();

  protected final Map<Integer, List<String[]>> dataFilesObjectMap = new LinkedHashMap<>();

  protected final ArrayList<GameObject> objects = new ArrayList<>();

  protected final Map<Integer, Map<List<ArkName>, GameObject>> objectMap = new HashMap<>();

  protected int hibernationV8Unknown1;

  protected int hibernationV8Unknown2;

  protected int hibernationV8Unknown3;

  protected int hibernationV8Unknown4;

  protected int hibernationUnknown1;

  protected int hibernationUnknown2;

  protected final ArrayList<String> hibernationClasses = new ArrayList<>();

  protected final ArrayList<Integer> hibernationIndices = new ArrayList<>();

  protected final ArrayList<HibernationEntry> hibernationEntries = new ArrayList<>();

  protected List<String> oldNameList;

  protected boolean hasUnknownData;

  public ArkSavegame() {}

  public ArkSavegame(Path filePath) throws IOException {
    readBinary(filePath);
  }

  public ArkSavegame(Path filePath, ReadingOptions options) throws IOException {
    readBinary(filePath, options);
  }

  public ArkSavegame(JsonNode node) {
    readJson(node);
  }

  public ArkSavegame(JsonNode node, ReadingOptions options) {
    readJson(node, options);
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

  public ArrayList<String> getDataFiles() {
    return dataFiles;
  }

  public ArrayList<EmbeddedData> getEmbeddedData() {
    return embeddedData;
  }

  @Override
  public ArrayList<GameObject> getObjects() {
    return objects;
  }

  @Override
  public Map<Integer, Map<List<ArkName>, GameObject>> getObjectMap() {
    return objectMap;
  }

  public boolean hasUnknownNames() {
    return oldNameList != null;
  }

  public boolean hasUnknownData() {
    return hasUnknownData;
  }

  public Map<Integer, List<String[]>> getDataFilesObjectMap() {
    return dataFilesObjectMap;
  }

  public ArrayList<String> getHibernationClasses() {
    return hibernationClasses;
  }

  public ArrayList<Integer> getHibernationIndices() {
    return hibernationIndices;
  }

  public ArrayList<HibernationEntry> getHibernationEntries() {
    return hibernationEntries;
  }

  @Override
  public void readBinary(ArkArchive archive, ReadingOptions options) {
    readBinaryHeader(archive);

    if (saveVersion > 5) {
      // Name table is located after the objects block, but will be needed to read the objects block
      readBinaryNameTable(archive);
    }

    readBinaryDataFiles(archive, options);
    readBinaryEmbeddedData(archive, options);
    readBinaryDataFilesObjectMap(archive, options);
    readBinaryObjects(archive, options);
    readBinaryObjectProperties(archive, options);

    if (saveVersion > 6) {
      readBinaryHibernation(archive, options);
    }

    oldNameList = archive.hasUnknownNames() ? archive.getNameTable() : null;
    hasUnknownData = archive.hasUnknownData();
  }

  protected void readBinaryHeader(ArkArchive archive) {
    saveVersion = archive.getShort();

    if (saveVersion < 5 || saveVersion > 9) {
      throw new UnsupportedOperationException("Found unknown Version " + saveVersion);
    }

    if (saveVersion > 6) {
      hibernationOffset = archive.getInt();
      int shouldBeZero = archive.getInt();
      if (shouldBeZero != 0) {
        throw new UnsupportedOperationException("The stuff at this position should be zero: " + Integer.toHexString(archive.position() - 4));
      }
    } else {
      hibernationOffset = 0;
    }

    if (saveVersion > 5) {
      nameTableOffset = archive.getInt();
      propertiesBlockOffset = archive.getInt();
    } else {
      nameTableOffset = 0;
      propertiesBlockOffset = 0;
    }

    gameTime = archive.getFloat();

    if (saveVersion > 8) {
      saveCount = archive.getInt();
    } else {
      saveCount = 0;
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

    dataFiles.clear();
    if (options.getDataFiles()) {
      for (int n = 0; n < count; n++) {
        dataFiles.add(archive.getString());
      }
    } else {
      archive.unknownData();
      for (int n = 0; n < count; n++) {
        archive.skipString();
      }
    }
  }

  protected void readBinaryEmbeddedData(ArkArchive archive, ReadingOptions options) {
    int count = archive.getInt();

    embeddedData.clear();
    if (options.getEmbeddedData()) {
      for (int n = 0; n < count; n++) {
        embeddedData.add(new EmbeddedData(archive));
      }
    } else {
      archive.unknownData();
      for (int n = 0; n < count; n++) {
        EmbeddedData.skip(archive);
      }
    }
  }

  protected void readBinaryDataFilesObjectMap(ArkArchive archive, ReadingOptions options) {
    dataFilesObjectMap.clear();
    if (options.getDataFilesObjectMap()) {
      int dataFilesCount = archive.getInt();
      for (int n = 0; n < dataFilesCount; n++) {
        int level = archive.getInt();
        int count = archive.getInt();
        String[] names = new String[count];
        for (int index = 0; index < count; index++) {
          names[index] = archive.getString();
        }
        dataFilesObjectMap.computeIfAbsent(level, l -> new ArrayList<>()).add(names);
      }
    } else {
      archive.unknownData();
      int count = archive.getInt();
      for (int entry = 0; entry < count; entry++) {
        archive.skipBytes(4);
        int stringCount = archive.getInt();
        for (int stringIndex = 0; stringIndex < stringCount; stringIndex++) {
          archive.skipString();
        }
      }
    }
  }

  protected void readBinaryObjects(ArkArchive archive, ReadingOptions options) {
    if (options.getGameObjects()) {
      int count = archive.getInt();

      objects.clear();
      objectMap.clear();
      for (int n = 0; n < count; n++) {
        addObject(new GameObject(archive), options.getBuildComponentTree());
      }
    } else {
      archive.unknownData();
      archive.unknownNames();
    }
  }

  protected void readBinaryObjectProperties(ArkArchive archive, ReadingOptions options) {
    if (options.getGameObjects() && options.getGameObjectProperties()) {
      if (options.isParallel()) {
        ForkJoinPool pool = new ForkJoinPool(options.getThreadCount(), ForkJoinPool.defaultForkJoinWorkerThreadFactory, null, true);

        pool.submit(() -> {
          IntStream stream = IntStream.range(0, objects.size()).parallel();

          if (options.getObjectFilter() != null) {
            stream = stream.filter(n -> options.getObjectFilter().test(objects.get(n)));
          }

          stream.forEach(n -> readBinaryObjectPropertiesImpl(n, archive.clone()));
        }).join();

        pool.shutdown();
      } else {
        IntStream stream = IntStream.range(0, objects.size());

        if (options.getObjectFilter() != null) {
          stream = stream.filter(n -> options.getObjectFilter().test(objects.get(n)));
        }

        stream.forEach(n -> readBinaryObjectPropertiesImpl(n, archive));
      }

      if (options.getObjectFilter() != null) {
        archive.unknownData();
        archive.unknownNames();
      }
    } else {
      archive.unknownData();
      archive.unknownNames();
    }
  }

  protected void readBinaryObjectPropertiesImpl(int n, ArkArchive archive) {
    objects.get(n).loadProperties(archive, (n < objects.size() - 1) ? objects.get(n + 1) : null, propertiesBlockOffset);
  }

  protected void readBinaryHibernation(ArkArchive archive, ReadingOptions options) {
    if (!options.getHibernation()) {
      hibernationV8Unknown1 = 0;
      hibernationV8Unknown2 = 0;
      hibernationV8Unknown3 = 0;
      hibernationV8Unknown4 = 0;
      hibernationUnknown1 = 0;
      hibernationUnknown2 = 0;
      hibernationClasses.clear();
      hibernationIndices.clear();
      hibernationEntries.clear();
      archive.unknownData();
      return;
    }

    archive.position(hibernationOffset);

    if (saveVersion > 7) {
      hibernationV8Unknown1 = archive.getInt();
      hibernationV8Unknown2 = archive.getInt();
      hibernationV8Unknown3 = archive.getInt();
      hibernationV8Unknown4 = archive.getInt();
    }

    // No hibernate section if we reached the nameTable
    if (archive.position() == nameTableOffset) {
      return;
    }

    hibernationUnknown1 = archive.getInt();
    hibernationUnknown2 = archive.getInt();

    int hibernatedClassesCount = archive.getInt();

    hibernationClasses.clear();
    hibernationClasses.ensureCapacity(hibernatedClassesCount);
    for (int index = 0; index < hibernatedClassesCount; index++) {
      hibernationClasses.add(archive.getString());
    }

    int hibernatedIndicesCount = archive.getInt();

    if (hibernatedIndicesCount != hibernatedClassesCount) {
      archive.debugMessage("hibernatedClassesCount does not match hibernatedIndicesCount");
      throw new UnsupportedOperationException();
    }

    hibernationIndices.clear();
    hibernationIndices.ensureCapacity(hibernatedIndicesCount);
    for (int index = 0; index < hibernatedIndicesCount; index++) {
      hibernationIndices.add(archive.getInt());
    }

    int hibernatedObjectsCount = archive.getInt();

    hibernationEntries.clear();
    hibernationEntries.ensureCapacity(hibernatedObjectsCount);
    for (int index = 0; index < hibernatedObjectsCount; index++) {
      hibernationEntries.add(new HibernationEntry(archive, options));
    }
  }

  @Override
  public void writeBinary(Path filePath, WritingOptions options) throws IOException {
    // calculateHeaderSize checks for valid known versions
    NameSizeCalculator calculator = ArkArchive.getNameSizer(saveVersion > 5);

    int size = calculateHeaderSize();
    size += calculateDataFilesSize();
    size += calculateEmbeddedDataSize();
    size += calculateDataFilesObjectMapSize();
    size += calculateObjectsSize(calculator);

    if (saveVersion > 6) {
      hibernationOffset = size;
      size += calculateHibernationSize(calculator);
    }

    Set<String> nameTable;

    if (saveVersion > 5) {
      nameTableOffset = size;

      nameTable = oldNameList != null ? new ListAppendingSet<>(oldNameList) : new LinkedHashSet<>();
      NameCollector collector = name -> nameTable.add(name.getName());

      objects.forEach(o -> o.collectNames(collector));

      if (oldNameList != null) {
        size += 4 + ((ListAppendingSet<String>) nameTable).getList().stream().mapToInt(ArkArchive::getStringLength).sum();
      } else {
        size += 4 + nameTable.stream().mapToInt(ArkArchive::getStringLength).sum();
      }
    } else {
      nameTable = null;
    }

    propertiesBlockOffset = size;

    size += calculateObjectPropertiesSize(calculator);

    try (FileChannel fc = FileChannel.open(filePath, StandardOpenOption.CREATE, StandardOpenOption.READ, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING)) {
      ByteBuffer buffer;

      if (options.usesMemoryMapping()) {
        buffer = fc.map(FileChannel.MapMode.READ_WRITE, 0, size);
      } else {
        buffer = ByteBuffer.allocateDirect(size);
      }

      ArkArchive archive = new ArkArchive(buffer, filePath);

      if (nameTable != null) {
        archive.setNameTable(oldNameList != null ? ((ListAppendingSet<String>) nameTable).getList() : new ArrayList<>(nameTable));
      }

      writeBinaryHeader(archive);
      writeBinaryDataFiles(archive);
      writeBinaryEmbeddedData(archive);
      writeBinaryDataFilesObjectMap(archive);
      writeBinaryObjects(archive);

      if (saveVersion > 6) {
        writeBinaryHibernation(archive);
      }

      if (saveVersion > 5) {
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

    if (saveVersion > 6) {
      archive.putInt(hibernationOffset);
      archive.putInt(0);
    }

    if (saveVersion > 5) {
      archive.putInt(nameTableOffset);
      archive.putInt(propertiesBlockOffset);
    }

    archive.putFloat(gameTime);

    if (saveVersion > 8) {
      archive.putInt(saveCount);
    }
  }

  protected void writeBinaryDataFiles(ArkArchive archive) {
    archive.putInt(dataFiles.size());
    dataFiles.forEach(archive::putString);
  }

  protected void writeBinaryEmbeddedData(ArkArchive archive) {
    archive.putInt(embeddedData.size());
    embeddedData.forEach(ed -> ed.writeBinary(archive));
  }

  protected void writeBinaryDataFilesObjectMap(ArkArchive archive) {
    archive.putInt(dataFilesObjectMap.size());
    for (Integer key : dataFilesObjectMap.keySet()) {
      for (String[] namesList : dataFilesObjectMap.get(key)) {
        archive.putInt(key.intValue());
        archive.putInt(namesList.length);
        for (String name : namesList) {
          archive.putString(name);
        }
      }
    }
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
      currentOffset = object.writeBinary(archive, currentOffset);
    }
  }

  protected void writeBinaryHibernation(ArkArchive archive) {
    archive.position(hibernationOffset);
    if (saveVersion > 7) {
      archive.putInt(hibernationV8Unknown1);
      archive.putInt(hibernationV8Unknown2);
      archive.putInt(hibernationV8Unknown3);
      archive.putInt(hibernationV8Unknown4);
    }

    if (hibernationEntries.isEmpty()) {
      return;
    }

    archive.putInt(hibernationUnknown1);
    archive.putInt(hibernationUnknown2);

    archive.putInt(hibernationClasses.size());
    hibernationClasses.forEach(archive::putString);

    archive.putInt(hibernationIndices.size());
    hibernationIndices.forEach(archive::putInt);

    archive.putInt(hibernationEntries.size());
    for (HibernationEntry object: hibernationEntries) {
      object.writeBinary(archive);
    }
  }

  protected void writeNameTable(ArkArchive archive) {
    archive.position(nameTableOffset);
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
      ForkJoinPool pool = new ForkJoinPool(options.getThreadCount(), ForkJoinPool.defaultForkJoinWorkerThreadFactory, null, true);

      for (GameObject object: objects) {
        pool.submit(() -> {
          object.writeProperties(archive.clone(), offset);
        });
      }

      pool.shutdown();
    } else {
      objects.stream().forEach(o -> o.writeProperties(archive, offset));
    }
  }

  protected int calculateHeaderSize() {
    if (saveVersion < 5 || saveVersion > 9) {
      throw new UnsupportedOperationException("Version " + saveVersion + " is unknown and cannot be written in binary form");
    }

    // saveVersion + gameTime
    int size = Short.BYTES + Float.BYTES;

    if (saveVersion > 5) {
      // nameTableOffset + propertiesBlockOffset
      size += Integer.BYTES * 2;
    }
    if (saveVersion > 6) {
      // hibernationOffset + shouldBeZero
      size += Integer.BYTES * 2;
    }
    if (saveVersion > 8) {
      // saveCount
      size += Integer.BYTES;
    }

    return size;
  }

  protected int calculateDataFilesSize() {
    return 4 + dataFiles.stream().mapToInt(ArkArchive::getStringLength).sum();
  }

  protected int calculateEmbeddedDataSize() {
    return 4 + embeddedData.stream().mapToInt(EmbeddedData::getSize).sum();
  }

  protected int calculateDataFilesObjectMapSize() {
    int size = 4;
    for (List<String[]> namesListList : dataFilesObjectMap.values()) {
      size += namesListList.size() * 8;
      for (String[] namesList : namesListList) {
        size += Arrays.stream(namesList).mapToInt(ArkArchive::getStringLength).sum();
      }
    }
    return size;
  }

  protected int calculateObjectsSize(NameSizeCalculator nameSizer) {
    return 4 + objects.parallelStream().mapToInt(o -> o.getSize(nameSizer)).sum();
  }

  protected int calculateObjectPropertiesSize(NameSizeCalculator nameSizer) {
    return objects.parallelStream().mapToInt(o -> o.getPropertiesSize(nameSizer)).sum();
  }

  protected int calculateHibernationSize(NameSizeCalculator nameSizer) {
    int size = saveVersion > 7 ? Integer.BYTES * 4 : 0;

    if (hibernationEntries.size() > 0) {
      size += Integer.BYTES * (5 + hibernationIndices.size());
      size += hibernationClasses.stream().mapToInt(ArkArchive::getStringLength).sum();
      size += hibernationEntries.stream().mapToInt(HibernationEntry::getSizeAndCollectNames).sum();
    }

    return size;
  }

  @Override
  public void readJson(JsonNode node, ReadingOptions options) {
    readJsonHeader(node);
    readJsonDataFiles(node, options);
    readJsonEmbeddedData(node, options);
    readJsonDataFilesObjectMap(node, options);
    readJsonObjects(node, options);
    readJsonHibernatedObjects(node, options);
  }

  protected void readJsonHeader(JsonNode node) {
    saveVersion = (short) node.path("saveVersion").asInt();
    gameTime = (float) node.path("gameTime").asDouble();
    saveCount = node.path("saveCount").asInt();

    JsonNode preservedNames = node.path("preservedNames");
    if (!preservedNames.isNull()) {
      oldNameList = new ArrayList<>(preservedNames.size());
      preservedNames.forEach(name -> oldNameList.add(name.asText()));
    } else {
      oldNameList = null;
    }
  }

  protected void readJsonDataFiles(JsonNode node, ReadingOptions options) {
    dataFiles.clear();
    if (options.getDataFiles()) {
      JsonNode dataFilesArray = node.path("dataFiles");
      if (!dataFilesArray.isNull()) {
        dataFiles.ensureCapacity(dataFilesArray.size());
        dataFilesArray.forEach(dataFile -> dataFiles.add(dataFile.asText()));
      }
    }
  }

  protected void readJsonEmbeddedData(JsonNode node, ReadingOptions options) {
    embeddedData.clear();
    if (options.getEmbeddedData()) {
      JsonNode embeddedDataArray = node.path("embeddedData");
      if (!embeddedDataArray.isNull()) {
        embeddedData.ensureCapacity(embeddedDataArray.size());
        embeddedDataArray.forEach(data -> embeddedData.add(new EmbeddedData(data)));
      }
    }
  }

  protected void readJsonDataFilesObjectMap(JsonNode node, ReadingOptions options) {
    dataFilesObjectMap.clear();
    if (options.getDataFilesObjectMap()) {
      JsonNode dataFilesObjectMapObject = node.path("dataFilesObjectMap");
      if (!dataFilesObjectMapObject.isNull()) {
        dataFilesObjectMapObject.fields().forEachRemaining(entry -> {
          List<String[]> objectNameList = new ArrayList<>(entry.getValue().size());
          for (JsonNode namesArray : entry.getValue()) {
            String[] names = new String[namesArray.size()];
            for (int index = 0; index < names.length; index++) {
              names[index] = namesArray.get(index).asText();
            }
            objectNameList.add(names);
          }
          dataFilesObjectMap.put(Integer.valueOf(entry.getKey()), objectNameList);
        });
      }
    }
  }

  protected void readJsonObjects(JsonNode node, ReadingOptions options) {
    objects.clear();
    objectMap.clear();
    if (options.getGameObjects()) {
      JsonNode objectsArray = node.path("objects");
      if (!objectsArray.isNull()) {
        objects.ensureCapacity(objectsArray.size());
        for (int i = 0; i < objectsArray.size(); i++) {
          addObject(new GameObject(objectsArray.get(i), options.getGameObjectProperties()), options.getBuildComponentTree());
        }
      }
    }
  }

  protected void readJsonHibernatedObjects(JsonNode node, ReadingOptions options) {
    hibernationClasses.clear();
    hibernationIndices.clear();
    hibernationEntries.clear();
    if (options.getHibernation() && node.hasNonNull("hibernation")) {
      JsonNode hibernation = node.get("hibernation");

      hibernationV8Unknown1 = hibernation.path("v8Unknown1").asInt();
      hibernationV8Unknown2 = hibernation.path("v8Unknown2").asInt();
      hibernationV8Unknown3 = hibernation.path("v8Unknown3").asInt();
      hibernationV8Unknown4 = hibernation.path("v8Unknown4").asInt();
      hibernationUnknown1 = hibernation.path("unknown1").asInt();
      hibernationUnknown2 = hibernation.path("unknown2").asInt();

      JsonNode classesArray = hibernation.path("classes");
      if (!classesArray.isNull()) {
        for (JsonNode clazz: classesArray) {
          hibernationClasses.add(clazz.asText());
        }
      }

      JsonNode indicesArray = hibernation.path("indices");
      if (!indicesArray.isNull()) {
        for (JsonNode index: indicesArray) {
          hibernationIndices.add(index.asInt());
        }
      }

      JsonNode entriesArray = hibernation.path("entries");
      if (!entriesArray.isNull()) {
        for (JsonNode hibernatedObject: entriesArray) {
          hibernationEntries.add(new HibernationEntry(hibernatedObject, options));
        }
      }
    } else {
      hibernationV8Unknown1 = 0;
      hibernationV8Unknown2 = 0;
      hibernationV8Unknown3 = 0;
      hibernationV8Unknown4 = 0;
      hibernationUnknown1 = 0;
      hibernationUnknown2 = 0;
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
  @Override
  public void writeJson(JsonGenerator generator, WritingOptions options) throws IOException {
    generator.writeStartObject();

    generator.writeNumberField("saveVersion", saveVersion);
    generator.writeNumberField("gameTime", gameTime);

    generator.writeNumberField("saveCount", saveCount);

    if (oldNameList != null && !oldNameList.isEmpty()) {
      generator.writeArrayFieldStart("preservedNames");

      for (String oldName: oldNameList) {
        generator.writeString(oldName);
      }

      generator.writeEndArray();
    }

    if (!dataFiles.isEmpty()) {
      generator.writeArrayFieldStart("dataFiles");

      for (String dataFile: dataFiles) {
        generator.writeString(dataFile);
      }

      generator.writeEndArray();
    }

    if (!embeddedData.isEmpty()) {
      generator.writeArrayFieldStart("embeddedData");

      for (EmbeddedData data: embeddedData) {
        data.writeJson(generator);
      }

      generator.writeEndArray();
    }

    if (!dataFilesObjectMap.isEmpty()) {
      generator.writeObjectFieldStart("dataFilesObjectMap");

      for (Entry<Integer, List<String[]>> entry : dataFilesObjectMap.entrySet()) {
        generator.writeArrayFieldStart(entry.getKey().toString());
        for (String[] namesList : entry.getValue()) {
          generator.writeStartArray();
          for (String name : namesList) {
            generator.writeString(name);
          }
          generator.writeEndArray();
        }
        generator.writeEndArray();
      }

      generator.writeEndObject();
    }

    if (!objects.isEmpty()) {
      generator.writeArrayFieldStart("objects");

      for (GameObject object: objects) {
        object.writeJson(generator, true);
      }

      generator.writeEndArray();
    }

    generator.writeObjectFieldStart("hibernation");

    generator.writeNumberField("v8Unknown1", hibernationV8Unknown1);
    generator.writeNumberField("v8Unknown2", hibernationV8Unknown2);
    generator.writeNumberField("v8Unknown3", hibernationV8Unknown3);
    generator.writeNumberField("v8Unknown4", hibernationV8Unknown4);

    generator.writeNumberField("unknown1", hibernationUnknown1);
    generator.writeNumberField("unknown2", hibernationUnknown2);

    if (!hibernationClasses.isEmpty()) {
      generator.writeArrayFieldStart("classes");

      for (String hibernationClass: hibernationClasses) {
        generator.writeString(hibernationClass);
      }

      generator.writeEndArray();
    }

    if (!hibernationIndices.isEmpty()) {
      generator.writeArrayFieldStart("indices");

      for (int hibernationIndex: hibernationIndices) {
        generator.writeNumber(hibernationIndex);
      }

      generator.writeEndArray();
    }

    if (!hibernationEntries.isEmpty()) {
      generator.writeArrayFieldStart("entries");

      for (HibernationEntry hibernationEntry: hibernationEntries) {
        hibernationEntry.writeJson(generator);
      }

      generator.writeEndArray();
    }

    generator.writeEndObject();

    generator.writeEndObject();
  }

}
