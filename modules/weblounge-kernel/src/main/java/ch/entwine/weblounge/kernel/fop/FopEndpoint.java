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

package ch.entwine.weblounge.kernel.fop;

import ch.entwine.weblounge.common.site.Environment;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.fop.apps.FOPException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.StreamingOutput;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;

/**
 * This class implements the <code>REST</code> endpoint for the FOP service.
 */
@Path("/")
@Produces(MediaType.TEXT_XML)
public class FopEndpoint {

  /** The loggin facility */
  private static final Logger logger = LoggerFactory.getLogger(FopEndpoint.class);

  /** The FOP service */
  protected transient FopService fopService = null;

  /** The request environment */
  protected Environment environment = Environment.Production;

  /** The endpoint documentation */
  private String docs = null;

  /**
   * Downloads both XML and XSL document and transforms them to PDF.
   * 
   * @param xmlURL
   *          the URL to the XML document
   * @param xslURL
   *          the URL to the XSL document
   * @return the generated PDF
   * @throws WebApplicationException
   *           if the XML document cannot be downloaded
   * @throws WebApplicationException
   *           if the XSL document cannot be downloaded
   * @throws WebApplicationException
   *           if the PDF creation fails
   */
  @POST
  @Path("/pdf")
  public Response transformToPdf(@FormParam("xml") String xmlURL,
      @FormParam("xsl") String xslURL, @FormParam("parameters") String params) {

    // Make sure we have a service
    if (fopService == null)
      throw new WebApplicationException(Status.SERVICE_UNAVAILABLE);

    final Document xml;
    final Document xsl;

    // Load the xml document
    InputStream xmlInputStream = null;
    try {
      DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
      xmlInputStream = new URL(xmlURL).openStream();
      xml = documentBuilder.parse(xmlInputStream);
    } catch (MalformedURLException e) {
      logger.warn("Error creating xml url from '{}'", xmlURL);
      throw new WebApplicationException(Status.BAD_REQUEST);
    } catch (IOException e) {
      logger.warn("Error accessing xml document at '{}': {}", xmlURL, e.getMessage());
      throw new WebApplicationException(Status.NOT_FOUND);
    } catch (ParserConfigurationException e) {
      logger.warn("Error setting up xml parser: {}", e.getMessage());
      throw new WebApplicationException(Status.BAD_REQUEST);
    } catch (SAXException e) {
      logger.warn("Error parsing xml document from {}: {}", xmlURL, e.getMessage());
      throw new WebApplicationException(Status.BAD_REQUEST);
    } finally {
      IOUtils.closeQuietly(xmlInputStream);
    }

    // Load the XLST stylesheet
    InputStream xslInputStream = null;
    try {
      DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
      xslInputStream = new URL(xslURL).openStream();
      xsl = documentBuilder.parse(xslInputStream);
    } catch (MalformedURLException e) {
      logger.warn("Error creating xsl url from '{}'", xslURL);
      throw new WebApplicationException(Status.BAD_REQUEST);
    } catch (IOException e) {
      logger.warn("Error accessing xsl stylesheet at '{}': {}", xslURL, e.getMessage());
      throw new WebApplicationException(Status.NOT_FOUND);
    } catch (ParserConfigurationException e) {
      logger.warn("Error setting up xml parser: {}", e.getMessage());
      throw new WebApplicationException(Status.BAD_REQUEST);
    } catch (SAXException e) {
      logger.warn("Error parsing xml document from {}: {}", xslURL, e.getMessage());
      throw new WebApplicationException(Status.BAD_REQUEST);
    } finally {
      IOUtils.closeQuietly(xslInputStream);
    }

    // Create the filename
    String name = FilenameUtils.getBaseName(xmlURL) + ".pdf";

    // Process the parameters
    final List<String[]> parameters = new ArrayList<String[]>();
    if (StringUtils.isNotBlank(params)) {
      for (String param : StringUtils.split(params, ";")) {
        String[] parameterValue = StringUtils.split(param, "=");
        if (parameterValue.length != 2) {
          logger.warn("Parameter for PDF generation is malformed: {}", param);
          throw new WebApplicationException(Status.BAD_REQUEST);
        }
        parameters.add(new String[] {
            StringUtils.trim(parameterValue[0]),
            StringUtils.trim(parameterValue[1]) });
      }
    }

    // Write the file contents back
    ResponseBuilder response = Response.ok(new StreamingOutput() {
      public void write(OutputStream os) throws IOException,
          WebApplicationException {
        try {
          fopService.xml2pdf(xml, xsl, parameters.toArray(new String[parameters.size()][2]), os);
        } catch (IOException e) {
          Throwable cause = e.getCause();
          if (cause == null || !"Broken pipe".equals(cause.getMessage()))
            logger.warn("Error writing file contents to response", e);
        } catch (TransformerConfigurationException e) {
          logger.error("Error setting up the XSL transfomer: {}", e.getMessage());
          throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
        } catch (FOPException e) {
          logger.error("Error creating PDF document: {}", e.getMessage());
          throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
        } catch (TransformerException e) {
          logger.error("Error transforming to PDF: {}", e.getMessage());
          throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
        } finally {
          IOUtils.closeQuietly(os);
        }
      }
    });

    // Set response information
    response.type("application/pdf");
    response.header("Content-Disposition", "inline; filename=" + name);
    response.lastModified(new Date());

    return response.build();
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
      docs = FopEndpointDocs.createDocumentation(servicePath);
    }
    return docs;
  }

  /**
   * Callback for OSGi to set the FOP service.
   * 
   * @param fopService
   *          the FOP service
   */
  void setFopService(FopService fopService) {
    this.fopService = fopService;
  }

  /**
   * Callback for OSGi to remove the FOP service.
   * 
   * @param fopService
   *          the FOP service
   */
  void removeFopService(FopService fopService) {
    this.fopService = null;
  }

  /**
   * {@inheritDoc}
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "Fop rest endpoint";
  }

}
