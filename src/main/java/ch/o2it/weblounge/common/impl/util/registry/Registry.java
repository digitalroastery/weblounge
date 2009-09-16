/*
 * Weblounge: Web Content Management System Copyright (c) 2007 The Weblounge
 * Team http://weblounge.o2it.ch
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any
 * later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */

package ch.o2it.weblounge.common.impl.util.registry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * This class implements a registry used to keep objects. The registry features
 * two parts: A permanent and a temporary part. Objects put into the permant
 * part are kept, while object put into the temporary part are weekley
 * referenced and may be lost if the garbage collector catches them while
 * hunting for unused objects.
 * 
 * @author Tobias Wunden
 * @version 1.0
 */
public class Registry<KeyType, ValueType> implements Map<KeyType, ValueType> {

  /** Items to be kept for a short time */
  protected Map<KeyType, ValueType> temporary = new WeakHashMap<KeyType, ValueType>();

  /** Items to be kept forever */
  protected Map<KeyType, ValueType> permanent = new HashMap<KeyType, ValueType>();

  /** the listener list */
  protected transient List<RegistryListener> listeners;

  /** registry identifier */
  protected String id_;

  /**
   * Creates a new registry.
   */
  public Registry() {
    permanent = new HashMap<KeyType, ValueType>();
    temporary = new WeakHashMap<KeyType, ValueType>();
    id_ = super.toString();
  }

  /**
   * Creates a new registry.
   * 
   * @param id
   *          the registry
   */
  public Registry(String id) {
    this();
    this.id_ = id;
  }

  /**
   * Adds a listener to the registry. The listener will be notified if changes
   * to the registry, such as adding or removing, occur.
   * 
   * @param l
   *          the listener to add
   */
  public void addRegistryListener(RegistryListener l) {
    if (listeners == null) {
      listeners = new ArrayList<RegistryListener>();
    }
    listeners.add(l);
  }

  /**
   * Removes the listener from the registry.
   * 
   * @param l
   *          the listener to remove
   */
  public void removeRegistryListener(RegistryListener l) {
    if (listeners == null) {
      listeners = new ArrayList<RegistryListener>();
    }
    listeners.remove(l);
  }

  /**
   * Returns the registry size. The size is calculated as the total of the
   * permanent and the temporary registry parts.
   * 
   * @see java.util.Map#size()
   */
  public int size() {
    return permanent.size() + temporary.size();
  }

  /**
   * Returns the number of permant registry members.
   * 
   * @return the permanent size
   */
  public int permanentSize() {
    return permanent.size();
  }

  /**
   * Returns the number of temporary registry members.
   * 
   * @return the temporary size
   */
  public int temporarySize() {
    return temporary.size();
  }

  /**
   * Returns <code>true</code> if this registry is empty.
   * 
   * @see java.util.Map#isEmpty()
   */
  public boolean isEmpty() {
    return permanent.size() == 0 && temporary.size() == 0;
  }

  /**
   * Returns <code>true</code> if the registry contains the specified key. The
   * lookup is being performed on both the permanent and the temporary part of
   * the registry.
   * 
   * @see java.util.Map#containsKey(java.lang.Object)
   */
  public boolean containsKey(Object key) {
    return permanent.containsKey(key) || temporary.containsKey(key);
  }

  /**
   * Returns <code>true</code> if the registry contains the specified permanent
   * key.
   * 
   * @return <code>true</code> if the key exists
   */
  public boolean containsPermanentKey(Object key) {
    return permanent.containsKey(key);
  }

  /**
   * Returns <code>true</code> if the registry contains the specified temporary
   * key.
   * 
   * @return <code>true</code> if the key exists
   */
  public boolean containsTemporaryKey(Object key) {
    return temporary.containsKey(key);
  }

  /**
   * Returns <code>true</code> if the registry contains the specified value. The
   * lookup is being performed on both the permanent and the temporary part of
   * the registry.
   * 
   * @see java.util.Map#containsValue(java.lang.Object)
   */
  public boolean containsValue(Object value) {
    return permanent.containsValue(value) || temporary.containsValue(value);
  }

  /**
   * Returns <code>true</code> if the registry contains the specified permanent
   * value.
   * 
   * @return <code>true</code> if the registry contains the value
   */
  public boolean containsPermanentValue(Object value) {
    return permanent.containsValue(value);
  }

  /**
   * Returns <code>true</code> if the registry contains the specified temporary
   * value.
   * 
   * @return <code>true</code> if the registry contains the value
   */
  public boolean containsTemporaryValue(Object value) {
    return temporary.containsValue(value);
  }

  /**
   * Returns the value for the specified key. The value is being looked up in
   * both the permanent and the temporary part of the registry.
   * 
   * @see java.util.Map#get(java.lang.Object)
   */
  public ValueType get(Object key) {
    ValueType value = permanent.get(key);
    return (value != null) ? value : temporary.get(key);
  }

  /**
   * Returns the value for the specified key from the permanent part of the
   * registry.
   * 
   * @return the value
   */
  public ValueType getPermanent(KeyType key) {
    return permanent.get(key);
  }

  /**
   * Returns the value for the specified key from the temporary part of the
   * registry.
   * 
   * @return the value
   */
  public Object getTemporary(Object key) {
    return temporary.get(key);
  }

  /**
   * Puts <code>value</code> into the permanent part of the registry.
   * 
   * @param key
   *          the key
   * @param value
   *          the value
   * @return any replaced object
   */
  public ValueType put(KeyType key, ValueType value) {
    return put(key, value, true);
  }

  /**
   * Puts <code>value</code> into the permanent part of the registry.
   * 
   * @param key
   *          the key
   * @param value
   *          the value
   * @return any replaced object
   */
  public ValueType put(KeyType key, ValueType value, boolean keep) {
    ValueType o = null;
    if (keep) {
      temporary.remove(value);
      o = permanent.put(key, value);
    } else {
      permanent.remove(value);
      o = temporary.put(key, value);
    }
    fireContentChanged(key, RegistryEvent.ADD);
    return o;
  }

  /**
   * Removes the object identified by <code>key</code> from the registry,
   * regardless of whether it was kept int the permanent or the temporary part.
   * 
   * @see java.util.Map#remove(java.lang.Object)
   */
  public ValueType remove(Object key) {
    ValueType value = permanent.remove(key);
    ValueType o = (value != null) ? value : temporary.remove(key);
    if (o != null)
      fireContentChanged(o, RegistryEvent.REMOVE);
    return o;
  }

  /**
   * Puts all values of map <code>m</code> into the permanent part of the
   * registry.
   * 
   * @param m
   *          the map
   */
  public void putAll(Map<? extends KeyType, ? extends ValueType> m) {
    putAll(m, true);
  }

  /**
   * Puts all values of map <code>m</code> into the registry. Depending on
   * <code>keep</code>, the values are stored in the permanent or in the
   * temporary part of the registry.
   * 
   * @param m
   *          the map
   * @param keep
   *          <code>true</code> to keep the values in the permanent part of the
   *          registry
   */
  public void putAll(Map<? extends KeyType, ? extends ValueType> m, boolean keep) {
    if (keep) {
      permanent.putAll(m);
      Iterator i = m.keySet().iterator();
      while (i.hasNext()) {
        temporary.remove(i);
      }
    } else {
      temporary.putAll(m);
      Iterator i = m.keySet().iterator();
      while (i.hasNext()) {
        permanent.remove(i);
      }
    }
    fireContentChanged(m, RegistryEvent.ADD);
  }

  /**
   * Clears all registry values.
   * 
   * @see java.util.Map#clear()
   */
  public void clear() {
    permanent.clear();
    temporary.clear();
    fireContentChanged(null, RegistryEvent.CLEAR);
  }

  /**
   * Clears all permanent registry values.
   */
  public void clearPermanent() {
    permanent.clear();
    fireContentChanged(null, RegistryEvent.CLEAR);
  }

  /**
   * Clears all temporary registry values.
   */
  public void clearTemporary() {
    temporary.clear();
    fireContentChanged(null, RegistryEvent.CLEAR);
  }

  /**
   * Returns the registry's keyset. The keyset is the combined set of the keys
   * from the permanent and the temporary part of the registry.
   * 
   * @see java.util.Map#keySet()
   */
  public Set<KeyType> keySet() {
    Set<KeyType> set = new HashSet<KeyType>(permanent.keySet());
    set.addAll(temporary.keySet());
    return set;
  }

  /**
   * Returns the registry's permanent keyset.
   * 
   * @return the keyset
   */
  public Set<KeyType> permanentKeySet() {
    return permanent.keySet();
  }

  /**
   * Returns the registry's temporary keyset.
   * 
   * @return the keyset
   */
  public Set<KeyType> temporaryKeySet() {
    return temporary.keySet();
  }

  /**
   * Returns the registry's values. The values collection is the combined set of
   * the values from the permanent and the temporary part of the registry.
   * 
   * @see java.util.Map#values()
   */
  public Collection<ValueType> values() {
    Collection<ValueType> collection = new ArrayList<ValueType>(permanent.values());
    collection.addAll(temporary.values());
    return collection;
  }

  /**
   * Returns the registry's permanent values.
   * 
   * @return the values
   */
  public Collection<ValueType> permanentValues() {
    return permanent.values();
  }

  /**
   * Returns the registry's temporary values.
   * 
   * @return the values
   */
  public Collection<ValueType> temporaryValues() {
    return temporary.values();
  }

  /**
   * Returns the registry's entry set. The entry set is the combined set of the
   * entries from the permanent and the temporary part of the registry.
   * 
   * @see java.util.Map#entrySet()
   */
  public Set<Map.Entry<KeyType, ValueType>> entrySet() {
    Set<Map.Entry<KeyType, ValueType>> set = new HashSet<Map.Entry<KeyType, ValueType>>(permanent.entrySet());
    set.addAll(temporary.entrySet());
    return set;
  }

  /**
   * Returns the registry's permanent entryset.
   * 
   * @return the entryset
   */
  public Set permanentEntrySet() {
    return permanent.entrySet();
  }

  /**
   * Returns the registry's temporary entryset.
   * 
   * @return the entryset
   */
  public Set temporaryEntrySet() {
    return temporary.entrySet();
  }

  /**
   * Moves the value identified by <code>key</code> from the permanent to the
   * temporary part of the registry.
   * 
   * @param key
   *          the key
   * @return the moved object
   */
  public ValueType forget(KeyType key) {
    ValueType o = permanent.remove(key);
    temporary.put(key, o);
    return o;
  }

  /**
   * Moves the value identified by <code>key</code> from the permanent to the
   * temporary part of the registry.
   * 
   * @param key
   *          the key
   * @return the moved object
   */
  public ValueType keep(KeyType key) {
    ValueType o = temporary.remove(key);
    permanent.put(key, o);
    return o;
  }

  /**
   * Returns <code>true</code> if <code>o</code> represents the same object by
   * means of this equality function implementation.
   * 
   * @param o
   *          the object to test for equality
   * @return <code>true</code> if this and o are equal
   */
  public boolean equals(Object o) {
    if (o != null && o instanceof Registry) {
      return id_.equals(((Registry) o).id_);
    }
    return false;
  }

  /**
   * Returns the hash code for this registry.
   * 
   * @return the hash code
   */
  public int hashCode() {
    return (id_ != null) ? id_.hashCode() : super.hashCode();
  }

  /**
   * Returns the registry identifier.
   * 
   * @return the registry identifier
   */
  public String toString() {
    if (id_ != null) {
      return id_;
    } else {
      return super.toString();
    }
  }

  /**
   * Fires an event to all registered listeners.
   */
  protected void fireContentChanged(Object key, int mode) {
    if (listeners != null && listeners.size() > 0) {
      RegistryEvent e = new RegistryEvent(this, key, mode);
      synchronized (listeners) {
        Iterator i = listeners.iterator();
        while (i.hasNext()) {
          ((RegistryListener) i.next()).registryChanged(e);
        }
      }
    }
  }

}