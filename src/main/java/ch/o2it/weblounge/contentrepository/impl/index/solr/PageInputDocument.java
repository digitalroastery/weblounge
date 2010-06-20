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

import static ch.o2it.weblounge.contentrepository.impl.index.solr.SolrFields.COVERAGE;
import static ch.o2it.weblounge.contentrepository.impl.index.solr.SolrFields.CREATED;
import static ch.o2it.weblounge.contentrepository.impl.index.solr.SolrFields.CREATED_BY;
import static ch.o2it.weblounge.contentrepository.impl.index.solr.SolrFields.DESCRIPTION;
import static ch.o2it.weblounge.contentrepository.impl.index.solr.SolrFields.ID;
import static ch.o2it.weblounge.contentrepository.impl.index.solr.SolrFields.MODIFIED;
import static ch.o2it.weblounge.contentrepository.impl.index.solr.SolrFields.MODIFIED_BY;
import static ch.o2it.weblounge.contentrepository.impl.index.solr.SolrFields.PAGELET_CONTENTS;
import static ch.o2it.weblounge.contentrepository.impl.index.solr.SolrFields.PAGELET_ELEMENTS;
import static ch.o2it.weblounge.contentrepository.impl.index.solr.SolrFields.PAGELET_PROPERTIES;
import static ch.o2it.weblounge.contentrepository.impl.index.solr.SolrFields.PAGELET_TYPE;
import static ch.o2it.weblounge.contentrepository.impl.index.solr.SolrFields.PAGELET_XML;
import static ch.o2it.weblounge.contentrepository.impl.index.solr.SolrFields.PAGE_XML;
import static ch.o2it.weblounge.contentrepository.impl.index.solr.SolrFields.PATH;
import static ch.o2it.weblounge.contentrepository.impl.index.solr.SolrFields.PREVIEW_XML;
import static ch.o2it.weblounge.contentrepository.impl.index.solr.SolrFields.PUBLISHED_BY;
import static ch.o2it.weblounge.contentrepository.impl.index.solr.SolrFields.PUBLISHED_FROM;
import static ch.o2it.weblounge.contentrepository.impl.index.solr.SolrFields.PUBLISHED_TO;
import static ch.o2it.weblounge.contentrepository.impl.index.solr.SolrFields.RIGHTS;
import static ch.o2it.weblounge.contentrepository.impl.index.solr.SolrFields.SUBJECTS;
import static ch.o2it.weblounge.contentrepository.impl.index.solr.SolrFields.TEMPLATE;
import static ch.o2it.weblounge.contentrepository.impl.index.solr.SolrFields.TITLE;

import ch.o2it.weblounge.common.content.Page;
import ch.o2it.weblounge.common.content.Pagelet;
import ch.o2it.weblounge.common.language.Language;

import java.text.MessageFormat;

/**
 * Extension to a <code>SolrUpdateableInputDocument</code> that facilitates in
 * posting weblounge pages to solr.
 */
public class PageInputDocument extends AbstractInputDocument {

  /** Serial version uid */
  private static final long serialVersionUID = 1812364663819822015L;

  /**
   * Creates an input document for the given page.
   * 
   * @param page
   *          the page
   */
  public PageInputDocument(Page page) {
    init(page);
  }

  /**
   * Populates this input document with the page data.
   * 
   * @param page
   *          the page
   */
  private void init(Page page) {
    setField(ID, page.getURI().getId());
    setField(PATH, page.getURI().getPath());

    // Page-level
    for (String subject : page.getSubjects())
      setField(SUBJECTS, subject);
    setField(TEMPLATE, page.getTemplate());

    // Creation, modification and publishing information
    setField(CREATED, SolrUtils.serializeDate(page.getCreationDate()));
    setField(CREATED_BY, SolrUtils.serializeUser(page.getCreator()));
    setField(MODIFIED, SolrUtils.serializeDate(page.getModificationDate()));
    setField(MODIFIED_BY, SolrUtils.serializeUser(page.getModifier()));
    setField(PUBLISHED_FROM, SolrUtils.serializeDate(page.getPublishFrom()));
    setField(PUBLISHED_TO, SolrUtils.serializeDate(page.getPublishTo()));
    setField(PUBLISHED_BY, SolrUtils.serializeUser(page.getPublisher()));

    // Language dependent fields
    for (Language l : page.languages()) {
      setFulltextField(getLocalizedFieldName(DESCRIPTION, l), page.getDescription(l, true), l);
      setField(getLocalizedFieldName(COVERAGE, l), page.getCoverage(l, true));
      setField(getLocalizedFieldName(RIGHTS, l), page.getRights(l, true));
      setField(getLocalizedFieldName(TITLE, l), page.getTitle(l, true));
    }

    // Pagelet elements and properties
    int i = 0;
    for (Pagelet p : page.getPagelets()) {
      for (Language l : p.languages()) {
        setFulltextField(getLocalizedFieldName(PAGELET_CONTENTS, l), serializeContent(p, l, false), l);
        setFulltextField(getLocalizedFieldName(PAGELET_CONTENTS, l), serializeProperties(p, false), l);
        setField(MessageFormat.format(PAGELET_ELEMENTS, i, l.getIdentifier()), serializeContent(p, l, true));
      }
      setField(MessageFormat.format(PAGELET_PROPERTIES, i), serializeProperties(p, true));
      setField(MessageFormat.format(PAGELET_XML, i), p.toXml());
      setField(MessageFormat.format(PAGELET_TYPE, i), p.getModule() + "/" + p.getIdentifier());
    }

    // Preview information
    StringBuffer preview = new StringBuffer();
    preview.append("<composer id=\"stage\">");
    for (Pagelet p : page.getPreview()) {
      preview.append(p.toXml());
    }
    preview.append("</composer>");
    setField(PREVIEW_XML, preview.toString());

    // The whole page
    setField(PAGE_XML, page.toXml());

  }

}
