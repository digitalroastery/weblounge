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
import static ch.entwine.weblounge.contentrepository.impl.index.solr.SolrFields.XML;

import ch.entwine.weblounge.common.content.FileSearchResultItem;
import ch.entwine.weblounge.common.content.PreviewGenerator;
import ch.entwine.weblounge.common.content.Resource;
import ch.entwine.weblounge.common.content.ResourceContentReader;
import ch.entwine.weblounge.common.content.ResourceMetadata;
import ch.entwine.weblounge.common.content.ResourceReader;
import ch.entwine.weblounge.common.content.ResourceURI;
import ch.entwine.weblounge.common.content.SearchResultItem;
import ch.entwine.weblounge.common.content.file.FileContent;
import ch.entwine.weblounge.common.content.file.FileResource;
import ch.entwine.weblounge.common.impl.content.file.FileContentReader;
import ch.entwine.weblounge.common.impl.content.file.FileResourceImpl;
import ch.entwine.weblounge.common.impl.content.file.FileResourceReader;
import ch.entwine.weblounge.common.impl.content.file.FileResourceSearchResultItemImpl;
import ch.entwine.weblounge.common.impl.content.file.FileResourceURIImpl;
import ch.entwine.weblounge.common.impl.url.WebUrlImpl;
import ch.entwine.weblounge.common.site.Site;
import ch.entwine.weblounge.common.url.WebUrl;
import ch.entwine.weblounge.contentrepository.impl.index.solr.FileInputDocument;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

/**
 * Implementation of a serializer for file resources.
 */
public class FileResourceSerializer extends AbstractResourceSerializer<FileContent, FileResource> {

  /** The logging facility */
  private static final Logger logger = LoggerFactory.getLogger(FileResourceSerializer.class);

  /** Alternate uri prefix */
  protected static final String URI_PREFIX = "/weblounge-files/";

  /** The preview generator */
  protected PreviewGenerator previewGenerator = null;

  /**
   * Creates a new file resource serializer.
   */
  public FileResourceSerializer() {
    super(FileResource.TYPE);
    // previewGenerator = new FilePreviewGenerator();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.contentrepository.ResourceSerializer#getMimeType(ch.entwine.weblounge.common.content.ResourceContent)
   */
  public String getMimeType(FileContent resourceContent) {
    return resourceContent.getMimetype();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.contentrepository.ResourceSerializer#supports(java.lang.String)
   */
  public boolean supports(String mimeType) {
    // This implementation always returns <code>false</code>, as it is the
    // default implementation anyway. Returning false here will give more
    // specialized serializers a chance to pick up the request
    return false;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.contentrepository.ResourceSerializer#newResource(ch.entwine.weblounge.common.site.Site)
   */
  public Resource<FileContent> newResource(Site site) {
    return new FileResourceImpl(new FileResourceURIImpl(site));
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.contentrepository.impl.AbstractResourceSerializer#createNewReader()
   */
  @Override
  protected FileResourceReader createNewReader()
      throws ParserConfigurationException, SAXException {
    return new FileResourceReader();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.contentrepository.ResourceSerializer#toMetadata(ch.entwine.weblounge.common.content.Resource)
   */
  public List<ResourceMetadata<?>> toMetadata(Resource<?> resource) {
    return new FileInputDocument((FileResource) resource).getMetadata();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.contentrepository.ResourceSerializer#toMetadata(ch.entwine.weblounge.common.content.SearchResultItem)
   */
  public List<ResourceMetadata<?>> toMetadata(SearchResultItem searchResultItem) {
    if (!(searchResultItem instanceof FileSearchResultItem))
      throw new IllegalArgumentException("This serializer can only handle file search result items");
    FileSearchResultItem fsri = (FileSearchResultItem)searchResultItem;
    String resourceXml = fsri.getResourceXml();
    Site site = searchResultItem.getSite();
    try {
      FileResource r = getReader().read(IOUtils.toInputStream(resourceXml, "UTF-8"), site);
      return toMetadata(r);
    } catch (SAXException e) {
      logger.warn("Error parsing resource " + searchResultItem.getId(), e);
      return null;
    } catch (IOException e) {
      logger.warn("Error parsing resource " + searchResultItem.getId(), e);
      return null;
    } catch (ParserConfigurationException e) {
      logger.warn("Error parsing resource " + searchResultItem.getId(), e);
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
          ResourceReader<FileContent, FileResource> reader = getReader();
          FileResource file = reader.read(IOUtils.toInputStream(resourceXml, "UTF-8"), site);
          return file;
        } catch (SAXException e) {
          logger.warn("Error parsing file from metadata", e);
          return null;
        } catch (IOException e) {
          logger.warn("Error parsing file from metadata", e);
          return null;
        } catch (ParserConfigurationException e) {
          logger.warn("Error parsing file from metadata", e);
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

    String path = null;
    if (metadataMap.get(PATH) != null)
      path = (String) metadataMap.get(PATH).getValues().get(0);
    else {
      path = URI_PREFIX + "/" + id;
    }

    ResourceURI uri = new FileResourceURIImpl(site, path, id, Resource.LIVE);
    WebUrl url = new WebUrlImpl(site, path);

    FileResourceSearchResultItemImpl result = new FileResourceSearchResultItemImpl(uri, url, relevance, site);

    if (metadataMap.get(XML) != null)
      result.setFileXml((String) metadataMap.get(XML).getValues().get(0));
    if (metadataMap.get(HEADER_XML) != null)
      result.setFileHeaderXml((String) metadataMap.get(HEADER_XML).getValues().get(0));
    // TODO: Add remaining metadata

    return result;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.contentrepository.ResourceSerializer#getContentReader()
   */
  public ResourceContentReader<FileContent> getContentReader()
      throws ParserConfigurationException, SAXException {
    return new FileContentReader();
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
    return "File serializer";
  }

}
