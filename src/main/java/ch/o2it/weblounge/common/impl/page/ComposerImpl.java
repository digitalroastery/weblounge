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

package ch.o2it.weblounge.common.impl.page;

import ch.o2it.weblounge.common.content.Composer;
import ch.o2it.weblounge.common.content.Pagelet;

import org.apache.commons.lang.StringUtils;

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
   * @see ch.o2it.weblounge.common.content.Composer#getIdentifier()
   */
  public String getIdentifier() {
    return identifier;
  }

  /**
   * Adds a pagelet to the list of pagelets.
   * 
   * @param pagelet
   *          the pagelet to add
   */
  public void addPagelet(Pagelet pagelet) {
    if (pagelet == null)
      throw new IllegalArgumentException("Pagelet must not be null");
    this.pagelets.add(pagelet);
  }
  
  /**
   * Removes the pagelet from the list of pagelets.
   * 
   * @param pagelet
   *          the pagelet to remove
   */
  public void removePagelet(Pagelet pagelet) {
    if (pagelet == null)
      throw new IllegalArgumentException("Pagelet must not be null");
    this.pagelets.remove(pagelet);
  }

  /**
   * Sets this composer's pagelets.
   * 
   * @param pagelets
   *          the pagelets;
   */
  public void setPagelets(Pagelet[] pagelets) {
    this.pagelets.clear();
    this.pagelets.addAll(Arrays.asList(pagelets));
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.Composer#getPagelets()
   */
  public Pagelet[] getPagelets() {
    return pagelets.toArray(new Pagelet[pagelets.size()]);
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
