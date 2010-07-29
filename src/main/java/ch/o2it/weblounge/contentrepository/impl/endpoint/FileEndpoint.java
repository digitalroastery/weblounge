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

import ch.o2it.weblounge.kernel.SiteManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

import javax.activation.MimetypesFileTypeMap;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
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
  private transient SiteManager sites = null;

  @GET
  @Path("/{fileid}")
  public Response getPage(@PathParam("fileid") String fileId) {

    if (fileId == null)
      return Response.status(Status.BAD_REQUEST).build();

      // TODO: Implement

      return Response.ok().build();
  }

  /*
  @GET
  @Path("/images/{image}")
  @Produces("image/*")
  public Response getImage(@PathParam("image") String image) {
      File f = new File(image);

      if (!f.exists()) {
          throw new WebApplicationException(404);
      }

      String mt = new MimetypesFileTypeMap().getContentType(f);
      return Response.ok(f, mt).build();
  }
  */
  
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
   * {@inheritDoc}
   *
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "file rest endpoint";
  }

}
