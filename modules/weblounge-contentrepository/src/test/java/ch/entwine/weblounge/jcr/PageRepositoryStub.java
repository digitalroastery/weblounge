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

import ch.entwine.weblounge.common.site.Site;

import javax.jcr.Repository;

/**
 * TODO: Comment PageRepositoryStub
 */
public class PageRepositoryStub extends PageRepository {

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.jcr.AbstractResourceRepository#bindRepository(javax.jcr.Repository)
   */
  @Override
  public void bindRepository(Repository repository) {
    super.bindRepository(repository);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.jcr.AbstractResourceRepository#bindSite(ch.entwine.weblounge.common.site.Site)
   */
  @Override
  public void bindSite(Site site) {
    super.bindSite(site);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.jcr.AbstractResourceRepository#bindJCRResourceSerializerRegistry(ch.entwine.weblounge.jcr.JCRResourceSerializerRegistry)
   */
  @Override
  public void bindJCRResourceSerializerRegistry(
      JCRResourceSerializerRegistry serializerRegistry) {
    super.bindJCRResourceSerializerRegistry(serializerRegistry);
  }

}
