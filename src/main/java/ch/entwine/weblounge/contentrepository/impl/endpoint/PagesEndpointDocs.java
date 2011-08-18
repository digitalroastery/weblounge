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
import static ch.entwine.weblounge.common.impl.util.doc.Status.forbidden;
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
 * Page endpoint documentation generator.
 */
public final class PagesEndpointDocs {

  /**
   * No need to instantiate this utility class.
   */
  private PagesEndpointDocs() {
  }

  /**
   * Creates the documentation.
   * 
   * @param endpointUrl
   *          the endpoint address
   * @return the endpoint documentation
   */
  public static String createDocumentation(String endpointUrl) {
    EndpointDocumentation docs = new EndpointDocumentation(endpointUrl, "pages");
    docs.setTitle("Weblounge Pages");
    
    String[] versions = {"0", "1"};

    // GET /
    Endpoint getAllPagesEndpoint = new Endpoint("/", Method.GET, "getallpages");
    getAllPagesEndpoint.setDescription("Returns a collection of pages matching the given parameters");
    getAllPagesEndpoint.addFormat(Format.xml());
    getAllPagesEndpoint.addStatus(ok("a collection (may be empty) of pages is returned as part of the response"));
    getAllPagesEndpoint.addStatus(badRequest("an invalid request was received"));
    getAllPagesEndpoint.addStatus(serviceUnavailable("the site or its content repository is temporarily offline"));
    getAllPagesEndpoint.addOptionalParameter(new Parameter("path", Parameter.Type.String, "The page path"));
    getAllPagesEndpoint.addOptionalParameter(new Parameter("subjects", Parameter.Type.String, "The page subjects, separated by a comma"));
    getAllPagesEndpoint.addOptionalParameter(new Parameter("searchterms", Parameter.Type.String, "search terms to search the pages content"));
    getAllPagesEndpoint.addOptionalParameter(new Parameter("filter", Parameter.Type.String, "Filter for the current result set"));
    String[] sortParams = {"published-asc", "published-desc", "created-asc", "created-desc", "modified-asc", "modified-desc"};
    getAllPagesEndpoint.addOptionalParameter(new Parameter("sort", Parameter.Type.Enum, "The sort parameter", "modified-desc", sortParams));
    getAllPagesEndpoint.addOptionalParameter(new Parameter("limit", Parameter.Type.String, "Offset within the result set", "10"));
    getAllPagesEndpoint.addOptionalParameter(new Parameter("offset", Parameter.Type.String, "Number of result items to include", "0"));
    getAllPagesEndpoint.addOptionalParameter(new Parameter("details", Parameter.Type.Boolean, "Whether to include the all page data", "false"));
    getAllPagesEndpoint.setTestForm(new TestForm());
    docs.addEndpoint(Endpoint.Type.READ, getAllPagesEndpoint);

    // GET /{page}
    Endpoint getPageByIdEndpoint = new Endpoint("/{page}", Method.GET, "getpagebyid");
    getPageByIdEndpoint.setDescription("Returns the page with the given id");
    getPageByIdEndpoint.addFormat(Format.xml());
    getPageByIdEndpoint.addStatus(ok("the page was found and is returned as part of the response"));
    getPageByIdEndpoint.addStatus(notFound("the page was not found or could not be loaded"));
    getPageByIdEndpoint.addStatus(badRequest("an invalid page identifier was received"));
    getPageByIdEndpoint.addStatus(serviceUnavailable("the site or its content repository is temporarily offline"));
    getPageByIdEndpoint.addPathParameter(new Parameter("page", Parameter.Type.String, "The page identifier"));
    getPageByIdEndpoint.addOptionalParameter(new Parameter("version", Parameter.Type.String, "The version", "0", versions));
    getPageByIdEndpoint.setTestForm(new TestForm());
    docs.addEndpoint(Endpoint.Type.READ, getPageByIdEndpoint);

    // GET /pending
    Endpoint getPending = new Endpoint("/pending", Method.GET, "getpending");
    getPending.setDescription("Returns all unmodified or unpublished pages");
    getPending.addFormat(Format.xml());
    getPending.addStatus(ok("A resultset was compiled and returned as part of the response"));
    getPending.addStatus(serviceUnavailable("The site or its content repository is temporarily offline"));
    getPending.addOptionalParameter(new Parameter("filter", Parameter.Type.String, "Filter for the current result set"));
    getPending.addOptionalParameter(new Parameter("sort", Parameter.Type.Enum, "The sort parameter", "modified-desc", sortParams));
    getPending.addOptionalParameter(new Parameter("limit", Parameter.Type.String, "Offset within the result set", "10"));
    getPending.addOptionalParameter(new Parameter("offset", Parameter.Type.String, "Number of result items to include", "0"));
    getPending.setTestForm(new TestForm());
    docs.addEndpoint(Endpoint.Type.READ, getPending);

    // POST /{page}
    Endpoint createPageEndpoint = new Endpoint("/", Method.POST, "createpage");
    createPageEndpoint.setDescription("Creates a new page, either at the given path or at a random location and returns the REST url of the created resource.");
    createPageEndpoint.addFormat(Format.xml());
    createPageEndpoint.addStatus(ok("the page was created and the response body contains it's resource url"));
    createPageEndpoint.addStatus(badRequest("the path was not specified"));
    createPageEndpoint.addStatus(badRequest("the page content is malformed"));
    createPageEndpoint.addStatus(conflict("a page already exists at the specified path"));
    createPageEndpoint.addStatus(methodNotAllowed("the site or its content repository is read-only"));
    createPageEndpoint.addStatus(serviceUnavailable("the site or its content repository is temporarily offline"));
    createPageEndpoint.addOptionalParameter(new Parameter("path", Parameter.Type.String, "The target path"));
    createPageEndpoint.addOptionalParameter(new Parameter("content", Parameter.Type.Text, "The page data"));
    createPageEndpoint.setTestForm(new TestForm());
    docs.addEndpoint(Endpoint.Type.WRITE, createPageEndpoint);

    // PUT /{page}
    Endpoint updatePageEndpoint = new Endpoint("/{page}", Method.PUT, "updatepage");
    updatePageEndpoint.setDescription("Updates the specified page. If the client supplies an If-Match header, the update is processed only if the header value matches the page's ETag");
    updatePageEndpoint.addFormat(Format.xml());
    updatePageEndpoint.addStatus(ok("the page was updated"));
    updatePageEndpoint.addStatus(badRequest("the page content was not specified"));
    updatePageEndpoint.addStatus(badRequest("the page content is malformed"));
    updatePageEndpoint.addStatus(notFound("the site or the page to update were not found"));
    updatePageEndpoint.addStatus(preconditionFailed("the page's etag does not match the value specified in the If-Match header"));
    updatePageEndpoint.addStatus(methodNotAllowed("the site or its content repository is read-only"));
    updatePageEndpoint.addStatus(serviceUnavailable("the site or its content repository is temporarily offline"));
    updatePageEndpoint.addPathParameter(new Parameter("page", Parameter.Type.String, "The page identifier"));
    updatePageEndpoint.addRequiredParameter(new Parameter("content", Parameter.Type.Text, "The page content"));
    updatePageEndpoint.setTestForm(new TestForm());
    docs.addEndpoint(Endpoint.Type.WRITE, updatePageEndpoint);

    // DELETE /{page}
    Endpoint deletePageEndpoint = new Endpoint("/{page}", Method.DELETE, "deletepage");
    deletePageEndpoint.setDescription("Deletes the specified page.");
    deletePageEndpoint.addFormat(Format.xml());
    deletePageEndpoint.addStatus(ok("the page was deleted"));
    deletePageEndpoint.addStatus(badRequest("the page was not specified"));
    deletePageEndpoint.addStatus(notFound("the page was not found"));
    deletePageEndpoint.addStatus(methodNotAllowed("the site or its content repository is read-only"));
    deletePageEndpoint.addStatus(serviceUnavailable("the site or its content repository is temporarily offline"));
    deletePageEndpoint.addPathParameter(new Parameter("page", Parameter.Type.String, "The page identifier"));
    deletePageEndpoint.setTestForm(new TestForm());
    docs.addEndpoint(Endpoint.Type.WRITE, deletePageEndpoint);

    // GET /{page}/composers/{composerId}
    Endpoint composerEndpoint = new Endpoint("/{page}/composers/{composer}", Method.GET, "getcomposer");
    composerEndpoint.setDescription("Returns the composer with the given id from the indicated page");
    composerEndpoint.addFormat(Format.xml());
    composerEndpoint.addStatus(ok("the composer was found and is returned as part of the response"));
    composerEndpoint.addStatus(notFound("the composer was not found or could not be loaded"));
    composerEndpoint.addStatus(badRequest("an invalid page or composer identifier was received"));
    composerEndpoint.addStatus(serviceUnavailable("the site or its content repository is temporarily offline"));
    composerEndpoint.addPathParameter(new Parameter("page", Parameter.Type.String, "The page identifier"));
    composerEndpoint.addPathParameter(new Parameter("composer", Parameter.Type.String, "The composer identifier"));
    composerEndpoint.addOptionalParameter(new Parameter("version", Parameter.Type.String, "The version", "0", versions));
    composerEndpoint.setTestForm(new TestForm());
    docs.addEndpoint(Endpoint.Type.READ, composerEndpoint);

    // GET /{page}/composers/{composerId}/pagelets/{pageletIndex}
    Endpoint pageletEndpoint = new Endpoint("/{page}/composers/{composer}/pagelets/{pageletindex}", Method.GET, "getpagelet");
    pageletEndpoint.setDescription("Returns the pagelet at the given index from the indicated composer on the page");
    pageletEndpoint.addFormat(Format.xml());
    pageletEndpoint.addStatus(ok("the pagelet was found and is returned as part of the response"));
    pageletEndpoint.addStatus(notFound("the pagelet was not found or could not be loaded"));
    pageletEndpoint.addStatus(badRequest("an invalid page, composer identifier or pagelet index was received"));
    pageletEndpoint.addStatus(serviceUnavailable("the site or its content repository is temporarily offline"));
    pageletEndpoint.addPathParameter(new Parameter("page", Parameter.Type.String, "The page identifier"));
    pageletEndpoint.addPathParameter(new Parameter("composer", Parameter.Type.String, "The composer identifier"));
    pageletEndpoint.addPathParameter(new Parameter("pageletindex", Parameter.Type.String, "The zero-based pagelet index"));
    pageletEndpoint.addOptionalParameter(new Parameter("version", Parameter.Type.String, "The version", "0", versions));
    pageletEndpoint.setTestForm(new TestForm());
    docs.addEndpoint(Endpoint.Type.READ, pageletEndpoint);
    
    // GET /{page}/children
    Endpoint getChildPagesByURIEndpoint = new Endpoint("/{page}/children", Method.GET, "getpagechildren");
    getChildPagesByURIEndpoint.setDescription("Returns children of the page with the given id");
    getChildPagesByURIEndpoint.addFormat(Format.xml());
    getChildPagesByURIEndpoint.addStatus(ok("the page was found and its children are returned as part of the response"));
    getChildPagesByURIEndpoint.addStatus(notFound("the page was not found or could not be loaded"));
    getChildPagesByURIEndpoint.addStatus(badRequest("an invalid page identifier was received"));
    getChildPagesByURIEndpoint.addStatus(serviceUnavailable("the site or its content repository is temporarily offline"));
    getChildPagesByURIEndpoint.addPathParameter(new Parameter("page", Parameter.Type.String, "The page identifier"));
    getChildPagesByURIEndpoint.setTestForm(new TestForm());
    docs.addEndpoint(Endpoint.Type.READ, getChildPagesByURIEndpoint);

    // PUT /{page}/lock
    Endpoint lockPageEndpoint = new Endpoint("/{page}/lock", Method.PUT, "lockpage");
    lockPageEndpoint.setDescription("Locks the specified page. If the client supplies an If-Match header, the lock is processed only if the header value matches the page's ETag");
    lockPageEndpoint.addFormat(Format.xml());
    lockPageEndpoint.addStatus(ok("the page was locked"));
    lockPageEndpoint.addStatus(badRequest("the page was not specified"));
    lockPageEndpoint.addStatus(preconditionFailed("the page's etag does not match the value specified in the If-Match header"));
    lockPageEndpoint.addStatus(notFound("the page was not found"));
    lockPageEndpoint.addStatus(forbidden("the page is already locked by another user"));
    lockPageEndpoint.addStatus(methodNotAllowed("the site or its content repository is read-only"));
    lockPageEndpoint.addStatus(serviceUnavailable("the site or its content repository is temporarily offline"));
    lockPageEndpoint.addOptionalParameter(new Parameter("user", Parameter.Type.String, "The future lock owner"));
    lockPageEndpoint.addPathParameter(new Parameter("page", Parameter.Type.String, "The page identifier"));
    lockPageEndpoint.setTestForm(new TestForm());
    docs.addEndpoint(Endpoint.Type.WRITE, lockPageEndpoint);

    // DELETE /{page}/unlock
    Endpoint unlockPageEndpoint = new Endpoint("/{page}/lock", Method.DELETE, "unlockpage");
    unlockPageEndpoint.setDescription("Unlocks the specified page. If the client supplies an If-Match header, the unlock is processed only if the header value matches the page's ETag");
    unlockPageEndpoint.addFormat(Format.xml());
    unlockPageEndpoint.addStatus(ok("the page was unlocked"));
    unlockPageEndpoint.addStatus(badRequest("the page was not specified"));
    unlockPageEndpoint.addStatus(preconditionFailed("the page's etag does not match the value specified in the If-Match header"));
    unlockPageEndpoint.addStatus(notFound("the page was not found"));
    unlockPageEndpoint.addStatus(forbidden("the current user does not have the rights to unlock the page"));
    unlockPageEndpoint.addStatus(methodNotAllowed("the site or its content repository is read-only"));
    unlockPageEndpoint.addStatus(serviceUnavailable("the site or its content repository is temporarily offline"));
    unlockPageEndpoint.addPathParameter(new Parameter("page", Parameter.Type.String, "The page identifier"));
    unlockPageEndpoint.setTestForm(new TestForm());
    docs.addEndpoint(Endpoint.Type.WRITE, unlockPageEndpoint);

    // PUT /{page}/publish
    Endpoint publishPageEndpoint = new Endpoint("/{page}/publish", Method.PUT, "publishpage");
    publishPageEndpoint.setDescription("Publishes the specified page. If the client supplies an If-Match header, the publish is processed only if the header value matches the page's ETag");
    publishPageEndpoint.addFormat(Format.xml());
    publishPageEndpoint.addStatus(ok("the page was published"));
    publishPageEndpoint.addStatus(badRequest("the page was not specified"));
    publishPageEndpoint.addStatus(preconditionFailed("the page's etag does not match the value specified in the If-Match header"));
    publishPageEndpoint.addStatus(notFound("the page was not found"));
    publishPageEndpoint.addStatus(forbidden("the page is locked by a different user"));
    publishPageEndpoint.addStatus(methodNotAllowed("the site or its content repository is read-only"));
    publishPageEndpoint.addStatus(serviceUnavailable("the site or its content repository is temporarily offline"));
    publishPageEndpoint.addPathParameter(new Parameter("page", Parameter.Type.String, "The page identifier"));
    publishPageEndpoint.addOptionalParameter(new Parameter("startdate", Parameter.Type.String, "The start of the publishing period"));
    publishPageEndpoint.addOptionalParameter(new Parameter("enddate", Parameter.Type.String, "The end of the publishing period"));
    publishPageEndpoint.setTestForm(new TestForm());
    docs.addEndpoint(Endpoint.Type.WRITE, publishPageEndpoint);

    // DELETE /{page}/publish
    Endpoint unpublishPageEndpoint = new Endpoint("/{page}/publish", Method.DELETE, "unpublishpage");
    unpublishPageEndpoint.setDescription("Unpublishs the specified page. If the client supplies an If-Match header, the unpublish is processed only if the header value matches the page's ETag");
    unpublishPageEndpoint.addFormat(Format.xml());
    unpublishPageEndpoint.addStatus(ok("the page was unpublished"));
    unpublishPageEndpoint.addStatus(badRequest("the page was not specified"));
    unpublishPageEndpoint.addStatus(preconditionFailed("the page's etag does not match the value specified in the If-Match header"));
    unpublishPageEndpoint.addStatus(notFound("the page was not found"));
    unpublishPageEndpoint.addStatus(forbidden("the page is locked by a different user"));
    unpublishPageEndpoint.addStatus(methodNotAllowed("the site or its content repository is read-only"));
    unpublishPageEndpoint.addStatus(serviceUnavailable("the site or its content repository is temporarily offline"));
    unpublishPageEndpoint.addPathParameter(new Parameter("page", Parameter.Type.String, "The page identifier"));
    unpublishPageEndpoint.setTestForm(new TestForm());
    docs.addEndpoint(Endpoint.Type.WRITE, unpublishPageEndpoint);

    return EndpointDocumentationGenerator.generate(docs);
  }

}
