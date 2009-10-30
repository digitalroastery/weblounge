/*
 * Weblounge: Web Content Management System Copyright (c) 2007 The Weblounge
 * Team http://weblounge.o2it.ch
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any
 * later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */

package ch.o2it.weblounge.common.impl.util;

import ch.o2it.weblounge.common.impl.language.LanguageSupport;
import ch.o2it.weblounge.common.impl.url.UrlSupport;
import ch.o2it.weblounge.common.language.Language;
import ch.o2it.weblounge.common.site.Site;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

/**
 * This class contains utility methods to handle templates.
 * 
 * @author Tobias Wunden
 * @version 1.0
 */

public class TemplateSupport {

  // Logging

  /** the class name, used for the logging facility */
  private final static String className = TemplateSupport.class.getName();

  /** Logging facility */
  private final static Logger log_ = LoggerFactory.getLogger(className);

  /**
   * Replaces the given variable in the source document with the specified
   * value. This method replaces <code>${variable}</code> with
   * <code>value</code>.
   * 
   * @param src
   *          the source document
   * @param variable
   *          the variable to replace
   * @param value
   *          the value
   * @return the modified source
   */
  public static String replace(String src, String variable, String value) {
    if (src != null && variable != null && value != null) {
      return src.replaceAll("\\$\\{" + variable + "\\}", value);
    }
    return null;
  }

  /**
   * Replaces the given variables in the source document with the specified
   * values.
   * 
   * @param src
   *          the source document
   * @param replacements
   *          the variables to replace
   * @return the modified source
   */
  public static String replace(String src, String[][] replacements) {
    if (src != null) {
      for (int i = 0; i < replacements.length; i++) {
        if (replacements[i][0] != null && replacements[i][1] != null) {
          String replacement = (replacements[i][1] != null) ? replacements[i][1] : "";
          src = src.replaceAll("\\$\\{" + replacements[i][0] + "\\}", replacement);
        }
      }
      return src;
    }
    return null;
  }

  /**
   * Loads the resource from the classpath. The <code>path</code> denotes the
   * path to the resource to load, e. g.
   * <code>/ch/o2it/weblounge/test.txt</code>.
   * 
   * @param path
   *          the resource path
   * @return the resource
   */
  public static String load(String path) {
    InputStream is = TemplateSupport.class.getResourceAsStream(path);
    InputStreamReader isr = null;
    StringBuffer buf = new StringBuffer();
    if (is != null) {
      try {
        log_.debug("Loading " + path);
        isr = new InputStreamReader(is, Charset.forName("UTF-8"));
        char[] chars = new char[1024];
        int count = 0;
        while ((count = isr.read(chars)) > 0) {
          for (int i = 0; i < count; buf.append(chars[i++]))
            ;
        }
        return buf.toString();
      } catch (Exception e) {
        log_.warn("Error reading " + path + ": " + e.getMessage());
      } finally {
        try {
          isr.close();
          is.close();
        } catch (Exception e) {
        }
      }
      log_.debug("Editor support (javascript) loaded");
    } else {
      log_.error("Repository item not found: " + path);
    }
    return null;
  }

  /**
   * Loads the resource identified by concatenating the package name from
   * <code>clazz</code> and <code>path</code> from the classpath.
   * 
   * @param path
   *          the path relative to the package name of <code>clazz</code>
   * @param clazz
   *          the class
   * @return the resource
   */
  public static String load(String path, Class clazz) {
    Arguments.checkNull(path, "path");
    Arguments.checkNull(clazz, "clazz");
    String pkg = "/" + clazz.getPackage().getName().replace('.', '/');
    InputStream is = clazz.getResourceAsStream(UrlSupport.concat(pkg, path));
    InputStreamReader isr = null;
    StringBuffer buf = new StringBuffer();
    if (is != null) {
      try {
        log_.debug("Loading " + path);
        isr = new InputStreamReader(is, Charset.forName("UTF-8"));
        char[] chars = new char[1024];
        int count = 0;
        while ((count = isr.read(chars)) > 0) {
          for (int i = 0; i < count; buf.append(chars[i++]))
            ;
        }
        return buf.toString();
      } catch (Exception e) {
        log_.warn("Error reading " + path + ": " + e.getMessage());
      } finally {
        try {
          isr.close();
          is.close();
        } catch (Exception e) {
        }
      }
      log_.debug("Editor support (javascript) loaded");
    } else {
      log_.error("Repository item not found: " + path);
    }
    return null;
  }

  /**
   * Loads the resource identified by concatenating the package name from
   * <code>clazz</code> and <code>path</code> from the classpath.
   * 
   * @param path
   *          the path relative to the package name of <code>clazz</code>
   * @param clazz
   *          the class
   * @param language
   *          the requested language
   * @param site
   *          the associated site
   * @return the resource
   */
  public static String load(String path, Class clazz, Language language,
      Site site) {
    Arguments.checkNull(path, "path");
    Arguments.checkNull(clazz, "clazz");
    Arguments.checkNull(site, "site");
    String pkg = "/" + clazz.getPackage().getName().replace('.', '/');

    // Try to find the template in any of the usual languages
    InputStream is = null;
    String[] templates = LanguageSupport.getLanguageVariantsByPriority(path, language, site);
    for (int i = 0; i < templates.length; i++) {
      is = clazz.getResourceAsStream(UrlSupport.concat(pkg, templates[i]));
      if (is != null) {
        path = templates[i];
        break;
      }
    }

    // If is is still null, then the template doesn't exist.
    if (is == null) {
      log_.error("Template " + path + " not found in any language");
    }

    // Load the template
    InputStreamReader isr = null;
    StringBuffer buf = new StringBuffer();
    if (is != null) {
      try {
        log_.debug("Loading " + path);
        isr = new InputStreamReader(is, Charset.forName("UTF-8"));
        char[] chars = new char[1024];
        int count = 0;
        while ((count = isr.read(chars)) > 0) {
          for (int i = 0; i < count; buf.append(chars[i++]))
            ;
        }
        return buf.toString();
      } catch (Exception e) {
        log_.warn("Error reading " + path + ": " + e.getMessage());
      } finally {
        try {
          isr.close();
          is.close();
        } catch (Exception e) {
        }
      }
      log_.debug("Template " + path + " loaded");
    } else {
      log_.error("Template not found: " + path);
    }
    return null;
  }

}