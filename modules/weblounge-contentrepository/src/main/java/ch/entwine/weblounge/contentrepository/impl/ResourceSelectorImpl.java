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

package ch.entwine.weblounge.contentrepository.impl;

import ch.entwine.weblounge.common.content.repository.ResourceSelector;
import ch.entwine.weblounge.common.language.Language;
import ch.entwine.weblounge.common.site.Site;

import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Default implementation for the search query api.
 */
public class ResourceSelectorImpl implements ResourceSelector {

  /** The site */
  protected Site site = null;

  /** The resource identifier */
  protected List<String> resourceId = new ArrayList<String>();

  /** The search language */
  protected Language language = null;

  /** The types */
  protected List<String> types = new ArrayList<String>();

  /** The types to block */
  protected List<String> withoutTypes = new ArrayList<String>();

  /** The query offset */
  protected int offset = 0;

  /** The query limit */
  protected int limit = -1;

  /** The resource versions */
  protected List<Long> versions = new ArrayList<Long>();

  /**
   * Creates a new search query that is operating on the given site.
   * 
   * @param site
   *          the site
   */
  public ResourceSelectorImpl(Site site) {
    this.site = site;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.ResourceSelector#getSite()
   */
  public Site getSite() {
    return site;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.ResourceSelector#withLimit(int)
   */
  public ResourceSelector withLimit(int limit) {
    this.limit = limit;
    return this;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.ResourceSelector#getLimit()
   */
  public int getLimit() {
    return limit;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.ResourceSelector#withOffset(int)
   */
  public ResourceSelector withOffset(int offset) {
    this.offset = Math.max(0, offset);
    return this;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.ResourceSelector#getOffset()
   */
  public int getOffset() {
    return offset;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.ResourceSelector#withIdentifier(java.lang.String)
   */
  public ResourceSelector withIdentifier(String id) {
    if (StringUtils.isBlank(id))
      throw new IllegalArgumentException("Id cannot be null");
    this.resourceId.add(id);
    return this;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.ResourceSelector#getIdentifiers()
   */
  public String[] getIdentifiers() {
    return resourceId.toArray(new String[resourceId.size()]);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.ResourceSelector#withTypes(java.lang.String)
   */
  public ResourceSelector withTypes(String... types) {
    for (String type : types) {
      this.types.add(type);
    }
    return this;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.ResourceSelector#withoutTypes(java.lang.String)
   */
  public ResourceSelector withoutTypes(String... types) {
    for (String type : types) {
      this.withoutTypes.add(type);
    }
    return this;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.ResourceSelector#getType()
   */
  public String[] getTypes() {
    return types.toArray(new String[types.size()]);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.ResourceSelector#getWithoutTypes()
   */
  public String[] getWithoutTypes() {
    return withoutTypes.toArray(new String[withoutTypes.size()]);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.ResourceSelector#withLanguage(ch.entwine.weblounge.common.language.Language)
   */
  public ResourceSelector withLanguage(Language language) {
    this.language = language;
    return this;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.ResourceSelector#getLanguage()
   */
  public Language getLanguage() {
    return language;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.ResourceSelector#withVersion(long)
   */
  public ResourceSelector withVersion(long version) {
    versions.add(version);
    return this;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.ResourceSelector#getVersions()
   */
  public Long[] getVersions() {
    return versions.toArray(new Long[versions.size()]);
  }

}
