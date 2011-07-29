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

package ch.entwine.weblounge.contentrepository.impl.endpoint;

import static ch.entwine.weblounge.common.impl.util.doc.Status.badRequest;
import static ch.entwine.weblounge.common.impl.util.doc.Status.conflict;
import static ch.entwine.weblounge.common.impl.util.doc.Status.methodNotAllowed;
import static ch.entwine.weblounge.common.impl.util.doc.Status.notFound;
import static ch.entwine.weblounge.common.impl.util.doc.Status.ok;
import static ch.entwine.weblounge.common.impl.util.doc.Status.preconditionFailed;
import static ch.entwine.weblounge.common.impl.util.doc.Status.serviceUnavailable;

import ch.entwine.weblounge.common.impl.util.doc.Endpoint;
import ch.entwine.weblounge.common.impl.util.doc.Endpoint.Method;
import ch.entwine.weblounge.common.impl.util.doc.EndpointDocumentation;
import ch.entwine.weblounge.common.impl.util.doc.EndpointDocumentationGenerator;
import ch.entwine.weblounge.common.impl.util.doc.Format;
import ch.entwine.weblounge.common.impl.util.doc.Parameter;
import ch.entwine.weblounge.common.impl.util.doc.TestForm;

/**
 * File endpoint documentation generator.
 */
public final class FilesEndpointDocs {

  /**
   * No need to instantiate this utility class.
   */
  private FilesEndpointDocs() {
  }
  
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

    String[] sortParams = {
        "published-asc",
        "published-desc",
        "created-asc",
        "created-desc",
        "modified-asc",
        "modified-desc" };

    // GET /?path=x&...
    Endpoint getFileByPathEndpoint = new Endpoint("/", Method.GET, "getfile");
    getFileByPathEndpoint.setDescription("Returns a set of files or a single file if the path parameter is specified");
    getFileByPathEndpoint.addFormat(Format.xml());
    getFileByPathEndpoint.addStatus(ok("A resultset was compiled and returned as part of the response"));
    getFileByPathEndpoint.addStatus(notFound("When a path parameter was specified, and no resource was found"));
    getFileByPathEndpoint.addStatus(badRequest("An invalid path was received"));
    getFileByPathEndpoint.addStatus(serviceUnavailable("The site or its content repository is temporarily offline"));
    getFileByPathEndpoint.addOptionalParameter(new Parameter("path", Parameter.Type.String, "The resource path"));
    getFileByPathEndpoint.addOptionalParameter(new Parameter("subjects", Parameter.Type.String, "The page subjects, separated by a comma"));
    getFileByPathEndpoint.addOptionalParameter(new Parameter("searchterms", Parameter.Type.String, "search terms to search the pages content"));
    getFileByPathEndpoint.addOptionalParameter(new Parameter("type", Parameter.Type.String, "The file type, e. g. 'image'"));
    getFileByPathEndpoint.addOptionalParameter(new Parameter("sort", Parameter.Type.Enum, "The sort parameter", "modified-desc", sortParams));
    getFileByPathEndpoint.addOptionalParameter(new Parameter("limit", Parameter.Type.String, "Offset within the result set", "10"));
    getFileByPathEndpoint.addOptionalParameter(new Parameter("offset", Parameter.Type.String, "Number of result items to include", "0"));
    getFileByPathEndpoint.setTestForm(new TestForm());
    docs.addEndpoint(Endpoint.Type.READ, getFileByPathEndpoint);

    // GET /{resource}
    Endpoint getFileByURIEndpoint = new Endpoint("/{resource}", Method.GET, "getfile");
    getFileByURIEndpoint.setDescription("Returns the file with the given id");
    getFileByURIEndpoint.addFormat(Format.xml());
    getFileByURIEndpoint.addStatus(ok("the file was found and is returned as part of the response"));
    getFileByURIEndpoint.addStatus(notFound("the file was not found or could not be loaded"));
    getFileByURIEndpoint.addStatus(badRequest("an invalid file identifier was received"));
    getFileByURIEndpoint.addStatus(serviceUnavailable("the site or its content repository is temporarily offline"));
    getFileByURIEndpoint.addPathParameter(new Parameter("resource", Parameter.Type.String, "The resource identifier"));
    getFileByURIEndpoint.setTestForm(new TestForm());
    docs.addEndpoint(Endpoint.Type.READ, getFileByURIEndpoint);

    // GET /{resource}/content/{language}
    Endpoint getFileContentEndpoint = new Endpoint("/{resource}/content/{language}", Method.GET, "getfilecontent");
    getFileContentEndpoint.setDescription("Returns the localized file contents with the given id");
    getFileContentEndpoint.addFormat(Format.xml());
    getFileContentEndpoint.addStatus(ok("the file content was found and is returned as part of the response"));
    getFileContentEndpoint.addStatus(notFound("the file was not found or could not be loaded"));
    getFileContentEndpoint.addStatus(notFound("the file content don't exist in the specified language"));
    getFileContentEndpoint.addStatus(badRequest("an invalid file identifier was received"));
    getFileContentEndpoint.addStatus(serviceUnavailable("the site or its content repository is temporarily offline"));
    getFileContentEndpoint.addPathParameter(new Parameter("resource", Parameter.Type.String, "The file identifier"));
    getFileContentEndpoint.addPathParameter(new Parameter("language", Parameter.Type.String, "The language identifier"));
    getFileContentEndpoint.setTestForm(new TestForm());
    docs.addEndpoint(Endpoint.Type.READ, getFileContentEndpoint);

    // GET /pending
    Endpoint getPending = new Endpoint("/pending", Method.GET, "getpending");
    getPending.setDescription("Returns all unmodified resources");
    getPending.addFormat(Format.xml());
    getPending.addStatus(ok("A resultset was compiled and returned as part of the response"));
    getPending.addStatus(serviceUnavailable("The site or its content repository is temporarily offline"));
    getPending.addOptionalParameter(new Parameter("filter", Parameter.Type.String, "Filter for the current result set"));
    getPending.addOptionalParameter(new Parameter("type", Parameter.Type.String, "The file type, e. g. 'image'"));
    getPending.addOptionalParameter(new Parameter("sort", Parameter.Type.Enum, "The sort parameter", "modified-desc", sortParams));
    getPending.addOptionalParameter(new Parameter("limit", Parameter.Type.String, "Offset within the result set", "10"));
    getPending.addOptionalParameter(new Parameter("offset", Parameter.Type.String, "Number of result items to include", "0"));
    getPending.setTestForm(new TestForm());
    docs.addEndpoint(Endpoint.Type.READ, getPending);

    // POST /
    Endpoint createFileEndpoint = new Endpoint("/", Method.POST, "createfile");
    createFileEndpoint.setDescription("Creates a new file, either at the given path or at a random location and returns the REST url of the created resource.");
    createFileEndpoint.addFormat(Format.xml());
    createFileEndpoint.addStatus(ok("the file was created and the response body contains it's resource url"));
    createFileEndpoint.addStatus(badRequest("the path was not specified"));
    createFileEndpoint.addStatus(badRequest("the file content is malformed"));
    createFileEndpoint.addStatus(conflict("a file already exists at the specified path"));
    createFileEndpoint.addStatus(methodNotAllowed("the site or its content repository is read-only"));
    createFileEndpoint.addStatus(serviceUnavailable("the site or its content repository is temporarily offline"));
    createFileEndpoint.addOptionalParameter(new Parameter("path", Parameter.Type.String, "The target path"));
    createFileEndpoint.addOptionalParameter(new Parameter("content", Parameter.Type.Text, "The resource data"));
    createFileEndpoint.setTestForm(new TestForm());
    docs.addEndpoint(Endpoint.Type.WRITE, createFileEndpoint);

    // PUT /{resource}
    Endpoint updateFileEndpoint = new Endpoint("/{resource}", Method.PUT, "updatefile");
    updateFileEndpoint.setDescription("Updates the specified file. If the client supplies an If-Match header, the update is processed only if the header value matches the file's ETag");
    updateFileEndpoint.addFormat(Format.xml());
    updateFileEndpoint.addStatus(ok("the file was updated"));
    updateFileEndpoint.addStatus(badRequest("the file content was not specified"));
    updateFileEndpoint.addStatus(badRequest("the file content is malformed"));
    updateFileEndpoint.addStatus(preconditionFailed("the file's etag does not match the value specified in the If-Match header"));
    updateFileEndpoint.addStatus(methodNotAllowed("the site or its content repository is read-only"));
    updateFileEndpoint.addStatus(serviceUnavailable("the site or its content repository is temporarily offline"));
    updateFileEndpoint.addPathParameter(new Parameter("resource", Parameter.Type.String, "The file identifier"));
    updateFileEndpoint.addOptionalParameter(new Parameter("content", Parameter.Type.Text, "The resource data"));
    updateFileEndpoint.setTestForm(new TestForm());
    docs.addEndpoint(Endpoint.Type.WRITE, updateFileEndpoint);

    // DELETE /{resource}
    Endpoint deleteFileEndpoint = new Endpoint("/{resource}", Method.DELETE, "deletefile");
    deleteFileEndpoint.setDescription("Deletes the specified file.");
    deleteFileEndpoint.addFormat(Format.xml());
    deleteFileEndpoint.addStatus(ok("the file was deleted"));
    deleteFileEndpoint.addStatus(badRequest("the file was not specified"));
    deleteFileEndpoint.addStatus(notFound("the file was not found"));
    deleteFileEndpoint.addStatus(methodNotAllowed("the site or its content repository is read-only"));
    deleteFileEndpoint.addStatus(serviceUnavailable("the site or its content repository is temporarily offline"));
    deleteFileEndpoint.addPathParameter(new Parameter("resource", Parameter.Type.String, "The file identifier"));
    deleteFileEndpoint.setTestForm(new TestForm());
    docs.addEndpoint(Endpoint.Type.WRITE, deleteFileEndpoint);

    // PUT /{resource}/content/{language}
    Endpoint updateFileContentEndpoint = new Endpoint("/{resource}/content/{language}", Method.PUT, "updatefilecontent");
    updateFileContentEndpoint.setDescription("Updates the specified file contents. If the client supplies an If-Match header, the update is processed only if the header value matches the file's ETag");
    updateFileContentEndpoint.addFormat(Format.xml());
    updateFileContentEndpoint.addStatus(ok("the file content was updated"));
    updateFileContentEndpoint.addStatus(badRequest("the file content was not specified"));
    updateFileContentEndpoint.addStatus(badRequest("the language does not exist"));
    updateFileContentEndpoint.addStatus(badRequest("the file content is malformed"));
    updateFileContentEndpoint.addStatus(preconditionFailed("the file's etag does not match the value specified in the If-Match header"));
    updateFileContentEndpoint.addStatus(methodNotAllowed("the site or it's content repository is read-only"));
    updateFileContentEndpoint.addStatus(serviceUnavailable("the site or it's content repository is temporarily offline"));
    updateFileContentEndpoint.addPathParameter(new Parameter("resource", Parameter.Type.String, "The file identifier"));
    updateFileContentEndpoint.addPathParameter(new Parameter("language", Parameter.Type.String, "The language"));
    updateFileContentEndpoint.addOptionalParameter(new Parameter("content", Parameter.Type.File, "The resource content"));
    updateFileContentEndpoint.setTestForm(new TestForm());
    docs.addEndpoint(Endpoint.Type.WRITE, updateFileContentEndpoint);

    // DELETE /{resource}/content/{language}
    Endpoint deleteFileContentEndpoint = new Endpoint("/{resource}/content/{language}", Method.DELETE, "deletefilecontent");
    deleteFileContentEndpoint.setDescription("Deletes the specified file content.");
    deleteFileContentEndpoint.addFormat(Format.xml());
    deleteFileContentEndpoint.addStatus(ok("the file content was deleted"));
    deleteFileContentEndpoint.addStatus(badRequest("the file content was not specified"));
    deleteFileContentEndpoint.addStatus(notFound("the file was not found"));
    deleteFileContentEndpoint.addStatus(notFound("the file content was not found"));
    deleteFileContentEndpoint.addStatus(methodNotAllowed("the site or its content repository is read-only"));
    deleteFileContentEndpoint.addStatus(serviceUnavailable("the site or its content repository is temporarily offline"));
    deleteFileContentEndpoint.addPathParameter(new Parameter("resource", Parameter.Type.String, "The file identifier"));
    deleteFileContentEndpoint.addPathParameter(new Parameter("language", Parameter.Type.String, "The language identifier"));
    deleteFileContentEndpoint.setTestForm(new TestForm());
    docs.addEndpoint(Endpoint.Type.WRITE, deleteFileContentEndpoint);

    // POST /uploads
    Endpoint uploadFileEndpoint = new Endpoint("/uploads", Method.POST, "uploadfile");
    uploadFileEndpoint.setDescription("Creates a new file as well as content, either at the given path or at a random location and returns the REST url of the created resource.");
    uploadFileEndpoint.addFormat(Format.xml());
    uploadFileEndpoint.addStatus(ok("the file was created and the response body contains it's resource url"));
    uploadFileEndpoint.addStatus(badRequest("the path was not specified"));
    uploadFileEndpoint.addStatus(badRequest("the file content is malformed"));
    uploadFileEndpoint.addStatus(badRequest("the language does not exist"));
    uploadFileEndpoint.addStatus(conflict("a file already exists at the specified path"));
    uploadFileEndpoint.addStatus(methodNotAllowed("the site or its content repository is read-only"));
    uploadFileEndpoint.addStatus(serviceUnavailable("the site or its content repository is temporarily offline"));
    uploadFileEndpoint.addOptionalParameter(new Parameter("path", Parameter.Type.String, "The target path"));
    uploadFileEndpoint.addOptionalParameter(new Parameter("mimeType", Parameter.Type.String, "The mime type"));
    uploadFileEndpoint.addOptionalParameter(new Parameter("language", Parameter.Type.String, "The language"));
    uploadFileEndpoint.addBodyParameter(true, null, "the file");
    uploadFileEndpoint.setTestForm(new TestForm());
    docs.addEndpoint(Endpoint.Type.WRITE, uploadFileEndpoint);

    return EndpointDocumentationGenerator.generate(docs);
  }
}
