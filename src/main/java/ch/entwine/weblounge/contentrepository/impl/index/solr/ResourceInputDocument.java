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

package ch.entwine.weblounge.contentrepository.impl.index.solr;

import static ch.entwine.weblounge.contentrepository.impl.index.solr.SolrFields.CONTENT_CREATED;
import static ch.entwine.weblounge.contentrepository.impl.index.solr.SolrFields.CONTENT_CREATED_BY;
import static ch.entwine.weblounge.contentrepository.impl.index.solr.SolrFields.CONTENT_FILENAME;
import static ch.entwine.weblounge.contentrepository.impl.index.solr.SolrFields.CONTENT_FILENAME_LOCALIZED;
import static ch.entwine.weblounge.contentrepository.impl.index.solr.SolrFields.CONTENT_MIMETYPE;
import static ch.entwine.weblounge.contentrepository.impl.index.solr.SolrFields.CONTENT_MIMETYPE_LOCALIZED;
import static ch.entwine.weblounge.contentrepository.impl.index.solr.SolrFields.CONTENT_XML;
import static ch.entwine.weblounge.contentrepository.impl.index.solr.SolrFields.COVERAGE;
import static ch.entwine.weblounge.contentrepository.impl.index.solr.SolrFields.COVERAGE_LOCALIZED;
import static ch.entwine.weblounge.contentrepository.impl.index.solr.SolrFields.CREATED;
import static ch.entwine.weblounge.contentrepository.impl.index.solr.SolrFields.CREATED_BY;
import static ch.entwine.weblounge.contentrepository.impl.index.solr.SolrFields.CREATED_BY_NAME;
import static ch.entwine.weblounge.contentrepository.impl.index.solr.SolrFields.DESCRIPTION;
import static ch.entwine.weblounge.contentrepository.impl.index.solr.SolrFields.DESCRIPTION_LOCALIZED;
import static ch.entwine.weblounge.contentrepository.impl.index.solr.SolrFields.HEADER_XML;
import static ch.entwine.weblounge.contentrepository.impl.index.solr.SolrFields.ID;
import static ch.entwine.weblounge.contentrepository.impl.index.solr.SolrFields.LOCKED_BY;
import static ch.entwine.weblounge.contentrepository.impl.index.solr.SolrFields.LOCKED_BY_NAME;
import static ch.entwine.weblounge.contentrepository.impl.index.solr.SolrFields.MODIFIED;
import static ch.entwine.weblounge.contentrepository.impl.index.solr.SolrFields.MODIFIED_BY;
import static ch.entwine.weblounge.contentrepository.impl.index.solr.SolrFields.MODIFIED_BY_NAME;
import static ch.entwine.weblounge.contentrepository.impl.index.solr.SolrFields.OWNED_BY;
import static ch.entwine.weblounge.contentrepository.impl.index.solr.SolrFields.OWNED_BY_NAME;
import static ch.entwine.weblounge.contentrepository.impl.index.solr.SolrFields.PATH;
import static ch.entwine.weblounge.contentrepository.impl.index.solr.SolrFields.PUBLISHED_BY;
import static ch.entwine.weblounge.contentrepository.impl.index.solr.SolrFields.PUBLISHED_BY_NAME;
import static ch.entwine.weblounge.contentrepository.impl.index.solr.SolrFields.PUBLISHED_FROM;
import static ch.entwine.weblounge.contentrepository.impl.index.solr.SolrFields.PUBLISHED_TO;
import static ch.entwine.weblounge.contentrepository.impl.index.solr.SolrFields.RIGHTS;
import static ch.entwine.weblounge.contentrepository.impl.index.solr.SolrFields.RIGHTS_LOCALIZED;
import static ch.entwine.weblounge.contentrepository.impl.index.solr.SolrFields.SUBJECT;
import static ch.entwine.weblounge.contentrepository.impl.index.solr.SolrFields.TITLE;
import static ch.entwine.weblounge.contentrepository.impl.index.solr.SolrFields.TITLE_LOCALIZED;
import static ch.entwine.weblounge.contentrepository.impl.index.solr.SolrFields.TYPE;
import static ch.entwine.weblounge.contentrepository.impl.index.solr.SolrFields.VERSION;
import static ch.entwine.weblounge.contentrepository.impl.index.solr.SolrFields.XML;

import ch.entwine.weblounge.common.content.Resource;
import ch.entwine.weblounge.common.content.ResourceContent;
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
    addField(ID, resource.getURI().getIdentifier(), true);
    addField(PATH, resource.getURI().getPath(), true);
    addField(TYPE, resource.getURI().getType(), true);
    addField(VERSION, resource.getURI().getVersion(), false);

    // Resource-level
    for (String subject : resource.getSubjects())
      addField(SUBJECT, subject, true);

    // Creation, modification and publishing information
    addField(OWNED_BY, SolrUtils.serializeUserId(resource.getOwner()), false);
    addField(OWNED_BY_NAME, SolrUtils.serializeUserName(resource.getOwner()), true);
    addField(CREATED, resource.getCreationDate(), false);
    addField(CREATED_BY, SolrUtils.serializeUserId(resource.getCreator()), false);
    addField(CREATED_BY_NAME, SolrUtils.serializeUserName(resource.getCreator()), true);
    addField(MODIFIED, resource.getModificationDate(), false);
    addField(MODIFIED_BY, SolrUtils.serializeUserId(resource.getModifier()), false);
    addField(MODIFIED_BY_NAME, SolrUtils.serializeUserName(resource.getModifier()), true);
    addField(PUBLISHED_FROM, resource.getPublishFrom(), false);
    addField(PUBLISHED_TO, resource.getPublishTo(), false);
    addField(PUBLISHED_BY, SolrUtils.serializeUserId(resource.getPublisher()), false);
    addField(PUBLISHED_BY_NAME, SolrUtils.serializeUserName(resource.getPublisher()), true);

    if (resource.isLocked()) {
      addField(LOCKED_BY, SolrUtils.serializeUserId(resource.getLockOwner()), false);
      addField(LOCKED_BY_NAME, SolrUtils.serializeUserName(resource.getLockOwner()), true);
    }

    // Language dependent header fields
    for (Language l : resource.languages()) {
      addField(DESCRIPTION, resource.getDescription(l, true), true);
      addField(getLocalizedFieldName(DESCRIPTION_LOCALIZED, l), resource.getDescription(l, true), false);
      addField(COVERAGE, resource.getCoverage(l, true), true);
      addField(getLocalizedFieldName(COVERAGE_LOCALIZED, l), resource.getCoverage(l, true), false);
      addField(RIGHTS, resource.getRights(l, true), true);
      addField(getLocalizedFieldName(RIGHTS_LOCALIZED, l), resource.getRights(l, true), false);
      addField(TITLE, resource.getTitle(l, true), true);
      addField(getLocalizedFieldName(TITLE_LOCALIZED, l), resource.getTitle(l, true), false);
    }

    // The whole resource
    String resourceXml = resource.toXml();
    addField(XML, resourceXml, false);

    // Resource header
    String headerXml = resourceXml.replaceAll("<body[^>]*>[\\s\\S]+?<\\/body>", "");
    if (!StringUtils.isBlank(headerXml)) {
      addField(HEADER_XML, headerXml, false);
    }

    // Resource contents
    for (ResourceContent content : resource.contents()) {
      Language l = content.getLanguage();
      addField(getLocalizedFieldName(CONTENT_XML, l), content.toXml(), false);
      addField(getLocalizedFieldName(CONTENT_CREATED, l), SolrUtils.serializeDate(content.getCreationDate()), false);
      addField(getLocalizedFieldName(CONTENT_CREATED_BY, l), SolrUtils.serializeUserId(content.getCreator()), false);
      addField(CONTENT_FILENAME, content.getFilename(), true);
      addField(getLocalizedFieldName(CONTENT_FILENAME_LOCALIZED, l), content.getFilename(), false);
      addField(CONTENT_MIMETYPE, content.getMimetype(), true);
      addField(getLocalizedFieldName(CONTENT_MIMETYPE_LOCALIZED, l), content.getMimetype(), false);
    }

  }

}
