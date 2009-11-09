/*
 *  Weblounge: Web Content Management System
 *  Copyright (c) 2009 The Weblounge Team
 *  http://weblounge.o2it.ch
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public License
 *  as published by the Free Software Foundation; either version 2
 *  of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this program; if not, write to the Free Software Foundation
 *  Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */

package ch.o2it.weblounge.common.impl.language;

import ch.o2it.weblounge.common.language.Language;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * This maps keeps key - value pairs in multiple languages. The operations as
 * defined in the interface <code>Map</code> operate on the default language
 * that can be set by calling <code>setDefaultLanguage(Language)</code>.
 */
public class MultilingualHashMap<K, V> extends LocalizableObject implements Map<K, V> {

  /** Language dependent content */
  private Map<Language, Map<K, V>> content = null;

  /**
   * Creates a new and empty multilingual map.
   */
  public MultilingualHashMap() {
    this(null);
  }

  /**
   * Creates a new and empty multilingual map with <code>language</code> as the
   * default language.
   * 
   * @param language
   *          the default language
   */
  public MultilingualHashMap(Language language) {
    super(language);
    content = new HashMap<Language, Map<K, V>>();
  }

  /**
   * Removes every content in all languages.
   */
  public void clear() {
    content.clear();
  }

  /**
   * Removes the content for language <code>language</code>.
   * 
   * @param language
   *          the language
   */
  public void clear(Language language) {
    Map<K, V> map = get(language, false);
    if (map != null)
      map.clear();
  }

  /**
   * @see java.util.Map#containsKey(java.lang.Object)
   */
  public boolean containsKey(Object key) {
    Map<K, V> map = get(getLanguage(), false);
    return map != null && map.containsKey(key);
  }

  /**
   * Returns <code>true</code> if the key exists for language
   * <code>language</code>.
   * 
   * @param language
   *          the language
   * @see java.util.Map#containsKey(java.lang.Object)
   */
  public boolean containsKey(Object key, Language language) {
    Map<K, V> map = get(language, false);
    return map != null && map.containsKey(key);
  }

  /**
   * @see java.util.Map#containsValue(java.lang.Object)
   */
  public boolean containsValue(Object value) {
    Map<K, V> map = get(getLanguage(), false);
    return map != null && map.containsValue(value);
  }

  /**
   * Returns <code>true</code> if the value exists for language
   * <code>language</code>.
   * 
   * @param language
   *          the language
   * @see java.util.Map#containsValue(java.lang.Object)
   */
  public boolean containsValue(Object value, Language language) {
    Map<K, V> map = get(language, false);
    return map != null && map.containsValue(value);
  }

  /**
   * @see java.util.Map#entrySet()
   */
  public Set<Map.Entry<K, V>> entrySet() {
    Map<K, V> map = get(getLanguage(), false);
    return (map != null) ? map.entrySet() : new HashSet<Map.Entry<K, V>>();
  }

  /**
   * Returns the entry set for language <code>language</code>.
   * 
   * @param language
   *          the language
   * @see java.util.Map#entrySet()
   */
  public Set<Map.Entry<K, V>> entrySet(Language language) {
    Map<K, V> map = get(language, false);
    return (map != null) ? map.entrySet() : new HashSet<Map.Entry<K, V>>();
  }

  /**
   * This method returns the value for the given key in the default language. If
   * there is no data for the default language but an original language has been
   * defined, then the key is looked up in that language and the korresponding
   * value is returned if existant, otherwise <code>null</code> is returned.
   * 
   * @see java.util.Map#get(java.lang.Object)
   */
  public V get(Object key) {
    return get(key, getLanguage(), false);
  }

  /**
   * Returns the value which is associated with <code>key</code> for the given
   * language. If no data for <code>language</code> exists, the lookup is being
   * processed using the default language. If there is no data for the default
   * language but an original language has been defined, then the key is looked
   * up in that language and the korresponding value is returned if existant,
   * otherwise <code>null</code> is returned.
   * 
   * @param language
   *          the language
   * @see java.util.Map#get(java.lang.Object)
   */
  public V get(Object key, Language language) {
    return get(key, language, false);
  }

  /**
   * Returns the value which is associated with <code>key</code> for the given
   * language.
   * 
   * @param language
   *          the language
   * @see java.util.Map#get(java.lang.Object)
   */
  public V get(Object key, Language language, boolean force) {
    Map<K, V> map = get(language, false);
    return (map != null) ? map.get(key) : null;
  }

  /**
   * @see java.util.Map#isEmpty()
   */
  public boolean isEmpty() {
    if (content.isEmpty())
      return true;
    Map<K, V> map = get(getLanguage(), false);
    return map == null || map.isEmpty();
  }

  /**
   * Returns <code>true</code> if there are no elements for the given language.
   * 
   * @param language
   *          the language
   */
  public boolean isEmpty(Language language) {
    Map<K, V> map = get(language, false);
    return map == null || map.isEmpty();
  }

  /**
   * @see java.util.Map#keySet()
   */
  public Set<K> keySet() {
    Map<K, V> map = get(getLanguage(), false);
    return (map != null) ? map.keySet() : new HashSet<K>();
  }

  /**
   * Returns the keyset for the given language.
   * 
   * @param language
   *          the language
   * @see java.util.Map#keySet()
   */
  public Set<K> keySet(Language language) {
    Map<K, V> map = get(language, false);
    return (map != null) ? map.keySet() : new HashSet<K>();
  }

  /**
   * @see java.util.Map#put(java.lang.Object, java.lang.Object)
   */
  public V put(K key, V value) {
    return put(key, value, getLanguage());
  }

  /**
   * Puts key and value into the map in the given language.
   * 
   * @param language
   *          the language
   * @param key
   *          the key
   * @param value
   *          the value
   * @see java.util.Map#put(java.lang.Object, java.lang.Object)
   */
  public V put(K key, V value, Language language) {
    Map<K, V> map = get(language, false);
    if (map == null) {
      map = new HashMap<K, V>();
      content.put(language, map);
      if (originalLanguage == null)
        setOriginalLanguage(language);
    }
    return map.put(key, value);
  }

  /**
   * @see java.util.Map#putAll(java.util.Map)
   */
  public void putAll(Map<? extends K, ? extends V> m) {
    putAll(m, getLanguage());
  }

  /**
   * Puts all key-entry pairs into the map, associated with the given language.
   * 
   * @param map
   *          the map to put
   * @language the language
   * @see java.util.Map#putAll(java.util.Map)
   */
  public void putAll(Map<? extends K, ? extends V> map2, Language language) {
    Map<K, V> map = get(language, false);
    if (map == null) {
      map = new HashMap<K, V>();
      content.put(language, map);
      if (originalLanguage == null)
        setOriginalLanguage(language);
    }
    map.putAll(map);
    super.enableLanguage(language);
  }

  /**
   * @see java.util.Map#remove(java.lang.Object)
   */
  public V remove(Object key) {
    Map<K, V> map = get(getLanguage(), false);
    return (map != null) ? map.remove(key) : null;
  }

  /**
   * Removes the entry from the map in the given language.
   * 
   * @param key
   *          the key to remove
   * @param language
   *          the language
   * @see java.util.Map#remove(java.lang.Object)
   */
  public V remove(Object key, Language language) {
    Map<K, V> map = get(language, true);
    if (map == null)
      return null;
    V element = map.remove(key);
    if (map.size() == 0) {
      content.remove(language);
      remove(language);
    }
    return element;
  }

  /**
   * Returns the number of languages used in this map.
   * 
   * @return the number of languages
   * @see java.util.Map#size()
   */
  public int size() {
    return content.size();
  }

  /**
   * Returns the number entries in the given language.
   * 
   * @return the number of entries in the given language
   * @see java.util.Map#size()
   */
  public int size(Language language) {
    Map<K, V> map = get(language, false);
    return (map != null) ? map.size() : 0;
  }

  /**
   * @see java.util.Map#values()
   */
  public Collection<V> values() {
    Map<K, V> map = get(getLanguage(), false);
    return (map != null) ? map.values() : new ArrayList<V>();
  }

  /**
   * Returns the values for language <code>language</code>.
   * 
   * @param language
   *          the language
   * @see java.util.Map#values()
   */
  public Collection<V> values(Language language) {
    Map<K, V> map = get(language, false);
    return (map != null) ? map.values() : new ArrayList<V>();
  }

  /**
   * Private implementation to get the set of entries for a given language.
   * 
   * @param language
   *          the language
   * @param force
   *          <code>true</code> to force the languege
   * @return the entry map
   */
  private Map<K, V> get(Language language, boolean force) {
    Map<K, V> map = get(language, false);
    if (map == null && !force) {
      Language defaultLanguage = getDefaultLanguage();
      if (defaultLanguage != null && (language == null || !language.equals(defaultLanguage))) {
        map = get(getLanguage(), false);
      } else if (content.size() == 1) {
        map = content.values().iterator().next();
      }
    }
    return map;
  }

}