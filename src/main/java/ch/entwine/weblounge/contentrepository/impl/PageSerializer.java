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

import static ch.entwine.weblounge.contentrepository.impl.index.solr.SolrFields.HEADER_XML;
import static ch.entwine.weblounge.contentrepository.impl.index.solr.SolrFields.ID;
import static ch.entwine.weblounge.contentrepository.impl.index.solr.SolrFields.PATH;
import static ch.entwine.weblounge.contentrepository.impl.index.solr.SolrFields.PREVIEW_XML;
import static ch.entwine.weblounge.contentrepository.impl.index.solr.SolrFields.XML;

import ch.entwine.weblounge.common.content.PageSearchResultItem;
import ch.entwine.weblounge.common.content.PreviewGenerator;
import ch.entwine.weblounge.common.content.Resource;
import ch.entwine.weblounge.common.content.ResourceContent;
import ch.entwine.weblounge.common.content.ResourceContentReader;
import ch.entwine.weblounge.common.content.ResourceMetadata;
import ch.entwine.weblounge.common.content.ResourceReader;
import ch.entwine.weblounge.common.content.ResourceURI;
import ch.entwine.weblounge.common.content.SearchResultItem;
import ch.entwine.weblounge.common.content.page.Page;
import ch.entwine.weblounge.common.impl.content.page.PageImpl;
import ch.entwine.weblounge.common.impl.content.page.PagePreviewGenerator;
import ch.entwine.weblounge.common.impl.content.page.PageReader;
import ch.entwine.weblounge.common.impl.content.page.PageSearchResultItemImpl;
import ch.entwine.weblounge.common.impl.content.page.PageURIImpl;
import ch.entwine.weblounge.common.impl.url.WebUrlImpl;
import ch.entwine.weblounge.common.site.Site;
import ch.entwine.weblounge.common.url.WebUrl;
import ch.entwine.weblounge.contentrepository.impl.index.solr.PageInputDocument;

import org.apache.commons.io.IOUtils;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

/**
 * Resource serializer for pages.
 */
public class PageSerializer extends AbstractResourceSerializer<ResourceContent, Page> {

  /** The logging facility */
  private static final Logger logger = LoggerFactory.getLogger(PageSerializer.class);

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
   * @see ch.entwine.weblounge.contentrepository.ResourceSerializer#supports(java.lang.String)
   */
  public boolean supports(String mimeType) {
    return false;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.contentrepository.ResourceSerializer#newResource(ch.entwine.weblounge.common.site.Site)
   */
  public Resource<ResourceContent> newResource(Site site) {
    return new PageImpl(new PageURIImpl(site));
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.contentrepository.impl.AbstractResourceSerializer#createNewReader()
   */
  @Override
  protected PageReader createNewReader() throws ParserConfigurationException,
      SAXException {
    return new PageReader();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.contentrepository.ResourceSerializer#toMetadata(ch.entwine.weblounge.common.content.Resource)
   */
  public List<ResourceMetadata<?>> toMetadata(Resource<?> page) {
    return new PageInputDocument((Page) page).getMetadata();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.contentrepository.ResourceSerializer#toMetadata(ch.entwine.weblounge.common.content.SearchResultItem)
   */
  public List<ResourceMetadata<?>> toMetadata(SearchResultItem searchResultItem) {
    if (!(searchResultItem instanceof PageSearchResultItem))
      throw new IllegalArgumentException("This serializer can only handle page search result items");
    PageSearchResultItem pageSearchResultItem = (PageSearchResultItem) searchResultItem;
    String resourceXml = pageSearchResultItem.getResourceXml();
    try {
      Page page = getReader().read(IOUtils.toInputStream(resourceXml, "UTF-8"), searchResultItem.getSite());
      return toMetadata(page);
    } catch (SAXException e) {
      logger.warn("Error parsing page " + searchResultItem.getId(), e);
      return null;
    } catch (IOException e) {
      logger.warn("Error parsing page " + searchResultItem.getId(), e);
      return null;
    } catch (ParserConfigurationException e) {
      logger.warn("Error parsing page " + searchResultItem.getId(), e);
      return null;
    }
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.contentrepository.ResourceSerializer#toResource(ch.entwine.weblounge.common.site.Site,
   *      java.util.List)
   */
  public Resource<?> toResource(Site site, List<ResourceMetadata<?>> metadata) {
    for (ResourceMetadata<?> metadataItem : metadata) {
      if (XML.equals(metadataItem.getName())) {
        String resourceXml = (String) metadataItem.getValues().get(0);
        try {
          ResourceReader<ResourceContent, Page> reader = getReader();
          Page page = reader.read(IOUtils.toInputStream(resourceXml, "UTF-8"), site);
          return page;
        } catch (SAXException e) {
          logger.warn("Error parsing page from metadata", e);
          return null;
        } catch (IOException e) {
          logger.warn("Error parsing page from metadata", e);
          return null;
        } catch (ParserConfigurationException e) {
          logger.warn("Error parsing page from metadata", e);
          return null;
        }
      }
    }
    return null;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.contentrepository.ResourceSerializer#toSearchResultItem(ch.entwine.weblounge.common.site.Site,
   *      double, List)
   */
  public SearchResultItem toSearchResultItem(Site site, double relevance,
      List<ResourceMetadata<?>> metadata) {
    
    Map<String, ResourceMetadata<?>> metadataMap = new HashMap<String, ResourceMetadata<?>>();
    for (ResourceMetadata<?> metadataItem : metadata) {
      metadataMap.put(metadataItem.getName(), metadataItem);
    }
    
    String id = (String) metadataMap.get(ID).getValues().get(0);
    String path = (String) metadataMap.get(PATH).getValues().get(0);

    ResourceURI uri = new PageURIImpl(site, path, id, Resource.LIVE);
    WebUrl url = new WebUrlImpl(site, path);

    PageSearchResultItemImpl result = new PageSearchResultItemImpl(uri, url, relevance, site);

    if (metadataMap.get(XML) != null)
      result.setResourceXml((String) metadataMap.get(XML).getValues().get(0));
    if (metadataMap.get(HEADER_XML) != null)
      result.setPageHeaderXml((String) metadataMap.get(HEADER_XML).getValues().get(0));
    if (metadataMap.get(PREVIEW_XML) != null)
      result.setPagePreviewXml((String) metadataMap.get(PREVIEW_XML).getValues().get(0));

    return result;
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
