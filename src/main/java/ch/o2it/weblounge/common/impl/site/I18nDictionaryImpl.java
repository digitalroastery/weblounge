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

package ch.o2it.weblounge.common.impl.site;

import ch.o2it.weblounge.common.impl.language.LanguageSupport;
import ch.o2it.weblounge.common.impl.util.xml.XMLUtilities;
import ch.o2it.weblounge.common.impl.util.xml.XPathHelper;
import ch.o2it.weblounge.common.language.Language;
import ch.o2it.weblounge.common.site.I18nDictionary;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringEscapeUtils;
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
  private final static Logger log_ = LoggerFactory.getLogger(I18nDictionaryImpl.class);

  /** The i18n directories */
  protected Map<Language, Properties> i18n = new HashMap<Language, Properties>();

  /** The defaults (language neutral) */
  private Properties defaults = new Properties();

  /** The default language */
  protected Language defaultLanguage = null;

  /**
   * Creates a new <code>i18n</code> dictionary.
   */
  public I18nDictionaryImpl() {
  }

  /**
   * Creates a new <code>i18n</code> dictionary with the given default language.
   * 
   * @param defaultLanguage
   *          the default language
   */
  public I18nDictionaryImpl(Language defaultLanguage) {
    this.defaultLanguage = defaultLanguage;
  }

  /**
   * {@inheritDoc}
   *
   * @see ch.o2it.weblounge.common.site.I18nDictionary#setDefaultLanguage(ch.o2it.weblounge.common.language.Language)
   */
  public void setDefaultLanguage(Language language) {
    this.defaultLanguage = language;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.site.I18nDictionary#add(java.lang.String,
   *      java.lang.String, ch.o2it.weblounge.common.language.Language)
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
      log_.warn("I18n key '{}' already defined", key);
    }
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.site.I18nDictionary#get(java.lang.String,
   *      ch.o2it.weblounge.common.language.Language)
   */
  public String get(String key, Language language) {
    return get(key, language, false);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.site.I18nDictionary#get(java.lang.String,
   *      ch.o2it.weblounge.common.language.Language, boolean)
   */
  public String get(String key, Language language, boolean force) {
    Properties p = i18n.get(language);
    String dictEntry = null;
    if (p != null)
      dictEntry = p.getProperty(key);

    // Either return the entry or try the default language fallback
    if (dictEntry != null)
      return dictEntry;
    else if (force)
      return null;
    else if (defaultLanguage != null && !language.equals(defaultLanguage))
      return get(key, defaultLanguage);

    // Try defaults
    String defaultValue = defaults.getProperty(key, key);
    if (defaultValue != null)
      return defaultValue;
    
    // Last resort: return the key itself
    return key;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.site.I18nDictionary#getAsHTML(java.lang.String,
   *      ch.o2it.weblounge.common.language.Language)
   */
  public String getAsHTML(String key, Language language) {
    return StringEscapeUtils.escapeHtml(get(key, language, false));
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.site.I18nDictionary#getAsHTML(java.lang.String,
   *      ch.o2it.weblounge.common.language.Language, boolean)
   */
  public String getAsHTML(String key, Language language, boolean force) {
    String value = get(key, language, true);
    return value != null ? StringEscapeUtils.escapeHtml(value) : null;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.site.I18nDictionary#remove(java.lang.String,
   *      ch.o2it.weblounge.common.language.Language)
   */
  public void remove(String key, Language language) {
    Properties p = null;
    if (language == null)
      p = i18n.get(language);
    else
      p = defaults;
    if (p != null)
      p.remove(key);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.site.I18nDictionary#remove(java.lang.String)
   */
  public void remove(String key) {
    defaults.remove(key);
    for (Properties p : i18n.values()) {
      p.remove(key);
    }
  }
  
  /**
   * Adds the the dictionary found in <code>file</code> to the current i18n
   * definitions. If <code>warn</code> is <code>false</code> then warnings about
   * existing keys are suppressed.
   * 
   * @param url
   *          the i18n dictionary
   * @param warn
   *          <code>true</code> to warn on duplicate keys
   */
  public void addDictionary(URL url, boolean warn) {
    DocumentBuilder docBuilder;
    try {
      docBuilder = XMLUtilities.getDocumentBuilder();
      Document doc = docBuilder.parse(url.openStream());
      String name = FilenameUtils.getBaseName(url.getFile());
      Language language = null;

      // Get the language

      String languageId = null;
      int lidstart = name.indexOf('_') + 1;
      if (lidstart > 0 && lidstart < name.length()) {
        languageId = name.substring(lidstart);
        language = LanguageSupport.getLanguage(languageId);
      }
      
      log_.info("Reading i18n dictionary {}", name);

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
          log_.warn("I18n key '{}' redefined in {}", key, url);
        }
        p.put(key, value);
      }
    } catch (ParserConfigurationException e) {
      log_.warn("Parser configuration error when reading i18n file {}: {}", url, e.getMessage());
    } catch (SAXException e) {
      log_.warn("SAX exception while parsing i18n file {}: {}", url, e.getMessage());
    } catch (IOException e) {
      log_.warn("IO exception while parsing i18n file {}: {}", url, e.getMessage());
    }
  }

}
