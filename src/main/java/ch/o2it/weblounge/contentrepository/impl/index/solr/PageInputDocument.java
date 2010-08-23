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

import static ch.o2it.weblounge.contentrepository.impl.index.solr.SolrFields.PAGELET_CONTENTS;
import static ch.o2it.weblounge.contentrepository.impl.index.solr.SolrFields.PAGELET_ELEMENTS;
import static ch.o2it.weblounge.contentrepository.impl.index.solr.SolrFields.PAGELET_PROPERTIES;
import static ch.o2it.weblounge.contentrepository.impl.index.solr.SolrFields.PAGELET_TYPE;
import static ch.o2it.weblounge.contentrepository.impl.index.solr.SolrFields.PAGELET_XML;
import static ch.o2it.weblounge.contentrepository.impl.index.solr.SolrFields.PREVIEW_XML;
import static ch.o2it.weblounge.contentrepository.impl.index.solr.SolrFields.TEMPLATE;

import ch.o2it.weblounge.common.content.page.Composer;
import ch.o2it.weblounge.common.content.page.Page;
import ch.o2it.weblounge.common.content.page.Pagelet;
import ch.o2it.weblounge.common.language.Language;

import java.text.MessageFormat;

/**
 * Extension to a <code>SolrUpdateableInputDocument</code> that facilitates in
 * posting weblounge pages to solr.
 */
public class PageInputDocument extends ResourceInputDocument {

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
  protected void init(Page page) {
    super.init(page);

    // Page-level
    addField(TEMPLATE, page.getTemplate(), false);

    // Pagelet elements and properties
    for (Composer composer : page.getComposers()) {
      int i = 0;
      for (Pagelet p : composer.getPagelets()) {
        String location = composer.getIdentifier() + "-" + i;
        for (Language l : p.languages()) {
          addField(getLocalizedFieldName(PAGELET_CONTENTS, l), serializeContent(p, l, false), l, true);
          addField(getLocalizedFieldName(PAGELET_CONTENTS, l), serializeProperties(p, false), l, true);
          addField(MessageFormat.format(PAGELET_ELEMENTS, location, l.getIdentifier()), serializeContent(p, l, true), l, false);
        }
        addField(MessageFormat.format(PAGELET_PROPERTIES, location), serializeProperties(p, true), false);
        addField(MessageFormat.format(PAGELET_XML, location), p.toXml(), false);
        addField(MessageFormat.format(PAGELET_TYPE, location), p.getModule() + "/" + p.getIdentifier(), false);
        i++;
      }
    }

    // Preview information
    StringBuffer preview = new StringBuffer();
    preview.append("<composer id=\"stage\">");
    for (Pagelet p : page.getPreview()) {
      preview.append(p.toXml());
    }
    preview.append("</composer>");
    addField(PREVIEW_XML, preview.toString(), false);

  }

}
