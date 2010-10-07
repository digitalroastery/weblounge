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

package ch.o2it.weblounge.common.impl.util.doc;

import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class models the documentation for an endpoint.
 */
public class EndpointDocumentation {

  /** Default template */
  public static final String TEMPLATE_DEFAULT = "/doc/endpoint.xhtml";

  /** Key used to indicate the formats */
  public static final String FORMAT_KEY = "{FORMAT}";

  /** List of endpoints, grouped inside the collections */
  protected List<EndpointCollection> endpointCollections = null;

  /** This is the document meta data */
  protected Map<String, String> metadata = null;

  /** Documentation notes */
  protected List<String> notes = null;

  /**
   * Creates a new endpoint documentation with the given name and url.
   * 
   * @param url
   *          the base url
   * @param name
   *          the documentation name
   */
  public EndpointDocumentation(String url, String name) {
    this(url, name, null);
  }

  /**
   * Create the endpoint documentation for the endpoint located at
   * <code>url</code>.
   * 
   * @param url
   *          this is the absolute base URL for this endpoint (e.g.
   *          /workflow/rest)
   * @param name
   *          the name of the set of endpoints (must be alphanumeric (includes
   *          _) and no spaces or special chars)
   * @param title
   *          the optional title of the document
   */
  public EndpointDocumentation(String url, String name, String title) {

    if (!EndpointDocumentation.isValid(name))
      throw new IllegalArgumentException("Name must be set and only alphanumeric");
    if (url == null || "".equals(url))
      throw new IllegalArgumentException("Url cannot be blank");

    // Strip off trailing slash
    if (url.endsWith("/"))
      url.substring(0, url.length() - 2);

    // General data
    this.metadata = new LinkedHashMap<String, String>();
    this.metadata.put("name", name);
    this.metadata.put("title", StringUtils.isBlank(title) ? name : title);
    this.metadata.put("url", url);

    this.notes = new ArrayList<String>();
    this.endpointCollections = new ArrayList<EndpointCollection>(2);
    this.endpointCollections.add(new EndpointCollection(Endpoint.Type.READ.name(), "Read"));
    this.endpointCollections.add(new EndpointCollection(Endpoint.Type.WRITE.name(), "Write"));
  }

  /**
   * Sets the document title.
   * 
   * @param title
   *          the document title
   */
  public void setTitle(String title) {
    metadata.put("title", title);
  }

  /**
   * Returns the map containing all of the elements that are referenced in the
   * underlying template used to render the documentation.
   * 
   * @return the properties map
   */
  public Map<String, Object> toMap() {
    LinkedHashMap<String, Object> m = new LinkedHashMap<String, Object>();
    m.put("metadata", this.metadata);
    m.put("notes", this.notes);
    
    ArrayList<EndpointCollection> collections = new ArrayList<EndpointCollection>();
    for (EndpointCollection collection : endpointCollections) {
      
      // Only pass through the collections with things in them
      if (collection.getEndpoints().isEmpty())
        continue;

      // Iterate over the endpoints
      for (Endpoint endpoint : collection.getEndpoints()) {
        
        // Balidate the endpoint
        if (!endpoint.getPathParameters().isEmpty()) {
          for (Parameter param : endpoint.getPathParameters()) {
            if (!endpoint.getPath().contains("{" + param.getName() + "}")) {
              throw new IllegalArgumentException("Path (" + endpoint.getPath() + ") does not match path parameter (" + param.getName() + ") for endpoint (" + endpoint.getName() + "), the path must contain all path param names");
            }
          }
        }
        
        // Validate the endpoint path
        Pattern pattern = Pattern.compile("\\{(.+?)\\}");
        Matcher matcher = pattern.matcher(endpoint.getPath());
        int count = 0;
        while (matcher.find()) {
          if (!FORMAT_KEY.equals(matcher.group())) {
            count++;
          }
        }
        if (count != endpoint.getPathParameters().size()) {
          throw new IllegalArgumentException("Path (" + endpoint.getPath() + ") does not match path parameters (" + endpoint.getPathParameters() + ") for endpoint (" + endpoint.getName() + "), the path must contain the same number of path params (" + count + ") as the pathParams list (" + endpoint.getPathParameters().size() + ")");
        }
        
        // Handle the forms
        if (endpoint.getForm() != null) {
          TestForm form = endpoint.getForm();
          if (form.isAutoGenerated()) {
            form = new TestForm(endpoint);
            endpoint.setTestForm(form); // replace
          }
          if (form.isEmpty()) {
            // clear the form if there is no data to test
            endpoint.setTestForm(null);
          }
        }
        
        // Handle the endpoint auto format paths
        if (endpoint.isAutoPathFormat()) {
          if (!endpoint.getOutputFormats().isEmpty()) {
            endpoint.setDefaultOutputFormat("." + FORMAT_KEY);
            StringBuilder sb = new StringBuilder();
            sb.append(".{");
            for (Format format : endpoint.getOutputFormats()) {
              if (sb.length() > 3) {
                sb.append("|");
              }
              sb.append(format.getName());
            }
            sb.append("}");
            endpoint.setPathFormatHtml(sb.toString());
          }
        } else {
          endpoint.setDefaultOutputFormat("");
          endpoint.setPathFormatHtml("");
        }
      }
      collections.add(collection);
    }

    m.put("endpointCollections", collections);
    return m;
  }

  /**
   * Returns the path to the default template.
   * 
   * @return the default template
   */
  public String getDefaultTemplatePath() {
    return TEMPLATE_DEFAULT;
  }

  /**
   * Adds a new endpoint to the document.
   * 
   * @param type
   *          the endpoint type
   * @param endpoint
   *          the endpoint
   */
  public void addEndpoint(Endpoint.Type type, Endpoint endpoint) {
    if (type == null || endpoint == null)
      throw new IllegalArgumentException("Type and endpoint must not be null");

    EndpointCollection collection = null;
    for (EndpointCollection holder : endpointCollections) {
      if (type.name().equals(holder.getName())) {
        collection = holder;
        break;
      }
    }
    if (collection == null) {
      throw new IllegalStateException("Could not find collection of type: " + type.name());
    }
    collection.addEndPoint(endpoint);
  }

  /**
   * Creates an abstract section which is displayed at the top of the doc
   * 
   * @param abstractText
   *          any text to place at the top of the document, can be html markup
   *          but must be valid
   */
  public void setAbstract(String abstractText) {
    if (StringUtils.isBlank(abstractText)) {
      this.metadata.remove("abstract");
    } else {
      this.metadata.put("abstract", abstractText);
    }
  }

  /**
   * Adds a note to the document.
   * 
   * @param note
   *          the note text
   */
  public void addNote(String note) {
    if (!StringUtils.isBlank(note)) {
      this.notes.add(note);
    }
  }

  /**
   * Returns the metadata item identified by <code>key</code> or
   * <code>null</code> if the item is not available.
   * 
   * @param key
   *          the item key
   * @return the metadata
   */
  public String getMetaData(String key) {
    return metadata.get(key);
  }

  /**
   * Returns the documentation metadata.
   * 
   * @return the metadata
   */
  public Map<String, String> getMeta() {
    return metadata;
  }

  /**
   * Returns the documentation notes.
   * 
   * @return the notes
   */
  public List<String> getNotes() {
    return notes;
  }

  /**
   * {@inheritDoc}
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "DOC:meta=" + metadata + ", notes=" + notes + ", " + endpointCollections;
  }

  /**
   * Returns <code>true</code> if <code>name</code> is not <code>null</code> or
   * the empty string and does not contain invalid characters such as
   * <tt>^, \, w, + $</tt>.
   * 
   * @param name
   *          the text
   * @return <code>true</code> if the text is valid
   */
  static boolean isValid(String name) {
    boolean valid = true;
    if (StringUtils.isBlank(name)) {
      valid = false;
    } else {
      if (!name.matches("^\\w+$")) {
        valid = false;
      }
    }
    return valid;
  }

  /**
   * Validates paths: VALID: /sample , /sample/{thing} , /{my}/{path}.xml ,
   * /my/fancy_path/is/{awesome}.{FORMAT} INVALID: sample, /sample/,
   * /sa#$%mple/path
   * 
   * @param path
   *          the path value to check
   * @return true if this path is valid, false otherwise
   */
  static boolean isValidPath(String path) {
    boolean valid = true;
    if (StringUtils.isBlank(path)) {
      valid = false;
    } else {
      if ("/".equals(path)) {
        valid = true;
      } else if (path.endsWith("/") || !path.startsWith("/")) {
        valid = false;
      } else {
        if (!path.matches("^[\\w\\/{}\\.]+$")) {
          valid = false;
        }
      }
    }
    return valid;
  }

}
