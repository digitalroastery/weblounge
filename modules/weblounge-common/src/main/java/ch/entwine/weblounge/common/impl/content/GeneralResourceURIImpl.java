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

package ch.entwine.weblounge.common.impl.content;

import ch.entwine.weblounge.common.content.MalformedResourceURIException;
import ch.entwine.weblounge.common.content.ResourceURI;
import ch.entwine.weblounge.common.site.Site;

/**
 * This implementation of a resource uri allows to specify resources by id and/
 * or path, without the need to also enter the resource type.
 */
public class GeneralResourceURIImpl extends ResourceURIImpl {

  /** Serial version uid */
  private static final long serialVersionUID = -7755049698079030934L;

  /**
   * Creates a new resource uri using id and path from the given uri and the
   * specified version.
   * 
   * @param uri
   *          the resource uri
   * @param version
   *          the new version
   */
  public GeneralResourceURIImpl(ResourceURI uri, long version) {
    super(null, uri.getSite(), uri.getPath(), uri.getIdentifier(), version);
  }

  /**
   * Creates a resource uri for the given site and path.
   * 
   * @param site
   *          the associated site
   * @param path
   *          the resource path
   * @throws MalformedResourceURIException
   *           if the path is malformed
   */
  public GeneralResourceURIImpl(Site site, String path)
      throws MalformedResourceURIException {
    super(null, site, path);
  }

  /**
   * Creates a resource uri for the given site, path and version.
   * 
   * @param site
   *          the associated site
   * @param path
   *          the resource path
   * @param version
   *          the version
   * @throws MalformedResourceURIException
   *           if the path is malformed
   */
  public GeneralResourceURIImpl(Site site, String path, long version)
      throws MalformedResourceURIException {
    super(null, site, path, version);
  }

  /**
   * Creates a resource uri for the given site, path and id.
   * 
   * @param site
   *          the associated site
   * @param path
   *          the resource path
   * @param id
   *          the resource identifier
   * @throws MalformedResourceURIException
   *           if the path is malformed
   */
  public GeneralResourceURIImpl(Site site, String path, String id)
      throws MalformedResourceURIException {
    super(null, site, path, id);
  }

  /**
   * Creates a resource uri for the given site, path, id and version.
   * 
   * @param site
   *          the associated site
   * @param path
   *          the resource path
   * @param id
   *          the resource identifier
   * @param version
   *          the version
   * @throws MalformedResourceURIException
   *           if the path is malformed
   */
  public GeneralResourceURIImpl(Site site, String path, String id, long version)
      throws MalformedResourceURIException {
    super(null, site, path, id, version);
  }

}
