/*
 *  Weblounge: Web Content Management System
 *  Copyright (c) 2012 The Weblounge Team
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
package ch.entwine.weblounge.common.impl.timeline;

import ch.entwine.weblounge.common.timeline.Asset;

import java.net.URL;

/**
 * Implementation of a timeline asset.
 */
public class AssetImpl implements Asset {

  /** URL to the media */
  protected URL media = null;

  /** URL to the thumbnail */
  protected URL thumbnail = null;

  /** The media credit */
  protected String credit = null;

  /** The asset title */
  protected String caption = null;

  /**
   * Creates an asset that is pointing to the given URL.
   * 
   * @param url
   *          the url
   */
  public AssetImpl(URL mediaURL) {
    this.media = mediaURL;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.timeline.Asset#setMedia(java.net.URL)
   */
  @Override
  public void setMedia(URL mediaURL) {
    if (mediaURL == null)
      throw new IllegalArgumentException("Url must not be null");
    this.media = mediaURL;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.timeline.Asset#getMedia()
   */
  @Override
  public URL getMedia() {
    return media;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.timeline.Asset#setThumbnail(java.net.URL)
   */
  @Override
  public void setThumbnail(URL url) {
    this.thumbnail = url;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.timeline.Asset#getThumbnail()
   */
  @Override
  public URL getThumbnail() {
    return thumbnail;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.timeline.Asset#setCredit(java.lang.String)
   */
  @Override
  public void setCredit(String credits) {
    this.credit = credits;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.timeline.Asset#getCredit()
   */
  @Override
  public String getCredit() {
    return credit;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.timeline.Asset#setCaption(java.lang.String)
   */
  @Override
  public void setCaption(String caption) {
    this.caption = caption;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.timeline.Asset#getCaption()
   */
  @Override
  public String getCaption() {
    return caption;
  }

}
