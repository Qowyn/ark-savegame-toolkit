package qowyn.ark;

import java.util.List;

import qowyn.ark.properties.Property;

public interface PropertyContainer {

  public List<Property<?>> getProperties();

  public void setProperties(List<Property<?>> properties);

  public default Property<?> getProperty(String name) {
    return getProperty(name, 0);
  }

  public default Property<?> getProperty(String name, int index) {
    for (Property<?> prop : getProperties()) {
      if (prop.getIndex() == index && prop.getNameString().equals(name)) {
        return prop;
      }
    }
    return null;
  }

  public default <T extends Property<?>> T getTypedProperty(String name, Class<T> clazz) {
    return getTypedProperty(name, clazz, 0);
  }

  @SuppressWarnings("unchecked")
  public default <T extends Property<?>> T getTypedProperty(String name, Class<T> clazz, int index) {
    for (Property<?> prop : getProperties()) {
      if (prop.getIndex() == index && prop.getNameString().equals(name) && clazz.isInstance(prop)) {
        return (T) prop;
      }
    }
    return null;
  }

  public default <T> T getPropertyValue(String name, Class<T> clazz) {
    return getPropertyValue(name, clazz, 0);
  }

  @SuppressWarnings("unchecked")
  public default <T> T getPropertyValue(String name, Class<T> clazz, int index) {
    for (Property<?> prop : getProperties()) {
      if (prop.getIndex() == index && prop.getNameString().equals(name) && clazz.isInstance(prop.getValue())) {
        return (T) prop.getValue();
      }
    }

    return null;
  }


  public default boolean hasAnyProperty(String name) {
    for (Property<?> prop : getProperties()) {
      if (prop.getNameString().equals(name)) {
        return true;
      }
    }
    return false;
  }

}
