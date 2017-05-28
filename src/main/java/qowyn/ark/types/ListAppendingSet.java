package qowyn.ark.types;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class ListAppendingSet<E> implements Set<E> {

  private final List<E> list;

  private final Set<E> set;

  public ListAppendingSet(Collection<E> list) {
    this.list = new ArrayList<>(list);
    this.set = new HashSet<>(list);
  }

  public List<E> getList() {
    return list;
  }

  @Override
  public int size() {
    return set.size();
  }

  @Override
  public boolean isEmpty() {
    return set.isEmpty();
  }

  @Override
  public boolean contains(Object o) {
    return set.contains(o);
  }

  @Override
  public ListAppendingSetIterator iterator() {
    return new ListAppendingSetIterator(set.iterator());
  }

  @Override
  public Object[] toArray() {
    return set.toArray();
  }

  @Override
  public <T> T[] toArray(T[] a) {
    return set.toArray(a);
  }

  @Override
  public boolean add(E e) {
    if (set.add(e)) {
      list.add(e);
      return true;
    }

    return false;
  }

  @Override
  public boolean remove(Object o) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean containsAll(Collection<?> c) {
    return set.containsAll(c);
  }

  @Override
  public boolean addAll(Collection<? extends E> c) {
    boolean modified = false;
    for (E e : c) {
      if (add(e)) {
        modified = true;
      }
    }
    return modified;
  }

  @Override
  public boolean retainAll(Collection<?> c) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean removeAll(Collection<?> c) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void clear() {
    throw new UnsupportedOperationException();
  }

  private class ListAppendingSetIterator implements Iterator<E> {

    private final Iterator<E> impl;

    public ListAppendingSetIterator(Iterator<E> impl) {
      this.impl = impl;
    }

    @Override
    public boolean hasNext() {
      return impl.hasNext();
    }

    @Override
    public E next() {
      return impl.next();
    }

  }

}
