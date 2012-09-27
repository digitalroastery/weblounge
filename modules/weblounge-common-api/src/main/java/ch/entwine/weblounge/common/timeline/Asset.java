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
package ch.entwine.weblounge.common.timeline;

import java.net.URL;

/**
 * An asset can be attached to a {@link PointInTime} and represent media that
 * goes along with the event.
 */
public interface Asset {

  /**
   * Sets the asset's media.
   * 
   * @param url
   *          the url
   */
  void setMedia(URL url);

  /**
   * Returns the link to the asset's media.
   * 
   * @return the media
   */
  URL getMedia();

  /**
   * Sets the asset's thumbnail.
   * 
   * @param url
   *          the url
   */
  void setThumbnail(URL url);

  /**
   * Returns the link to the asset's thumbnail or <code>null</code> if no
   * thumbnail was specified.
   * 
   * @return the thumbnail
   */
  URL getThumbnail();

  /**
   * Sets the credits on this asset.
   * 
   * @param credits
   *          the credits
   */
  void setCredit(String credits);

  /**
   * Returns credits for this asset.
   * 
   * @return the credits
   */
  String getCredit();

  /**
   * Sets the asset's title.
   * 
   * @param caption
   *          the title
   */
  void setCaption(String caption);

  /**
   * Returns the asset's title.
   * 
   * @return the title
   */
  String getCaption();

}
