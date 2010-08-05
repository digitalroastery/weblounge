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

import ch.o2it.weblounge.common.content.page.Composer;
import ch.o2it.weblounge.common.content.page.Page;
import ch.o2it.weblounge.common.content.page.Pagelet;
import ch.o2it.weblounge.common.language.Language;
import ch.o2it.weblounge.common.site.Action;
import ch.o2it.weblounge.common.site.Site;
import ch.o2it.weblounge.common.url.WebUrl;
import ch.o2it.weblounge.common.user.AuthenticatedUser;
import ch.o2it.weblounge.taglib.ParseException;
import ch.o2it.weblounge.taglib.TagVariableDefinitionParser;

import javax.servlet.jsp.tagext.TagData;
import javax.servlet.jsp.tagext.TagExtraInfo;
import javax.servlet.jsp.tagext.VariableInfo;

/**
 * This class provides runtime information about the <code>ContextTag</code> jsp
 * tag.
 */
public class ContextTagExtraInfo extends TagExtraInfo {

  /**
   * Returns the information on the exported tag variables.
   * 
   * @see javax.servlet.jsp.tagext.TagExtraInfo#getVariableInfo(javax.servlet.jsp.tagext.TagData)
   */
  public VariableInfo[] getVariableInfo(TagData tagData) {
    String definitions = tagData.getAttributeString("define");
    if (definitions != null) {
      try {
        ContextTagVariables variables = new ContextTagVariables(TagVariableDefinitionParser.parse(definitions));
        VariableInfo[] varinfo = new VariableInfo[variables.size()];
        int i = 0;

        String name;
        if ((name = variables.getUri()) != null)
          varinfo[i++] = new VariableInfo(name, java.lang.String.class.getName(), true, VariableInfo.NESTED);
        if ((name = variables.getUrl()) != null)
          varinfo[i++] = new VariableInfo(name, WebUrl.class.getName(), true, VariableInfo.NESTED);
        if ((name = variables.getAction()) != null)
          varinfo[i++] = new VariableInfo(name, Action.class.getName(), true, VariableInfo.NESTED);
        if ((name = variables.getSite()) != null)
          varinfo[i++] = new VariableInfo(name, Site.class.getName(), true, VariableInfo.NESTED);
        if ((name = variables.getUser()) != null)
          varinfo[i++] = new VariableInfo(name, AuthenticatedUser.class.getName(), true, VariableInfo.NESTED);
        if ((name = variables.getLanguage()) != null)
          varinfo[i++] = new VariableInfo(name, Language.class.getName(), true, VariableInfo.NESTED);
        if ((name = variables.getPage()) != null)
          varinfo[i++] = new VariableInfo(name, Page.class.getName(), true, VariableInfo.NESTED);
        if ((name = variables.getPagelet()) != null)
          varinfo[i++] = new VariableInfo(name, Pagelet.class.getName(), true, VariableInfo.NESTED);
        if ((name = variables.getComposer()) != null)
          varinfo[i++] = new VariableInfo(name, Composer.class.getName(), true, VariableInfo.NESTED);

        return varinfo;
      } catch (ParseException ex) {
        return new VariableInfo[0];
      }
    } else {
      return new VariableInfo[] {
          new VariableInfo(ContextTagVariables.URI, java.lang.String.class.getName(), true, VariableInfo.NESTED),
          new VariableInfo(ContextTagVariables.URL, WebUrl.class.getName(), true, VariableInfo.NESTED),
          new VariableInfo(ContextTagVariables.SITE, Site.class.getName(), true, VariableInfo.NESTED),
          new VariableInfo(ContextTagVariables.USER, AuthenticatedUser.class.getName(), true, VariableInfo.NESTED),
          new VariableInfo(ContextTagVariables.LANGUAGE, Language.class.getName(), true, VariableInfo.NESTED) };
    }
  }

  /**
   * Returns <code>true</code> if the tag data is valid. This is the case if the
   * <code>define</code> attribute can be parsed without exception.
   * 
   * @see javax.servlet.jsp.tagext.TagExtraInfo#isValid(javax.servlet.jsp.tagext.TagData)
   */
  public boolean isValid(TagData tagData) {
    String definitions = tagData.getAttributeString("define");
    if (definitions != null) {
      try {
        TagVariableDefinitionParser.parse(definitions);
      } catch (ParseException ex) {
        return false;
      }
    }
    return true;
  }

}
