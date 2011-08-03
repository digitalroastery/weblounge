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

import ch.entwine.weblounge.common.content.PreviewGenerator;
import ch.entwine.weblounge.common.content.Resource;
import ch.entwine.weblounge.common.content.ResourceContentReader;
import ch.entwine.weblounge.common.content.ResourceMetadata;
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

import org.xml.sax.SAXException;

import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

/**
 * Implementation of a serializer for file resources.
 */
public class FileResourceSerializer extends AbstractResourceSerializer<FileContent, FileResource> {

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
   * @see ch.entwine.weblounge.contentrepository.ResourceSerializer#supportsContent(java.lang.String)
   */
  public boolean supportsContent(String mimeType) {
    // This implementation always returns <code>false</code>, as it is the
    // default implementation anyway. Returning false here will give more
    // specialized serializers a chance to pick up the request
    return false;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.contentrepository.ResourceSerializer#createNewResource(ch.entwine.weblounge.common.site.Site)
   */
  public Resource<FileContent> createNewResource(Site site) {
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
   * @see ch.entwine.weblounge.contentrepository.ResourceSerializer#getMetadata(ch.entwine.weblounge.common.content.Resource)
   */
  public List<ResourceMetadata<?>> getMetadata(Resource<?> resource) {
    return new FileInputDocument((FileResource) resource).getMetadata();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.contentrepository.ResourceSerializer#createSearchResultItem(ch.entwine.weblounge.common.site.Site,
   *      double, java.util.Map)
   */
  public SearchResultItem createSearchResultItem(Site site, double relevance,
      Map<String, ResourceMetadata<?>> metadata) {
    String id = (String) metadata.get(ID).getValues().get(0);

    String path = null;
    if (metadata.get(PATH) != null)
      path = (String) metadata.get(PATH).getValues().get(0);
    else {
      path = URI_PREFIX + "/" + id;
    }

    ResourceURI uri = new FileResourceURIImpl(site, path, id, Resource.LIVE);
    WebUrl url = new WebUrlImpl(site, path);

    FileResourceSearchResultItemImpl result = new FileResourceSearchResultItemImpl(uri, url, relevance, site);

    if (metadata.get(XML) != null)
      result.setFileXml((String) metadata.get(XML).getValues().get(0));
    if (metadata.get(HEADER_XML) != null)
      result.setFileHeaderXml((String) metadata.get(HEADER_XML).getValues().get(0));
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
