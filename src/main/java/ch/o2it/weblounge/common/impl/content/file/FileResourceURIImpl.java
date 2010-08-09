/*
 *  Weblounge: Web Content Management System
 *  Copyright (c) 2010 The Weblounge Team
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

package ch.o2it.weblounge.common.impl.content.file;

import ch.o2it.weblounge.common.content.MalformedResourceURIException;
import ch.o2it.weblounge.common.content.ResourceURI;
import ch.o2it.weblounge.common.content.file.FileResource;
import ch.o2it.weblounge.common.impl.content.ResourceURIImpl;
import ch.o2it.weblounge.common.request.WebloungeRequest;
import ch.o2it.weblounge.common.site.Site;

/**
 * This a <code>ResourceURI</code> intended to represent files of type
 * <code>ch.o2it.weblounge.common.content.file.Page</code>.
 */
public class FileResourceURIImpl extends ResourceURIImpl {

  /** The serial version uid */
  private static final long serialVersionUID = -4786684576702578116L;

  /**
   * Creates a new {@link PageURI} from the given request, which is used to
   * determine <code>site</code>, <code>path</code> and <code>version</code>.
   * 
   * @param request
   *          the request
   */
  public FileResourceURIImpl(WebloungeRequest request) {
    super(FileResource.TYPE, request.getSite(), request.getUrl().getPath(), request.getVersion(), null);
  }

  /**
   * Creates a new {@link ResourceURI} that is equal to <code>uri</code> except
   * for the version which is switched to <code>version</code>.
   * 
   * @param uri
   *          the uri
   * @param version
   *          the version
   */
  public FileResourceURIImpl(ResourceURI uri, long version) {
    super(uri, version);
  }

  /**
   * Creates a new {@link ResourceURI} pointing to the live version of the file
   * identified by <code>site</code> and <code>path</code>.
   * 
   * @param site
   *          the site
   * @param path
   *          the path
   * @throws MalformedResourceURIException
   *           if the uri cannot be created. Usually, this is due to a malformed
   *           <code>path</code> parameter
   */
  public FileResourceURIImpl(Site site, String path)
      throws MalformedResourceURIException {
    super(FileResource.TYPE, site, path);
  }

  /**
   * Creates a new {@link ResourceURI} pointing to a specific version of the
   * file identified by <code>site</code>, <code>path</code> and
   * <code>version</code> .
   * 
   * @param site
   *          the site
   * @param path
   *          the path
   * @param version
   *          the version
   * @throws MalformedResourceURIException
   *           if the uri cannot be created. Usually, this is due to a malformed
   *           <code>path</code> parameter
   */
  public FileResourceURIImpl(Site site, String path, long version)
      throws MalformedResourceURIException {
    super(FileResource.TYPE, site, path, version);
  }

  /**
   * Creates a new {@link ResourceURI} pointing to a specific version of the
   * file identified by <code>id<code>, <code>site</code>, <code>path</code> and
   * <code>version</code>.
   * 
   * @param site
   *          the site
   * @param path
   *          the path
   * @param id
   *          the file identifier
   * @throws MalformedResourceURIException
   *           if the uri cannot be created. Usually, this is due to a malformed
   *           <code>path</code> parameter
   */
  public FileResourceURIImpl(Site site, String path, String id)
      throws MalformedResourceURIException {
    super(FileResource.TYPE, site, path, id);
  }

  /**
   * Creates a new {@link ResourceURI} pointing to a specific version of the
   * file identified by <code>id<code>, <code>site</code>, <code>path</code> and
   * <code>version</code>.
   * 
   * @param site
   *          the site
   * @param path
   *          the path
   * @param version
   *          the version
   * @param id
   *          the file identifier
   * @throws MalformedResourceURIException
   *           if the uri cannot be created. Usually, this is due to a malformed
   *           <code>path</code> parameter
   */
  public FileResourceURIImpl(Site site, String path, long version, String id)
      throws MalformedResourceURIException {
    super(FileResource.TYPE, site, path, version, id);
  }

}
