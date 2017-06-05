package qowyn.ark;

import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import qowyn.ark.types.ArkName;

public class ArkArchive {

  private ByteBuffer mbb;

  private List<String> nameTable;

  private Map<String, Integer> nameMap;

  private final ArkArchiveState state;

  private static final Logger LOGGER = Logger.getLogger(ArkArchive.class.getName());

  private static final int BUFFER_LENGTH = 4096;

  private final char[] smallCharBuffer = new char[BUFFER_LENGTH];

  private final byte[] smallByteBuffer = new byte[BUFFER_LENGTH];

  public ArkArchive(ByteBuffer mbb) {
    this.mbb = mbb.order(ByteOrder.LITTLE_ENDIAN);
    this.state = new ArkArchiveState();
  }

  public ArkArchive(ArkArchive toClone) {
    this.mbb = toClone.mbb.duplicate().order(ByteOrder.LITTLE_ENDIAN);
    this.nameTable = toClone.nameTable;
    this.nameMap = toClone.nameMap;
    this.state = toClone.state;
  }

  public List<String> getNameTable() {
    return nameTable;
  }

  public void setNameTable(List<String> nameTable) {
    if (nameTable != null) {
      this.nameTable = Collections.unmodifiableList(new ArrayList<>(nameTable));
      Map<String, Integer> nameMapBuilder = new HashMap<>();

      int index = 0;
      for (String name : nameTable) {
        nameMapBuilder.put(name, ++index);
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

  protected ArkName getNameFromTable() {
    int id = mbb.getInt();

    if (id < 1 || id > nameTable.size()) {
      System.err.println("Found invalid nametable index " + id + " at " + Integer.toHexString(mbb.position() - 4));
      return null;
    }

    String name = nameTable.get(id - 1);
    int instance = mbb.getInt();

    // Get or create ArkName
    return ArkName.from(name, instance);
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
      LOGGER.log(Level.SEVERE, LoggerHelper.format("Trying to read %d bytes at %h with just %d bytes left", readSize, mbb.position() - 4, mbb.limit() - mbb.position()));
      throw new BufferOverflowException();
    }

    boolean isLarge = absSize > BUFFER_LENGTH;

    if (isLarge) {
      LOGGER.log(Level.INFO, LoggerHelper.format("Large String (%d) at %h", absSize, mbb.position() - 4));
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
    if (!hasNameTable()) {
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

    if (absSize > 10000) {
      LOGGER.log(Level.INFO, LoggerHelper.format("Large String (%d) at %h", absSize, mbb.position() - 4));
    }

    if (readSize + mbb.position() > mbb.limit()) {
      LOGGER.log(Level.SEVERE, LoggerHelper.format("Trying to skip %d bytes at %h with just %d bytes left", readSize, mbb.position() - 4, mbb.limit() - mbb.position()));
      throw new BufferOverflowException();
    }

    mbb.position(mbb.position() + readSize);
  }

  public void skipBytes(int count) {
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
      LOGGER.log(Level.INFO, LoggerHelper.format("Boolean at %h with value %d, returning true", mbb.position() - 4, val));
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
    // index is 1 based
    int index = nameMap.getOrDefault(name.getName(), 0);

    if (index == 0) {
      throw new UnsupportedOperationException("Uncollected Name: " + name.getName());
    }

    mbb.putInt(index);
    mbb.putInt(name.getInstance());
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
    if (hasNameTable()) {
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
   * Set the unknownNames flag to true
   */
  public void unknownNames() {
    state.unknownNames = true;
  }

  /**
   * Determines how many bytes {@code value} will need if written to disk.
   * 
   * @param value The {@link ArkName} to get the size of
   * @param nameTable <tt>true</tt> if the ArkArchive will have a nameTable
   * @return The amount of bytes needed to store {@code value}
   */
  public static int getNameLength(ArkName value, boolean nameTable) {
    if (nameTable) {
      return 8;
    } else {
      return getStringLength(value.toString());
    }
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

  private static boolean isAscii(String value) {
    for (int i = 0; i < value.length(); i++) {
      if (value.charAt(i) > '\u007f') {
        return false;
      }
    }
    return true;
  }

}
