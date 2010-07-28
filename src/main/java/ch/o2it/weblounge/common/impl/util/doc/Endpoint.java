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

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Models an endpoint identified by some url. The endpoint documentation
 * contains the semantics as well as the technical specifications.
 */
public class Endpoint {

  public static enum Type {
    WRITE, READ
  };

  public static enum Method {
    GET, POST, PUT, DELETE, ANY
  };

  /** The endpoint's name */
  private String name = null;

  /** Http method */
  private String method = null;

  /** Path to the endpoint */
  private String path = null;

  /** Textual description */
  private String description = null;

  /** The body parameter */
  private Parameter bodyParameter = null;

  /** List of path parameters */
  private List<Parameter> pathParameters = null;

  /** List of required parameters */
  private List<Parameter> requiredParameters = null;

  /** List of optional parameters */
  private List<Parameter> optionalParameters = null;

  /** List of possible output formats */
  private List<Format> outputFormats = null;

  /** List of possible response statuses */
  private List<Status> responseStatuses = null;

  /** Text notes */
  private List<String> notes = null;

  /** A test form */
  private TestForm form = null;

  /** Should paths automatically be generated */
  private boolean autoPathFormat = false;
  String defaultOutputFormat = "";
  String pathFormatHtml = "";

  /**
   * Creates a new endpoint.
   * 
   * @param path
   *          the path for this endpoint (e.g. /search OR /add/{id})
   * @param method
   *          the HTTP method used for this endpoint
   * @param name
   *          the endpoint name
   */
  public Endpoint(String path, Method method, String name) {
    this(path, method, name, null);
  }

  /**
   * Creates a new endpoint.
   * 
   * @param path
   *          the path for this endpoint (e.g. /search OR /add/{id})
   * @param method
   *          the HTTP method used for this endpoint
   * @param name
   *          the endpoint name
   * @param description
   *          the optional endpoint description
   */
  public Endpoint(String path, Method method, String name, String description) {
    if (!EndpointDocumentation.isValid(name))
      throw new IllegalArgumentException("Name must not be null and must be alphanumeric");
    if (method == null)
      throw new IllegalArgumentException("Method must not be null");
    if (!EndpointDocumentation.isValidPath(path))
      throw new IllegalArgumentException("Path must not be null and must look like /a/b/{c}");

    this.name = name;
    this.method = method.name().toUpperCase();
    this.path = path;
    this.description = description;
  }

  /**
   * Creates and adds a special parameter which is to be sent as the body of the
   * request. Generally, the type of this parameter should be <code>FILE</code>
   * or <code>TEXT</code> but nothing stops you from using the other types.
   * <p>
   * This is always a required parameter as you should never design an endpoint
   * that takes a file sometimes but not always.
   * 
   * @param isBinary
   *          if <code>true</code> then this should use an uploader to send,
   *          otherwise the data can be placed in a text area
   * @param defaultValue
   *          the default value (only viable for text)
   * @param description
   *          the optional description
   * @return the new parameter object in case you want to set attributes
   */
  public Parameter addBodyParameter(boolean isBinary, String defaultValue,
      String description) {
    Parameter.Type type = isBinary ? Parameter.Type.FILE : Parameter.Type.TEXT;
    Parameter parameter = new Parameter("BODY", type, description, defaultValue);
    parameter.setRequired(true);
    parameter.setAttribute("rows", "8");
    this.bodyParameter = parameter;
    return parameter;
  }

  /**
   * Creates and adds a path parameter for this endpoint. This would be a
   * parameter which is passed as part of the path (e.g.
   * <code>/my/path/{param}</code>) and thus must use a name which is safe to
   * place in a URL and does not contain a slash (/).
   * 
   * @param parameter
   *          the path parameter to add
   * @throws IllegalArgumentException
   *           if the parameter is null
   */
  public void addPathParameter(Parameter parameter) {
    if (parameter == null)
      throw new IllegalArgumentException("Parameter must not be null");

    if (Parameter.Type.FILE.name().equals(parameter.getType()) || Parameter.Type.TEXT.name().equals(parameter.getType()))
      throw new IllegalStateException("Cannot add path parameter of type FILE or TEXT");

    parameter.setRequired(true);
    parameter.setIsPathParameter(true);
    if (this.pathParameters == null) {
      this.pathParameters = new ArrayList<Parameter>(3);
    }
    this.pathParameters.add(parameter);
  }

  /**
   * Creates and adds a required form parameter for this endpoint. This would be
   * a parameter which is encoded as part of the request body (commonly referred
   * to as a post or form parameter).
   * <p>
   * Note: It is advised to use path parameters unless the required parameter is
   * not part of an identifier for the resource
   * 
   * @param parameter
   *          the required parameter to add
   * @throws IllegalArgumentException
   *           if the parameter is null
   * @throws IllegalStateException
   *           if a required parameter is added to a <code>GET</code> endpoint
   */
  public void addRequiredParameter(Parameter parameter) {
    if (parameter == null)
      throw new IllegalArgumentException("Parameter must not be null");
    if (isGetMethod())
      throw new IllegalStateException("Cannot add required parameter to GET endpoints");

    parameter.setRequired(true);
    parameter.setIsPathParameter(false);
    if (this.requiredParameters == null) {
      this.requiredParameters = new ArrayList<Parameter>(3);
    }
    this.requiredParameters.add(parameter);
  }

  /**
   * Adds an optional parameter for this endpoint, this would be a parameter
   * which is passed in the query string (for GET) or encoded as part of the
   * body otherwise (often referred to as a post or form parameter)
   * 
   * @param parameter
   *          the optional parameter to add
   * @throws IllegalArgumentException
   *           if the parameter is null
   */
  public void addOptionalParameter(Parameter param) {
    if (param == null)
      throw new IllegalArgumentException("Parameter must not be null");

    param.setRequired(false);
    param.setIsPathParameter(false);
    if (this.optionalParameters == null) {
      this.optionalParameters = new ArrayList<Parameter>(3);
    }
    this.optionalParameters.add(param);
  }

  /**
   * Adds a format for the return data for this endpoint
   * 
   * @param format
   *          a format object
   * @throws IllegalArgumentException
   *           if the parameters are null
   */
  public void addFormat(Format format) {
    if (format == null) {
      throw new IllegalArgumentException("Format must not be null");
    }
    if (this.outputFormats == null) {
      this.outputFormats = new ArrayList<Format>(2);
    }
    this.outputFormats.add(format);
  }

  /**
   * Adds a response status for this endpoint
   * 
   * @param status
   *          a response status object
   * @throws IllegalArgumentException
   *           if the parameters are null
   */
  public void addStatus(Status status) {
    if (status == null) {
      throw new IllegalArgumentException("Status must not be null");
    }
    if (this.responseStatuses == null) {
      this.responseStatuses = new ArrayList<Status>(3);
    }
    this.responseStatuses.add(status);
  }

  /**
   * Adds a note for this endpoint
   * 
   * @param note
   *          a note object
   * @throws IllegalArgumentException
   *           if the parameters are null
   */
  public void addNote(String note) {
    if (StringUtils.isBlank(note)) {
      throw new IllegalArgumentException("Note must not be null");
    }
    if (this.notes == null) {
      this.notes = new ArrayList<String>(3);
    }
    this.notes.add(note);
  }

  /**
   * Sets the test form for this endpoint, if this is null then no test form is
   * rendered for this endpoint
   * 
   * @param form
   *          the test form object (null to clear the form)
   * @throws IllegalArgumentException
   *           if the parameters are null
   */
  public void setTestForm(TestForm form) {
    this.form = form;
  }

  /**
   * Setting this to true will cause the path to be filled in with format
   * extensions which will work with the {FORMAT} convention (which is
   * automatically filled in with the selected or default format key - e.g.
   * json) <br/>
   * This will generate a path like /your/path.{FORMAT} and will show the
   * following on screen GET /your/path.{xml|json} if you have 2 formats in this
   * endpoint
   * 
   * @param autoPathFormat
   *          true to enable, false to disable
   */
  public void setAutoPathFormat(boolean autoPathFormat) {
    this.autoPathFormat = autoPathFormat;
  }

  /**
   * Returns <code>true</code> if this endpoint's method is <code>GET</code>,
   * <code>false</code> otherwise.
   * 
   * @return <code>true</code> if this endpoint method is <code>GET</code>
   */
  public boolean isGetMethod() {
    boolean match = false;
    if (Method.GET.name().equals(this.method)) {
      match = true;
    }
    return match;
  }

  /**
   * Returns the url encoded query string.
   * 
   * @return the calculated query string for a GET endpoint (e.g. ?blah=1), will
   *         be url encoded for html display
   */
  public String getQueryString() {
    String qs = "";
    if (isGetMethod()) {
      if (this.optionalParameters != null && !this.optionalParameters.isEmpty()) {
        StringBuilder sb = new StringBuilder();
        sb.append("?");
        for (Parameter p : this.optionalParameters) {
          if (sb.length() > 2) {
            sb.append("&");
          }
          sb.append(p.getName());
          sb.append("=");
          if (p.getDefaultValue() != null) {
            sb.append(p.getDefaultValue());
          } else {
            sb.append("{");
            sb.append(p.getName());
            sb.append("}");
          }
        }
        qs = StringEscapeUtils.escapeHtml(sb.toString());
      }
    }
    return qs;
  }

  /**
   * Returns the endpoint's name.
   * 
   * @return the name
   */
  public String getName() {
    return name;
  }

  /**
   * Returns the endpoint's <code>HTTP</code> method.
   * 
   * @return the method
   */
  public String getMethod() {
    return method;
  }

  /**
   * Returns the url path to the endpoint.
   * 
   * @return the path
   */
  public String getPath() {
    return path;
  }

  /**
   * Sets the endpoint description.
   * 
   * @param description
   *          the endpoint description
   */
  public void setDescription(String description) {
    this.description = description;
  }

  /**
   * Returns the textual description.
   * 
   * @return the description
   */
  public String getDescription() {
    return description;
  }

  /**
   * Returns the body parameter or <code>null</code> if no body parameter is
   * set, which is the case in all requests other than <code>POST</code>.
   * 
   * @return the body parameter
   */
  public Parameter getBodyParameter() {
    return bodyParameter;
  }

  /**
   * Returns the path parameters.
   * 
   * @return the path parameters
   */
  public List<Parameter> getPathParameters() {
    if (this.pathParameters == null) {
      this.pathParameters = new ArrayList<Parameter>(0);
    }
    return this.pathParameters;
  }

  /**
   * Returns the list of required parameters.
   * 
   * @return the required parameters
   */
  public List<Parameter> getRequiredParameters() {
    if (this.requiredParameters == null) {
      this.requiredParameters = new ArrayList<Parameter>(0);
    }
    return this.requiredParameters;
  }

  /**
   * Returns the list of optional parameters.
   * 
   * @return the optional parameters
   */
  public List<Parameter> getOptionalParameters() {
    if (this.optionalParameters == null) {
      this.optionalParameters = new ArrayList<Parameter>(0);
    }
    return this.optionalParameters;
  }

  /**
   * Returns the list of output formats.
   * 
   * @return the output formats
   */
  public List<Format> getOutputFormats() {
    if (this.outputFormats == null) {
      this.outputFormats = new ArrayList<Format>(0);
    }
    return this.outputFormats;
  }

  /**
   * Returns the list of possible response statuses.
   * 
   * @return the response statuses
   */
  public List<Status> getResponseStatuses() {
    if (this.responseStatuses == null) {
      this.responseStatuses = new ArrayList<Status>(0);
    }
    return this.responseStatuses;
  }

  /**
   * Returns the notes.
   * 
   * @return the notes
   */
  public List<String> getNotes() {
    if (this.notes == null) {
      this.notes = new ArrayList<String>(0);
    }
    return this.notes;
  }

  /**
   * Returns the test form or <code>null</code> if no test form has been
   * specified.
   * 
   * @return the test form
   */
  public TestForm getForm() {
    return form;
  }

  /**
   * Returns the default output format used to create urls.
   * 
   * @return the default format
   */
  public String getDefaultOutputFormat() {
    return defaultOutputFormat;
  }

  public String getPathFormatHtml() {
    return pathFormatHtml;
  }

  public boolean isAutoPathFormat() {
    return autoPathFormat;
  }

  /**
   * {@inheritDoc}
   *
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "name";
  }

}
