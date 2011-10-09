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

package ch.entwine.weblounge.common.impl.content.image;

import ch.entwine.weblounge.common.content.ImageSearchResultItem;
import ch.entwine.weblounge.common.content.ResourceURI;
import ch.entwine.weblounge.common.content.image.ImageResource;
import ch.entwine.weblounge.common.impl.content.AbstractResourceSearchResultItemImpl;
import ch.entwine.weblounge.common.url.WebUrl;

/**
 * Default implementation of a
 * {@link ch.entwine.weblounge.common.content.SearchResultItem}.
 */
public class ImageResourceSearchResultItemImpl extends AbstractResourceSearchResultItemImpl implements ImageSearchResultItem {

  /** The image xml */
  protected String imageXml = null;

  /** The image header xml */
  protected String headerXml = null;

  /** The file preview xml */
  protected String previewXml = null;

  /**
   * Creates a new search result item with the given uri. The
   * <code>source</code> is the object that created the item, usually, this will
   * be the site itself but it could very well be a module that added to a
   * search result.
   * 
   * @param uri
   *          the page uri
   * @param url
   *          the url to show the hit
   * @param relevance
   *          the score inside the search result
   * @param source
   *          the object that produced the result item
   */
  public ImageResourceSearchResultItemImpl(ResourceURI uri, WebUrl url,
      double relevance, Object source) {
    super(uri, url, relevance, source);
  }

  /**
   * Sets the file xml.
   * 
   * @param xml
   *          the xml
   */
  public void setResourceXml(String xml) {
    this.imageXml = xml;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.ResourceSearchResultItem#getResourceXml()
   */
  public String getResourceXml() {
    return this.imageXml;
  }

  /**
   * Sets the file header xml.
   * 
   * @param xml
   *          the xml
   */
  public void setImageHeaderXml(String xml) {
    this.headerXml = xml;
  }

  /**
   * Returns the xml that makes up the header portion of the file.
   * 
   * @return the file header xml
   */
  public String getImageHeaderXml() {
    return this.headerXml;
  }

  /**
   * Sets the file header xml.
   * 
   * @param xml
   *          the xml
   */
  public void setImagePreviewXml(String xml) {
    this.previewXml = xml;
  }

  /**
   * {@inheritDoc}
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "Image " + uri.toString();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.ImageSearchResultItem#getImageResource()
   */
  public ImageResource getImageResource() {
    if (resource == null) {
      resource = new LazyImageResourceImpl(uri, imageXml, headerXml, previewXml);
    }
    return (ImageResource) resource;
  }

}