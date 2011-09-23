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

import ch.entwine.weblounge.common.content.ImageSearchResultItem;
import ch.entwine.weblounge.common.content.PreviewGenerator;
import ch.entwine.weblounge.common.content.Resource;
import ch.entwine.weblounge.common.content.ResourceContentReader;
import ch.entwine.weblounge.common.content.ResourceMetadata;
import ch.entwine.weblounge.common.content.ResourceReader;
import ch.entwine.weblounge.common.content.ResourceURI;
import ch.entwine.weblounge.common.content.SearchResultItem;
import ch.entwine.weblounge.common.content.image.ImageContent;
import ch.entwine.weblounge.common.content.image.ImageResource;
import ch.entwine.weblounge.common.impl.content.image.ImageContentReader;
import ch.entwine.weblounge.common.impl.content.image.ImageMetadata;
import ch.entwine.weblounge.common.impl.content.image.ImageMetadataUtils;
import ch.entwine.weblounge.common.impl.content.image.ImagePreviewGenerator;
import ch.entwine.weblounge.common.impl.content.image.ImageResourceImpl;
import ch.entwine.weblounge.common.impl.content.image.ImageResourceReader;
import ch.entwine.weblounge.common.impl.content.image.ImageResourceSearchResultItemImpl;
import ch.entwine.weblounge.common.impl.content.image.ImageResourceURIImpl;
import ch.entwine.weblounge.common.impl.security.UserImpl;
import ch.entwine.weblounge.common.impl.url.WebUrlImpl;
import ch.entwine.weblounge.common.language.Language;
import ch.entwine.weblounge.common.security.User;
import ch.entwine.weblounge.common.site.Site;
import ch.entwine.weblounge.common.url.WebUrl;
import ch.entwine.weblounge.contentrepository.impl.index.solr.ImageInputDocument;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
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

  /** The preview generator */
  protected PreviewGenerator previewGenerator = null;

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
    previewGenerator = new ImagePreviewGenerator();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.contentrepository.ResourceSerializer#getMimeType(ch.entwine.weblounge.common.content.ResourceContent)
   */
  public String getMimeType(ImageContent resourceContent) {
    return resourceContent.getMimetype();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.contentrepository.ResourceSerializer#supports(java.lang.String)
   */
  public boolean supports(String mimeType) {
    return mimeType != null && mimeType.toLowerCase().startsWith("image/");
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.contentrepository.ResourceSerializer#newResource(ch.entwine.weblounge.common.site.Site)
   */
  public Resource<ImageContent> newResource(Site site) {
    return new ImageResourceImpl(new ImageResourceURIImpl(site));
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.contentrepository.ResourceSerializer#newResource(ch.entwine.weblounge.common.site.Site,
   *      java.io.InputStream, ch.entwine.weblounge.common.security.User,
   *      ch.entwine.weblounge.common.language.Language)
   */
  public Resource<ImageContent> newResource(Site site, InputStream is,
      User user, Language language) {
    ImageMetadata imageMetadata = ImageMetadataUtils.extractMetadata(new BufferedInputStream(is));
    ImageResource imageResource = new ImageResourceImpl(new ImageResourceURIImpl(site));
    imageResource.setCreated(user, new Date());

    if (!StringUtils.isBlank(imageMetadata.getCaption())) {
      imageResource.setTitle(imageMetadata.getCaption(), language);
    }
    if (!StringUtils.isBlank(imageMetadata.getLegend())) {
      imageResource.setDescription(imageMetadata.getLegend(), language);
    }
    if (!StringUtils.isBlank(imageMetadata.getPhotographer())) {
      imageResource.setCreator(new UserImpl(user.getLogin(), user.getRealm(), imageMetadata.getPhotographer()));
    }
    for (String keyword : imageMetadata.getKeywords()) {
      imageResource.addSubject(keyword);
    }
    if (!StringUtils.isBlank(imageMetadata.getCopyright())) {
      imageResource.setRights(imageMetadata.getCopyright(), language);
    }
    if (imageMetadata.getDateTaken() != null) {
      imageResource.setCreationDate(imageMetadata.getDateTaken());
    }
    return imageResource;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.contentrepository.impl.AbstractResourceSerializer#createNewReader()
   */
  @Override
  protected ImageResourceReader createNewReader()
      throws ParserConfigurationException, SAXException {
    return new ImageResourceReader();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.contentrepository.ResourceSerializer#toMetadata(ch.entwine.weblounge.common.content.Resource)
   */
  public List<ResourceMetadata<?>> toMetadata(Resource<?> resource) {
    return new ImageInputDocument((ImageResource) resource).getMetadata();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.contentrepository.ResourceSerializer#toMetadata(ch.entwine.weblounge.common.content.SearchResultItem)
   */
  public List<ResourceMetadata<?>> toMetadata(SearchResultItem searchResultItem) {
    if (!(searchResultItem instanceof ImageSearchResultItem))
      throw new IllegalArgumentException("This serializer can only handle image search result items");
    ImageSearchResultItem fsri = (ImageSearchResultItem) searchResultItem;
    String resourceXml = fsri.getResourceXml();
    ImageResource r;
    try {
      r = getReader().read(IOUtils.toInputStream(resourceXml), searchResultItem.getSite());
    } catch (SAXException e) {
      logger.warn("Error parsing image resource " + searchResultItem.getId(), e);
      return null;
    } catch (IOException e) {
      logger.warn("Error parsing image resource " + searchResultItem.getId(), e);
      return null;
    } catch (ParserConfigurationException e) {
      logger.warn("Error parsing image resource " + searchResultItem.getId(), e);
      return null;
    }
    return toMetadata(r);
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

    ResourceURI uri = new ImageResourceURIImpl(site, path, id, Resource.LIVE);
    WebUrl url = new WebUrlImpl(site, path);

    ImageResourceSearchResultItemImpl result = new ImageResourceSearchResultItemImpl(uri, url, relevance, site);

    if (metadataMap.get(XML) != null)
      result.setResourceXml((String) metadataMap.get(XML).getValues().get(0));
    if (metadataMap.get(HEADER_XML) != null)
      result.setImageHeaderXml((String) metadataMap.get(HEADER_XML).getValues().get(0));
    // TODO: Add remaining metadata

    return result;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.contentrepository.ResourceSerializer#getContentReader()
   */
  public ResourceContentReader<ImageContent> getContentReader()
      throws ParserConfigurationException, SAXException {
    return new ImageContentReader();
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
    return "Image serializer";
  }

}
