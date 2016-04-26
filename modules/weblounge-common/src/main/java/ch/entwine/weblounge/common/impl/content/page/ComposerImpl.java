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

package ch.entwine.weblounge.common.impl.content.page;

import ch.entwine.weblounge.common.content.page.Composer;
import ch.entwine.weblounge.common.content.page.Pagelet;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Default implementation of a <code>Composer</code>.
 */
public class ComposerImpl implements Composer {

  /** The composer identifier */
  protected String identifier = null;

  /** The pagelets */
  protected List<Pagelet> pagelets = null;

  /**
   * Creates a new composer with the given identifier.
   * 
   * @param identifier
   *          the composer identifier
   */
  public ComposerImpl(String identifier) {
    if (StringUtils.trimToNull(identifier) == null)
      throw new IllegalArgumentException("Composer identifier cannot be null");
    this.identifier = identifier;
    this.pagelets = new ArrayList<Pagelet>();
  }

  /**
   * Creates a new composer with the given identifier and set of pagelets.
   * 
   * @param identifier
   *          the composer identifier
   * @param pagelets
   *          the composer's content
   */
  public ComposerImpl(String identifier, Pagelet[] pagelets) {
    this(identifier);
    setPagelets(pagelets);
  }

  /**
   * Creates a new composer with the given identifier and set of pagelets.
   * 
   * @param identifier
   *          the composer identifier
   * @param pagelets
   *          the composer's content
   */
  public ComposerImpl(String identifier, List<Pagelet> pagelets) {
    this(identifier);
    if (pagelets == null)
      throw new IllegalArgumentException("Pagelets cannot be null");
    this.pagelets = pagelets;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.page.Composer#getIdentifier()
   */
  public String getIdentifier() {
    return identifier;
  }

  /**
   * Sets the composer identifier.
   * 
   * @param identifier the identifier
   */
  public void setIdentifier(String identifier) {
    this.identifier = identifier;
  }

  /**
   * Adds a pagelet to the list of pagelets.
   * 
   * @param pagelet
   *          the pagelet to add
   * @throws IllegalArgumentException
   *           if <code>pagelet</code> is <code>null</code>
   */
  public void addPagelet(Pagelet pagelet) throws IllegalArgumentException {
    if (pagelet == null)
      throw new IllegalArgumentException("Pagelet must not be null");
    this.pagelets.add(pagelet);
  }

  /**
   * Removes the pagelet from the list of pagelets.
   * 
   * @param pagelet
   *          the pagelet to remove
   * @throws IllegalArgumentException
   *           if <code>pagelet</code> is <code>null</code>
   */
  public void removePagelet(Pagelet pagelet) throws IllegalArgumentException {
    if (pagelet == null)
      throw new IllegalArgumentException("Pagelet must not be null");
    this.pagelets.remove(pagelet);
  }

  /**
   * Sets this composer's pagelets.
   * 
   * @param pagelets
   *          the pagelets;
   * @throws IllegalArgumentException
   *           if <code>pagelets</code> is <code>null</code>
   */
  public void setPagelets(Pagelet[] pagelets) throws IllegalArgumentException {
    if (pagelets == null)
      throw new IllegalArgumentException("Pagelets must not be null");
    this.pagelets.clear();
    this.pagelets.addAll(Arrays.asList(pagelets));
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.page.Composer#getPagelets()
   */
  public Pagelet[] getPagelets() {
    return pagelets.toArray(new Pagelet[pagelets.size()]);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.page.Composer#getPagelets(java.lang.String,
   *      java.lang.String)
   */
  public Pagelet[] getPagelets(String module, String renderer) {
    List<Pagelet> result = new ArrayList<Pagelet>();
    for (Pagelet p : pagelets) {
      if (module.equals(p.getModule()) && renderer.equals(p.getIdentifier()))
        result.add(p);
    }
    return result.toArray(new Pagelet[result.size()]);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.page.Composer#getPagelet(int)
   */
  public Pagelet getPagelet(int index) {
    return pagelets.get(index);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.page.Composer#size()
   */
  public int size() {
    return pagelets.size();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.page.Composer#toXml()
   */
  public String toXml() {
    StringBuffer b = new StringBuffer();

    b.append("<composer id=\"");
    b.append(identifier);
    b.append("\">");

    for (Pagelet pagelet : pagelets) {
      b.append(pagelet.toXml());
    }

    b.append("</composer>");

    return b.toString();
  }

  /**
   * {@inheritDoc}
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return identifier;
  }

}
