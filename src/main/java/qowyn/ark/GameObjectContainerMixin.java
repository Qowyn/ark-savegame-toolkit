package qowyn.ark;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import qowyn.ark.types.ArkName;

interface GameObjectContainerMixin extends GameObjectContainer {

  public Map<Integer, Map<List<ArkName>, GameObject>> getObjectMap();

  public default void addObject(GameObject object, boolean processNames) {
    List<GameObject> objects = getObjects();

    if (processNames) {
      Map<List<ArkName>, GameObject> map = getObjectMap().get(object.isFromDataFile() ? object.getDataFileIndex() : null);

      if (map != null) {
        if (object.hasParentNames()) {
          List<ArkName> targetName = object.getParentNames();
    
          GameObject parent = map.get(targetName);
          if (parent != null) {
            parent.addComponent(object);
            object.setParent(parent);
          }
        }

        map.putIfAbsent(object.getNames(), object);
      } else {
        map = new HashMap<>();
        map.put(object.getNames(), object);
        getObjectMap().put(object.isFromDataFile() ? object.getDataFileIndex() : null, map);
      };
    }

    object.setId(objects.size());
    objects.add(object);
  }

}
