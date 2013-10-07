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

package ch.entwine.weblounge.search.impl;

import static ch.entwine.weblounge.search.impl.IndexSchema.CONTENT_CREATED;
import static ch.entwine.weblounge.search.impl.IndexSchema.CONTENT_CREATED_BY;
import static ch.entwine.weblounge.search.impl.IndexSchema.CONTENT_EXTERNAL_REPRESENTATION;
import static ch.entwine.weblounge.search.impl.IndexSchema.CONTENT_FILENAME;
import static ch.entwine.weblounge.search.impl.IndexSchema.CONTENT_FILENAME_LOCALIZED;
import static ch.entwine.weblounge.search.impl.IndexSchema.CONTENT_MIMETYPE;
import static ch.entwine.weblounge.search.impl.IndexSchema.CONTENT_MIMETYPE_LOCALIZED;
import static ch.entwine.weblounge.search.impl.IndexSchema.CONTENT_SOURCE;
import static ch.entwine.weblounge.search.impl.IndexSchema.CONTENT_XML;
import static ch.entwine.weblounge.search.impl.IndexSchema.COVERAGE;
import static ch.entwine.weblounge.search.impl.IndexSchema.COVERAGE_LOCALIZED;
import static ch.entwine.weblounge.search.impl.IndexSchema.CREATED;
import static ch.entwine.weblounge.search.impl.IndexSchema.CREATED_BY;
import static ch.entwine.weblounge.search.impl.IndexSchema.CREATED_BY_NAME;
import static ch.entwine.weblounge.search.impl.IndexSchema.DESCRIPTION;
import static ch.entwine.weblounge.search.impl.IndexSchema.DESCRIPTION_LOCALIZED;
import static ch.entwine.weblounge.search.impl.IndexSchema.HEADER_XML;
import static ch.entwine.weblounge.search.impl.IndexSchema.LOCKED_BY;
import static ch.entwine.weblounge.search.impl.IndexSchema.LOCKED_BY_NAME;
import static ch.entwine.weblounge.search.impl.IndexSchema.MODIFIED;
import static ch.entwine.weblounge.search.impl.IndexSchema.MODIFIED_BY;
import static ch.entwine.weblounge.search.impl.IndexSchema.MODIFIED_BY_NAME;
import static ch.entwine.weblounge.search.impl.IndexSchema.OWNED_BY;
import static ch.entwine.weblounge.search.impl.IndexSchema.OWNED_BY_NAME;
import static ch.entwine.weblounge.search.impl.IndexSchema.PATH;
import static ch.entwine.weblounge.search.impl.IndexSchema.PATH_PREFIX;
import static ch.entwine.weblounge.search.impl.IndexSchema.PUBLISHED_BY;
import static ch.entwine.weblounge.search.impl.IndexSchema.PUBLISHED_BY_NAME;
import static ch.entwine.weblounge.search.impl.IndexSchema.PUBLISHED_FROM;
import static ch.entwine.weblounge.search.impl.IndexSchema.PUBLISHED_TO;
import static ch.entwine.weblounge.search.impl.IndexSchema.RESOURCE_ID;
import static ch.entwine.weblounge.search.impl.IndexSchema.RIGHTS;
import static ch.entwine.weblounge.search.impl.IndexSchema.RIGHTS_LOCALIZED;
import static ch.entwine.weblounge.search.impl.IndexSchema.SERIES;
import static ch.entwine.weblounge.search.impl.IndexSchema.SUBJECT;
import static ch.entwine.weblounge.search.impl.IndexSchema.TITLE;
import static ch.entwine.weblounge.search.impl.IndexSchema.TITLE_LOCALIZED;
import static ch.entwine.weblounge.search.impl.IndexSchema.TYPE;
import static ch.entwine.weblounge.search.impl.IndexSchema.UID;
import static ch.entwine.weblounge.search.impl.IndexSchema.VERSION;
import static ch.entwine.weblounge.search.impl.IndexSchema.XML;

import ch.entwine.weblounge.common.content.Resource;
import ch.entwine.weblounge.common.content.ResourceContent;
import ch.entwine.weblounge.common.content.ResourceURI;
import ch.entwine.weblounge.common.language.Language;

import org.apache.commons.lang.StringUtils;

/**
 * Extension to a <code>SolrUpdateableInputDocument</code> that facilitates in
 * posting weblounge resources to solr.
 */
public class ResourceInputDocument extends ResourceMetadataCollection {

  /**
   * Populates this input document with the resource data.
   * 
   * @param resource
   *          the resource
   */
  protected void init(Resource<?> resource) {
    ResourceURI uri = resource.getURI();

    if (uri.getIdentifier() == null)
      throw new IllegalArgumentException("Resource must have an identifier");

    addField(UID, uri.getUID(), false, false);
    addField(RESOURCE_ID, uri.getIdentifier(), true, false);
    addField(PATH, uri.getPath(), true, true);
    addField(TYPE, uri.getType(), true, false);
    addField(VERSION, uri.getVersion(), false, false);

    // Path elements
    if (!StringUtils.isBlank(uri.getPath())) {
      String path = uri.getPath();

      // Index the whole path as prefixes
      StringBuffer prefix = new StringBuffer();
      for (int i = 0; i < path.length(); i++) {
        prefix.append(path.charAt(i));
        addField(PATH_PREFIX, prefix.toString(), false, false);
      }

      // Index sub paths
      for (String s : path.split("/")) {
        if ("".equals(s))
          continue;
        addField(PATH_PREFIX, s, true, false);
        StringBuffer subprefix = new StringBuffer("/");
        for (int i = 0; i < s.length(); i++) {
          subprefix.append(s.charAt(i));
          addField(PATH_PREFIX, subprefix.toString(), false, false);
        }
        addField(PATH_PREFIX, "/" + s + "/", false, false);
      }
    }

    // Resource-level
    for (String subject : resource.getSubjects())
      addField(SUBJECT, subject, true, false);

    for (String series : resource.getSeries())
      addField(SERIES, series, true, true);

    // Creation, modification and publishing information
    addField(OWNED_BY, IndexUtils.serializeUserId(resource.getOwner()), false, false);
    addField(OWNED_BY_NAME, IndexUtils.serializeUserName(resource.getOwner()), true, false);
    addField(CREATED, resource.getCreationDate(), false, false);
    addField(CREATED_BY, IndexUtils.serializeUserId(resource.getCreator()), false, false);
    addField(CREATED_BY_NAME, IndexUtils.serializeUserName(resource.getCreator()), true, false);
    addField(MODIFIED, resource.getModificationDate(), false, false);
    addField(MODIFIED_BY, IndexUtils.serializeUserId(resource.getModifier()), false, false);
    addField(MODIFIED_BY_NAME, IndexUtils.serializeUserName(resource.getModifier()), true, false);
    addField(PUBLISHED_FROM, resource.getPublishFrom(), false, false);
    addField(PUBLISHED_TO, resource.getPublishTo(), false, false);
    addField(PUBLISHED_BY, IndexUtils.serializeUserId(resource.getPublisher()), false, false);
    addField(PUBLISHED_BY_NAME, IndexUtils.serializeUserName(resource.getPublisher()), true, false);

    if (resource.isLocked()) {
      addField(LOCKED_BY, IndexUtils.serializeUserId(resource.getLockOwner()), false, false);
      addField(LOCKED_BY_NAME, IndexUtils.serializeUserName(resource.getLockOwner()), true, false);
    }

    // Language dependent header fields
    for (Language l : resource.languages()) {
      addField(DESCRIPTION, resource.getDescription(l, true), true, false);
      addField(getLocalizedFieldName(DESCRIPTION_LOCALIZED, l), resource.getDescription(l, true), false, false);
      addField(COVERAGE, resource.getCoverage(l, true), true, false);
      addField(getLocalizedFieldName(COVERAGE_LOCALIZED, l), resource.getCoverage(l, true), false, false);
      addField(RIGHTS, resource.getRights(l, true), true, false);
      addField(getLocalizedFieldName(RIGHTS_LOCALIZED, l), resource.getRights(l, true), false, false);
      addField(TITLE, resource.getTitle(l, true), true, false);
      addField(getLocalizedFieldName(TITLE_LOCALIZED, l), resource.getTitle(l, true), false, false);
    }

    // The whole resource
    String resourceXml = resource.toXml();
    addField(XML, resourceXml, false, false);

    // Resource header
    String headerXml = resourceXml.replaceAll("<body[^>]*>[\\s\\S]+?<\\/body>", "");
    if (!StringUtils.isBlank(headerXml)) {
      addField(HEADER_XML, headerXml, false, false);
    }

    // Resource contents
    for (ResourceContent content : resource.contents()) {
      Language l = content.getLanguage();
      addField(getLocalizedFieldName(CONTENT_XML, l), content.toXml(), false, false);
      addField(getLocalizedFieldName(CONTENT_CREATED, l), IndexUtils.serializeDate(content.getCreationDate()), false, false);
      addField(getLocalizedFieldName(CONTENT_CREATED_BY, l), IndexUtils.serializeUserId(content.getCreator()), false, false);
      addField(CONTENT_SOURCE, content.getSource(), true, false);
      addField(CONTENT_EXTERNAL_REPRESENTATION, content.getExternalLocation(), true, false);
      addField(CONTENT_FILENAME, content.getFilename(), true, true);
      addField(getLocalizedFieldName(CONTENT_FILENAME_LOCALIZED, l), content.getFilename(), true, true);
      addField(CONTENT_MIMETYPE, content.getMimetype(), true, false);
      addField(getLocalizedFieldName(CONTENT_MIMETYPE_LOCALIZED, l), content.getMimetype(), true, false);
    }

  }

}
