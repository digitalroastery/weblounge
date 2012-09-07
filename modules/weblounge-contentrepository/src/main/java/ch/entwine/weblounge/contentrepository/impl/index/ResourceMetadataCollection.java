/*
 *  Weblounge: Web Content Management System
 *  Copyright (c) 2003 - 2011 The Weblounge Team
 *  http://entwinemedia.com/weblounge
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

package ch.entwine.weblounge.contentrepository.impl.index;

import ch.entwine.weblounge.common.content.ResourceMetadata;
import ch.entwine.weblounge.common.content.page.Pagelet;
import ch.entwine.weblounge.common.impl.content.ResourceMetadataImpl;
import ch.entwine.weblounge.common.language.Language;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Extension to a <code>SolrUpdateableInputDocument</code> that facilitates in
 * posting weblounge pages to solr. This implementation provides utility methods
 * that will ease handling of objects such as dates or users and help prevent
 * posting of <code>null</code> values.
 */
public class ResourceMetadataCollection implements Collection<ResourceMetadata<?>> {

  /** The metadata */
  protected Map<String, ResourceMetadata<?>> metadata = new HashMap<String, ResourceMetadata<?>>();

  /**
   * Adds the field and its value to the search index.
   * 
   * @param fieldName
   *          the field name
   * @param fieldValue
   *          the value
   * @param addToFulltext
   *          <code>true</code> to add the contents to the fulltext field as
   *          well
   */
  @SuppressWarnings("unchecked")
  public void addField(String fieldName, Object fieldValue,
      boolean addToFulltext) {
    if (fieldName == null)
      throw new IllegalArgumentException("Field name cannot be null");
    if (fieldValue == null)
      return;

    ResourceMetadata<Object> m = (ResourceMetadata<Object>)metadata.get(fieldName);
    if (m == null) {
      m = new ResourceMetadataImpl<Object>(fieldName);
      metadata.put(fieldName, m);
    }

    m.setAddToFulltext(addToFulltext);

    if (fieldValue.getClass().isArray()) {
      Object[] fieldValues = (Object[]) fieldValue;
      for (Object v : fieldValues) {
        m.addValue(v);
      }
    } else {
      m.addValue(fieldValue);
    }
  }

  /**
   * Returns the localized field name, which is the original field name extended
   * by an underscore and the language identifier.
   * 
   * @param fieldName
   *          the field name
   * @param language
   *          the language
   * @return the localized field name
   */
  protected String getLocalizedFieldName(String fieldName, Language language) {
    return MessageFormat.format(fieldName, language.getIdentifier());
  }

  /**
   * Returns a string representation of the pagelet's element content in the
   * specified language. If <code>format</code> is <code>true</code> then the
   * content is formatted as <code>field=&lt;value&gt;;;</code>, otherwise just
   * the values are added.
   * 
   * @param pagelet
   *          the pagelet
   * @param language
   *          the language
   * @return the serialized element content
   */
  protected String[] serializeContent(Pagelet pagelet, Language language) {
    List<String> result = new ArrayList<String>();
    for (String element : pagelet.getContentNames(language)) {
      String[] content = pagelet.getMultiValueContent(element, language, true);
      for (String c : content) {
        StringBuffer buf = new StringBuffer();
        buf.append(element).append("=").append(c);
        result.add(buf.toString());
      }
    }
    return result.toArray(new String[result.size()]);
  }

  /**
   * Returns a string representation of the pagelet's element properties. If
   * <code>format</code> is <code>true</code> then the property is formatted as
   * <code>field=&lt;value&gt;</code>, otherwise just the values are added.
   * 
   * @param pagelet
   *          the pagelet
   * @return the serialized element properties
   */
  protected String[] serializeProperties(Pagelet pagelet) {
    List<String> result = new ArrayList<String>();
    for (String property : pagelet.getPropertyNames()) {
      for (String v : pagelet.getMultiValueProperty(property)) {
        StringBuffer buf = new StringBuffer();
        buf.append(property).append("=").append(v);
        result.add(buf.toString());
      }
    }
    return result.toArray(new String[result.size()]);
  }

  /**
   * Returns the metadata as a list of {@link ResourceMetadata} items.
   * 
   * @return the metadata items
   */
  public List<ResourceMetadata<?>> getMetadata() {
    List<ResourceMetadata<?>> result = new ArrayList<ResourceMetadata<?>>();
    result.addAll(metadata.values());
    return result;
  }

  /**
   * {@inheritDoc}
   * 
   * @see java.util.Collection#add(java.lang.Object)
   */
  public boolean add(ResourceMetadata<?> e) {
    return metadata.put(e.getName(), e) != null;
  }

  /**
   * {@inheritDoc}
   * 
   * @see java.util.Collection#addAll(java.util.Collection)
   */
  public boolean addAll(Collection<? extends ResourceMetadata<?>> c) {
    for (ResourceMetadata<?> m : c) {
      metadata.put(m.getName(), m);
    }
    return true;
  }

  /**
   * {@inheritDoc}
   * 
   * @see java.util.Collection#clear()
   */
  public void clear() {
    metadata.clear();
  }

  /**
   * {@inheritDoc}
   * 
   * @see java.util.Collection#contains(java.lang.Object)
   */
  public boolean contains(Object o) {
    return metadata.values().contains(o);
  }

  /**
   * {@inheritDoc}
   * 
   * @see java.util.Collection#containsAll(java.util.Collection)
   */
  public boolean containsAll(Collection<?> c) {
    for (Object o : c) {
      if (!metadata.values().contains(o))
        return false;
    }
    return true;
  }

  /**
   * {@inheritDoc}
   * 
   * @see java.util.Collection#isEmpty()
   */
  public boolean isEmpty() {
    return metadata.isEmpty();
  }

  /**
   * {@inheritDoc}
   * 
   * @see java.util.Collection#iterator()
   */
  public Iterator<ResourceMetadata<?>> iterator() {
    List<ResourceMetadata<?>> result = new ArrayList<ResourceMetadata<?>>();
    result.addAll(metadata.values());
    return result.iterator();
  }

  /**
   * {@inheritDoc}
   * 
   * @see java.util.Collection#remove(java.lang.Object)
   */
  public boolean remove(Object o) {
    return metadata.remove(o) != null;
  }

  /**
   * {@inheritDoc}
   * 
   * @see java.util.Collection#removeAll(java.util.Collection)
   */
  public boolean removeAll(Collection<?> c) {
    boolean removed = false;
    for (Object o : c)
      removed |= metadata.remove(o) != null;
    return removed;
  }

  /**
   * {@inheritDoc}
   * 
   * @see java.util.Collection#retainAll(java.util.Collection)
   */
  public boolean retainAll(Collection<?> c) {
    boolean removed = false;
    for (ResourceMetadata<?> m : metadata.values()) {
      if (!c.contains(m)) {
        metadata.remove(m);
        removed = true;
      }
    }
    return removed;
  }

  /**
   * {@inheritDoc}
   * 
   * @see java.util.Collection#size()
   */
  public int size() {
    return metadata.size();
  }

  /**
   * {@inheritDoc}
   * 
   * @see java.util.Collection#toArray()
   */
  public Object[] toArray() {
    return metadata.values().toArray();
  }

  /**
   * {@inheritDoc}
   * 
   * @see java.util.Collection#toArray(T[])
   */
  @SuppressWarnings({ "unchecked", "hiding" })
  public <ResourceMetadata> ResourceMetadata[] toArray(ResourceMetadata[] a) {
    return (ResourceMetadata[]) metadata.values().toArray(new ResourceMetadataImpl[metadata.size()]);
  }

}
