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

import ch.entwine.weblounge.common.content.PreviewGenerator;
import ch.entwine.weblounge.common.content.Resource;
import ch.entwine.weblounge.common.content.ResourceContent;
import ch.entwine.weblounge.common.content.ResourceURI;
import ch.entwine.weblounge.common.content.ResourceUtils;
import ch.entwine.weblounge.common.content.image.ImageStyle;
import ch.entwine.weblounge.common.content.repository.ContentRepository;
import ch.entwine.weblounge.common.content.repository.ContentRepositoryException;
import ch.entwine.weblounge.common.impl.content.image.ImageStyleUtils;
import ch.entwine.weblounge.common.impl.language.LanguageUtils;
import ch.entwine.weblounge.common.language.Language;
import ch.entwine.weblounge.common.language.UnknownLanguageException;
import ch.entwine.weblounge.common.site.Environment;
import ch.entwine.weblounge.common.site.ImageScalingMode;
import ch.entwine.weblounge.common.site.Module;
import ch.entwine.weblounge.common.site.Site;
import ch.entwine.weblounge.common.site.SiteURL;
import ch.entwine.weblounge.contentrepository.ResourceSerializer;
import ch.entwine.weblounge.contentrepository.ResourceSerializerFactory;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.osgi.service.component.ComponentContext;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.StreamingOutput;

/**
 * This class implements the <code>REST</code> endpoint for resource previews.
 */
@Path("/")
public class PreviewsEndpoint extends ContentRepositoryEndpoint {

  /** The endpoint documentation */
  private String docs = null;

  /** The list of image styles */
  private List<ImageStyle> styles = new ArrayList<ImageStyle>();

  /**
   * OSGi callback on component deactivation.
   * 
   * @param ctx
   *          the component context
   */
  void deactivate(ComponentContext ctx) {
    styles.clear();
  }

  /**
   * Returns the resource with the given identifier and styled using the
   * requested image style or a <code>404</code> if the resource or the resource
   * content could not be found.
   * <p>
   * If the content is not available in the requested language, the original
   * language version is used.
   * 
   * @param request
   *          the request
   * @param resourceId
   *          the resource identifier
   * @param languageId
   *          the language identifier
   * @param styleId
   *          the image style identifier
   * @return the image
   */
  @GET
  @Path("/{resource}/locales/{language}/styles/{style}")
  public Response getPreview(@Context HttpServletRequest request,
      @PathParam("resource") String resourceId,
      @PathParam("language") String languageId,
      @PathParam("style") String styleId,
      @QueryParam("version") @DefaultValue("0") long version) {

    // Check the parameters
    if (resourceId == null)
      throw new WebApplicationException(Status.BAD_REQUEST);

    // Get the resource
    final Site site = getSite(request);
    final Resource<?> resource = loadResource(request, resourceId, null, version);
    if (resource == null)
      throw new WebApplicationException(Status.NOT_FOUND);

    // Extract the language
    Language language = null;
    try {
      language = LanguageUtils.getLanguage(languageId);
      if (!resource.supportsLanguage(language)) {
        if (!resource.contents().isEmpty())
          language = resource.getOriginalContent().getLanguage();
        else if (resource.supportsLanguage(site.getDefaultLanguage()))
          language = site.getDefaultLanguage();
        else if (resource.languages().size() == 1)
          language = resource.languages().iterator().next();
        else
          throw new WebApplicationException(Status.NOT_FOUND);
      }
    } catch (UnknownLanguageException e) {
      throw new WebApplicationException(Status.BAD_REQUEST);
    }

    // Search the site for the image style
    ImageStyle style = null;
    for (Module m : site.getModules()) {
      style = m.getImageStyle(styleId);
      if (style != null) {
        break;
      }
    }

    // Search the global styles
    if (style == null) {
      for (ImageStyle s : styles) {
        if (s.getIdentifier().equals(styleId)) {
          style = s;
          break;
        }
      }
    }

    // The image style was not found
    if (style == null)
      throw new WebApplicationException(Status.BAD_REQUEST);

    // Is there an up-to-date, cached version on the client side?
    if (!ResourceUtils.hasChanged(request, resource, style, language)) {
      return Response.notModified().build();
    }

    // Load the content
    ResourceContent resourceContent = resource.getContent(language);

    ResourceURI resourceURI = resource.getURI();
    final ContentRepository contentRepository = getContentRepository(site, false);

    // When there is no scaling required, just return the original
    if (ImageScalingMode.None.equals(style.getScalingMode())) {
      return getResourceContent(request, resource, language);
    }

    // Find a serializer
    ResourceSerializer<?, ?> serializer = ResourceSerializerFactory.getSerializerByType(resourceURI.getType());
    if (serializer == null)
      throw new WebApplicationException(Status.PRECONDITION_FAILED);

    // Does the serializer come with a preview generator?
    PreviewGenerator previewGenerator = serializer.getPreviewGenerator();
    if (previewGenerator == null)
      throw new WebApplicationException(Status.NOT_FOUND);

    // Load the resource contents from the repository
    InputStream resourceInputStream = null;
    long contentLength = -1;

    // Create the target file name
    StringBuilder filename = new StringBuilder();
    String basename = null;
    if (resourceContent != null)
      basename = FilenameUtils.getBaseName(resourceContent.getFilename());
    else
      basename = resource.getIdentifier();
    String suffix = previewGenerator.getSuffix(resource, language, style);
    filename.append(basename);
    filename.append("-").append(version);
    filename.append("-").append(language.getIdentifier());
    if (StringUtils.isNotBlank(suffix)) {
      filename.append(".").append(suffix);
    }

    // Load the input stream from the scaled image
    InputStream contentRepositoryIs = null;
    FileOutputStream fos = null;
    File scaledResourceFile = null;
    try {
      scaledResourceFile = ImageStyleUtils.createScaledFile(resourceURI, filename.toString(), language, style);
      long lastModified = ResourceUtils.getModificationDate(resource, language).getTime();
      if (!scaledResourceFile.isFile() || scaledResourceFile.lastModified() < lastModified) {
        contentRepositoryIs = contentRepository.getContent(resourceURI, language);
        fos = new FileOutputStream(scaledResourceFile);
        logger.debug("Creating scaled image '{}' at {}", resource, scaledResourceFile);

        // What is the current environment?
        Environment environment = Environment.Production;
        for (SiteURL url : site.getConnectors()) {
          if (request.getRequestURL().toString().startsWith(url.toExternalForm())) {
            environment = url.getEnvironment();
            break;
          }
        }

        previewGenerator.createPreview(resource, environment, language, style, contentRepositoryIs, fos);
        if (scaledResourceFile.length() > 1) {
          scaledResourceFile.setLastModified(lastModified);
        } else {
          logger.error("Error scaling '{}': file size is 0", resourceURI);
          IOUtils.closeQuietly(resourceInputStream);
          FileUtils.deleteQuietly(scaledResourceFile);
        }
      }

      // Did scaling work? If not, cleanup and tell the user
      if (scaledResourceFile.length() == 0) {
        File f = scaledResourceFile;
        while (f != null && f.isDirectory() && f.listFiles().length == 0) {
          FileUtils.deleteQuietly(f);
          f = f.getParentFile();
        }
        throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
      }

      // The scaled resource should now exist
      resourceInputStream = new FileInputStream(scaledResourceFile);
      contentLength = scaledResourceFile.length();
    } catch (ContentRepositoryException e) {
      logger.error("Error loading {} image '{}' from {}: {}", new Object[] {
          language,
          resource,
          contentRepository,
          e.getMessage() });
      logger.error(e.getMessage(), e);
      IOUtils.closeQuietly(resourceInputStream);
      FileUtils.deleteQuietly(scaledResourceFile);

      File f = scaledResourceFile;
      while (f != null && f.isDirectory() && f.listFiles().length == 0) {
        FileUtils.deleteQuietly(f);
        f = f.getParentFile();
      }

      throw new WebApplicationException();
    } catch (IOException e) {
      logger.error("Error scaling image '{}': {}", resourceURI, e.getMessage());
      IOUtils.closeQuietly(resourceInputStream);

      File f = scaledResourceFile;
      while (f != null && f.isDirectory() && f.listFiles().length == 0) {
        FileUtils.deleteQuietly(f);
        f = f.getParentFile();
      }

      throw new WebApplicationException();
    } catch (IllegalArgumentException e) {
      logger.error("Image '{}' is of unsupported format: {}", resourceURI, e.getMessage());
      IOUtils.closeQuietly(resourceInputStream);

      File f = scaledResourceFile;
      while (f != null && f.isDirectory() && f.listFiles().length == 0) {
        FileUtils.deleteQuietly(f);
        f = f.getParentFile();
      }

      throw new WebApplicationException();
    } catch (Throwable t) {
      logger.error("Error scaling image '{}': {}", resourceURI, t.getMessage());
      IOUtils.closeQuietly(resourceInputStream);

      File f = scaledResourceFile;
      while (f != null && f.isDirectory() && f.listFiles().length == 0) {
        FileUtils.deleteQuietly(f);
        f = f.getParentFile();
      }

      throw new WebApplicationException();
    } finally {
      IOUtils.closeQuietly(contentRepositoryIs);
      IOUtils.closeQuietly(fos);
    }

    // Create the response
    final InputStream is = resourceInputStream;
    ResponseBuilder response = Response.ok(new StreamingOutput() {
      public void write(OutputStream os) throws IOException,
          WebApplicationException {
        try {
          IOUtils.copy(is, os);
          os.flush();
        } finally {
          IOUtils.closeQuietly(is);
        }
      }
    });

    // Add mime type header
    String mimetype = previewGenerator.getContentType(resource, language, style);
    if (mimetype == null)
      mimetype = MediaType.APPLICATION_OCTET_STREAM;
    response.type(mimetype);

    // Add last modified header
    response.lastModified(ResourceUtils.getModificationDate(resource, language));

    // Add ETag header
    String eTag = ResourceUtils.getETagValue(resource, style);
    response.tag(eTag);

    // Add filename header
    response.header("Content-Disposition", "inline; filename=" + filename);

    // Content length
    response.header("Content-Length", Long.toString(contentLength));

    // Send the response
    return response.build();
  }

  /**
   * Returns the list of image styles that are registered for a site.
   * 
   * @param request
   *          the request
   * @return the list of image styles
   */
  @GET
  @Produces(MediaType.TEXT_XML)
  @Path("/styles")
  public Response getImagestyles(@Context HttpServletRequest request) {
    Site site = getSite(request);
    if (site == null)
      throw new WebApplicationException(Status.NOT_FOUND);

    StringBuffer buf = new StringBuffer("<styles>");

    // Add styles of current site
    for (Module m : site.getModules()) {
      ImageStyle[] styles = m.getImageStyles();
      for (ImageStyle style : styles) {
        buf.append(style.toXml());
      }
    }

    // Add global styles
    for (ImageStyle style : styles) {
      buf.append(style.toXml());
    }

    buf.append("</styles>");

    ResponseBuilder response = Response.ok(buf.toString());
    return response.build();
  }

  /**
   * Returns the image styles or a <code>404</code>.
   * 
   * @param request
   *          the request
   * @param styleId
   *          the image style identifier
   * @return the image
   */
  @GET
  @Produces(MediaType.TEXT_XML)
  @Path("/styles/{style}")
  public Response getImagestyle(@Context HttpServletRequest request,
      @PathParam("style") String styleId) {
    Site site = getSite(request);

    // Search styles of current site
    for (Module m : site.getModules()) {
      ImageStyle style = m.getImageStyle(styleId);
      if (style != null) {
        ResponseBuilder response = Response.ok(style.toXml());
        return response.build();
      }
    }

    // Search global styles
    for (ImageStyle style : styles) {
      if (style.getIdentifier().equals(styleId)) {
        ResponseBuilder response = Response.ok(style.toXml());
        return response.build();
      }
    }

    // The image style was not found
    throw new WebApplicationException(Status.NOT_FOUND);
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
      docs = PreviewsEndpointDocs.createDocumentation(servicePath);
    }
    return docs;
  }

  /**
   * Callback from OSGi declarative services on registration of a new image
   * style in the service registry.
   * 
   * @param style
   *          the image style
   */
  void addImageStyle(ImageStyle style) {
    styles.add(style);
  }

  /**
   * Callback from OSGi declarative services on removal of an image style from
   * the service registry.
   * 
   * @param style
   *          the image style
   */
  void removeImageStyle(ImageStyle style) {
    styles.remove(style);
  }

  /**
   * {@inheritDoc}
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "Previews rest endpoint";
  }

}
