/*
 *  Weblounge: Web Content Management System
 *  Copyright (c) 2010 The Weblounge Team
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

package ch.o2it.weblounge.common.impl.util.xml;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.xml.namespace.NamespaceContext;

/**
 * Helper implementation of {@link XPathNamespaceContext} which is mostly used
 * to help identify test runs of Xpath expressions.
 */
public class XPathNamespaceContext implements NamespaceContext {

  /** Flag to indicate test runs */
  private boolean test = false;

  /** The defined namespace mappings */
  private Map<String, String> definitions = new HashMap<String, String>();

  /** The used namespace mappings */
  private Map<String, String> mappings = new HashMap<String, String>();

  /**
   * Creates a new namespace context.
   * 
   * @param test
   *          <code>true</code> if we are running in a test case
   */
  public XPathNamespaceContext(boolean test) {
    this.test = test;
    if (test)
      definitions.put("ns", "http://test-uri.com");
  }

  /**
   * Returns <code>true</code> if this namespace context is created for testing
   * purposes.
   * 
   * @return <code>true</code> if this is a test case
   */
  public boolean isTest() {
    return test;
  }

  /**
   * Defines a new namespace uri.
   * 
   * @param prefix
   *          the prefix
   * @param nsURI
   *          the uri
   */
  public void defineNamespaceURI(String prefix, String nsURI) {
    definitions.put(prefix, nsURI);
  }

  /**
   * {@inheritDoc}
   * 
   * @see javax.xml.namespace.NamespaceContext#getNamespaceURI(java.lang.String)
   */
  public String getNamespaceURI(String prefix) {
    String namespace = mappings.get(prefix);
    if (namespace != null)
      return namespace;
    if (definitions.size() == 1) {
      namespace = definitions.values().iterator().next();
      mappings.put(prefix, namespace);
      return namespace;
    }
    return null;
  }

  /**
   * {@inheritDoc}
   * 
   * @see javax.xml.namespace.NamespaceContext#getPrefix(java.lang.String)
   */
  public String getPrefix(String namespaceURI) {
    for (Map.Entry<String, String> entry : definitions.entrySet()) {
      if (entry.getValue().equals(namespaceURI))
        return entry.getKey();
    }
    return null;
  }

  /**
   * {@inheritDoc}
   * 
   * @see javax.xml.namespace.NamespaceContext#getPrefixes(java.lang.String)
   */
  public Iterator<?> getPrefixes(String namespaceURI) {
    return mappings.keySet().iterator();
  }

}
