package qowyn.ark;

import java.util.Iterator;
import java.util.List;

import qowyn.ark.types.ObjectReference;

public interface GameObjectContainer extends Iterable<GameObject> {

  public List<GameObject> getObjects();

  public default GameObject getObject(ObjectReference reference) {
    if (reference == null || reference.getObjectType() != ObjectReference.TYPE_ID) {
      return null;
    }

    if (reference.getObjectId() > -1 && reference.getObjectId() < getObjects().size()) {
      return getObjects().get(reference.getObjectId());
    } else {
      return null;
    }
  }

  @Override
  public default Iterator<GameObject> iterator() {
    return getObjects().iterator();
  }

}
