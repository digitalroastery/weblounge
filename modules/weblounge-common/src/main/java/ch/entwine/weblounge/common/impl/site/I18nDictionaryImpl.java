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

package ch.entwine.weblounge.common.impl.site;

import ch.entwine.weblounge.common.impl.language.LanguageUtils;
import ch.entwine.weblounge.common.impl.util.xml.XMLUtils;
import ch.entwine.weblounge.common.impl.util.xml.XPathHelper;
import ch.entwine.weblounge.common.language.Language;
import ch.entwine.weblounge.common.site.I18nDictionary;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;

/**
 * Default implementation of the site specific <code>i18n</code> dictionary.
 */
public class I18nDictionaryImpl implements I18nDictionary {

  /** Logging facility */
  private static final Logger logger = LoggerFactory.getLogger(I18nDictionaryImpl.class);

  /** The i18n directories */
  protected Map<Language, Properties> i18n = new HashMap<Language, Properties>();

  /** The defaults (language neutral) */
  private final Properties defaults = new Properties();

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.site.I18nDictionary#add(java.lang.String,
   *      java.lang.String)
   */
  public void add(String key, String value) {
    add(key, value, null);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.site.I18nDictionary#add(java.lang.String,
   *      java.lang.String, ch.entwine.weblounge.common.language.Language)
   */
  public void add(String key, String value, Language language) {
    if (key == null)
      throw new IllegalArgumentException("I18n key cannot be null");
    if (value == null)
      throw new IllegalArgumentException("I18n value cannot be null");
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
    if (p.get(key) == null) {
      p.put(key, value);
    } else {
      logger.warn("I18n key '{}' already defined", key);
    }
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.site.I18nDictionary#get(java.lang.String)
   */
  public String get(String key) {
    return get(key, null);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.site.I18nDictionary#get(java.lang.String,
   *      ch.entwine.weblounge.common.language.Language)
   */
  public String get(String key, Language language) {
    Properties p = null;
    if (language != null)
      p = i18n.get(language);

    if (p == null)
      p = defaults;

    String dictEntry = null;
    if (p != null)
      dictEntry = p.getProperty(key);

    // Either return the entry or try the default language fallback
    if (dictEntry != null)
      return dictEntry;
    else {
      dictEntry = defaults.getProperty(key);
      if (dictEntry != null)
        return dictEntry;
    }

    // Last resort: return the key itself
    return key;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.site.I18nDictionary#getAsHTML(java.lang.String)
   */
  public String getAsHTML(String key) {
    String value = get(key);
    return value != null ? StringEscapeUtils.escapeHtml4(value) : null;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.site.I18nDictionary#getAsHTML(java.lang.String,
   *      ch.entwine.weblounge.common.language.Language)
   */
  public String getAsHTML(String key, Language language) {
    String value = get(key, language);
    return value != null ? StringEscapeUtils.escapeHtml4(value) : null;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.site.I18nDictionary#remove(java.lang.String)
   */
  public void remove(String key) {
    defaults.remove(key);
    for (Properties p : i18n.values()) {
      p.remove(key);
    }
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.site.I18nDictionary#remove(java.lang.String,
   *      ch.entwine.weblounge.common.language.Language)
   */
  public void remove(String key, Language language) {
    Properties p = null;
    if (language != null)
      p = i18n.get(language);
    else
      p = defaults;
    if (p != null)
      p.remove(key);
  }

  /**
   * Adds the the dictionary found in <code>file</code> to the current i18n
   * definitions. The implementation tries to derive the language from the
   * filename, which is expected to be of the form
   * <code>&lt;name&gt;_&lt;language&gt;.xml</code>, where &lt;language&gt; is
   * the ISO language identifier.
   * 
   * @param url
   *          the i18n dictionary
   */
  public void addDictionary(URL url) {
    String name = FilenameUtils.getBaseName(url.getFile());
    Language language = null;
    String languageId = null;
    int lidstart = name.indexOf('_') + 1;
    if (lidstart > 0 && lidstart < name.length()) {
      languageId = name.substring(lidstart);
      language = LanguageUtils.getLanguage(languageId);
    }
    addDictionary(url, language);
  }

  /**
   * Adds the the dictionary found in <code>file</code> to the current i18n
   * definitions.
   * 
   * @param url
   *          the i18n dictionary
   * @param language
   *          the dictionary language
   */
  public void addDictionary(URL url, Language language) {
    DocumentBuilder docBuilder;
    try {
      docBuilder = XMLUtils.getDocumentBuilder();
      Document doc = docBuilder.parse(url.openStream());

      if (language != null)
        logger.debug("Reading i18n dictionary {} ({})", url.getFile(), language);
      else
        logger.debug("Reading default i18n dictionary {}", url.getFile());

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

      int invalidKeys = 0;
      int invalidValues = 0;

      // Read and store the messages

      XPath path = XMLUtils.getXPath();
      NodeList nodes = XPathHelper.selectList(doc, "/resources/string", path);
      for (int j = 0; j < nodes.getLength(); j++) {
        Node messageNode = nodes.item(j);
        String key = XPathHelper.valueOf(messageNode, "@name", path);
        String value = XPathHelper.valueOf(messageNode, "text()", path);
        if (key == null) {
          invalidKeys++;
          continue;
        }
        if (value == null) {
          logger.debug("I18n dictionary {} lacks a value for key '{}'", url, key);
          invalidValues++;
          continue;
        }
        if (p.containsKey(key)) {
          logger.warn("I18n key '{}' redefined in {}", key, url);
        }
        p.put(key, value);
      }

      if (invalidKeys > 0)
        logger.warn("I18n dictionary {} contains {} entries withou a key", url, invalidKeys);
      if (invalidValues > 0)
        logger.warn("I18n dictionary {} contains {} keys without values", url, invalidValues);

    } catch (ParserConfigurationException e) {
      logger.warn("Parser configuration error when reading i18n dictionary {}: {}", url, e.getMessage());
    } catch (SAXException e) {
      logger.warn("SAX exception while parsing i18n dictionary {}: {}", url, e.getMessage());
    } catch (IOException e) {
      logger.warn("IO exception while parsing i18n dictionary {}: {}", url, e.getMessage());
    }
  }

}
