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

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.http.HeaderIterator;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.w3c.dom.Document;

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
    try {
      byte[] buffer = new byte[(int) templateFile.length()];
      FileInputStream f = new FileInputStream(templateFile);
      f.read(buffer);
      f.close();
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
    try {
      byte[] buffer = new byte[(int) templateFile.length()];
      FileInputStream f = new FileInputStream(templateFile);
      f.read(buffer);
      f.close();
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
    responseXml = StringEscapeUtils.unescapeHtml(responseXml);
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    factory.setNamespaceAware(true);
    DocumentBuilder builder = factory.newDocumentBuilder();
    return builder.parse(new ByteArrayInputStream(responseXml.getBytes("utf-8")));
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
   * Parses the <code>HTTP</code> response body into a <code>JSON</code> object.
   * 
   * @param response
   *          the response
   * @return the parsed json
   * @throws Exception
   *           if parsing fails
   */
  public static JSONObject parseJSONResponse(HttpResponse response)
      throws Exception {
    String responseJson = EntityUtils.toString(response.getEntity(), "utf-8");
    JSONObject json = new JSONObject(responseJson);
    return json;
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
    responseXML = StringEscapeUtils.unescapeHtml(responseXML);
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
        for (String[] param : params)
          qparams.add(new BasicNameValuePair(param[0], param[1]));
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
    }
    return httpClient.execute(request);
  }

}