/*
 *  Weblounge: Web Content Management System
 *  Copyright (c) 2009 The Weblounge Team
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

package ch.o2it.weblounge.common.impl.util;

import ch.o2it.weblounge.common.impl.language.LanguageSupport;
import ch.o2it.weblounge.common.impl.util.encoding.HTMLEncoding;
import ch.o2it.weblounge.common.impl.util.xml.XMLUtilities;
import ch.o2it.weblounge.common.impl.util.xml.XPathHelper;
import ch.o2it.weblounge.common.language.Language;
import ch.o2it.weblounge.common.site.Site;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.swing.filechooser.FileFilter;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;

/**
 * This class is used as a backend to the i18n tag that is used to localize jsp
 * templates.
 * 
 * TODO: This class is keeping a global cache of keys, which is not good
 */
public class I18n {

  /** The registered i18n directories */
  private static Map<Language, Properties> i18n = new HashMap<Language, Properties>();

  /** The defaults */
  private static Properties defaults = new Properties();

  /** Logging facility */
  private final static Logger log_ = LoggerFactory.getLogger(I18n.class);

  /**
   * Returns the localized message identified by <code>key</code> or
   * <code>null</code> if no such message could be found. Note that the site's
   * default language is used as a fallback.
   * 
   * @param key
   *          the key into the resource bundle
   * @param language
   *          the language variant to return
   * @return the result
   */
  public static String get(String key, Language language, Site site) {
    Properties p = i18n.get(language);
    String dictEntry = null;
    if (p != null)
      dictEntry = p.getProperty(key);

    // Either return the entry or try the default language fallback
    if (dictEntry != null)
      return dictEntry;
    else if (site != null && !language.equals(site.getDefaultLanguage()))
      return get(key, site.getDefaultLanguage(), site);

    // Try defaults
    return defaults.getProperty(key, key);
  }

  /**
   * Returns the localized message identified by <code>key</code> or
   * <code>null</code> if no such message could be found.
   * 
   * @param key
   *          the key into the resource bundle
   * @param variables
   *          the variables to substitute
   * @param language
   *          the language variant to return
   * @return the result
   */
  public static String get(String key, String[] variables, Language language,
      Site site) {
    String msg = get(key, language, site);
    if (msg == null)
      return null;
    MessageFormat mf = new MessageFormat(escape(msg));
    return mf.format(variables);
  }

  /**
   * Returns the localized and html encoded message identified by
   * <code>key</code> or <code>null</code> if no such message could be found.
   * 
   * @param key
   *          the key into the resource bundle
   * @param language
   *          the language variant to return
   * @return the result
   */
  public static String toHTML(String key, Language language, Site site) {
    return HTMLEncoding.toHTML(get(key, language, site));
  }

  /**
   * Returns the localized and html encoded message identified by
   * <code>key</code> or <code>null</code> if no such message could be found.
   * 
   * @param key
   *          the key into the resource bundle
   * @param variables
   *          the variables to substitute
   * @param language
   *          the language variant to return
   * @return the result
   */
  public static String toHTML(String key, String[] variables,
      Language language, Site site) {
    String msg = get(key, language, site);
    if (msg == null)
      return null;
    MessageFormat mf = new MessageFormat(escape(msg));
    return HTMLEncoding.toHTML(mf.format(variables));
  }

  /**
   * Adds the dictionaries found in the given directory.
   * 
   * @param i18nDir
   *          the directory containing the language variants
   */
  public static void addDictionary(File i18nDir) {
    if (i18n != null && i18nDir.exists() && i18nDir.canRead() && i18nDir.isDirectory()) {
      File[] files = i18nDir.listFiles();
      for (int i = 0; i < files.length; i++) {
        File f = files[i];
        addDictionary(f, false);
      }
    }
  }

  /**
   * Adds the the dictionary found in <code>file</code> to the current i18n
   * definitions. If <code>warn</code> is <code>false</code> then warnings about
   * existing keys are supressed.
   * 
   * @param f
   *          the i18n dictionary
   * @param warn
   *          <code>true</code> to warn about duplicate keys
   */
  public static void addDictionary(File f, boolean warn) {
    DocumentBuilder docBuilder;
    try {
      docBuilder = XMLUtilities.getDocumentBuilder();
      Document doc = docBuilder.parse(f);
      String name = f.getName();
      Language language = null;

      // Get the language

      if (f.isFile() && f.canRead() && name.startsWith("message") && name.endsWith(".xml")) {
        String languageId = null;
        int lidstart = f.getName().indexOf('_') + 1;
        int lidend = f.getName().lastIndexOf('.');
        if (lidstart > 0 && lidstart < name.length() && lidend > lidstart && lidend < name.length()) {
          languageId = name.substring(lidstart, lidend);
          language = LanguageSupport.getLanguage(languageId);
        }
      }

      log_.info("Reading i18n dictionary " + f);

      // Get the target properties

      Properties p = null;
      if (language != null) {
        p = i18n.get(language);
        if (p == null) {
          p = new Properties();
          i18n.put(language, p);
        }
      } else {
        p = defaults;
      }

      // Read and store the messages

      XPath path = XMLUtilities.getXPath();
      NodeList nodes = XPathHelper.selectList(doc, "/i18n/message", path);
      for (int j = 0; j < nodes.getLength(); j++) {
        Node messageNode = nodes.item(j);
        String key = XPathHelper.valueOf(messageNode, "@name", path);
        String value = XPathHelper.valueOf(messageNode, "value/text()", path);
        if (warn && p.containsKey(key)) {
          log_.warn("I18n key '" + key + "' redefined in " + f);
        }
        p.put(key, value);
      }
    } catch (ParserConfigurationException e) {
      log_.warn("Parser configuration error when reading i18n file " + f + ":" + e.getMessage());
    } catch (SAXException e) {
      log_.warn("SAX exception while parsing i18n file " + f + ":" + e.getMessage());
    } catch (IOException e) {
      log_.warn("IO exception while parsing i18n file " + f + ":" + e.getMessage());
    }
  }

  /**
   * Escape any single quote characters that are included in the specified
   * message string.
   * 
   * @param string
   *          The string to be escaped
   * @return escaped string
   */
  protected static String escape(String string) {
    if ((string == null) || (string.indexOf('\'') < 0)) {
      return (string);
    }
    int n = string.length();
    StringBuffer sb = new StringBuffer(n);
    for (int i = 0; i < n; i++) {
      char ch = string.charAt(i);
      if (ch == '\'') {
        sb.append('\'');
      }
      sb.append(ch);
    }
    return (sb.toString());
  }

  /**
   * Class used to filter the acceptable files for the i18n mechanism. The files
   * have to be named <code>xyz.xml</code> or <code>xyz_de.xml</code>.
   * 
   * @author Tobias Wunden
   * 
   */
  static final class I18nFileFilter extends FileFilter {

    public boolean accept(File f) {
      if (f != null) {
        String name = f.getName();
        return name.endsWith(".xml");
      }
      return false;
    }

    /**
     * Returns a description for this filter
     * 
     * @see javax.swing.filechooser.FileFilter#getDescription()
     */
    public String getDescription() {
      return "i18n files";
    }

  }

}