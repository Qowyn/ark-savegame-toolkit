package qowyn.ark;

import java.util.function.Predicate;

public class ReadingOptions {

  private boolean dataFiles = true;

  private boolean embeddedData = true;

  private boolean gameObjects = true;

  private boolean gameObjectProperties = true;
  
  private boolean parallelReading = false;
  
  private boolean memoryMapping = true;

  private Predicate<GameObject> objectFilter = null;

  public static ReadingOptions create() {
    return new ReadingOptions();
  }

  public Predicate<GameObject> getObjectFilter() {
    return objectFilter;
  }

  public ReadingOptions withObjectFilter(Predicate<GameObject> objectFilter) {
    this.objectFilter = objectFilter;
    return this;
  }

  public boolean getDataFiles() {
    return dataFiles;
  }

  public ReadingOptions withDataFiles(boolean dataFiles) {
    this.dataFiles = dataFiles;
    return this;
  }

  public boolean getEmbeddedData() {
    return embeddedData;
  }

  public ReadingOptions withEmbeddedData(boolean embeddedData) {
    this.embeddedData = embeddedData;
    return this;
  }

  public boolean getGameObjects() {
    return gameObjects;
  }

  public ReadingOptions withGameObjects(boolean gameObjects) {
    this.gameObjects = gameObjects;
    return this;
  }

  public boolean getGameObjectProperties() {
    return gameObjectProperties;
  }

  public ReadingOptions withGameObjectProperties(boolean gameObjectProperties) {
    this.gameObjectProperties = gameObjectProperties;
    return this;
  }
  
  public boolean getParallelReading() {
    return parallelReading;
  }
  
  public ReadingOptions withParallelReading(boolean parallelReading) {
    this.parallelReading = parallelReading;
    return this;
  }
  
  public boolean getMemoryMapping() {
    return memoryMapping;
  }
  
  public ReadingOptions withMemoryMapping(boolean memoryMapping) {
    this.memoryMapping = memoryMapping;
    return this;
  }

}
