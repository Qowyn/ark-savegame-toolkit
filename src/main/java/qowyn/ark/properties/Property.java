package qowyn.ark.properties;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;

import qowyn.ark.ArkArchive;
import qowyn.ark.NameContainer;
import qowyn.ark.NameSizeCalculator;
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
   * @param nameSizer function to calculate the size of a name in bytes in the current context
   * @return value of dataSize field
   */
  public int calculateDataSize(NameSizeCalculator nameSizer);

  /**
   * Calculates the amount of bytes required to serialize this property.
   * 
   * Includes everything contained in this property.
   * 
   * @param nameTable function to calculate the size of a name in bytes in the current context
   * @return amount of bytes required to write this property in raw binary representation
   */
  public int calculateSize(NameSizeCalculator nameSizer);

  public void writeBinary(ArkArchive archive);

  public void writeJson(JsonGenerator generator) throws IOException;

}
