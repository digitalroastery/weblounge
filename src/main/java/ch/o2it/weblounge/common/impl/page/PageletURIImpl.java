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

package ch.o2it.weblounge.common.impl.page;

import ch.o2it.weblounge.common.content.PageURI;
import ch.o2it.weblounge.common.content.PageletURI;
import ch.o2it.weblounge.common.site.Site;

/**
 * This class describes a pagelet's position with respect to its url, composer
 * and position within this composer.
 */
public class PageletURIImpl implements PageletURI {

  /** The pagelet's url */
  private PageURI pageURI;

  /** The pagelet's composer */
  private String composer;

  /** The pagelet's position within the specified composer */
  private int position;

  /**
   * Creates a new <code>PageletLocationImpl</code> with the given url, composer
   * and position.
   * 
   *@param pageURI
   *          the pagelet's url
   *@param composer
   *          the pagelet's composer
   *@param position
   *          the position within the composer
   */
  public PageletURIImpl(PageURI pageURI, String composer, int position) {
    this.pageURI = pageURI;
    this.composer = composer;
    this.position = position;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.PageletURI#getSite()
   */
  public Site getSite() {
    return pageURI.getSite();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.PageletURI#getPageURI()
   */
  public PageURI getPageURI() {
    return pageURI;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.PageletURI#getComposer()
   */
  public String getComposer() {
    return composer;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.PageletURI#getPosition()
   */
  public int getPosition() {
    return position;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.PageletURI#setURI(ch.o2it.weblounge.common.content.PageURI)
   */
  public void setURI(PageURI uri) {
    this.pageURI = uri;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.PageletURI#setComposer(java.lang.String)
   */
  public void setComposer(String composer) {
    this.composer = composer;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.PageletURI#setPosition(int)
   */
  public void setPosition(int position) {
    this.position = position;
  }

  /**
   * Returns the location's hash code.
   * 
   * @see java.lang.Object#hashCode()
   */
  public int hashCode() {
    if (pageURI != null && composer != null)
      return pageURI.hashCode() | (composer.hashCode() >> 8) | (position >> 8);
    else
      return super.hashCode();
  }

  /**
   * Returns <code>true</code> if <code>o</code> equals this pagelet location.
   * 
   * @see java.lang.Object#equals(java.lang.Object)
   */
  public boolean equals(Object o) {
    if (o instanceof PageletURI) {
      PageletURI l = (PageletURI) o;
      if (l.getPageURI() != null && l.getComposer() != null)
        return l.getPageURI().equals(pageURI) && l.getComposer().equals(composer) && l.getPosition() == position;
    }
    return false;
  }

  /**
   * {@inheritDoc}
   * 
   * @see java.lang.Comparable#compareTo(java.lang.Object)
   */
  public int compareTo(PageletURI l) {
    if (!pageURI.equals(l.getPageURI()) || !composer.equals(l.getComposer()))
      return 0;
    return Integer.valueOf(position).compareTo(Integer.valueOf(l.getPosition()));
  }

  /**
   * Returns the page uri along with the pagelet position on that page as in the
   * following example:
   * <pre>
   *  /test/a/b [composer=main,position=7]
   * </pre>
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return pageURI + " [composer=" + composer + ",position=" + position + "]";
  }

}