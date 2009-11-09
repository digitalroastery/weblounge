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

package ch.o2it.weblounge.common.impl.language;

import ch.o2it.weblounge.common.ConfigurationException;
import ch.o2it.weblounge.common.impl.util.xml.XPathHelper;
import ch.o2it.weblounge.common.language.Language;
import ch.o2it.weblounge.common.language.LanguageManager;
import ch.o2it.weblounge.common.language.UnknownLanguageException;
import ch.o2it.weblounge.common.language.UnsupportedLanguageException;
import ch.o2it.weblounge.common.site.Site;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

import javax.xml.xpath.XPath;

/**
 * <code>LanguageSupport</code> is a helper class the facilitates the handling
 * of languages in numerous ways.
 */
public final class LanguageSupport {

  /** Logging facility */
  private final static Logger log_ = LoggerFactory.getLogger(LanguageSupport.class);

  /** Globally available languages */
  private final static Map<String, Language> systemLanguages = new HashMap<String, Language>();

  /**
   * Returns the language object that represents the given locale.
   * 
   * @param locale
   *          the locale
   * @return the language
   * @throws UnsupportedLanguageException
   *           if there is no language for the given locale
   */
  public static Language getLanguage(Locale locale)
      throws UnsupportedLanguageException {
    Language language = systemLanguages.get(locale.getLanguage());
    if (language == null)
      systemLanguages.put(locale.getLanguage(), new LanguageImpl(locale));
    return language;
  }

  /**
   * Returns the language object identified by the language identifier.
   * 
   * @param languageCode
   *          the language identifier
   * @return the language
   * @throws UnsupportedLanguageException
   *           if there is no language for the given locale
   */
  public static Language getLanguage(String languageCode)
      throws UnsupportedLanguageException {
    Language language = systemLanguages.get(languageCode);
    if (language != null)
      return language;
    for (Locale locale : Locale.getAvailableLocales()) {
      if (locale.getLanguage().equals(languageCode)) {
        language = new LanguageImpl(locale);
        systemLanguages.put(languageCode, language);
      }
    }
    return language;
  }

  /**
   * Reads the names of an object described in a weblounge configuration file in
   * various languages and applies them to the multilingual object
   * <code>o</code>. <br>
   * The required format of the input node is as follows:
   * 
   * <pre>
   *     &lt;role&gt;
   *         &lt;id&gt;editor&lt;/id&gt;
   *         &lt;name language=&quot;de&quot;&gt;Editor&lt;/name&gt;
   *         &lt;name language=&quot;fr&quot;&gt;Editeur&lt;/name&gt;
   *         &lt;name language=&quot;it&quot;&gt;Editore&lt;/name&gt;
   * &lt;/role&gt;
   * 
   * <pre>
   * &lt;br&gt;
   * The method throws a &lt;code&gt;ConfigurationException&lt;/code&gt; if no name is
   * provided the site default language.
   * 
   * &#064;param configuration the xml configuration node containing the descriptions
   * &#064;param defaultLanguage the default language
   * &#064;param o the described object
   * &#064;param javascript &lt;code&gt;true&lt;/code&gt; to filter out &quot; and '
   * &#064;param path the XPath object used to parse the configuration
   * &#064;return the multilingual object with the descriptions applied
   * &#064;throws ConfigurationException if the description in the site default language
   * has not been provided
   */
  public static LocalizableContent<String> addDescriptions(XPath path,
      Node configuration, Language[] allLanguages, Language defaultLanguage,
      LocalizableContent<String> o, boolean javascript)
      throws ConfigurationException {
    Map<Language, String> languages = LanguageSupport.readDescriptions(path, configuration, allLanguages, defaultLanguage);
    for (Map.Entry<Language, String> entry : languages.entrySet()) {
      Language l = entry.getKey();
      String description = entry.getValue();
      if (javascript) {
        description = description.replaceAll("\"", "");
        description = description.replaceAll("'", "");
      }
      o.put(description, l);
      if (l.equals(defaultLanguage)) {
        o.setDefaultLanguage(defaultLanguage);
      }
    }

    if (defaultLanguage != null && o.getDefaultLanguage() == null) {
      throw new ConfigurationException("Default description (" + defaultLanguage + ") not found!");
    }
    return o;
  }

  /**
   * Reads the names of an object described in a weblounge configuration file in
   * various languages and applies them to the multilingual object
   * <code>o</code>. <br>
   * The required format of the input node is as follows:
   * 
   * <pre>
   *     &lt;role&gt;
   *         &lt;id&gt;editor&lt;/id&gt;
   *         &lt;name language=&quot;de&quot;&gt;Editor&lt;/name&gt;
   *         &lt;name language=&quot;fr&quot;&gt;Editeur&lt;/name&gt;
   *         &lt;name language=&quot;it&quot;&gt;Editore&lt;/name&gt;
   * &lt;/role&gt;
   * 
   * <pre>
   * &lt;br&gt;
   * The method throws a &lt;code&gt;ConfigurationException&lt;/code&gt; if no name is
   * provided the site default language.
   * 
   * &#064;param configuration the xml configuration node containing the descriptions
   * &#064;param defaultLanguage the default language
   * &#064;param o the described object
   * &#064;param path the XPath object used to parse the configuration
   * &#064;return the multilingual object with the descriptions applied
   * &#064;throws ConfigurationException if the description in the site default language
   * has not been provided
   */
  public static LocalizableContent<String> addDescriptions(XPath path,
      Node configuration, Language[] allLanguages, Language defaultLanguage,
      LocalizableContent<String> o) throws ConfigurationException {
    return addDescriptions(path, configuration, allLanguages, defaultLanguage, o, false);
  }

  /**
   * Returns a String array containing all the variants defined for the given
   * Site. For example, if <code>s</code> is <tt>file.jsp</tt> and
   * <code>language</code> is <tt>german</tt>, then this method returns
   * <tt>file_de.jsp</tt>.
   * 
   * @param s
   *          the file name
   * @param language
   *          the language variant to obtain
   * @return the localized variant of the text
   */
  public static String getLanguageVariant(String s, Language language) {
    int suffixPosition = s.lastIndexOf(".");
    String suffix = "";
    if (suffixPosition > -1) {
      suffix = s.substring(suffixPosition);
      s = s.substring(0, suffixPosition);
    }
    s += "_" + language.getIdentifier() + suffix;
    return s;
  }

  /**
   * Returns a String array containing the language variants of <code>s</code>
   * for the given language and site.
   * <p>
   * The priorities are evaluated as follows:
   * <ul>
   * <li>The requested language <code>l</code></li>
   * <li>The site default language of <code>site</code></li>
   * <li>The international version, equal to the original <code>s</code></li>
   * </ul>
   * 
   * @param s
   *          the file name
   * @param l
   *          the language variant to obtain
   * @param site
   *          the site
   * @return the localized variant of the text
   */
  public static String[] getLanguageVariantsByPriority(String s, Language l,
      Site site) {
    String[] variants = new String[3 - ((l == null) ? 1 : 0)];
    if (l != null) {
      variants[0] = getLanguageVariant(s, l);
    }
    variants[1 - ((l == null) ? 1 : 0)] = getLanguageVariant(s, site.getDefaultLanguage());
    variants[2 - ((l == null) ? 1 : 0)] = s;
    return variants;
  }

  /**
   * Returns all language variants of the filename <code>s</code> that are
   * supported by the given site or, more precise, by the associated
   * <code>LanguageRegistry</code>.
   * 
   * @param s
   *          the filename
   * @param languages
   *          the languages used to build the variants
   */
  public static String[] getLanguageVariants(String s, LanguageManager languages) {
    String[] result = new String[languages.getLanguageCount() + 1];
    result[0] = s;
    Iterator<Language> li = languages.languages();
    int i = 1;
    while (li.hasNext()) {
      Language language = (Language) li.next();
      result[i] = getLanguageVariant(s, language);
      i++;
    }
    return result;
  }

  /**
   * Returns all language variants of the filename <code>s</code> that are
   * supported by the given site or, more precise, by the associated
   * <code>LanguageRegistry</code>.
   * <p>
   * The last entry of the returned variants is the original string
   * <code>s</code>.
   * 
   * @param s
   *          the filename
   * @param languages
   *          the languages used to build the variants
   */
  public static String[] getLanguageVariants(String s, Language[] languages) {
    String[] result = new String[languages.length + 1];
    result[languages.length] = s;
    for (int i = 0; i < languages.length; i++) {
      Language language = languages[i];
      result[i] = getLanguageVariant(s, language);
    }
    return result;
  }

  /**
   * Returns the original version string of text <code>s</code>. For example, if
   * <tt>s</tt> equals <tt>file_de.jsp</tt> then this method returns
   * <tt>file.jsp</tt>.
   * 
   * @param s
   *          the language filename
   * @param languages
   *          the languages
   * @return the original filenme
   */
  public static String getInternationalVersion(String s,
      LanguageManager languages) {
    Language l = extractLanguage(s, languages);
    if (l == null) {
      return s;
    }
    int languagePos = s.indexOf("_" + l.getIdentifier());
    String original = s.substring(0, languagePos);
    if (s.length() > languagePos + 3) {
      String suffix = s.substring(languagePos + 3);
      original += suffix;
    }
    return original;
  }

  /**
   * Returns the language of this file. For example, if <tt>s</tt> is
   * <tt>file_de.jsp</tt> then this method returns the german language object. <br>
   * <b>Note:</b> This method returns <code>null</code> if the string does not
   * contains an unknown language identifier or no language identifier at all.
   * 
   * @param s
   *          the filename
   * @param languages
   *          the languages
   * @return the language object or <code>null</code>
   */
  public static Language extractLanguage(String s, LanguageManager languages) {
    int languagePosition = s.lastIndexOf("_");
    if ((languagePosition < 0) || (languagePosition + 1 > s.length())) {
      return null;
    }
    try {
      String languageId = s.substring(languagePosition + 1, languagePosition + 3);
      return languages.getLanguage(languageId);
    } catch (UnknownLanguageException e) {
      return null;
    }
  }

  /**
   * Reads the names of an object described in a weblounge configuration file in
   * various languages and returns them as a hashmap of language - name
   * mappings. <br>
   * The required format of the input node is as follows:
   * 
   * <pre>
   *     &lt;role&gt;
   *         &lt;id&gt;editor&lt;/id&gt;
   *         &lt;name language=&quot;de&quot;&gt;Editor&lt;/name&gt;
   *         &lt;name language=&quot;fr&quot;&gt;Editeur&lt;/name&gt;
   *         &lt;name language=&quot;it&quot;&gt;Editore&lt;/name&gt;
   * &lt;/role&gt;
   * 
   * <pre>
   * &lt;br&gt;
   * The method throws a &lt;code&gt;ConfigurationException&lt;/code&gt; if no name is
   * provided in the site default language and the &lt;code&gt;site&lt;/code&gt; parameter
   * is not null. Otherwise, the check is not performed.
   * 
   * &#064;param xmlNode the xml configuration node
   * &#064;param defaultLanguage the default language
   * &#064;param path the XPath object used to parse the configuration
   * &#064;return List a hashmap containing the descriptions
   * &#064;throws ConfigurationException if the site default language has not been
   * provided
   */
  private static Map<Language, String> readDescriptions(XPath path,
      Node xmlNode, Language[] languages, Language defaultLanguage)
      throws ConfigurationException {
    if (languages.length == 0)
      return new HashMap<Language, String>();

    Map<Language, String> descriptions = new HashMap<Language, String>();
    boolean isDefaultSupported = false;

    try {
      boolean found = false;
      NodeList nodes = XPathHelper.selectList(xmlNode, "name", path);
      for (int i = 0; i < nodes.getLength(); i++) {
        Node name = nodes.item(i);
        String description = XPathHelper.valueOf(name, "text()", path);
        String lAttrib = XPathHelper.valueOf(name, "@language", path);
        Language language = null;
        for (Language l : languages) {
          if (l.getIdentifier().equals(lAttrib)) {
            language = l;
            break;
          }
        }
        if (language == null) {
          log_.debug("Found name in unsupported language " + lAttrib);
          continue;
        }
        if (defaultLanguage != null && defaultLanguage.equals(language))
          isDefaultSupported = true;
        log_.debug("Found description " + description);
        descriptions.put(language, description);
        found = true;
      }
      if (!found) {
        log_.info("No descriptions found for node '" + xmlNode.getLocalName() + "'");
      }
    } catch (Exception e1) {
      log_.warn("Error when reading language versions");
    }
    if (defaultLanguage != null && !isDefaultSupported) {
      throw new ConfigurationException("Default language " + defaultLanguage + " is not supported!");
    }
    return descriptions;
  }

}