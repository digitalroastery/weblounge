/*
 *  Weblounge: Web Content Management System
 *  Copyright (c) 2011 The Weblounge Team
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

package ch.o2it.weblounge.workbench.endpoint;

import ch.o2it.weblounge.common.content.MalformedResourceURIException;
import ch.o2it.weblounge.common.content.Resource;
import ch.o2it.weblounge.common.content.ResourceContent;
import ch.o2it.weblounge.common.content.ResourceURI;
import ch.o2it.weblounge.common.content.file.FileContent;
import ch.o2it.weblounge.common.content.file.FileResource;
import ch.o2it.weblounge.common.content.page.Composer;
import ch.o2it.weblounge.common.content.page.Page;
import ch.o2it.weblounge.common.content.page.PageTemplate;
import ch.o2it.weblounge.common.content.page.Pagelet;
import ch.o2it.weblounge.common.content.repository.ContentRepository;
import ch.o2it.weblounge.common.content.repository.ContentRepositoryException;
import ch.o2it.weblounge.common.content.repository.WritableContentRepository;
import ch.o2it.weblounge.common.impl.content.ResourceURIImpl;
import ch.o2it.weblounge.common.impl.content.ResourceUtils;
import ch.o2it.weblounge.common.impl.content.file.FileContentImpl;
import ch.o2it.weblounge.common.impl.content.file.FileResourceImpl;
import ch.o2it.weblounge.common.impl.content.page.PageURIImpl;
import ch.o2it.weblounge.common.impl.url.UrlUtils;
import ch.o2it.weblounge.common.language.Language;
import ch.o2it.weblounge.common.site.Site;
import ch.o2it.weblounge.kernel.SiteManager;
import ch.o2it.weblounge.workbench.PageletEditor;
import ch.o2it.weblounge.workbench.WorkbenchService;
import ch.o2it.weblounge.workbench.suggest.SubjectSuggestion;
import ch.o2it.weblounge.workbench.suggest.SuggestionList;

import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.IOUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.StreamingOutput;

/**
 * This class exposes the web methods of the workbench.
 */
@Path("/")
public class WorkbenchEndpoint {

  /** Logging facility */
  static final Logger logger = LoggerFactory.getLogger(WorkbenchEndpoint.class);

  /** Regular expression to match the resource type */
  protected static final Pattern resourceTypeRegex = Pattern.compile(".*<\\s*([\\w]*) .*");

  /** The workbench */
  protected transient WorkbenchService workbench = null;

  /** The sites that are online */
  protected transient SiteManager sites = null;

  /** The endpoint documentation */
  private String docs = null;

  @SuppressWarnings("unchecked")
  @GET
  @Path("/page/{pageid}/head")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getPageHead(@Context HttpServletRequest request,
      @PathParam("pageid") String pageId) {

    if (pageId == null)
      return Response.status(Status.BAD_REQUEST).build();

    // Load the page
    Page page = (Page) loadResource(request, pageId, Page.TYPE);
    if (page == null) {
      return Response.status(Status.NOT_FOUND).build();
    }

    // Is there an up-to-date, cached version on the client side?
    if (!ResourceUtils.isModified(page, request)) {
      return Response.notModified().build();
    }

    Site site = getSite(request);

    JSONObject pageJSON = new JSONObject();
    pageJSON.put("id", page.getIdentifier());
    pageJSON.put("path", page.getPath());
    pageJSON.put("version", page.getVersion());
    pageJSON.put("template", page.getTemplate());
    pageJSON.put("layout", page.getLayout());
    pageJSON.put("index", page.isIndexed());
    JSONObject titles = new JSONObject();
    for (Language lang : site.getLanguages())
      titles.put(lang.getIdentifier(), page.getTitle(lang));
    pageJSON.put("title", titles);

    // Return the page header
    return Response.ok(pageJSON.toJSONString()).build();
  }

  @SuppressWarnings("unchecked")
  @GET
  @Path("/languages")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getLanguages(@Context HttpServletRequest request) {
    Site site = getSite(request);

    JSONArray langs = new JSONArray();
    for (Language lang : site.getLanguages()) {
      JSONObject l = new JSONObject();
      l.put("id", lang.getIdentifier());
      // l.put("locale", lang.getLocale());
      l.put("name", lang.getDescription(lang));
      langs.add(l);
    }

    return Response.ok(langs.toJSONString()).build();
  }
  
  @GET
  @Path("/templates")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getTemplates(@Context HttpServletRequest request) {
    Site site = getSite(request);

    JSONArray langs = new JSONArray();
    for (PageTemplate template : site.getTemplates()) {
      JSONObject l = new JSONObject();
      l.put("id", template.getIdentifier());
      // l.put("locale", lang.getLocale());
      l.put("name", template.getName());
      langs.add(l);
    }

    return Response.ok(langs.toJSONString()).build();
  }

  /**
   * Returns the pagelet specified by <code>pageId</code>,
   * <code>composerId</code> and <code>pageletIndex</code> or a <code>404</code>
   * if either of the the page, the composer or the pagelet does not exist.
   * 
   * @param request
   *          the request
   * @param pageId
   *          the page identifier
   * @param composerId
   *          the composer identifier
   * @param pageletIndex
   *          the pagelet index within the composer
   * @return the pagelet
   */
  @SuppressWarnings("unchecked")
  @GET
  @Path("/{page}/composers/{composer}/pagelets/{pageletindex}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getPagelet(@Context HttpServletRequest request,
      @PathParam("page") String pageId,
      @PathParam("composer") String composerId,
      @PathParam("pageletindex") int pageletIndex) {

    if (pageId == null)
      return Response.status(Status.BAD_REQUEST).build();
    else if (composerId == null)
      return Response.status(Status.BAD_REQUEST).build();

    // Load the page
    Page page = (Page) loadResource(request, pageId, Page.TYPE);
    if (page == null) {
      return Response.status(Status.NOT_FOUND).build();
    }

    // Is there an up-to-date, cached version on the client side?
    if (!ResourceUtils.isModified(page, request)) {
      return Response.notModified().build();
    }

    // Load the composer
    Composer composer = page.getComposer(composerId);
    if (composer == null || composer.size() < pageletIndex) {
      return Response.status(Status.NOT_FOUND).build();
    }

    Pagelet pagelet = composer.getPagelets()[pageletIndex];

    JSONObject json = new JSONObject();
    json.put("module", pagelet.getModule());
    json.put("id", pagelet.getIdentifier());

    // Return the pagelet
    return Response.ok(json.toJSONString()).build();
  }

  /**
   * Returns an editor.
   * 
   * @param request
   *          the request
   * @param imageId
   *          the resource identifier
   * @return the image
   */
  @GET
  @Path("/edit/{page}/{composer}/{pageletindex}")
  @Produces({ MediaType.TEXT_XML, MediaType.APPLICATION_JSON })
  public Response getPageletEditor(@Context HttpServletRequest request,
      @PathParam("page") String pageURI,
      @PathParam("composer") String composerId,
      @PathParam("pageletindex") int pagelet) {

    // Load the site
    Site site = getSite(request);
    if (site == null)
      throw new WebApplicationException(Status.NOT_FOUND);

    // Return the editor
    // TODO: Work on work page instead of live
    ResourceURI uri = new PageURIImpl(site, null, pageURI, Resource.LIVE);
    PageletEditor editor;
    try {
      editor = workbench.getEditor(site, uri, composerId, pagelet);
    } catch (IOException e) {
      throw new WebApplicationException(e);
    }
    if (editor == null)
      throw new WebApplicationException(Status.NOT_FOUND);

    return Response.ok(editor.toXml()).build();
  }

  /**
   * Returns a list of suggested subjects based on an initial hint. The number
   * of suggestions returned can be specified using the <code>limit</code>
   * parameter.
   * 
   * @return the endpoint documentation
   */
  @GET
  @Path("/suggest/subjects/{hint}")
  @Produces({ MediaType.TEXT_XML, MediaType.APPLICATION_JSON })
  public Response suggestSubjects(@Context HttpServletRequest request,
      @PathParam("hint") String hint,
      @QueryParam("highlight") String highlightTag,
      @QueryParam("limit") int limit) {
    SuggestionList<SubjectSuggestion> list = new SuggestionList<SubjectSuggestion>("subjects", hint, highlightTag);
    try {
      list.addAll(workbench.suggestTags(getSite(request), hint, limit));
      return Response.ok(list.toXml()).build();
    } catch (IllegalStateException e) {
      throw new WebApplicationException(Status.SERVICE_UNAVAILABLE);
    } catch (Exception e) {
      throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
    }
  }

  @POST
  @Path("/files/add")
  @Produces(MediaType.APPLICATION_JSON)
  public Response addFile(@Context HttpServletRequest request) {
    
    List<Resource> addedFiles = new ArrayList<Resource>();

    if (ServletFileUpload.isMultipartContent(request)) {
      // Create a new file upload handler
      ServletFileUpload upload = new ServletFileUpload();

      // Parse the request
      FileItemIterator iter;
      try {
        iter = upload.getItemIterator(request);
        while (iter.hasNext()) {
          FileItemStream item = iter.next();
          String name = item.getFieldName();
          InputStream stream = item.openStream();
          if (!item.isFormField()) {
            addedFiles.add(addNewFileToRepo(getSite(request), name, stream));
          }
        }
      } catch (FileUploadException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    } else {
      return Response.status(Status.BAD_REQUEST).build();
    }

    return Response.ok("{\"id\":\"153354-asdf543-as5-a5s3-\"}").build();
  }

  private Resource addNewFileToRepo(Site site, String filename, InputStream stream) {
    ResourceURI fileUri = new ResourceURIImpl(FileResource.TYPE, site, "/my/path/to/the/file/"+filename.hashCode());
    Resource res = new FileResourceImpl(fileUri);
    ResourceContent content = new FileContentImpl(filename, site.getDefaultLanguage(), "application/pdf");
    WritableContentRepository contentRepository = (WritableContentRepository) getContentRepository(site, true);
    res.addContent(content);
    try {
      contentRepository.put(res);
    } catch (IllegalStateException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (ContentRepositoryException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    
    return res;
  }

  /**
   * Returns the endpoint documentation.
   * 
   * @return the endpoint documentation
   */
  @GET
  @Path("/docs")
  @Produces(MediaType.TEXT_HTML)
  public String getDocumentation(@Context HttpServletRequest request) {
    if (docs == null) {
      String docsPath = request.getRequestURI();
      String docsPathExtension = request.getPathInfo();
      String servicePath = request.getRequestURI().substring(0, docsPath.length() - docsPathExtension.length());
      docs = WorkbenchEndpointDocs.createDocumentation(servicePath);
    }
    return docs;
  }

  /**
   * Loads the given resource content.
   * 
   * @param request
   *          the servlet request
   * @param resource
   *          the resource
   * @param language
   *          the language
   * @return the resource content
   */
  protected Response getResourceContent(HttpServletRequest request,
      final Resource<?> resource, final Language language) {

    // Check the parameters
    if (resource == null)
      throw new WebApplicationException(Status.BAD_REQUEST);

    // Is there an up-to-date, cached version on the client side?
    if (!ResourceUtils.isModified(resource, request)) {
      return Response.notModified().build();
    }

    // Load the content
    ResourceContent resourceContent = resource.getContent(language);
    if (resourceContent == null) {
      throw new WebApplicationException(Status.NOT_FOUND);
    }

    // Check the ETag
    String eTagValue = ResourceUtils.getETagValue(resource, language);
    if (!ResourceUtils.isMismatch(resource, language, request)) {
      return Response.notModified(new EntityTag(eTagValue)).build();
    }

    Site site = getSite(request);
    final ContentRepository contentRepository = getContentRepository(site, false);
    final Language selectedLanguage = language;

    // Create the response
    final InputStream is;
    try {
      is = contentRepository.getContent(resource.getURI(), selectedLanguage);
    } catch (IOException e) {
      throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
    } catch (ContentRepositoryException e) {
      throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
    }

    // Is the content locally available
    if (is == null)
      throw new WebApplicationException(Status.NOT_FOUND);

    // Write the file contents back
    ResponseBuilder response = Response.ok(new StreamingOutput() {
      public void write(OutputStream os) throws IOException,
          WebApplicationException {
        try {
          IOUtils.copy(is, os);
        } catch (IOException e) {
          logger.warn("Error writing file contents to response", e);
        } finally {
          IOUtils.closeQuietly(is);
        }
      }
    });

    // Set file-related response information
    if (resourceContent instanceof FileContent) {
      FileContent fileContent = (FileContent) resourceContent;
      if (fileContent.getMimetype() != null)
        response.type(fileContent.getMimetype());
      else
        response.type(MediaType.APPLICATION_OCTET_STREAM);
      if (fileContent.getSize() > 0)
        response.header("Content-Length", fileContent.getSize());
    }

    // Add an e-tag and send the response
    response.header("Content-Disposition", "inline; filename=" + resource.getContent(selectedLanguage).getFilename());
    response.tag(new EntityTag(eTagValue));
    response.lastModified(resource.getModificationDate());
    return response.build();
  }

  /**
   * Returns the resource identified by the given request and resource
   * identifier or <code>null</code> if either one of the site, the site's
   * content repository or the resource itself is not available.
   * <p>
   * If <code>resourceType</code> is not <code>null</code>, the loaded resource
   * is only returned if the resource type matches.
   * 
   * @param request
   *          the servlet request
   * @param resourceId
   *          the resource identifier
   * @param resourceType
   *          the resource type
   * @return the resource
   */
  protected Resource<?> loadResource(HttpServletRequest request,
      String resourceId, String resourceType) {
    if (sites == null) {
      logger.debug("Unable to load resource '{}': no sites registered", resourceId);
      return null;
    }

    // Extract the site
    Site site = getSite(request);

    // Look for the content repository
    ContentRepository contentRepository = site.getContentRepository();
    if (contentRepository == null) {
      logger.warn("No content repository found for site '{}'", site);
      throw new WebApplicationException(Status.SERVICE_UNAVAILABLE);
    }

    // Load the resource and return it
    try {
      ResourceURI resourceURI = new ResourceURIImpl(resourceType, site, null, resourceId);
      Resource<?> resource = contentRepository.get(resourceURI);
      if (resource == null)
        return null;
      if (resourceType != null && !resourceType.equals(resource.getURI().getType())) {
        return null;
      }
      return resource;
    } catch (ContentRepositoryException e) {
      throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
    }
  }

  /**
   * Returns the resource identified by the given request and resource path or
   * <code>null</code> if either one of the site, the site's content repository
   * or the resource itself is not available.
   * <p>
   * If <code>resourceType</code> is not <code>null</code>, the loaded resource
   * is only returned if the resource type matches.
   * 
   * @param request
   *          the servlet request
   * @param resourcePath
   *          the resource path
   * @param resourceType
   *          the resource type
   * @return the resource
   */
  protected Resource<?> loadResourceByPath(HttpServletRequest request,
      String resourcePath, String resourceType) {
    if (sites == null) {
      logger.debug("Unable to load resource '{}': no sites registered", resourcePath);
      return null;
    }

    // Extract the site
    Site site = getSite(request);

    // Look for the content repository
    ContentRepository contentRepository = site.getContentRepository();
    if (contentRepository == null) {
      logger.warn("No content repository found for site '{}'", site);
      throw new WebApplicationException(Status.SERVICE_UNAVAILABLE);
    }

    // Load the resource and return it
    try {
      ResourceURI resourceURI = new ResourceURIImpl(resourceType, site, resourcePath);
      Resource<?> resource = contentRepository.get(resourceURI);
      if (resource == null)
        return null;
      if (resourceType != null && !resourceType.equals(resource.getURI().getType())) {
        return null;
      }
      return resource;
    } catch (MalformedResourceURIException e) {
      throw new WebApplicationException(Status.BAD_REQUEST);
    } catch (ContentRepositoryException e) {
      throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
    }
  }

  /**
   * Tries to locate the content repository for the given site. If
   * <code>writable</code> is <code>true</code>, the method tries to cast the
   * repository to a <code>WritableContentRepository</code>.
   * <p>
   * This method throws a corresponding <code>WebApplicationException</code> in
   * case of failure.
   * 
   * @param site
   *          the site
   * @param writable
   *          <code>true</code> to request a writable repository
   * @return the repository
   * @throws WebApplicationException
   *           if the repository can't be located or if it's not writable
   */
  protected ContentRepository getContentRepository(Site site, boolean writable) {
    try {
      ContentRepository contentRepository = site.getContentRepository();
      if (contentRepository == null) {
        logger.warn("No content repository found for site '{}'", site);
        throw new WebApplicationException(Status.SERVICE_UNAVAILABLE);
      }
      if (writable) {
        WritableContentRepository wcr = (WritableContentRepository) contentRepository;
        return wcr;
      } else {
        return contentRepository;
      }
    } catch (ClassCastException e) {
      logger.warn("Content repository '{}' is not writable", site);
      throw new WebApplicationException(Status.NOT_ACCEPTABLE);
    }
  }

  /**
   * Extracts the site from the request and returns it. If the site is not found
   * or it's not running, a corresponding <code>WebApplicationException</code>
   * is thrown.
   * 
   * @param request
   *          the http request
   * @return the site
   * @throws WebApplicationException
   *           if the site is not found or is not running
   */
  protected Site getSite(HttpServletRequest request)
      throws WebApplicationException {
    URL url = UrlUtils.toURL(request, false, false);
    Site site = sites.findSiteByURL(url);
    if (site == null) {
      throw new WebApplicationException(Status.NOT_FOUND);
    } else if (!site.isOnline()) {
      throw new WebApplicationException(Status.SERVICE_UNAVAILABLE);
    }
    return site;
  }

  /**
   * Returns the resource type or <code>null</code> if the type could not be
   * extracted from the specified document.
   * 
   * @param input
   *          the input text
   * @return the resource type
   */
  protected String getResourceType(String input) {
    Matcher m = resourceTypeRegex.matcher(input);
    if (m.matches()) {
      return m.group(1);
    }
    return null;
  }

  /**
   * Callback for OSGi to set the site manager.
   * 
   * @param siteManager
   *          the site manager
   */
  void setSiteManager(SiteManager siteManager) {
    this.sites = siteManager;
  }

  /**
   * Callback for OSGi to remove the site manager.
   * 
   * @param siteManager
   *          the site manager
   */
  void removeSiteManager(SiteManager siteManager) {
    this.sites = null;
  }

  /**
   * Callback for OSGi to set the workbench.
   * 
   * @param workbench
   *          the workbench implementation
   */
  void setWorkbench(WorkbenchService workbench) {
    this.workbench = workbench;
  }

  /**
   * {@inheritDoc}
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "workbench rest endpoint";
  }

}
