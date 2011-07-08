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

import ch.entwine.weblounge.common.content.PreviewGenerator;
import ch.entwine.weblounge.common.content.Resource;
import ch.entwine.weblounge.common.content.ResourceContent;
import ch.entwine.weblounge.common.content.ResourceContentReader;
import ch.entwine.weblounge.common.content.ResourceMetadata;
import ch.entwine.weblounge.common.content.page.Page;
import ch.entwine.weblounge.common.impl.content.page.PageImpl;
import ch.entwine.weblounge.common.impl.content.page.PagePreviewGenerator;
import ch.entwine.weblounge.common.impl.content.page.PageReader;
import ch.entwine.weblounge.common.impl.content.page.PageURIImpl;
import ch.entwine.weblounge.common.site.Site;
import ch.entwine.weblounge.contentrepository.impl.index.solr.PageInputDocument;

import org.osgi.service.component.ComponentContext;
import org.xml.sax.SAXException;

import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

/**
 * Resource serializer for pages.
 */
public class PageSerializer extends AbstractResourceSerializer<ResourceContent, Page> {

  /** The preview generator */
  protected PagePreviewGenerator previewGenerator = null;

  /**
   * Creates a new page serializer.
   */
  public PageSerializer() {
    super(Page.TYPE);
    previewGenerator = new PagePreviewGenerator();
  }

  /**
   * Callback from OSGi declarative services on component startup.
   * 
   * @param ctx
   *          the component context
   */
  void activate(ComponentContext ctx) {
    previewGenerator.activate(ctx);
  }

  /**
   * Callback from OSGi declarative services on component shutdown.
   */
  void deactivate() {
    previewGenerator.deactivate();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.contentrepository.ResourceSerializer#getMimeType(ch.entwine.weblounge.common.content.ResourceContent)
   */
  public String getMimeType(ResourceContent resourceContent) {
    return "text/html";
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.contentrepository.ResourceSerializer#supportsContent(java.lang.String)
   */
  public boolean supportsContent(String mimeType) {
    return false;
  }
  
  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.contentrepository.ResourceSerializer#createNewResource(ch.entwine.weblounge.common.site.Site)
   */
  public Resource<ResourceContent> createNewResource(Site site) {
    return new PageImpl(new PageURIImpl(site));
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.contentrepository.impl.AbstractResourceSerializer#createNewReader()
   */
  @Override
  protected PageReader createNewReader()
      throws ParserConfigurationException, SAXException {
    return new PageReader();
  }

  /**
   * {@inheritDoc}
   *
   * @see ch.entwine.weblounge.contentrepository.ResourceSerializer#getMetadata(ch.entwine.weblounge.common.content.Resource)
   */
  public List<ResourceMetadata<?>> getMetadata(Resource<?> page) {
    return new PageInputDocument((Page)page).getMetadata();
  }

  /**
   * {@inheritDoc}
   *
   * @see ch.entwine.weblounge.contentrepository.ResourceSerializer#getContentReader()
   */
  public ResourceContentReader<ResourceContent> getContentReader()
      throws ParserConfigurationException, SAXException {
    return null;
  }
  
  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.contentrepository.ResourceSerializer#getPreviewGenerator()
   */
  public PreviewGenerator getPreviewGenerator() {
    return previewGenerator;
  }

  /**
   * {@inheritDoc}
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "Page serializer";
  }

}
