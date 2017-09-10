package qowyn.ark;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonNumber;
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
import qowyn.ark.types.ListAppendingSet;
import qowyn.ark.types.ObjectReference;

public class ArkSavegame implements GameObjectContainer {

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

  public ArkSavegame(String fileName) throws IOException {
    this(fileName, new ReadingOptions());
  }

  public ArkSavegame(String fileName, ReadingOptions options) throws IOException {
    readBinary(fileName, options);
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

  public boolean hasUnknownNames() {
    return oldNameList != null;
  }

  public boolean hasUnknownData() {
    return hasUnknownData;
  }

  public void readBinary(String fileName, ReadingOptions options) throws IOException {
    Path filePath = Paths.get(fileName);
    try (FileChannel fc = FileChannel.open(filePath, StandardOpenOption.READ)) {
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
      ArkArchive archive = new ArkArchive(buffer, filePath);
      readBinary(archive, options);
    }
  }

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
      for (int n = 0; n < count; n++) {
        GameObject gameObject = new GameObject(archive);
        gameObject.setId(n);
        objects.add(gameObject);
      }
    } else {
      archive.unknownData();
      archive.unknownNames();
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
      hibernationEntries.add(new HibernationEntry(archive, options.getHibernationObjectProperties()));
    }
  }

  public void writeBinary(String fileName) throws FileNotFoundException, IOException {
    writeBinary(fileName, new WritingOptions());
  }

  public void writeBinary(String fileName, WritingOptions options) throws FileNotFoundException, IOException {
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

    try (FileChannel fc = FileChannel.open(Paths.get(fileName), StandardOpenOption.CREATE, StandardOpenOption.READ, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING)) {
      ByteBuffer buffer;

      if (options.usesMemoryMapping()) {
        buffer = fc.map(FileChannel.MapMode.READ_WRITE, 0, size);
      } else {
        buffer = ByteBuffer.allocateDirect(size);
      }

      ArkArchive archive = new ArkArchive(buffer);

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
    embeddedData.forEach(ed -> ed.write(archive));
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
      currentOffset = object.write(archive, currentOffset);
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
      ForkJoinPool pool = new ForkJoinPool(Runtime.getRuntime().availableProcessors(), ForkJoinPool.defaultForkJoinWorkerThreadFactory, null, true);

      final ThreadLocal<ArkArchive> sharedArchive = ThreadLocal.withInitial(archive::clone);

      pool.submit(() -> objects.parallelStream().forEach(o -> o.writeProperties(sharedArchive.get(), offset))).join();

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

  public void readJson(JsonObject object, ReadingOptions options) {
    readJsonHeader(object);
    readJsonDataFiles(object, options);
    readJsonEmbeddedData(object, options);
    readJsonDataFilesObjectMap(object, options);
    readJsonObjects(object, options);
    readJsonHibernatedObjects(object, options);
  }

  protected void readJsonHeader(JsonObject object) {
    saveVersion = (short) object.getInt("saveVersion");
    gameTime = object.getJsonNumber("gameTime").bigDecimalValue().floatValue();
    saveCount = object.getInt("saveCount", 0);

    if (object.containsKey("preservedNames")) {
      JsonArray preservedNames = object.getJsonArray("preservedNames");
      oldNameList = new ArrayList<>(preservedNames.size());
      preservedNames.getValuesAs(JsonString.class).forEach(s -> oldNameList.add(s.getString()));
    } else {
      oldNameList = null;
    }
  }

  protected void readJsonDataFiles(JsonObject object, ReadingOptions options) {
    dataFiles.clear();
    if (options.getDataFiles()) {
      JsonArray dataFilesArray = object.getJsonArray("dataFiles");
      if (dataFilesArray != null) {
        dataFiles.ensureCapacity(dataFilesArray.size());
        dataFilesArray.getValuesAs(JsonString.class).forEach(s -> dataFiles.add(s.getString()));
      }
    }
  }

  protected void readJsonEmbeddedData(JsonObject object, ReadingOptions options) {
    embeddedData.clear();
    if (options.getEmbeddedData()) {
      JsonArray embeddedDataArray = object.getJsonArray("embeddedData");
      if (embeddedDataArray != null) {
        embeddedData.ensureCapacity(embeddedDataArray.size());
        embeddedDataArray.getValuesAs(JsonObject.class).forEach(o -> embeddedData.add(new EmbeddedData(o)));
      }
    }
  }

  protected void readJsonDataFilesObjectMap(JsonObject object, ReadingOptions options) {
    dataFilesObjectMap.clear();
    if (options.getDataFilesObjectMap()) {
      JsonObject dataFilesObjectMapObject = object.getJsonObject("dataFilesObjectMap");
      if (dataFilesObjectMapObject != null) {
        dataFilesObjectMapObject.forEach((key, list) -> {
          List<JsonArray> namesListList = ((JsonArray) list).getValuesAs(JsonArray.class);
          List<String[]> objectNameList = new ArrayList<>(namesListList.size());
          for (JsonArray namesArray : namesListList) {
            List<JsonString> namesList = namesArray.getValuesAs(JsonString.class);
            String[] names = new String[namesList.size()];
            for (int index = 0; index < names.length; index++) {
              names[index] = namesList.get(index).getString();
            }
            objectNameList.add(names);
          }
          dataFilesObjectMap.put(Integer.valueOf(key), objectNameList);
        });
      }
    }
  }

  protected void readJsonObjects(JsonObject object, ReadingOptions options) {
    objects.clear();
    if (options.getGameObjects()) {
      JsonArray objectsArray = object.getJsonArray("objects");
      if (objectsArray != null) {
        objects.ensureCapacity(objectsArray.size());
        objectsArray.getValuesAs(JsonObject.class).forEach(o -> objects.add(new GameObject(o, options.getGameObjectProperties())));

        for (int i = 0; i < objects.size(); i++) {
          objects.get(i).setId(i);
        }
      }
    }
  }

  protected void readJsonHibernatedObjects(JsonObject object, ReadingOptions options) {
    hibernationClasses.clear();
    hibernationIndices.clear();
    hibernationEntries.clear();
    if (options.getHibernation()) {
      JsonObject hibernation = object.getJsonObject("hibernation");

      if (hibernation == null) {
        hibernationV8Unknown1 = 0;
        hibernationV8Unknown2 = 0;
        hibernationV8Unknown3 = 0;
        hibernationV8Unknown4 = 0;
        hibernationUnknown1 = 0;
        hibernationUnknown2 = 0;
        return;
      }

      hibernationV8Unknown1 = hibernation.getInt("v8Unknown1", 0);
      hibernationV8Unknown2 = hibernation.getInt("v8Unknown2", 0);
      hibernationV8Unknown3 = hibernation.getInt("v8Unknown3", 0);
      hibernationV8Unknown4 = hibernation.getInt("v8Unknown4", 0);
      hibernationUnknown1 = hibernation.getInt("unknown1", 0);
      hibernationUnknown2 = hibernation.getInt("unknown2", 0);

      JsonArray classesArray = hibernation.getJsonArray("classes");
      if (classesArray != null) {
        for (JsonString clazz: classesArray.getValuesAs(JsonString.class)) {
          hibernationClasses.add(clazz.getString());
        }
      }

      JsonArray indicesArray = hibernation.getJsonArray("indices");
      if (indicesArray != null) {
        for (JsonNumber index: indicesArray.getValuesAs(JsonNumber.class)) {
          hibernationIndices.add(index.intValue());
        }
      }

      JsonArray entriesArray = hibernation.getJsonArray("entries");
      if (entriesArray != null) {
        for (JsonObject hibernatedObject: entriesArray.getValuesAs(JsonObject.class)) {
          hibernationEntries.add(new HibernationEntry(hibernatedObject, options.getHibernationObjectProperties()));
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
  @SuppressWarnings("unchecked")
  public void writeJson(JsonGenerator generator, WritingOptions options) {
    generator.writeStartObject();

    generator.write("saveVersion", saveVersion);
    generator.write("gameTime", gameTime);

    generator.write("saveCount", saveCount);

    if (oldNameList != null && !oldNameList.isEmpty()) {
      generator.writeStartArray("preservedNames");

      oldNameList.forEach(generator::write);

      generator.writeEnd();
    }

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

    if (!dataFilesObjectMap.isEmpty()) {
      generator.writeStartObject("dataFilesObjectMap");

      for (Entry<Integer, List<String[]>> entry : dataFilesObjectMap.entrySet()) {
        generator.writeStartArray(entry.getKey().toString());
        for (String[] namesList : entry.getValue()) {
          generator.writeStartArray();
          for (String name : namesList) {
            generator.write(name);
          }
          generator.writeEnd();
        }
        generator.writeEnd();
      }

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

    generator.writeStartObject("hibernation");

    generator.write("v8Unknown1", hibernationV8Unknown1);
    generator.write("v8Unknown2", hibernationV8Unknown2);
    generator.write("v8Unknown3", hibernationV8Unknown3);
    generator.write("v8Unknown4", hibernationV8Unknown4);

    generator.write("unknown1", hibernationUnknown1);
    generator.write("unknown2", hibernationUnknown2);

    if (!hibernationClasses.isEmpty()) {
      generator.writeStartArray("classes");

      hibernationClasses.forEach(generator::write);

      generator.writeEnd();
    }

    if (!hibernationIndices.isEmpty()) {
      generator.writeStartArray("indices");

      hibernationIndices.forEach(generator::write);

      generator.writeEnd();
    }

    if (!hibernationEntries.isEmpty()) {
      generator.writeStartArray("entries");

      hibernationEntries.forEach(o -> generator.write(o.toJson()));

      generator.writeEnd();
    }

    generator.writeEnd();

    generator.writeEnd();
  }

  public JsonObject toJson() {
    JsonObjectBuilder builder = Json.createObjectBuilder();

    builder.add("saveVersion", saveVersion);
    builder.add("gameTime", gameTime);

    if (saveCount > 0) {
      builder.add("saveCount", saveCount);
    }

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

    if (!dataFilesObjectMap.isEmpty()) {
      JsonObjectBuilder objectMapBuilder = Json.createObjectBuilder();

      for (Entry<Integer, List<String[]>> entry : dataFilesObjectMap.entrySet()) {
        JsonArrayBuilder namesListListBuilder = Json.createArrayBuilder();
        for (String[] namesList : entry.getValue()) {
          JsonArrayBuilder namesListBuilder = Json.createArrayBuilder();
          for (String name : namesList) {
            namesListBuilder.add(name);
          }
          namesListListBuilder.add(namesListBuilder);
        }
        objectMapBuilder.add(entry.getKey().toString(), namesListListBuilder);
      }

      builder.add("dataFilesObjectMap", objectMapBuilder);
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
