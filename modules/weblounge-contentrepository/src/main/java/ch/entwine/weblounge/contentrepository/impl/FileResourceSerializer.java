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

import static ch.entwine.weblounge.search.impl.IndexSchema.HEADER_XML;
import static ch.entwine.weblounge.search.impl.IndexSchema.PATH;
import static ch.entwine.weblounge.search.impl.IndexSchema.RESOURCE_ID;
import static ch.entwine.weblounge.search.impl.IndexSchema.VERSION;
import static ch.entwine.weblounge.search.impl.IndexSchema.XML;

import ch.entwine.weblounge.common.content.PreviewGenerator;
import ch.entwine.weblounge.common.content.Resource;
import ch.entwine.weblounge.common.content.ResourceContentReader;
import ch.entwine.weblounge.common.content.ResourceMetadata;
import ch.entwine.weblounge.common.content.ResourceReader;
import ch.entwine.weblounge.common.content.ResourceURI;
import ch.entwine.weblounge.common.content.SearchResultItem;
import ch.entwine.weblounge.common.content.file.FileContent;
import ch.entwine.weblounge.common.content.file.FilePreviewGenerator;
import ch.entwine.weblounge.common.content.file.FileResource;
import ch.entwine.weblounge.common.impl.content.file.FileContentReader;
import ch.entwine.weblounge.common.impl.content.file.FileResourceImpl;
import ch.entwine.weblounge.common.impl.content.file.FileResourceReader;
import ch.entwine.weblounge.common.impl.content.file.FileResourceSearchResultItemImpl;
import ch.entwine.weblounge.common.impl.content.file.FileResourceURIImpl;
import ch.entwine.weblounge.common.impl.url.WebUrlImpl;
import ch.entwine.weblounge.common.language.Language;
import ch.entwine.weblounge.common.security.User;
import ch.entwine.weblounge.common.site.Site;
import ch.entwine.weblounge.common.url.WebUrl;
import ch.entwine.weblounge.search.impl.FileInputDocument;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
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

  /** The preview generators */
  protected List<FilePreviewGenerator> previewGenerators = new ArrayList<FilePreviewGenerator>();

  /**
   * Creates a new file resource serializer.
   */
  public FileResourceSerializer() {
    super(FileResource.TYPE);
  }

  @Override
  public String getMimeType(FileContent resourceContent) {
    return resourceContent.getMimetype();
  }

  @Override
  public boolean supports(String mimeType) {
    // This implementation always returns <code>false</code>, as it is the
    // default implementation anyway. Returning false here will give more
    // specialized serializers a chance to pick up the request
    return false;
  }

  @Override
  public Resource<FileContent> newResource(Site site) {
    return new FileResourceImpl(new FileResourceURIImpl(site));
  }

  @Override
  public Resource<FileContent> newResource(Site site, InputStream is,
      User user, Language language) {
    Resource<FileContent> fileResource = newResource(site);
    fileResource.setCreated(user, new Date());
    return fileResource;
  }

  @Override
  protected FileResourceReader createNewReader()
      throws ParserConfigurationException, SAXException {
    return new FileResourceReader();
  }

  @Override
  public List<ResourceMetadata<?>> toMetadata(Resource<?> resource) {
    if (resource != null)
      return new FileInputDocument((FileResource) resource).getMetadata();
    return null;
  }

  @Override
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

  @Override
  public SearchResultItem toSearchResultItem(Site site, double relevance,
      List<ResourceMetadata<?>> metadata) {

    Map<String, ResourceMetadata<?>> metadataMap = new HashMap<String, ResourceMetadata<?>>();
    for (ResourceMetadata<?> metadataItem : metadata) {
      metadataMap.put(metadataItem.getName(), metadataItem);
    }

    // resource id
    String id = (String) metadataMap.get(RESOURCE_ID).getValues().get(0);

    // resource version
    long version = (Long) metadataMap.get(VERSION).getValues().get(0);

    // path
    String path = null;
    WebUrl url = null;
    if (metadataMap.get(PATH) != null) {
      try {
        path = (String) metadataMap.get(PATH).getValues().get(0);
        url = new WebUrlImpl(site, path);
      } catch (IllegalArgumentException e) {
        logger.debug("Path {}:/{} for file {} is invalid", new Object[] {
            site.getIdentifier(),
            path,
            id });
        path = URI_PREFIX + "/" + id;
        url = new WebUrlImpl(site, path);
      }
    } else {
      path = URI_PREFIX + "/" + id;
      url = new WebUrlImpl(site, path);
    }

    ResourceURI uri = new FileResourceURIImpl(site, path, id, version);
    FileResourceSearchResultItemImpl result = new FileResourceSearchResultItemImpl(uri, url, relevance, site, metadata);

    if (metadataMap.get(XML) != null)
      result.setFileXml((String) metadataMap.get(XML).getValues().get(0));
    if (metadataMap.get(HEADER_XML) != null)
      result.setFileHeaderXml((String) metadataMap.get(HEADER_XML).getValues().get(0));
    // TODO: Add remaining metadata

    return result;
  }

  @Override
  public ResourceContentReader<FileContent> getContentReader()
      throws ParserConfigurationException, SAXException {
    return new FileContentReader();
  }

  @Override
  public PreviewGenerator getPreviewGenerator(Resource<?> resource, Language language) {
    for (FilePreviewGenerator generator : previewGenerators) {
      if (generator.supports(resource, language)) {
        logger.trace("File preview generator {} agrees to handle {}", generator, resource);
        return generator;
      }
    }
    logger.trace("No file preview generator found to handle {}", resource);
    return null;
  }

  /**
   * Adds the preview generator to the list of registered preview generators.
   * 
   * @param generator
   *          the generator
   */
  void addPreviewGenerator(FilePreviewGenerator generator) {
    previewGenerators.add(generator);
    Collections.sort(previewGenerators, new Comparator<PreviewGenerator>() {
      public int compare(PreviewGenerator a, PreviewGenerator b) {
        return Integer.valueOf(b.getPriority()).compareTo(a.getPriority());
      }
    });
  }

  /**
   * Removes the preview generator from the list of registered preview
   * generators.
   * 
   * @param generator
   *          the generator
   */
  void removePreviewGenerator(FilePreviewGenerator generator) {
    previewGenerators.remove(generator);
  }

  @Override
  public String toString() {
    return "File serializer";
  }

}
