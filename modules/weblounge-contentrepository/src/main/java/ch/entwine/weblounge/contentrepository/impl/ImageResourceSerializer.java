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
import ch.entwine.weblounge.common.content.image.ImageContent;
import ch.entwine.weblounge.common.content.image.ImagePreviewGenerator;
import ch.entwine.weblounge.common.content.image.ImageResource;
import ch.entwine.weblounge.common.impl.content.image.ImageContentReader;
import ch.entwine.weblounge.common.impl.content.image.ImageMetadata;
import ch.entwine.weblounge.common.impl.content.image.ImageMetadataUtils;
import ch.entwine.weblounge.common.impl.content.image.ImageResourceImpl;
import ch.entwine.weblounge.common.impl.content.image.ImageResourceReader;
import ch.entwine.weblounge.common.impl.content.image.ImageResourceSearchResultItemImpl;
import ch.entwine.weblounge.common.impl.content.image.ImageResourceURIImpl;
import ch.entwine.weblounge.common.impl.url.WebUrlImpl;
import ch.entwine.weblounge.common.language.Language;
import ch.entwine.weblounge.common.security.User;
import ch.entwine.weblounge.common.site.Site;
import ch.entwine.weblounge.common.url.WebUrl;
import ch.entwine.weblounge.search.impl.ImageInputDocument;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import java.io.BufferedInputStream;
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
public class ImageResourceSerializer extends AbstractResourceSerializer<ImageContent, ImageResource> {

  /** The logging facility */
  private static final Logger logger = LoggerFactory.getLogger(ImageResourceSerializer.class);

  /** Alternate uri prefix */
  protected static final String URI_PREFIX = "/weblounge-images/";

  /** The preview generators */
  protected List<ImagePreviewGenerator> previewGenerators = new ArrayList<ImagePreviewGenerator>();

  /**
   * this gets rid of exception for not using native acceleration
   */
  static {
    System.setProperty("com.sun.media.jai.disableMediaLib", "true");
  }

  /**
   * Creates a new image resource serializer.
   */
  public ImageResourceSerializer() {
    super(ImageResource.TYPE);
  }

  @Override
  public String getMimeType(ImageContent resourceContent) {
    return resourceContent.getMimetype();
  }

  @Override
  public boolean supports(String mimeType) {
    return mimeType != null && mimeType.toLowerCase().startsWith("image/");
  }

  @Override
  public Resource<ImageContent> newResource(Site site) {
    return new ImageResourceImpl(new ImageResourceURIImpl(site));
  }

  @Override
  public Resource<ImageContent> newResource(Site site, InputStream is,
      User user, Language language) {
    ImageMetadata imageMetadata = ImageMetadataUtils.extractMetadata(new BufferedInputStream(is));
    ImageResource imageResource = new ImageResourceImpl(new ImageResourceURIImpl(site));
    imageResource.setCreated(user, new Date());

    if (imageMetadata == null)
      return imageResource;

    if (!StringUtils.isBlank(imageMetadata.getCaption())) {
      imageResource.setTitle(imageMetadata.getCaption(), language);
    }
    if (!StringUtils.isBlank(imageMetadata.getLegend())) {
      imageResource.setDescription(imageMetadata.getLegend(), language);
    }
    for (String keyword : imageMetadata.getKeywords()) {
      imageResource.addSubject(keyword);
    }
    if (!StringUtils.isBlank(imageMetadata.getCopyright())) {
      imageResource.setRights(imageMetadata.getCopyright(), language);
    }
    return imageResource;
  }

  @Override
  protected ImageResourceReader createNewReader()
      throws ParserConfigurationException, SAXException {
    return new ImageResourceReader();
  }

  @Override
  public List<ResourceMetadata<?>> toMetadata(Resource<?> resource) {
    if (resource != null) {
      return new ImageInputDocument((ImageResource) resource).getMetadata();
    }
    return null;
  }

  @Override
  public Resource<?> toResource(Site site, List<ResourceMetadata<?>> metadata) {
    for (ResourceMetadata<?> metadataItem : metadata) {
      if (XML.equals(metadataItem.getName())) {
        String resourceXml = (String) metadataItem.getValues().get(0);
        try {
          ResourceReader<ImageContent, ImageResource> reader = getReader();
          ImageResource image = reader.read(IOUtils.toInputStream(resourceXml, "UTF-8"), site);
          return image;
        } catch (SAXException e) {
          logger.warn("Error parsing image resource from metadata", e);
          return null;
        } catch (IOException e) {
          logger.warn("Error parsing image resource from metadata", e);
          return null;
        } catch (ParserConfigurationException e) {
          logger.warn("Error parsing image resource from metadata", e);
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
        logger.debug("Path {}:{} for image {} is invalid", new Object[] {
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

    ResourceURI uri = new ImageResourceURIImpl(site, path, id, version);

    ImageResourceSearchResultItemImpl result = new ImageResourceSearchResultItemImpl(uri, url, relevance, site, metadata);

    if (metadataMap.get(XML) != null)
      result.setResourceXml((String) metadataMap.get(XML).getValues().get(0));
    if (metadataMap.get(HEADER_XML) != null)
      result.setImageHeaderXml((String) metadataMap.get(HEADER_XML).getValues().get(0));
    // TODO: Add remaining metadata

    return result;
  }

  @Override
  public ResourceContentReader<ImageContent> getContentReader()
      throws ParserConfigurationException, SAXException {
    return new ImageContentReader();
  }

  @Override
  public PreviewGenerator getPreviewGenerator(Resource<?> resource, Language language) {
    for (ImagePreviewGenerator generator : previewGenerators) {
      if (generator.supports(resource, language)) {
        logger.trace("Image preview generator {} agrees to handle {}", generator, resource);
        return generator;
      }
    }
    logger.trace("No image preview generator found to handle {}", resource);
    return null;
  }

  /**
   * Returns the highest ranked image preview generator that supports the given
   * format or <code>null</code> if no preview generator is available.
   * 
   * @param format
   *          the format
   * @return the preview generator
   */
  public PreviewGenerator getPreviewGenerator(String format) {
    for (ImagePreviewGenerator generator : previewGenerators) {
      if (generator.supports(format)) {
        logger.trace("Image preview generator {} agrees to handle format '{}'", generator, format);
        return generator;
      }
    }
    logger.trace("No image preview generator found to handle format '{}'", format);
    return null;
  }

  /**
   * Adds the preview generator to the list of registered preview generators.
   * 
   * @param generator
   *          the generator
   */
  void addPreviewGenerator(ImagePreviewGenerator generator) {
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
  void removePreviewGenerator(ImagePreviewGenerator generator) {
    previewGenerators.remove(generator);
  }

  /**
   * {@inheritDoc}
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "Image serializer";
  }

}
