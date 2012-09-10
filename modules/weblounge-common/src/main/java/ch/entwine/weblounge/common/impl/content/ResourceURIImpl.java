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

package ch.entwine.weblounge.common.impl.content;

import ch.entwine.weblounge.common.content.MalformedResourceURIException;
import ch.entwine.weblounge.common.content.Resource;
import ch.entwine.weblounge.common.content.ResourceURI;
import ch.entwine.weblounge.common.impl.url.UrlImpl;
import ch.entwine.weblounge.common.site.Site;

import java.util.UUID;

/**
 * Implementation of the {@link ResourceURI} interface.
 */
public class ResourceURIImpl extends UrlImpl implements ResourceURI {

  /** Serial version UID */
  private static final long serialVersionUID = -686750395794924219L;

  /** The resource identifier */
  protected String id = null;

  /** The associated site */
  protected Site site = null;

  /** The resource type */
  protected String type = null;

  /** The resource */
  protected long version = Resource.LIVE;

  /** The to string representation */
  private String external = null;

  /**
   * Creates a new {@link ResourceURI} that is equal to <code>uri</code> except
   * for the version which is switched to <code>version</code>.
   * 
   * @param uri
   *          the uri
   * @param version
   *          the version
   */
  public ResourceURIImpl(ResourceURI uri, long version) {
    this(uri.getType(), uri.getSite(), uri.getPath(), uri.getIdentifier(), version);
  }

  /**
   * Creates a new {@link ResourceURI} pointing to the live version of the
   * resource identified by <code>site</code> and <code>path</code>.
   * 
   * @param type
   *          the resource type
   * @param site
   *          the site
   * @param path
   *          the path
   * @throws MalformedResourceURIException
   *           if the uri cannot be created. Usually, this is due to a malformed
   *           <code>path</code> parameter
   */
  public ResourceURIImpl(String type, Site site, String path)
      throws MalformedResourceURIException {
    this(type, site, path, null, Resource.LIVE);
  }

  /**
   * Creates a new {@link ResourceURI} pointing to a specific version of the
   * resource identified by <code>site</code>, <code>path</code> and
   * <code>version</code>.
   * 
   * @param type
   *          the resource type
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
  public ResourceURIImpl(String type, Site site, String path, long version)
      throws MalformedResourceURIException {
    this(type, site, path, null, version);
  }

  /**
   * Creates a new {@link ResourceURI} pointing to a specific version of the
   * resource identified by <code>id<code>, <code>site</code>, <code>path</code>
   * and <code>version</code>.
   * 
   * @param type
   *          the resource type
   * @param site
   *          the site
   * @param path
   *          the path
   * @param id
   *          the resource identifier
   * @throws MalformedResourceURIException
   *           if the uri cannot be created. Usually, this is due to a malformed
   *           <code>path</code> parameter
   */
  public ResourceURIImpl(String type, Site site, String path, String id)
      throws MalformedResourceURIException {
    this(type, site, path, id, Resource.LIVE);
  }

  /**
   * Creates a new {@link ResourceURI} pointing to a specific version of the
   * resource identified by <code>id<code>, <code>site</code>, <code>path</code>
   * and <code>version</code>.
   * 
   * @param type
   *          the resource type
   * @param site
   *          the site
   * @param path
   *          the path
   * @param id
   *          the resource identifier
   * @param version
   *          the version
   * @throws MalformedResourceURIException
   *           if the uri cannot be created. Usually, this is due to a malformed
   *           <code>path</code> parameter
   */
  public ResourceURIImpl(String type, Site site, String path, String id,
      long version) throws MalformedResourceURIException {
    super(path, '/');
    if (site == null)
      throw new IllegalArgumentException("Site must not be null");
    if (path != null && !path.startsWith("/"))
      path = "/" + path;
    if (path == null && id == null)
      id = UUID.randomUUID().toString();
    this.type = type;
    this.site = site;
    this.id = id;
    this.version = version;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.ResourceURI#getUID()
   */
  @Override
  public String getUID() {
    if (id != null)
      return id + "." + version;
    else
      return path + "." + version;
  }

  /**
   * Sets the resource identifier.
   * 
   * @param id
   *          the identifier
   */
  public void setIdentifier(String id) {
    this.id = id;
    external = null;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.ResourceURI#getIdentifier()
   */
  public String getIdentifier() throws MalformedResourceURIException {
    return id;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.ResourceURI#getParentURI()
   */
  public ResourceURI getParentURI() throws MalformedResourceURIException {
    String parentPath = getParentPath();
    if (parentPath == null)
      return null;
    return new ResourceURIImpl(type, site, parentPath, id, version);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.ResourceURI#getPath()
   */
  @Override
  public String getPath() throws MalformedResourceURIException {
    return path;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.impl.url.UrlImpl#setPath(java.lang.String)
   */
  @Override
  public void setPath(String path) {
    super.setPath(path);
    external = null;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.ResourceURI#getSite()
   */
  public Site getSite() throws MalformedResourceURIException {
    return site;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.ResourceURI#setVersion(long)
   */
  public void setVersion(long version) {
    this.version = version;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.ResourceURI#getVersion()
   */
  public long getVersion() throws MalformedResourceURIException {
    return version;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.ResourceURI#getVersion(long)
   */
  public ResourceURI getVersion(long version)
      throws MalformedResourceURIException {
    return new ResourceURIImpl(type, site, path, version);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.ResourceURI#setType(java.lang.String)
   */
  public void setType(String type) {
    this.type = type;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.ResourceURI#getType()
   */
  public String getType() {
    return type;
  }

  /**
   * {@inheritDoc}
   * 
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    return (id != null) ? id.hashCode() : path.hashCode();
  }

  /**
   * {@inheritDoc}
   * 
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    if (obj instanceof ResourceURI) {
      ResourceURI uri = (ResourceURI) obj;
      if (id == null) {
        if (path != null && !pathEquals(uri))
          return false;
      } else if (uri.getIdentifier() == null) {
        if (path != null && !pathEquals(uri))
          return false;
      } else if (!id.equals(uri.getIdentifier()))
        return false;
      if (!pathEquals(uri))
        return false;
      if (version != uri.getVersion())
        return false;
      if (!site.equals(uri.getSite()))
        return false;
      if (type == null && uri.getType() != null)
        return false;
      if (type != null && !type.equals(uri.getType()))
        return false;
      return true;
    }
    return super.equals(obj);
  }

  /**
   * Returns <code>true</code> if the resource uri equals this one with respect
   * to the path.
   * 
   * @param uri
   *          the uri
   * @return <code>true</code> if the path of this uri equals that of
   *         <code>uri</code>
   */
  private boolean pathEquals(ResourceURI uri) {
    if (path == null && uri.getPath() != null)
      return false;
    if (path != null && !path.equals(uri.getPath()))
      return false;
    return true;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.impl.url.UrlImpl#toString()
   */
  @Override
  public String toString() {
    if (external == null) {
      StringBuffer buf = new StringBuffer(site.getIdentifier());
      buf.append(":");
      buf.append((path != null) ? path : id);
      external = buf.toString();
    }
    return external;
  }

}
