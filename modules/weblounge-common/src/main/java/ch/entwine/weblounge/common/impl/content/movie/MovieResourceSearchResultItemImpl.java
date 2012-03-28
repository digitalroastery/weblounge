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

package ch.entwine.weblounge.common.impl.content.movie;

import ch.entwine.weblounge.common.content.MovieSearchResultItem;
import ch.entwine.weblounge.common.content.ResourceMetadata;
import ch.entwine.weblounge.common.content.ResourceURI;
import ch.entwine.weblounge.common.content.movie.MovieResource;
import ch.entwine.weblounge.common.impl.content.AbstractResourceSearchResultItemImpl;
import ch.entwine.weblounge.common.url.WebUrl;

import java.util.List;

/**
 * Audio visual implementation of a
 * {@link ch.entwine.weblounge.common.content.SearchResultItem}.
 */
public class MovieResourceSearchResultItemImpl extends AbstractResourceSearchResultItemImpl implements MovieSearchResultItem {

  /** The audio visual xml */
  protected String audioVisualXml = null;

  /** The audio visual header xml */
  protected String headerXml = null;

  /** The audio visual preview xml */
  protected String previewXml = null;

  /**
   * Creates a new search result item with the given uri. The
   * <code>source</code> is the object that created the item, usually, this will
   * be the site itself but it could very well be a module that added to a
   * search result.
   * 
   * @param uri
   *          the audio visual uri
   * @param url
   *          the url to show the hit
   * @param relevance
   *          the score inside the search result
   * @param source
   *          the object that produced the result item
   * @param metadata
   *          the search metadata
   */
  public MovieResourceSearchResultItemImpl(ResourceURI uri, WebUrl url,
      double relevance, Object source, List<ResourceMetadata<?>> metadata) {
    super(uri, url, relevance, source, metadata);
  }

  /**
   * Sets the file xml.
   * 
   * @param xml
   *          the xml
   */
  public void setResourceXml(String xml) {
    this.audioVisualXml = xml;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.ResourceSearchResultItem#getResourceXml()
   */
  public String getResourceXml() {
    return this.audioVisualXml;
  }

  /**
   * Sets the audio visual header xml.
   * 
   * @param xml
   *          the xml
   */
  public void setAudioVisualHeaderXml(String xml) {
    this.headerXml = xml;
  }

  /**
   * Returns the xml that makes up the header portion of the file.
   * 
   * @return the file header xml
   */
  public String getAudioVisualHeaderXml() {
    return this.headerXml;
  }

  /**
   * Sets the audio visual header xml.
   * 
   * @param xml
   *          the xml
   */
  public void setAudioVisualPreviewXml(String xml) {
    this.previewXml = xml;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.MovieSearchResultItem#getMovieResource()
   */
  public MovieResource getMovieResource() {
    if (resource == null) {
      resource = new LazyMovieResourceImpl(uri, audioVisualXml, headerXml, previewXml);
    }
    return (MovieResource) resource;
  }

  /**
   * {@inheritDoc}
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "Audio Visual " + uri.toString();
  }

}