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

package ch.o2it.weblounge.contentrepository.impl.endpoint;

import static ch.o2it.weblounge.common.impl.util.doc.Status.CONFLICT;
import static ch.o2it.weblounge.common.impl.util.doc.Status.METHOD_NOT_ALLOWED;
import static ch.o2it.weblounge.common.impl.util.doc.Status.PRECONDITION_FAILED;

import static ch.o2it.weblounge.common.impl.util.doc.Status.BAD_REQUEST;
import static ch.o2it.weblounge.common.impl.util.doc.Status.NOT_FOUND;
import static ch.o2it.weblounge.common.impl.util.doc.Status.OK;
import static ch.o2it.weblounge.common.impl.util.doc.Status.SERVICE_UNAVAILABLE;

import ch.o2it.weblounge.common.impl.util.doc.Endpoint;
import ch.o2it.weblounge.common.impl.util.doc.EndpointDocumentation;
import ch.o2it.weblounge.common.impl.util.doc.EndpointDocumentationGenerator;
import ch.o2it.weblounge.common.impl.util.doc.Format;
import ch.o2it.weblounge.common.impl.util.doc.Parameter;
import ch.o2it.weblounge.common.impl.util.doc.TestForm;
import ch.o2it.weblounge.common.impl.util.doc.Endpoint.Method;

/**
 * File endpoint documentation generator.
 */
public final class FilesEndpointDocs {

  /**
   * Creates the documentation.
   * 
   * @param endpointUrl
   *          the endpoint address
   * @return the endpoint documentation
   */
  public static String createDocumentation(String endpointUrl) {
    EndpointDocumentation docs = new EndpointDocumentation(endpointUrl, "files");
    docs.setTitle("Weblounge Files");

    // GET /{resource}
    Endpoint fileEndpoint = new Endpoint("/{resource}", Method.GET, "getfile");
    fileEndpoint.setDescription("Returns the file with the given id");
    fileEndpoint.addFormat(Format.xml());
    fileEndpoint.addStatus(OK("the file was found and is returned as part of the response"));
    fileEndpoint.addStatus(NOT_FOUND("the file was not found or could not be loaded"));
    fileEndpoint.addStatus(BAD_REQUEST("an invalid file identifier was received"));
    fileEndpoint.addStatus(SERVICE_UNAVAILABLE("the site or its content repository is temporarily offline"));
    fileEndpoint.addPathParameter(new Parameter("resource", Parameter.Type.STRING, "The file identifier"));
    fileEndpoint.setTestForm(new TestForm());
    docs.addEndpoint(Endpoint.Type.READ, fileEndpoint);

    // GET /{resource}/content/{language}
    Endpoint fileContentEndpoint = new Endpoint("/{resource}/content/{language}", Method.GET, "getfilecontent");
    fileContentEndpoint.setDescription("Returns the localized file contents with the given id");
    fileContentEndpoint.addFormat(Format.xml());
    fileContentEndpoint.addStatus(OK("the file content was found and is returned as part of the response"));
    fileContentEndpoint.addStatus(NOT_FOUND("the file was not found or could not be loaded"));
    fileContentEndpoint.addStatus(NOT_FOUND("the file content don't exist in the specified language"));
    fileContentEndpoint.addStatus(BAD_REQUEST("an invalid file identifier was received"));
    fileContentEndpoint.addStatus(SERVICE_UNAVAILABLE("the site or its content repository is temporarily offline"));
    fileContentEndpoint.addPathParameter(new Parameter("resource", Parameter.Type.STRING, "The file identifier"));
    fileContentEndpoint.addPathParameter(new Parameter("language", Parameter.Type.STRING, "The language identifier"));
    fileContentEndpoint.setTestForm(new TestForm());
    docs.addEndpoint(Endpoint.Type.READ, fileContentEndpoint);

    // POST /{resource}
    Endpoint createPageEndpoint = new Endpoint("/", Method.POST, "createfile");
    createPageEndpoint.setDescription("Creates a new file, either at the given path or at a random location and returns the REST url of the created resource.");
    createPageEndpoint.addFormat(Format.xml());
    createPageEndpoint.addStatus(OK("the file was created and the response body contains it's resource url"));
    createPageEndpoint.addStatus(BAD_REQUEST("the path was not specified"));
    createPageEndpoint.addStatus(BAD_REQUEST("the file content is malformed"));
    createPageEndpoint.addStatus(CONFLICT("a file already exists at the specified path"));
    createPageEndpoint.addStatus(METHOD_NOT_ALLOWED("the site or its content repository is read-only"));
    createPageEndpoint.addStatus(SERVICE_UNAVAILABLE("the site or its content repository is temporarily offline"));
    createPageEndpoint.addOptionalParameter(new Parameter("path", Parameter.Type.STRING, "The target path"));
    createPageEndpoint.addOptionalParameter(new Parameter("file", Parameter.Type.STRING, "The file data"));
    createPageEndpoint.setTestForm(new TestForm());
    //docs.addEndpoint(Endpoint.Type.WRITE, createPageEndpoint);

    // PUT /{resource}
    Endpoint updatePageEndpoint = new Endpoint("/{resource}", Method.PUT, "updatefile");
    updatePageEndpoint.setDescription("Updates the specified file. If the client supplies an If-Match header, the update is processed only if the header value matches the file's ETag");
    updatePageEndpoint.addFormat(Format.xml());
    updatePageEndpoint.addStatus(OK("the file was updated"));
    updatePageEndpoint.addStatus(BAD_REQUEST("the file content was not specified"));
    createPageEndpoint.addStatus(BAD_REQUEST("the file content is malformed"));
    updatePageEndpoint.addStatus(PRECONDITION_FAILED("the file's etag does not match the value specified in the If-Match header"));
    updatePageEndpoint.addStatus(METHOD_NOT_ALLOWED("the site or its content repository is read-only"));
    updatePageEndpoint.addStatus(SERVICE_UNAVAILABLE("the site or its content repository is temporarily offline"));
    updatePageEndpoint.addPathParameter(new Parameter("resource", Parameter.Type.STRING, "The file identifier"));
    updatePageEndpoint.addRequiredParameter(new Parameter("file", Parameter.Type.STRING, "The file content"));
    updatePageEndpoint.setTestForm(new TestForm());
    //docs.addEndpoint(Endpoint.Type.WRITE, updatePageEndpoint);

    // DELETE /{resource}
    Endpoint deletePageEndpoint = new Endpoint("/{resource}", Method.DELETE, "deletefile");
    deletePageEndpoint.setDescription("Deletes the specified file.");
    deletePageEndpoint.addFormat(Format.xml());
    deletePageEndpoint.addStatus(OK("the file was deleted"));
    deletePageEndpoint.addStatus(BAD_REQUEST("the file was not specified"));
    deletePageEndpoint.addStatus(NOT_FOUND("the file was not found"));
    deletePageEndpoint.addStatus(METHOD_NOT_ALLOWED("the site or its content repository is read-only"));
    deletePageEndpoint.addStatus(SERVICE_UNAVAILABLE("the site or its content repository is temporarily offline"));
    deletePageEndpoint.addPathParameter(new Parameter("resource", Parameter.Type.STRING, "The file identifier"));
    deletePageEndpoint.setTestForm(new TestForm());
    //docs.addEndpoint(Endpoint.Type.WRITE, deletePageEndpoint);

    return EndpointDocumentationGenerator.generate(docs);
  }
}
