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

package ch.entwine.weblounge.common.impl.util;

import ch.entwine.weblounge.common.impl.util.config.ConfigurationUtils;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.http.Header;
import org.apache.http.HeaderIterator;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.w3c.dom.Document;
import org.w3c.tidy.Tidy;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

/**
 * Utility class containing a few helper methods.
 */
public final class TestUtils {

  /** Name of the property to indicate an ongoing unit or integration test */
  private static final String TEST_PROPERTY = "weblounge.test";

  /**
   * This utility class is not intended to be instantiated.
   */
  private TestUtils() {
    // Nothing to do
  }

  /**
   * Loads the <code>XML</code> data from the specified file on the class path
   * and returns it after having stripped off any newlines, line breaks and
   * otherwise disturbing spaces and characters.
   * 
   * @param path
   *          the resource path
   * @return the contents of the resource
   */
  public static String loadXmlFromResource(String path) {
    File templateFile = new File(TestUtils.class.getResource(path).getPath());
    String template = null;
    try (FileInputStream f = new FileInputStream(templateFile)) {
      byte[] buffer = new byte[(int) templateFile.length()];
      f.read(buffer);
      template = new String(buffer, "utf-8").replaceFirst("<\\?.*?>", "");
      template = template.replaceAll("(>\\s*)+", ">").replaceAll("(\\s*<)+", "<");
    } catch (IOException e) {
      throw new RuntimeException("Error reading test resource at " + path);
    }
    return template;
  }

  /**
   * Loads the <code>JSON</code> data from the specified file on the class path
   * and returns it after having stripped off any newlines, line breaks and
   * otherwise disturbing spaces and characters.
   * 
   * @param path
   *          the resource path
   * @return the contents of the resource
   */
  public static String loadJsonFromResource(String path) {
    File templateFile = new File(TestUtils.class.getResource(path).getPath());
    String template = null;
    try (FileInputStream f = new FileInputStream(templateFile)) {
      byte[] buffer = new byte[(int) templateFile.length()];
      f.read(buffer);
      template = new String(buffer, "utf-8");
      template = template.replaceAll("(\"\\s*)", "\"").replaceAll("(\\s*\")+", "\"");
      template = template.replaceAll("(\\s*\\{\\s*)", "{").replaceAll("(\\s*\\}\\s*)", "}");
      template = template.replaceAll("(\\s*\\[\\s*)", "[").replaceAll("(\\s*\\]\\s*)", "]");
      template = template.replaceAll("(\\s*,\\s*)", ",");
    } catch (IOException e) {
      throw new RuntimeException("Error reading test resource at " + path);
    }
    return template;
  }

  /**
   * Parses the <code>HTTP</code> response body into a <code>DOM</code>
   * document.
   * 
   * @param response
   *          the response
   * @return the parsed xml
   * @throws Exception
   *           if parsing fails
   */
  public static Document parseXMLResponse(HttpResponse response)
      throws Exception {
    String responseXml = EntityUtils.toString(response.getEntity(), "utf-8");
    responseXml = StringEscapeUtils.unescapeHtml4(responseXml);

    // Depending on whether it's an HTML page, let's make sure we end up with a
    // valid DOM
    Header contentTypeHeader = response.getFirstHeader("Content-Type");
    String contentType = contentTypeHeader != null ? contentTypeHeader.getValue() : null;

    Document doc = null;
    if ("text/html".equals(contentType)) {
      Tidy tidy = new Tidy();
      tidy.setOnlyErrors(true);
      tidy.setOutputEncoding("utf-8");
      doc = tidy.parseDOM(IOUtils.toInputStream(responseXml, "utf-8"), null);
    } else {
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      factory.setNamespaceAware(true);
      DocumentBuilder builder = factory.newDocumentBuilder();
      doc = builder.parse(new ByteArrayInputStream(responseXml.getBytes("utf-8")));
    }
    return doc;
  }

  /**
   * Parses the <code>HTTP</code> response body into a
   * <code>java.lang.String</code>.
   * 
   * @param response
   *          the response
   * @return the response text
   * @throws Exception
   *           if extracting the text fails
   */
  public static String parseTextResponse(HttpResponse response)
      throws Exception {
    String responseText = EntityUtils.toString(response.getEntity(), "utf-8");
    return responseText;
  }

  /**
   * Returns an unescaped version of the HTML string, which also does not
   * contain any newline, return or tab characters.
   * 
   * @param responseHTML
   * @return
   */
  public static String unescapeHtml(String responseHTML) {
    String responseXML = responseHTML.replaceAll("\\r", "").replaceAll("\\n", "").replaceAll("\\t", "");
    responseXML = StringEscapeUtils.unescapeHtml4(responseXML);
    return responseXML;
  }

  /**
   * Issues the the given request.
   * 
   * @param httpClient
   *          the http client
   * @param request
   *          the request
   * @param params
   *          the request parameters
   * @throws Exception
   *           if the request fails
   */
  public static HttpResponse request(HttpClient httpClient,
      HttpUriRequest request, String[][] params) throws Exception {
    if (params != null) {
      if (request instanceof HttpGet) {
        List<NameValuePair> qparams = new ArrayList<NameValuePair>();
        if (params.length > 0) {
          for (String[] param : params) {
            if (param.length < 2)
              continue;
            qparams.add(new BasicNameValuePair(param[0], param[1]));
          }
        }
        URI requestURI = request.getURI();
        URI uri = URIUtils.createURI(requestURI.getScheme(), requestURI.getHost(), requestURI.getPort(), requestURI.getPath(), URLEncodedUtils.format(qparams, "utf-8"), null);
        HeaderIterator headerIterator = request.headerIterator();
        request = new HttpGet(uri);
        while (headerIterator.hasNext()) {
          request.addHeader(headerIterator.nextHeader());
        }
      } else if (request instanceof HttpPost) {
        List<NameValuePair> formparams = new ArrayList<NameValuePair>();
        for (String[] param : params)
          formparams.add(new BasicNameValuePair(param[0], param[1]));
        UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, "utf-8");
        ((HttpPost) request).setEntity(entity);
      } else if (request instanceof HttpPut) {
        List<NameValuePair> formparams = new ArrayList<NameValuePair>();
        for (String[] param : params)
          formparams.add(new BasicNameValuePair(param[0], param[1]));
        UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, "utf-8");
        ((HttpPut) request).setEntity(entity);
      }
    } else {
      if (request instanceof HttpPost || request instanceof HttpPut) {
        UrlEncodedFormEntity entity = new UrlEncodedFormEntity(new ArrayList<NameValuePair>(), "utf-8");
        ((HttpEntityEnclosingRequestBase) request).setEntity(entity);
      }
    }
    return httpClient.execute(request);
  }

  /**
   * Enables testing by setting a system property. This method is used to add
   * test specific code to production implementations while using a consistent
   * methodology to determine testing status.
   * <p>
   * Use {@link #isTest()} to determine whether testing has been turned on.
   */
  public static void startTesting() {
    System.setProperty(TEST_PROPERTY, Boolean.TRUE.toString());
  }

  /**
   * Returns <code>true</code> if a test is currently going on.
   * 
   * @return <code>true</code> if the current code is being executed as a test
   */
  public static boolean isTest() {
    return ConfigurationUtils.isTrue(System.getProperty(TEST_PROPERTY));
  }

}