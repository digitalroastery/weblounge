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
 * TODO: Comment JCRResourceUtils
 */
public class JCRResourceUtils {

  public static String getAbsNodePath(ResourceURI uri) {
    String absPath = UrlUtils.concat(JCRResourceConstants.SITES_ROOT_NODE_REL_PATH, uri.getSite().getIdentifier(), JCRResourceConstants.RESOURCES_NODE_NAME, uri.getPath());
    absPath = StringUtils.removeEnd(absPath, "/");
    absPath = "/" + absPath;
    return absPath;
  }

  public static String getAbsParentNodePath(ResourceURI uri) {
    String absPath = getAbsNodePath(uri);
    absPath = absPath.substring(0, absPath.lastIndexOf("/"));
    return absPath;
  }

  public static String getNodeName(ResourceURI uri) {
    String path = uri.getPath();
    String name = StringUtils.removeEnd(path, "/");
    return name.substring(name.lastIndexOf("/") + 1, path.length() - 1);
  }

}