/*
 *  Weblounge: Web Content Management System
 *  Copyright (c) 2013 The Weblounge Team
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
package ch.entwine.weblounge.jcr;

import ch.entwine.weblounge.common.content.ResourceURI;
import ch.entwine.weblounge.common.url.UrlUtils;

import org.apache.commons.lang.StringUtils;

/**
 * Utility class for working with resources and a JCR repository.
 */
public final class JCRResourceUtils {

  private JCRResourceUtils() {
  }

  /**
   * Returns the absolute path of the resource's node.
   * <p>
   * If you have a resource uri with a site value <code>my-site</code> and a
   * path <code>/my/test/resource/</code> this method will return
   * <code>/sites/my-site/resources/my/test/resource</code> as absolute node
   * path.
   * 
   * @param uri
   *          the resource uri
   * @return the absolute node path
   */
  public static String getAbsNodePath(ResourceURI uri) {
    if (uri == null)
      throw new IllegalArgumentException("URI must not be null");

    String absPath = UrlUtils.concat(JCRResourceConstants.SITES_ROOT_NODE_REL_PATH, uri.getSite().getIdentifier(), JCRResourceConstants.RESOURCES_NODE_NAME, uri.getPath());
    absPath = StringUtils.removeEnd(absPath, "/");
    absPath = "/" + absPath;
    return absPath;
  }

  /**
   * Return the absolute path of the resource's parent node.
   * <p>
   * If you have a resource uri with a site value <code>my-site</code> and a
   * path <code>/my/test/resource/</code> this method will return
   * <code>/sites/my-site/resources/my/test</code> as absolute node path.
   * 
   * @param uri
   *          the resource uri
   * @return the absolute path of the parent node
   */
  public static String getAbsParentNodePath(ResourceURI uri) {
    String absPath = getAbsNodePath(uri);
    absPath = absPath.substring(0, absPath.lastIndexOf("/"));
    return absPath;
  }

  /**
   * Returns the node name of the given URI.
   * <p>
   * For the uri with path <code>/this/is/my/page</code> the node name
   * </code>page</code> will be returned.
   * 
   * @param uri
   *          the uri
   * @return the node name
   */
  public static String getNodeName(ResourceURI uri) {
    if (uri == null)
      throw new IllegalArgumentException("URI must not be null");

    String absPath = getAbsNodePath(uri);
    String name = StringUtils.removeEnd(absPath, "/");
    return name.substring(name.lastIndexOf("/") + 1, absPath.length());
  }

}