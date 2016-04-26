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

package ch.entwine.weblounge.common.impl.language;

import ch.entwine.weblounge.common.content.Resource;
import ch.entwine.weblounge.common.impl.util.xml.XPathHelper;
import ch.entwine.weblounge.common.language.Language;
import ch.entwine.weblounge.common.language.Localizable;
import ch.entwine.weblounge.common.language.UnknownLanguageException;
import ch.entwine.weblounge.common.site.Site;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

/**
 * <code>LanguageSupport</code> is a helper class the facilitates the handling
 * of languages in numerous ways.
 */
public final class LanguageUtils {

  /** Logging facility */
  private static final Logger logger = LoggerFactory.getLogger(LanguageUtils.class);

  /** Regular expression to extract CH_de style Accept-Language headers */
  private static final Pattern ACCEPT_LANGUAGE_HEADER = Pattern.compile("([\\w][\\w])_([\\w][\\w])");

  /** Globally available languages */
  private static final Map<String, Language> systemLanguages = new HashMap<String, Language>();

  /**
   * This class is not meant to be instantiated.
   */
  private LanguageUtils() {
    // Nothing to be done here
  }

  /**
   * Returns the language object that represents the given locale.
   * 
   * @param locale
   *          the locale
   * @return the language
   * @throws UnknownLanguageException
   *           if there is no language for the given locale
   */
  public static Language getLanguage(Locale locale)
      throws UnknownLanguageException {

    // Do we know this language already?
    Language language = systemLanguages.get(locale.getLanguage());
    if (language != null)
      return language;

    // Makes sure we get the locale in the right format (might be hand crafted)
    Matcher matcher = ACCEPT_LANGUAGE_HEADER.matcher(locale.getLanguage());
    if (matcher.matches()) {
      locale = new Locale(matcher.group(2), matcher.group(1));
    }

    // Check the system locales for a match
    Locale systemLocale = null;
    try {
      for (Locale l : Locale.getAvailableLocales()) {
        if (l.getISO3Language().equals(locale.getISO3Language())) {
          systemLocale = l;
          break;
        } else if (l.getLanguage().equals(l.getLanguage())) {
          systemLocale = l;
          break;
        }
      }
    } catch (MissingResourceException e) {
      logger.debug("No 3 found for '{}': {}", locale, e.getMessage());
    }

    // Is there a matching system locale?
    if (systemLocale != null) {
      language = new LanguageImpl(locale);
      systemLanguages.put(locale.getLanguage(), language);
      return language;
    }

    // Apparently not...
    throw new UnknownLanguageException(locale.getLanguage());
  }

  /**
   * Returns the language object identified by the language identifier.
   * 
   * @param languageCode
   *          the language identifier
   * @return the language
   * @throws UnknownLanguageException
   *           if there is no language for the given locale
   */
  public static Language getLanguage(String languageCode)
      throws UnknownLanguageException {
    Language language = systemLanguages.get(languageCode);
    if (language != null)
      return language;
    for (Locale locale : Locale.getAvailableLocales()) {
      if (locale.getLanguage().equals(languageCode) || locale.getISO3Language().equals(languageCode)) {
        language = new LanguageImpl(new Locale(locale.getLanguage(), "", ""));
        systemLanguages.put(languageCode, language);
        break;
      }
    }
    if (language == null)
      throw new UnknownLanguageException(languageCode);
    return language;
  }

  /**
   * Reads the names of an object described in a weblounge configuration file in
   * various languages and applies them to the multilingual object
   * <code>o</code>.
   * <p>
   * The localized content is looked up in the tags specified by parameter
   * <code>tagName</code>, language identifier are expected in the
   * <code>language</code> attribute of these tags.
   * <p>
   * If the description is found in the default language, then
   * {@link LocalizableContent#setDefaultLanguage(Language)} is called.
   * <p>
   * The required format of the input node is as follows:
   * 
   * <pre>
   *     &lt;role&gt;
   *         &lt;id&gt;editor&lt;/id&gt;
   *         &lt;name language=&quot;de&quot;&gt;Editor&lt;/name&gt;
   *         &lt;name language=&quot;fr&quot;&gt;Editeur&lt;/name&gt;
   *         &lt;name language=&quot;it&quot;&gt;Editore&lt;/name&gt;
   * &lt;/role&gt;
   * </pre>
   * <p>
   * The method throws a &lt;code&gt;ConfigurationException&lt;/code&gt; if no
   * name is provided the site default language.
   * 
   * @param configuration
   *          the XML configuration node containing the descriptions
   * @param tagName
   *          the tag name containing the localized content
   * @param defaultLanguage
   *          the default language
   * @param o
   *          the localizable object
   * @param escape
   *          &lt;code&gt;true&lt;/code&gt; to filter out &quot; and '
   * @return the localized content
   */
  public static LocalizableContent<String> addDescriptions(Node configuration,
      String tagName, Language defaultLanguage, LocalizableContent<String> o,
      boolean escape) {

    XPath xpath = XPathFactory.newInstance().newXPath();
    return addDescriptions(configuration, tagName, defaultLanguage, o, escape, xpath);
  }

  /**
   * Reads the names of an object described in a weblounge configuration file in
   * various languages and applies them to the multilingual object
   * <code>o</code>.
   * <p>
   * The localized content is looked up in the tags specified by parameter
   * <code>tagName</code>, language identifier are expected in the
   * <code>language</code> attribute of these tags.
   * <p>
   * If the description is found in the default language, then
   * {@link LocalizableContent#setDefaultLanguage(Language)} is called.
   * <p>
   * The required format of the input node is as follows:
   * 
   * <pre>
   *     &lt;role&gt;
   *         &lt;id&gt;editor&lt;/id&gt;
   *         &lt;name language=&quot;de&quot;&gt;Editor&lt;/name&gt;
   *         &lt;name language=&quot;fr&quot;&gt;Editeur&lt;/name&gt;
   *         &lt;name language=&quot;it&quot;&gt;Editore&lt;/name&gt;
   * &lt;/role&gt;
   * </pre>
   * <p>
   * The method throws a &lt;code&gt;ConfigurationException&lt;/code&gt; if no
   * name is provided the site default language.
   * 
   * @param configuration
   *          the XML configuration node containing the descriptions
   * @param tagName
   *          the tag name containing the localized content
   * @param defaultLanguage
   *          the default language
   * @param o
   *          the localizable object
   * @param escape
   *          &lt;code&gt;true&lt;/code&gt; to filter out &quot; and '
   * @param xpath
   *          the xpath processor
   * @return the localized content
   */
  public static LocalizableContent<String> addDescriptions(Node configuration,
      String tagName, Language defaultLanguage, LocalizableContent<String> o,
      boolean escape, XPath xpath) {

    if (configuration == null)
      throw new IllegalArgumentException("Cannot extract from empty configuration");
    if (tagName == null)
      throw new IllegalArgumentException("Tagname must be specified");
    if (o == null)
      o = new LocalizableContent<String>();

    NodeList nodes = XPathHelper.selectList(configuration, tagName, xpath);
    for (int i = 0; i < nodes.getLength(); i++) {
      Node name = nodes.item(i);
      String description = XPathHelper.valueOf(name, "text()", xpath);
      String lAttrib = XPathHelper.valueOf(name, "@language", xpath);
      Language language = LanguageUtils.getLanguage(lAttrib);
      if (language == null) {
        logger.debug("Found name in unsupported language {}", lAttrib);
        continue;
      }

      // Escape?
      if (escape) {
        description = description.replaceAll("\"", "");
        description = description.replaceAll("'", "");
      }

      // Add the entry
      logger.debug("Found description {}", description);
      o.put(description, language);
      if (language.equals(defaultLanguage)) {
        o.setDefaultLanguage(defaultLanguage);
      }
    }
    return o;
  }

  /**
   * Returns the localized variant for the given language.
   * <p>
   * For example, if <code>s</code> is <tt>file.jsp</tt> and
   * <code>language</code> is <tt>German</tt>, then this method returns
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
   * Returns all language variants of the filename <code>s</code>, including
   * <code>s</code> itself (last in line).
   * 
   * @param s
   *          the filename
   * @param languages
   *          the languages used to build the variants
   */
  public static String[] getLanguageVariants(String s, Language... languages) {
    String[] result = new String[languages.length + 1];
    result[languages.length] = s;
    for (int i = 0; i < languages.length; i++) {
      Language language = languages[i];
      result[i] = getLanguageVariant(s, language);
    }
    return result;
  }

  /**
   * Returns the original version string of text <code>s</code>.
   * <p>
   * For example, if <tt>s</tt> equals <tt>file_de.jsp</tt> then this method
   * returns <tt>file.jsp</tt>.
   * 
   * @param s
   *          the language filename
   * @param languages
   *          the languages
   * @return the original filename
   */
  public static String getBaseVersion(String s) {
    Language l = extractLanguage(s);
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
   * <tt>file_de.jsp</tt> then this method returns the German language object.
   * <p>
   * <b>Note:</b> This method returns <code>null</code> if the string contains
   * an unknown language identifier or no language identifier at all.
   * 
   * @param s
   *          the filename
   * @return the language object or <code>null</code>
   */
  public static Language extractLanguage(String s) {
    int languagePosition = s.lastIndexOf("_");
    if ((languagePosition < 0) || (languagePosition + 1 > s.length()))
      return null;

    Language l = null;
    String languageId = s.substring(languagePosition + 1, languagePosition + 3);
    l = getLanguage(languageId);
    return l;
  }

  /**
   * Returns the language out of <code>choices</code> that matches the client's
   * requirements as indicated through the <code>Accept-Language</code> header.
   * If no match is possible, <code>null</code> is returned.
   * 
   * @param choices
   *          the available locales
   * @param request
   *          the http request
   */
  public static Language getPreferredLanguage(Set<Language> choices,
      HttpServletRequest request) {
    if (request.getHeader("Accept-Language") != null) {
      Enumeration<?> locales = request.getLocales();
      while (locales.hasMoreElements()) {
        try {
          Language l = getLanguage((Locale) locales.nextElement());
          if (choices.contains(l))
            return l;
        } catch (UnknownLanguageException e) {
          // never mind, some clients will send stuff like "*" as the locale
        }

      }
    }
    return null;
  }

  /**
   * Returns the preferred one out of of those languages that are requested by
   * the client through the <code>Accept-Language</code> header and are
   * supported by both the localizable and the site.
   * <p>
   * The preferred one is defined by the following priorities:
   * <ul>
   * <li>Requested by the client</li>
   * <li>The localizable's original language</li>
   * <li>The site default language</li>
   * <li>The first language of what is supported by both the localizable and the
   * site</li>
   * </ul>
   * 
   * @param localizable
   *          the localizable
   * @param request
   *          the http request
   * @param site
   *          the site
   */
  public static Language getPreferredLanguage(Localizable localizable,
      HttpServletRequest request, Site site) {

    // Path
    String[] pathElements = StringUtils.split(request.getRequestURI(), "/");
    for (String element : pathElements) {
      for (Language l : localizable.languages()) {
        if (l.getIdentifier().equals(element)) {
          return l;
        }
      }
    }

    // Accept-Language header
    if (request.getHeader("Accept-Language") != null) {
      Enumeration<?> locales = request.getLocales();
      while (locales.hasMoreElements()) {
        try {
          Language l = getLanguage((Locale) locales.nextElement());
          if (localizable != null && !localizable.supportsLanguage(l))
            continue;
          if (!site.supportsLanguage(l))
            continue;
          return l;
        } catch (UnknownLanguageException e) {
          // never mind, some clients will send stuff like "*" as the locale
        }
      }
    }

    // The localizable's original language
    if (localizable != null && localizable instanceof Resource) {
      Resource<?> r = (Resource<?>) localizable;
      if (r.getOriginalContent() != null) {
        if (site.supportsLanguage(r.getOriginalContent().getLanguage()))
          return r.getOriginalContent().getLanguage();
      }
    }

    // Site default language
    if (localizable != null && localizable.supportsLanguage(site.getDefaultLanguage())) {
      return site.getDefaultLanguage();
    }

    // Any match
    if (localizable != null) {
      for (Language l : site.getLanguages()) {
        if (localizable.supportsLanguage(l)) {
          return l;
        }
      }
    }

    return null;
  }

  /**
   * Returns the preferred one out of of those languages that are requested by
   * the client through the <code>Accept-Language</code> header and are
   * supported by both the resource in that there is resource content in that
   * language and the site.
   * <p>
   * The preferred one is defined by the following priorities:
   * <ul>
   * <li>Requested by the client</li>
   * <li>The resource's original language</li>
   * <li>The site default language</li>
   * <li>The first language of what is supported by both the resource and the
   * site</li>
   * </ul>
   * 
   * @param resource
   *          the resource
   * @param request
   *          the http request
   * @param site
   *          the site
   */
  public static Language getPreferredContentLanguage(Resource<?> resource,
      HttpServletRequest request, Site site) {

    if (resource == null)
      throw new IllegalArgumentException("Resource must not be null");

    // Path
    String[] pathElements = StringUtils.split(request.getRequestURI(), "/");
    for (String element : pathElements) {
      for (Language l : resource.contentLanguages()) {
        if (l.getIdentifier().equals(element)) {
          return l;
        }
      }
    }

    // Accept-Language header
    if (request.getHeader("Accept-Language") != null) {
      Enumeration<?> locales = request.getLocales();
      while (locales.hasMoreElements()) {
        try {
          Language l = getLanguage((Locale) locales.nextElement());
          if (l == null)
            continue;
          if (!resource.supportsContentLanguage(l))
            continue;
          if (!site.supportsLanguage(l))
            continue;
          return l;
        } catch (UnknownLanguageException e) {
          // never mind, some clients will send stuff like "*" as the locale
        }
      }
    }

    // Original content
    if (resource.getOriginalContent() != null) {
      if (site.supportsLanguage(resource.getOriginalContent().getLanguage()))
        return resource.getOriginalContent().getLanguage();
    }

    // Site default language
    if (resource.supportsContentLanguage(site.getDefaultLanguage())) {
      return site.getDefaultLanguage();
    }

    // Any match
    for (Language l : site.getLanguages()) {
      if (resource.supportsContentLanguage(l)) {
        return l;
      }
    }

    return null;
  }

  /**
   * Returns the preferred one out of of those languages that are requested by
   * the client through the <code>Accept-Language</code> header and are
   * supported by the site. If there is no match, the site's default language is
   * returned.
   * <p>
   * The preferred one is defined by the following priorities:
   * <ul>
   * <li>Requested by the client</li>
   * <li>The site default language</li>
   * </ul>
   * 
   * @param request
   *          the http request
   * @param site
   *          the site
   */
  public static Language getPreferredLanguage(HttpServletRequest request,
      Site site) {

    // Accept-Language header
    if (request.getHeader("Accept-Language") != null) {
      Enumeration<?> locales = request.getLocales();
      while (locales.hasMoreElements()) {
        try {
          Language l = getLanguage((Locale) locales.nextElement());
          if (site.supportsLanguage(l))
            return l;
        } catch (UnknownLanguageException e) {
          // never mind, some clients will send stuff like "*" as the locale
        }
      }
    }

    return site.getDefaultLanguage();
  }

  /**
   * Returns the first language out of <code>choices</code> that is supported by
   * the <code>localizable</code>. If there is no such language,
   * <code>null</code> is returned.
   * 
   * @param localizable
   *          the localizable object
   * @param languages
   *          the prioritized list of possible languages
   * @return the first matching language or <code>null</code>
   * @throws IllegalArgumentException
   *           if <code>localizable</code> is <code>null</code>
   */
  public static Language getPreferredLanguage(Localizable localizable,
      Language... languages) {
    if (localizable == null)
      throw new IllegalArgumentException("Localizable cannot be null");
    if (languages == null)
      return null;
    for (Language l : languages) {
      if (localizable.supportsLanguage(l))
        return l;
    }
    return null;
  }

  /**
   * Returns the first language out of <code>choices</code> that is supported by
   * the <code>localizable</code>. If there is no such language,
   * <code>null</code> is returned.
   * 
   * @param localizable
   *          the localizable object
   * @param languages
   *          the prioritized list of possible languages
   * @return the first matching language or <code>null</code>
   * @throws IllegalArgumentException
   *           if <code>localizable</code> is <code>null</code>
   */
  public static Language getPreferredContentLanguage(Resource<?> resource,
      Language... languages) {
    if (resource == null)
      throw new IllegalArgumentException("Resource cannot be null");
    if (languages == null)
      return null;
    for (Language l : languages) {
      if (resource.supportsContentLanguage(l))
        return l;
    }
    return null;
  }

}