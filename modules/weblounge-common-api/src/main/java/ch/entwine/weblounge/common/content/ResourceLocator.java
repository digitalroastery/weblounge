/*
 *  Weblounge: Web Content Management System
 *  Copyright (c) 2011 The Weblounge Team
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

package ch.entwine.weblounge.common.content;

import ch.entwine.weblounge.common.content.image.ImageStyle;
import ch.entwine.weblounge.common.content.page.HTMLInclude;
import ch.entwine.weblounge.common.language.Language;

import java.net.URL;

/**
 * A resource locator maps a resource's {@link java.net.URL} pointing to the
 * local installation to the url, where it is actually stored.
 * <p>
 * The intended use is when writing alternate storage implementations. Code that
 * is in charge of creating links to resources is required to look for an
 * implementation of a {@link ResourceLocator} and consult it for the resource's
 * actual storage location.
 */
public interface ResourceLocator<T extends Resource<?>> {

  /**
   * Returns the {@link URL} to the actual resource location or
   * <code>null</code> if this locator is not able to resolve the location.
   * 
   * @param resource
   *          the resource to look up
   * @param language
   *          the language to look up
   * @return the location to the resource
   */
  URL getLocation(T resource, Language language);

  /**
   * Returns the {@link URL} to the resource's preview location or
   * <code>null</code> if this locator is not able to resolve the location.
   * 
   * @param resource
   *          the resource to look up
   * @param language
   *          the language to look up
   * @return the location to the resource
   */
  URL getLocation(T resource, Language language, ImageStyle imageStyle);

  /**
   * Returns the {@link URL} to the include's location or <code>null</code> if
   * the locator is not able to resolve the location.
   * 
   * @param include
   *          the include
   * @return the location to the include
   */
  URL getLocation(HTMLInclude include);

}
