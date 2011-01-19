/*
 *  Weblounge: Web Content Management System
 *  Copyright (c) 2010 The Weblounge Team
 *  http://weblounge.o2it.ch
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

package ch.o2it.weblounge.contentrepository.impl.index.solr;

import static ch.o2it.weblounge.contentrepository.impl.index.solr.SolrFields.CONTENT_CREATED;
import static ch.o2it.weblounge.contentrepository.impl.index.solr.SolrFields.CONTENT_CREATED_BY;
import static ch.o2it.weblounge.contentrepository.impl.index.solr.SolrFields.CONTENT_FILENAME;
import static ch.o2it.weblounge.contentrepository.impl.index.solr.SolrFields.CONTENT_FILENAME_LOCALIZED;
import static ch.o2it.weblounge.contentrepository.impl.index.solr.SolrFields.CONTENT_MIMETYPE;
import static ch.o2it.weblounge.contentrepository.impl.index.solr.SolrFields.CONTENT_MIMETYPE_LOCALIZED;
import static ch.o2it.weblounge.contentrepository.impl.index.solr.SolrFields.CONTENT_XML;
import static ch.o2it.weblounge.contentrepository.impl.index.solr.SolrFields.COVERAGE;
import static ch.o2it.weblounge.contentrepository.impl.index.solr.SolrFields.COVERAGE_LOCALIZED;
import static ch.o2it.weblounge.contentrepository.impl.index.solr.SolrFields.CREATED;
import static ch.o2it.weblounge.contentrepository.impl.index.solr.SolrFields.CREATED_BY;
import static ch.o2it.weblounge.contentrepository.impl.index.solr.SolrFields.DESCRIPTION;
import static ch.o2it.weblounge.contentrepository.impl.index.solr.SolrFields.DESCRIPTION_LOCALIZED;
import static ch.o2it.weblounge.contentrepository.impl.index.solr.SolrFields.HEADER_XML;
import static ch.o2it.weblounge.contentrepository.impl.index.solr.SolrFields.ID;
import static ch.o2it.weblounge.contentrepository.impl.index.solr.SolrFields.MODIFIED;
import static ch.o2it.weblounge.contentrepository.impl.index.solr.SolrFields.MODIFIED_BY;
import static ch.o2it.weblounge.contentrepository.impl.index.solr.SolrFields.PATH;
import static ch.o2it.weblounge.contentrepository.impl.index.solr.SolrFields.PUBLISHED_BY;
import static ch.o2it.weblounge.contentrepository.impl.index.solr.SolrFields.PUBLISHED_FROM;
import static ch.o2it.weblounge.contentrepository.impl.index.solr.SolrFields.PUBLISHED_TO;
import static ch.o2it.weblounge.contentrepository.impl.index.solr.SolrFields.RIGHTS;
import static ch.o2it.weblounge.contentrepository.impl.index.solr.SolrFields.RIGHTS_LOCALIZED;
import static ch.o2it.weblounge.contentrepository.impl.index.solr.SolrFields.SUBJECT;
import static ch.o2it.weblounge.contentrepository.impl.index.solr.SolrFields.TITLE;
import static ch.o2it.weblounge.contentrepository.impl.index.solr.SolrFields.TITLE_LOCALIZED;
import static ch.o2it.weblounge.contentrepository.impl.index.solr.SolrFields.TYPE;
import static ch.o2it.weblounge.contentrepository.impl.index.solr.SolrFields.VERSION;
import static ch.o2it.weblounge.contentrepository.impl.index.solr.SolrFields.XML;

import ch.o2it.weblounge.common.content.Resource;
import ch.o2it.weblounge.common.content.ResourceContent;
import ch.o2it.weblounge.common.language.Language;

import org.apache.commons.lang.StringUtils;

/**
 * Extension to a <code>SolrUpdateableInputDocument</code> that facilitates in
 * posting weblounge resources to solr.
 */
public class ResourceInputDocument extends AbstractInputDocument {

  /** Serial version uid */
  private static final long serialVersionUID = 1812364663819822015L;

  /**
   * Populates this input document with the resource data.
   * 
   * @param resource
   *          the resource
   */
  protected void init(Resource<?> resource) {
    addField(ID, resource.getURI().getId(), true);
    addField(PATH, resource.getURI().getPath(), true);
    addField(TYPE, resource.getURI().getType(), true);
    addField(VERSION, resource.getURI().getVersion(), false);

    // Resource-level
    for (String subject : resource.getSubjects())
      addField(SUBJECT, subject, true);

    // Creation, modification and publishing information
    //setField(CREATED, SolrUtils.serializeDate(resource.getCreationDate()), false);
    addField(CREATED, resource.getCreationDate(), false);
    addField(CREATED_BY, SolrUtils.serializeUser(resource.getCreator()), false);
    addField(MODIFIED, SolrUtils.serializeDate(resource.getModificationDate()), false);
    addField(MODIFIED_BY, SolrUtils.serializeUser(resource.getModifier()), false);
    addField(PUBLISHED_FROM, SolrUtils.serializeDate(resource.getPublishFrom()), false);
    addField(PUBLISHED_TO, SolrUtils.serializeDate(resource.getPublishTo()), false);
    addField(PUBLISHED_BY, SolrUtils.serializeUser(resource.getPublisher()), false);

    // Language dependent header fields
    for (Language l : resource.languages()) {
      addField(DESCRIPTION, resource.getDescription(l, true), l, true);
      addField(getLocalizedFieldName(DESCRIPTION_LOCALIZED, l), resource.getDescription(l, true), l, false);
      addField(COVERAGE, resource.getCoverage(l, true), l, true);
      addField(getLocalizedFieldName(COVERAGE_LOCALIZED, l), resource.getCoverage(l, true), l, false);
      addField(RIGHTS, resource.getRights(l, true), l, true);
      addField(getLocalizedFieldName(RIGHTS_LOCALIZED, l), resource.getRights(l, true), l, false);
      addField(TITLE, resource.getTitle(l, true), l, true);
      addField(getLocalizedFieldName(TITLE_LOCALIZED, l), resource.getTitle(l, true), l, false);
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
      addField(getLocalizedFieldName(CONTENT_CREATED_BY, l), SolrUtils.serializeUser(content.getCreator()), false);
      addField(CONTENT_FILENAME, content.getFilename(), true);
      addField(getLocalizedFieldName(CONTENT_FILENAME_LOCALIZED, l), content.getFilename(), false);
      addField(CONTENT_MIMETYPE, content.getMimetype(), true);
      addField(getLocalizedFieldName(CONTENT_MIMETYPE_LOCALIZED, l), content.getMimetype(), false);
    }
    
  }

}
