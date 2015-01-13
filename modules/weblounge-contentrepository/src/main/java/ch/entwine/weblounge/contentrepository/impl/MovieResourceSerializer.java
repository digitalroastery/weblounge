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
import ch.entwine.weblounge.common.content.movie.MovieContent;
import ch.entwine.weblounge.common.content.movie.MoviePreviewGenerator;
import ch.entwine.weblounge.common.content.movie.MovieResource;
import ch.entwine.weblounge.common.impl.content.movie.MovieContentReader;
import ch.entwine.weblounge.common.impl.content.movie.MovieResourceImpl;
import ch.entwine.weblounge.common.impl.content.movie.MovieResourceReader;
import ch.entwine.weblounge.common.impl.content.movie.MovieResourceSearchResultItemImpl;
import ch.entwine.weblounge.common.impl.content.movie.MovieResourceURIImpl;
import ch.entwine.weblounge.common.impl.url.WebUrlImpl;
import ch.entwine.weblounge.common.language.Language;
import ch.entwine.weblounge.common.security.User;
import ch.entwine.weblounge.common.site.Site;
import ch.entwine.weblounge.common.url.WebUrl;
import ch.entwine.weblounge.search.impl.MovieInputDocument;

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
 * Implementation of a serializer for image resources.
 */
public class MovieResourceSerializer extends AbstractResourceSerializer<MovieContent, MovieResource> {

  /** The logging facility */
  private static final Logger logger = LoggerFactory.getLogger(MovieResourceSerializer.class);

  /** Alternate uri prefix */
  protected static final String URI_PREFIX = "/weblounge-movies/";

  /** The preview generators */
  protected List<MoviePreviewGenerator> previewGenerators = new ArrayList<MoviePreviewGenerator>();

  /**
   * Creates a new image resource serializer.
   */
  public MovieResourceSerializer() {
    super(MovieResource.TYPE);
  }

  @Override
  public String getMimeType(MovieContent resourceContent) {
    return resourceContent.getMimetype();
  }

  @Override
  public boolean supports(String mimeType) {
    if (mimeType == null)
      return false;
    if (mimeType.toLowerCase().startsWith("audio/"))
      return true;
    if (mimeType.toLowerCase().startsWith("video/"))
      return true;
    return false;
  }

  @Override
  public Resource<MovieContent> newResource(Site site) {
    return new MovieResourceImpl(new MovieResourceURIImpl(site));
  }

  @Override
  public Resource<MovieContent> newResource(Site site, InputStream is,
      User user, Language language) {

    // TODO: Extract av metadata

    MovieResource avResource = new MovieResourceImpl(new MovieResourceURIImpl(site));
    avResource.setCreated(user, new Date());

    return avResource;
  }

  @Override
  protected MovieResourceReader createNewReader()
      throws ParserConfigurationException, SAXException {
    return new MovieResourceReader();
  }

  @Override
  public List<ResourceMetadata<?>> toMetadata(Resource<?> resource) {
    if (resource != null) {
      return new MovieInputDocument((MovieResource) resource).getMetadata();
    }
    return null;
  }

  @Override
  public Resource<?> toResource(Site site, List<ResourceMetadata<?>> metadata) {
    for (ResourceMetadata<?> metadataItem : metadata) {
      if (XML.equals(metadataItem.getName())) {
        String resourceXml = (String) metadataItem.getValues().get(0);
        try {
          ResourceReader<MovieContent, MovieResource> reader = getReader();
          MovieResource audioVisual = reader.read(IOUtils.toInputStream(resourceXml, "UTF-8"), site);
          return audioVisual;
        } catch (SAXException e) {
          logger.warn("Error parsing audio visual resource from metadata", e);
          return null;
        } catch (IOException e) {
          logger.warn("Error parsing audio visual resource from metadata", e);
          return null;
        } catch (ParserConfigurationException e) {
          logger.warn("Error parsing audio visual resource from metadata", e);
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
        logger.debug("Path {}:/{} for movie {} is invalid", new Object[] {
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

    ResourceURI uri = new MovieResourceURIImpl(site, path, id, version);
    MovieResourceSearchResultItemImpl result = new MovieResourceSearchResultItemImpl(uri, url, relevance, site, metadata);

    if (metadataMap.get(XML) != null)
      result.setResourceXml((String) metadataMap.get(XML).getValues().get(0));
    if (metadataMap.get(HEADER_XML) != null)
      result.setAudioVisualHeaderXml((String) metadataMap.get(HEADER_XML).getValues().get(0));
    // TODO: Add remaining metadata

    return result;
  }

  @Override
  public ResourceContentReader<MovieContent> getContentReader()
      throws ParserConfigurationException, SAXException {
    return new MovieContentReader();
  }

  @Override
  public PreviewGenerator getPreviewGenerator(Resource<?> resource, Language language) {
    for (MoviePreviewGenerator generator : previewGenerators) {
      if (generator.supports(resource, language)) {
        logger.trace("Movie preview generator {} agrees to handle {}", generator, resource);
        return generator;
      }
    }
    logger.trace("No movie preview generator found to handle {}", resource);
    return null;
  }

  /**
   * Adds the preview generator to the list of registered preview generators.
   * 
   * @param generator
   *          the generator
   */
  void addPreviewGenerator(MoviePreviewGenerator generator) {
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
  void removePreviewGenerator(MoviePreviewGenerator generator) {
    previewGenerators.remove(generator);
  }

  /**
   * {@inheritDoc}
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "Audio visual serializer";
  }

}
