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

package ch.entwine.weblounge.common.impl.util.doc;

import freemarker.core.ParseException;
import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Template;
import freemarker.template.TemplateException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Map;

/**
 * Model used to create endpoint documentation.
 */
public final class EndpointDocumentationGenerator {

  /** Logger facility */
  private static final Logger logger = LoggerFactory.getLogger(EndpointDocumentationGenerator.class);

  /** The template processor */
  private static Configuration freemarkerConfig = null;

  static {
    freemarkerConfig = new Configuration();
    freemarkerConfig.setObjectWrapper(new DefaultObjectWrapper());
    freemarkerConfig.clearTemplateCache();
  }

  /**
   * This class is not intended to be instantiated.
   */
  private EndpointDocumentationGenerator() {
    // Nothing to be done here
  }

  /**
   * Handles the replacement of the variable strings within textual templates
   * and also allows the setting of variables for the control of logical
   * branching within the text template as well<br/>
   * Uses and expects freemarker (http://freemarker.org/) style templates (that
   * is using ${name} as the marker for a replacement)<br/>
   * NOTE: These should be compatible with Velocity
   * (http://velocity.apache.org/) templates if you use the formal notation
   * (formal: ${variable}, shorthand: $variable)
   * 
   * @param templateName
   *          this is the key to cache the template under
   * @param textTemplate
   *          a freemarker/velocity style text template, cannot be null or empty
   *          string
   * @param data
   *          a set of replacement values which are in the map like so:<br/>
   *          key => value (String => Object)<br/>
   *          "username" => "aaronz"<br/>
   * @return the processed template
   */
  private static String processTextTemplate(String templateName,
      String textTemplate, Map<String, Object> data) {

    if (freemarkerConfig == null)
      throw new IllegalStateException("FreemarkerConfig is not initialized");
    if (StringUtils.trimToNull(templateName) == null)
      throw new IllegalArgumentException("The templateName cannot be null or empty string, " + "please specify a key name to use when processing this template (can be anything moderately unique)");
    if (data == null || data.size() == 0)
      return textTemplate;
    if (StringUtils.trimToNull(textTemplate) == null)
      throw new IllegalArgumentException("The textTemplate cannot be null or empty string, " + "please pass in at least something in the template or do not call this method");

    // get the template
    Template template = null;
    try {
      template = new Template(templateName, new StringReader(textTemplate), freemarkerConfig);
    } catch (ParseException e) {
      String msg = "Failure while parsing the Doc template (" + templateName + "), template is invalid: " + e + " :: template=" + textTemplate;
      logger.error(msg);
      throw new RuntimeException(msg, e);
    } catch (IOException e) {
      throw new RuntimeException("Failure while creating freemarker template", e);
    }

    // process the template
    String result = null;
    try {
      Writer output = new StringWriter();
      template.process(data, output);
      result = output.toString();
      logger.debug("Generated complete document ({} chars) from template ({})", result.length(), templateName);
    } catch (TemplateException e) {
      result = "Failed while processing the template (" + templateName + "): " + e.getMessage() + "\n";
      result += "Template: " + textTemplate + "\n";
      result += "Data: " + data;
      logger.error("Failed while processing the Doc template ({}): {}", templateName, e);
    } catch (IOException e) {
      throw new RuntimeException("Failure while sending freemarker output to stream", e);
    }

    return result;
  }

  /**
   * Use this method to generate the documentation using passed in document data
   * 
   * @param data
   *          any populated DocData object
   * @return the documentation (e.g. REST html) as a string
   * @throws IllegalArgumentException
   *           if the input data is invalid in some way
   */
  public static String generate(EndpointDocumentation data) {
    String template = loadTemplate(data.getDefaultTemplatePath());
    return generate(data, template);
  }

  /**
   * Use this method to generate the documentation using passed in document
   * data, allows the user to specify the template that is used
   * 
   * @param data
   *          any populated DocData object
   * @param template
   *          any freemarker template which works with the DocData data
   *          structure
   * @return the documentation (e.g. REST html) as a string
   * @throws IllegalArgumentException
   *           if the input data is invalid in some way
   */
  public static String generate(EndpointDocumentation data, String template) {
    if (template == null) {
      throw new IllegalArgumentException("template must be set");
    }
    return processTextTemplate(data.getMetaData("name"), template, data.toMap());
  }

  /**
   * Loads a template based on the given path
   * 
   * @param path
   *          the path to load the template from (uses the current classloader)
   * @return the template as a string
   */
  public static String loadTemplate(String path) {
    String textTemplate;
    InputStream in = null;
    try {
      in = EndpointDocumentationGenerator.class.getResourceAsStream(path);
      if (in == null) {
        throw new IllegalStateException("No template file could be found at: " + path);
      }
      textTemplate = new String(IOUtils.toByteArray(in), "utf-8");
    } catch (IOException e) {
      logger.error("failed to load template file from path (" + path + "): " + e, e);
      textTemplate = null;
    } finally {
      IOUtils.closeQuietly(in);
    }
    return textTemplate;
  }

}
