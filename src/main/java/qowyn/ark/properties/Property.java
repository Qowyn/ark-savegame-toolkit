package qowyn.ark.properties;

import javax.json.JsonValue;

import qowyn.ark.ArkArchive;
import qowyn.ark.NameContainer;
import qowyn.ark.types.ArkName;

public interface Property<T> extends NameContainer {

  public Class<T> getValueClass();

  public ArkName getName();

  public String getNameString();

  public void setName(ArkName name);

  public void setNameString(String nameString);

  public ArkName getType();

  public String getTypeString();

  public int getDataSize();

  public void setDataSize(int length);

  public int getIndex();

  public void setIndex(int index);

  public T getValue();

  public void setValue(T value);

  /**
   * Calculates the value for the dataSize field
   * 
   * @param nameTable <tt>true</tt> if using String deduplication will be used
   * @return value of dataSize field
   */
  public int calculateDataSize(boolean nameTable);

  /**
   * Calculates the amount of bytes required to serialize this property.
   * 
   * Includes everything contained in this property.
   * 
   * @param nameTable <tt>true</tt> if using String deduplication will be used
   * @return amount of bytes required to write this property in raw binary representation
   */
  public int calculateSize(boolean nameTable);

  public JsonValue toJson();

  public void write(ArkArchive archive);

}
