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

package ch.entwine.weblounge.common.impl.content.file;

import ch.entwine.weblounge.common.content.FileSearchResultItem;
import ch.entwine.weblounge.common.content.ResourceURI;
import ch.entwine.weblounge.common.content.file.FileResource;
import ch.entwine.weblounge.common.impl.content.AbstractResourceSearchResultItemImpl;
import ch.entwine.weblounge.common.url.WebUrl;

/**
 * Default implementation of a
 * {@link ch.entwine.weblounge.common.content.SearchResultItem}.
 */
public class FileResourceSearchResultItemImpl extends AbstractResourceSearchResultItemImpl implements FileSearchResultItem {

  /** The file xml */
  protected String fileXml = null;

  /** The file header xml */
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
  public FileResourceSearchResultItemImpl(ResourceURI uri, WebUrl url,
      double relevance, Object source) {
    super(uri, url, relevance, source);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.ResourceSearchResultItem#getResourceURI()
   */
  public ResourceURI getResourceURI() {
    return new FileResourceURIImpl(url.getSite(), url.getPath(), id);
  }

  /**
   * Sets the file xml.
   * 
   * @param xml
   *          the xml
   */
  public void setFileXml(String xml) {
    this.fileXml = xml;
  }

  /**
   * Returns the xml that makes up the whole file.
   * 
   * @return the file xml
   */
  public String getResourceXml() {
    return this.fileXml;
  }

  /**
   * Sets the file header xml.
   * 
   * @param xml
   *          the xml
   */
  public void setFileHeaderXml(String xml) {
    this.headerXml = xml;
  }

  /**
   * Returns the xml that makes up the header portion of the file.
   * 
   * @return the file header xml
   */
  public String getFileHeaderXml() {
    return this.headerXml;
  }

  /**
   * Sets the file header xml.
   * 
   * @param xml
   *          the xml
   */
  public void setFilePreviewXml(String xml) {
    this.previewXml = xml;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.FileSearchResultItem#getFileResource()
   */
  public FileResource getFileResource() {
    if (resource == null) {
      ResourceURI uri = new FileResourceURIImpl(url.getSite(), url.getPath(), id);
      resource = new LazyFileResourceImpl(uri, fileXml, headerXml, previewXml);
    }
    return (FileResource) resource;
  }

  /**
   * {@inheritDoc}
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "File " + uri.toString();
  }

}