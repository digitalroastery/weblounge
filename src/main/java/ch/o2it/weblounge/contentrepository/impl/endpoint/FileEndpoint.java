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

import ch.o2it.weblounge.dispatcher.SiteRegistrationService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

/**
 * This class implements the <code>REST</code> endpoint for file data.
 */
@Path("/")
@Produces(MediaType.TEXT_XML)
public class FileEndpoint {

  /** Logging facility */
  private static final Logger logger = LoggerFactory.getLogger(FileEndpoint.class);

  /** The sites that are online */
  private transient SiteRegistrationService sites = null;

  @GET
  @Path("/{fileid}")
  public Response getPage(@PathParam("fileid") String fileId) {

    logger.info("File rest endpoint hit!");
    
    if (fileId == null)
      return Response.status(Status.BAD_REQUEST).build();

      // TODO: Implement

      return Response.ok().build();
  }
  
  /**
   * Callback for OSGi to set the site locator.
   * 
   * @param siteLocator
   *          the site locator
   */
  void setSiteLocator(SiteRegistrationService siteLocator) {
    this.sites = siteLocator;
  }

  /**
   * Callback for OSGi to remove the site locator.
   * 
   * @param siteLocator
   *          the site locator
   */
  void removeSiteLocator(SiteRegistrationService siteLocator) {
    this.sites = null;
  }

  /**
   * {@inheritDoc}
   *
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "files rest endpoint";
  }

}
