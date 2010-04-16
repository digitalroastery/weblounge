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

package ch.o2it.weblounge.taglib.content;

import ch.o2it.weblounge.taglib.TagVariableDefinitions;

/**
 * Holds the variable definitions for the <code>ContextTag</code>.
 */
public class ContextTagVariables {

  public static final String URI = "uri";
  public static final String URL = "url";
  public static final String ACTION = "action";
  public static final String SITE = "site";
  public static final String USER = "user";
  public static final String LANGUAGE = "language";
  public static final String PAGE = "page";
  public static final String PAGELET = "pagelet";
  public static final String COMPOSER = "composer";

  /** The variable definitions */
  private TagVariableDefinitions definitions = null;

  ContextTagVariables(TagVariableDefinitions definitions) {
    this.definitions = definitions;
  }

  public final String getUri() {
    return (definitions.exists(URI)) ? definitions.getAlias(URI) : null;
  }

  public final String getUrl() {
    return (definitions.exists(URL)) ? definitions.getAlias(URL) : null;
  }

  public final String getAction() {
    return (definitions.exists(ACTION)) ? definitions.getAlias(ACTION) : null;
  }

  public final String getSite() {
    return (definitions.exists(SITE)) ? definitions.getAlias(SITE) : null;
  }

  public final String getLanguage() {
    return (definitions.exists(LANGUAGE)) ? definitions.getAlias(LANGUAGE) : null;
  }

  public final String getUser() {
    return (definitions.exists(USER)) ? definitions.getAlias(USER) : null;
  }

  public final String getPage() {
    return (definitions.exists(PAGE)) ? definitions.getAlias(PAGE) : null;
  }

  public final String getPagelet() {
    return (definitions.exists(PAGELET)) ? definitions.getAlias(PAGELET) : null;
  }

  public final String getComposer() {
    return (definitions.exists(COMPOSER)) ? definitions.getAlias(COMPOSER) : null;
  }

  public final int size() {
    return definitions.size();
  }

}
