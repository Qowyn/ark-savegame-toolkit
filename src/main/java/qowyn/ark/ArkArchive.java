package qowyn.ark;

import java.io.PrintStream;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import qowyn.ark.types.ArkName;

/**
 * Class used to read primitives from and write primitives to a ByteBuffer.
 * 
 * This class is not thread-safe.
 * 
 * @author Roland Firmont
 *
 */
public class ArkArchive {

  private final Path sourceFile;

  private final int totalOffset;

  private ByteBuffer mbb;

  private List<String> nameTable;

  private Map<String, Integer> nameMap;

  private int nameOffset;

  private boolean instanceInTable;

  private final ArkArchiveState state;

  public static PrintStream debugOutput = System.err;

  public static boolean reportLargeStrings = false;

  private static final int BUFFER_LENGTH = 4096;

  private final char[] smallCharBuffer = new char[BUFFER_LENGTH];

  private final byte[] smallByteBuffer = new byte[BUFFER_LENGTH];

  private boolean useNameTable = true;

  private final boolean isSlice;

  public ArkArchive(ByteBuffer mbb, Path sourceFile) {
    this.sourceFile = sourceFile;
    this.mbb = mbb.order(ByteOrder.LITTLE_ENDIAN);
    this.state = new ArkArchiveState();
    this.isSlice = false;
    this.totalOffset = 0;
  }

  public ArkArchive(ByteBuffer mbb) {
    this.sourceFile = null;
    this.mbb = mbb.order(ByteOrder.LITTLE_ENDIAN);
    this.state = new ArkArchiveState();
    this.isSlice = false;
    this.totalOffset = 0;
  }

  protected ArkArchive(ArkArchive toClone) {
    this.sourceFile = toClone.sourceFile;
    this.mbb = toClone.mbb.duplicate().order(ByteOrder.LITTLE_ENDIAN);
    this.nameTable = toClone.nameTable;
    this.nameMap = toClone.nameMap;
    this.nameOffset = toClone.nameOffset;
    this.instanceInTable = toClone.instanceInTable;
    this.state = toClone.state;
    this.isSlice = toClone.isSlice;
    this.totalOffset = toClone.totalOffset;
  }

  protected ArkArchive(ArkArchive toClone, int size) {
    int oldLimit = toClone.mbb.limit();

    if (toClone.mbb.position() + size > oldLimit) {
      toClone.debugMessage(LoggerHelper.format("Requesting %d bytes with only %d bytes available", size, oldLimit - toClone.mbb.position()));
      throw new BufferUnderflowException();
    }

    toClone.mbb.limit(toClone.mbb.position() + size);

    this.sourceFile = toClone.sourceFile;
    this.mbb = toClone.mbb.slice().order(ByteOrder.LITTLE_ENDIAN);
    this.state = toClone.state;
    this.isSlice = true;
    this.totalOffset = toClone.totalOffset + toClone.mbb.position();

    toClone.mbb.limit(oldLimit).position(toClone.mbb.position() + size);
  }

  public List<String> getNameTable() {
    return nameTable;
  }

  public void setNameTable(List<String> nameTable) {
    setNameTable(nameTable, 1, false);
  }

  public void setNameTable(List<String> nameTable, int offset, boolean instanceInTable) {
    if (nameTable != null) {
      this.nameTable = Collections.unmodifiableList(new ArrayList<>(nameTable));
      this.nameOffset = offset;
      this.instanceInTable = instanceInTable;
      Map<String, Integer> nameMapBuilder = new HashMap<>();

      int index = offset;
      for (String name : nameTable) {
        nameMapBuilder.put(name, index++);
      }

      this.nameMap = Collections.unmodifiableMap(nameMapBuilder);
    } else {
      this.nameTable = null;
      this.nameMap = null;
    }
  }

  public boolean hasNameTable() {
    return nameTable != null;
  }

  public boolean hasInstanceInNameTable() {
    return instanceInTable;
  }

  protected ArkName getNameFromTable() {
    int id = mbb.getInt();
    int internalId = id - nameOffset;

    if (internalId < 0 || internalId >= nameTable.size()) {
      debugMessage(LoggerHelper.format("Found invalid nametable index %d (%d)", id, internalId), -4);
      return null;
    }

    String name = nameTable.get(internalId);
    if (instanceInTable) {
      return ArkName.from(name);
    } else {
      int instance = mbb.getInt();

      // Get or create ArkName
      return ArkName.from(name, instance);
    }
  }

  public String getString() {
    int size = mbb.getInt();

    if (size == 0) {
      return null;
    }

    if (size == 1) {
      mbb.position(mbb.position() + 1);
      return "";
    }

    if (size == -1) {
      mbb.position(mbb.position() + 2);
      return "";
    }

    boolean multibyte = size < 0;
    int absSize = Math.abs(size);
    int readSize = multibyte ? absSize * 2 : absSize;

    if (readSize + mbb.position() > mbb.limit()) {
      debugMessage(LoggerHelper.format("Trying to read %d bytes with just %d bytes left", readSize, mbb.limit() - mbb.position()));
      throw new BufferUnderflowException();
    }

    boolean isLarge = absSize > BUFFER_LENGTH;

    if (isLarge && reportLargeStrings) {
      debugMessage(LoggerHelper.format("String (%d) larger than internal Buffer (%d)", absSize, BUFFER_LENGTH));
    }

    if (multibyte) {
      char[] buffer = isLarge ? new char[absSize] : smallCharBuffer;
      mbb.asCharBuffer().get(buffer, 0, absSize);
      String result = new String(buffer, 0, absSize - 1);

      mbb.position(mbb.position() + absSize * 2);

      return result;
    } else {
      byte[] buffer = isLarge ? new byte[absSize] : smallByteBuffer;
      mbb.get(buffer, 0, absSize);

      return new String(buffer, 0, absSize - 1, StandardCharsets.US_ASCII);
    }
  }

  public ArkName getName() {
    if (!hasNameTable() || !useNameTable) {
      return ArkName.from(getString());
    } else {
      return getNameFromTable();
    }
  }

  public void skipString() {
    int size = mbb.getInt();

    boolean multibyte = size < 0;
    int absSize = Math.abs(size);
    int readSize = multibyte ? absSize * 2 : absSize;

    if (readSize + mbb.position() > mbb.limit()) {
      debugMessage(LoggerHelper.format("Trying to skip %d bytes with just %d bytes left", readSize, mbb.limit() - mbb.position()));
      throw new BufferUnderflowException();
    }

    if (absSize > BUFFER_LENGTH && reportLargeStrings) {
      debugMessage(LoggerHelper.format("String (%d) larger than internal Buffer (%d)", absSize, BUFFER_LENGTH));
    }

    mbb.position(mbb.position() + readSize);
  }

  public void skipBytes(int count) {
    if (count + mbb.position() > mbb.limit()) {
      debugMessage(LoggerHelper.format("Trying to skip %d bytes with just %d bytes left", count, mbb.limit() - mbb.position()));
      throw new BufferUnderflowException();
    } else if (count + mbb.position() < 0) {
      debugMessage(LoggerHelper.format("Trying to unskip %d bytes with just %d bytes left", count, mbb.position()));
      throw new BufferUnderflowException();
    }

    mbb.position(mbb.position() + count);
  }

  public int getInt() {
    return mbb.getInt();
  }

  public short getShort() {
    return mbb.getShort();
  }

  public int position() {
    return mbb.position();
  }

  public void position(int position) {
    mbb.position(position);
  }

  public int limit() {
    return mbb.limit();
  }

  public float getFloat() {
    return mbb.getFloat();
  }

  public long getLong() {
    return mbb.getLong();
  }

  public double getDouble() {
    return mbb.getDouble();
  }

  public byte getByte() {
    return mbb.get();
  }

  public byte[] getBytes(int length) {
    byte[] ret = new byte[length];

    mbb.get(ret);

    return ret;
  }

  public void getBytes(byte[] bytes) {
    mbb.get(bytes);
  }

  public void getBytes(byte[] bytes, int offset, int length) {
    mbb.get(bytes, offset, length);
  }

  public boolean getBoolean() {
    int val = mbb.getInt();
    if (val < 0 || val > 1) {
      debugMessage(LoggerHelper.format("Boolean with value %d, returning true", val), -4);
    }
    return val != 0;
  }

  /**
   * Writes the index of name in nameTable into the ArkArchive.
   * 
   * Writes the nameIndex of name into the ArkArchive.
   * 
   * Ensures name is in the current nameTable.
   * 
   * @param name
   */
  protected void putNameIntoTable(ArkName name) {
    if (instanceInTable) {
      Integer index = nameMap.get(name.toString());

      if (index == null) {
        throw new UnsupportedOperationException("Uncollected Name: " + name);
      }

      mbb.putInt(index);
    } else {
      Integer index = nameMap.get(name.getName());

      if (index == null) {
        throw new UnsupportedOperationException("Uncollected Name: " + name.getName());
      }

      mbb.putInt(index);
      mbb.putInt(name.getInstance());
    }
  }

  /**
   * Writes string to the archive If string contains non-ascii chars a multibyte string will be
   * written
   * 
   * @param string
   */
  public void putString(String string) {
    if (string == null) {
      mbb.putInt(0);
      return;
    }

    if (string.isEmpty()) {
      mbb.putInt(1);
      mbb.put((byte) 0);
      return;
    }

    int length = string.length() + 1;
    boolean multibyte = !isAscii(string);

    if (!multibyte) {
      mbb.putInt(length);
      mbb.put(string.getBytes(StandardCharsets.US_ASCII));
      mbb.put((byte) 0);
    } else {
      mbb.putInt(-length);
      mbb.asCharBuffer().put(string);
      mbb.position(mbb.position() + length * 2 - 2);
      mbb.putShort((short) 0);
    }
  }

  /**
   * Writes name to the archive.
   * 
   * @param name
   */
  public void putName(ArkName name) {
    if (hasNameTable() && useNameTable) {
      putNameIntoTable(name);
    } else {
      putString(name.toString());
    }
  }

  public void putLong(long value) {
    mbb.putLong(value);
  }

  public void putInt(int value) {
    mbb.putInt(value);
  }

  public void putShort(short value) {
    mbb.putShort(value);
  }

  public void putByte(byte value) {
    mbb.put(value);
  }

  public void putDouble(double value) {
    mbb.putDouble(value);
  }

  public void putFloat(float value) {
    mbb.putFloat(value);
  }

  /**
   * Writes a boolean as an int
   * 
   * @param value
   */
  public void putBoolean(boolean value) {
    mbb.putInt(value ? 1 : 0);
  }

  /**
   * Writes {@code value} directly to the archive.
   * 
   * @param value The data to write
   */
  public void putBytes(byte[] value) {
    mbb.put(value);
  }

  /**
   * Writes {@code value} directly to the archive.
   * 
   * @param value The data to write
   */
  public void putBytes(byte[] value, int offset, int length) {
    mbb.put(value, offset, length);
  }

  /**
   * Indicates that some data couldn't be read.
   * 
   * @return true if some data has been lost
   */
  public boolean hasUnknownData() {
    return state.unknownData;
  }

  /**
   * Set the unknownData flag to true
   */
  public void unknownData() {
    state.unknownData = true;
  }

  /**
   * Indicates that there might be unknown references to some names. If the current file has to be
   * written back to disk this should be considered by keeping all old names and adding new names to
   * the end of the list.
   * 
   * @return true if there are unknown names
   */
  public boolean hasUnknownNames() {
    return state.unknownNames;
  }

  /**
   * Set the unknownNames flag to true, except for slices.
   * Slices have either their own nameTable or no nameTable at all.
   */
  public void unknownNames() {
    if (isSlice) {
      state.unknownData = true;
    } else {
      state.unknownNames = true;
    }
  }

  /**
   * Enable or disable the current nameTable
   * @param use
   */
  public void setUseNameTable(boolean use) {
    this.useNameTable = use;
  }

  /**
   * Returns true if the current nameTable will be used to read ArkName values
   * @return
   */
  public boolean useNameTable() {
    return useNameTable;
  }

  public ArkArchive clone() {
    return new ArkArchive(this);
  }

  public ArkArchive slice(int size) {
    return new ArkArchive(this, size);
  }

  public int getTotalOffset() {
    return totalOffset;
  }

  public int getTotalPosition() {
    return totalOffset + mbb.position();
  }

  /**
   * Debug utility
   */
  public void debugMessage() {
    debugMessage("Current position", 0);
  }

  /**
   * Debug utility
   */
  public void debugMessage(String message) {
    debugMessage(message, 0);
  }

  public void debugMessage(String message, int offset) {
    if (debugOutput == null) {
      return;
    }

    debugOutput.print(message);
    debugOutput.print(" at 0x");
    debugOutput.print(Integer.toHexString(mbb.position() + offset));
    if (totalOffset > 0) {
      debugOutput.print(" (0x");
      debugOutput.print(Integer.toHexString(getTotalPosition() + offset));
      debugOutput.print(")");
    }
    if (sourceFile != null) {
      debugOutput.print(" in ");
      debugOutput.print(sourceFile);
    }
    debugOutput.println();
  }

  /**
   * Debug utility
   */
  public void debugMessage(Supplier<String> messageSupplier) {
    if (debugOutput == null) {
      return;
    }

    debugMessage(messageSupplier.get(), 0);
  }

  /**
   * Debug utility
   */
  public void debugMessage(Supplier<String> messageSupplier, int offset) {
    if (debugOutput == null) {
      return;
    }

    debugMessage(messageSupplier.get(), offset);
  }

  public NameSizeCalculator getNameSizer() {
    return getNameSizer(nameTable != null && useNameTable, instanceInTable);
  }

  /**
   * Determines how many bytes {@code value} will need if written to disk.
   * 
   * @param value The {@link String} to get the size of
   * @return The amount of bytes needed to store {@code value}
   */
  public static int getStringLength(String value) {
    if (value == null) {
      return 4;
    }
    if (value.isEmpty()) {
      return 5;
    }
    int length = value.length() + 1;
    boolean multibyte = !isAscii(value);

    return (multibyte ? length * 2 : length) + 4;
  }

  private static final NameSizeCalculator TABLE_WITH_INSTANCE = name -> 4;

  private static final NameSizeCalculator TABLE_WITHOUT_INSTANCE = name -> 8;

  private static final NameSizeCalculator WITHOUT_TABLE = name -> ArkArchive.getStringLength(name.toString());

  public static NameSizeCalculator getNameSizer(boolean nameTable) {
    return getNameSizer(nameTable, false);
  }

  public static NameSizeCalculator getNameSizer(boolean nameTable, boolean instanceInTable) {
    if (nameTable) {
      if (instanceInTable) {
        return TABLE_WITH_INSTANCE;
      } else {
        return TABLE_WITHOUT_INSTANCE;
      }
    } else {
      return WITHOUT_TABLE;
    }
  }

  private static boolean isAscii(String value) {
    for (int i = 0; i < value.length(); i++) {
      if (value.charAt(i) > '\u007f') {
        return false;
      }
    }
    return true;
  }

}
