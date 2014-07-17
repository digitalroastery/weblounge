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

package ch.entwine.weblounge.security.sql.endpoint;

import ch.entwine.weblounge.common.impl.language.LanguageUtils;
import ch.entwine.weblounge.common.impl.security.PasswordEncoder;
import ch.entwine.weblounge.common.impl.security.RoleImpl;
import ch.entwine.weblounge.common.impl.security.SecurityUtils;
import ch.entwine.weblounge.common.impl.security.SystemRole;
import ch.entwine.weblounge.common.impl.security.WebloungeUserImpl;
import ch.entwine.weblounge.common.language.UnknownLanguageException;
import ch.entwine.weblounge.common.security.DigestType;
import ch.entwine.weblounge.common.security.User;
import ch.entwine.weblounge.common.security.UserExistsException;
import ch.entwine.weblounge.common.security.UserShadowedException;
import ch.entwine.weblounge.common.security.WebloungeUser;
import ch.entwine.weblounge.common.site.Site;
import ch.entwine.weblounge.common.url.UrlUtils;
import ch.entwine.weblounge.kernel.site.SiteManager;
import ch.entwine.weblounge.security.sql.SQLDirectoryProvider;
import ch.entwine.weblounge.security.sql.entities.JpaAccount;
import ch.entwine.weblounge.security.sql.entities.JpaRole;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

/**
 * This class implements the <code>REST</code> endpoint to manage the SQL
 * directory provider.
 */
@Path("/")
@Produces(MediaType.APPLICATION_XML)
public class SQLDirectoryProviderEndpoint {

  /** The logging facility */
  private static final Logger logger = LoggerFactory.getLogger(SQLDirectoryProviderEndpoint.class);

  /** The endpoint documentation */
  private String docs = null;

  /** The cache configuration factory */
  private SQLDirectoryProvider directory = null;

  /** The sites that are online */
  protected SiteManager sites = null;

  @GET
  @Path("/")
  public Response getStatistics(@Context HttpServletRequest request) {
    Site site = getSite(request);

    try {
      StringBuilder stats = new StringBuilder();
      stats.append("<directory id=\"").append(site.getIdentifier()).append("\">");
      stats.append("<enabled>").append(directory.isSiteEnabled(site)).append("</enabled>");
      stats.append("<users>").append(directory.getAccounts(site).size()).append("</users>");
      stats.append("</directory>");
      Response response = Response.ok(stats.toString()).build();
      return response;
    } catch (Throwable t) {
      logger.warn("Error creating directory statistics: {}", t.getMessage());
      return Response.serverError().build();
    }
  }

  @PUT
  @Path("/status")
  public Response enableSite(@Context HttpServletRequest request) {
    Site site = getSite(request);

    // Make sure that the user owns the roles required for this operation
    User user = SecurityUtils.getUser();
    if (!SecurityUtils.userHasRole(user, SystemRole.SITEADMIN))
      return Response.status(Status.FORBIDDEN).build();

    // Enable login
    try {
      if (directory.isSiteEnabled(site))
        return Response.notModified().build();
      directory.enableSite(site);
      return Response.ok().build();
    } catch (Throwable t) {
      logger.warn("Error enabling site logins: {}", t.getMessage());
      return Response.serverError().build();
    }
  }

  @DELETE
  @Path("/status")
  public Response disableSite(@Context HttpServletRequest request) {
    Site site = getSite(request);

    // Make sure that the user owns the roles required for this operation
    User user = SecurityUtils.getUser();
    if (!SecurityUtils.userHasRole(user, SystemRole.SITEADMIN))
      return Response.status(Status.FORBIDDEN).build();

    // Disable login
    try {
      if (!directory.isSiteEnabled(site))
        return Response.notModified().build();
      directory.disableSite(site);
      return Response.ok().build();
    } catch (Throwable t) {
      logger.warn("Error disabling site logins: {}", t.getMessage());
      return Response.serverError().build();
    }
  }

  @POST
  @Path("/account")
  public Response createAccount(@FormParam("login") String login,
      @FormParam("password") String password, @FormParam("email") String eMail,
      @Context HttpServletRequest request) {

    // TODO: If not, return a one time pad that needs to be used when verifying
    // the e-mail

    // Check the arguments
    if (StringUtils.isBlank(login))
      return Response.status(Status.BAD_REQUEST).build();

    Response response = null;
    Site site = getSite(request);

    // Hash the password
    if (StringUtils.isNotBlank(password)) {
      logger.debug("Hashing password for user '{}@{}' using md5", login, site.getIdentifier());
      password = PasswordEncoder.encode(StringUtils.trim(password));
    }

    // Create the user
    try {
      JpaAccount account = directory.addAccount(site, login, password);
      account.setEmail(StringUtils.trimToNull(eMail));
      directory.updateAccount(account);
      response = Response.created(new URI(UrlUtils.concat(request.getRequestURL().toString(), account.getLogin()))).build();
    } catch (UserExistsException e) {
      logger.warn("Error creating account: {}", e.getMessage());
      return Response.status(Status.CONFLICT).build();
    } catch (UserShadowedException e) {
      logger.warn("Error creating account: {}", e.getMessage());
      return Response.status(Status.CONFLICT).build();
    } catch (Throwable t) {
      logger.warn("Error creating account: {}", t.getMessage());
      response = Response.serverError().build();
    }
    return response;
  }

  @GET
  @Path("/account/{login}")
  public Response getAccount(@PathParam("login") String login,
      @Context HttpServletRequest request) {
    Site site = getSite(request);

    JpaAccount account = null;
    try {
      account = directory.getAccount(site, login);
    } catch (Throwable t) {
      logger.warn("Error accessing account '{}': {}", login, t.getMessage());
      throw new WebApplicationException();
    }

    if (account == null)
      throw new WebApplicationException(Status.NOT_FOUND);

    WebloungeUser wu = new WebloungeUserImpl(login, directory.getIdentifier());
    wu.setFirstName(account.getFirstname());
    wu.setLastName(account.getLastname());
    wu.setEmail(account.getEmail());
    wu.setInitials(account.getInitials());
    wu.setLastLogin(account.getLastLoginDate(), account.getLastLoginFrom());
    wu.setChallenge(account.getChallenge());
    if (account.getLanguage() != null)
      wu.setLanguage(LanguageUtils.getLanguage(account.getLanguage()));
    if (account.getResponse() != null)
      wu.setResponse(account.getResponse().getBytes(Charset.forName("utf-8")), DigestType.md5);
    for (JpaRole r : account.getRoles()) {
      wu.addPublicCredentials(new RoleImpl(r.getContext(), r.getRolename()));
    }

    return Response.ok(wu.toXml()).build();
  }

  @GET
  @Path("/account/{login}/activate")
  public Response activateAccount(@PathParam("login") String login,
      @QueryParam("activation") String activation,
      @Context HttpServletRequest request) {
    Site site = getSite(request);
    try {
      boolean success = directory.activateAccount(site, login, activation);
      return (success) ? Response.ok().build() : Response.status(Status.UNAUTHORIZED).build();
    } catch (Throwable t) {
      logger.warn("Error activating account '{}': {}", login, t.getMessage());
      return Response.serverError().build();
    }
  }

  @PUT
  @Path("/account/{id}")
  public Response updateAccount(@PathParam("id") String login,
      @FormParam("password") String password,
      @FormParam("firstname") String firstname,
      @FormParam("lastname") String lastname,
      @FormParam("initials") String initials, @FormParam("email") String email,
      @FormParam("language") String language,
      @FormParam("challenge") String challenge,
      @FormParam("response") String response,
      @Context HttpServletRequest request) {

    // Make sure that the user owns the roles required for this operation
    User user = SecurityUtils.getUser();
    if (!SecurityUtils.userHasRole(user, SystemRole.SITEADMIN) && !user.getLogin().equals(login))
      return Response.status(Status.FORBIDDEN).build();

    JpaAccount account = null;
    Site site = getSite(request);
    try {
      account = directory.getAccount(site, login);
      if (account == null)
        return Response.status(Status.NOT_FOUND).build();

      // Hash the password
      if (StringUtils.isNotBlank(password)) {
        logger.debug("Hashing password for user '{}@{}' using md5", login, site.getIdentifier());
        String digestPassword = PasswordEncoder.encode(StringUtils.trim(password));
        account.setPassword(digestPassword);
      }

      account.setFirstname(StringUtils.trimToNull(firstname));
      account.setLastname(StringUtils.trimToNull(lastname));
      account.setInitials(StringUtils.trimToNull(initials));
      account.setEmail(StringUtils.trimToNull(email));

      // The language
      if (StringUtils.isNotBlank(language)) {
        try {
          account.setLanguage(LanguageUtils.getLanguage(language));
        } catch (UnknownLanguageException e) {
          return Response.status(Status.BAD_REQUEST).build();
        }
      } else {
        account.setLanguage(null);
      }

      // Hash the response
      if (StringUtils.isNotBlank(response)) {
        logger.debug("Hashing response for user '{}@{}' using md5", login, site.getIdentifier());
        String digestResponse = PasswordEncoder.encode(StringUtils.trim(response));
        account.setResponse(digestResponse);
      }

      directory.updateAccount(account);
      return Response.ok().build();
    } catch (Throwable t) {
      return Response.serverError().build();
    }
  }

  @PUT
  @Path("/account/{id}/password")
  public Response updateAccountPassword(@PathParam("id") String login,
      @FormParam("password") String password,
      @Context HttpServletRequest request) {

    // Make sure that the user owns the roles required for this operation
    User user = SecurityUtils.getUser();
    if (!SecurityUtils.userHasRole(user, SystemRole.SITEADMIN) && !user.getLogin().equals(login))
      return Response.status(Status.FORBIDDEN).build();

    JpaAccount account = null;
    Site site = getSite(request);
    try {
      account = directory.getAccount(site, login);
      if (account == null)
        return Response.status(Status.NOT_FOUND).build();

      // Hash the password
      if (StringUtils.isNotBlank(password)) {
        logger.debug("Hashing password for user '{}@{}' using md5", login, site.getIdentifier());
        String digestPassword = PasswordEncoder.encode(StringUtils.trim(password));
        account.setPassword(digestPassword);
      } else {
        account.setPassword(null);
      }

      directory.updateAccount(account);
      return Response.ok().build();
    } catch (Throwable t) {
      return Response.serverError().build();
    }
  }

  @PUT
  @Path("/account/{id}/challenge")
  public Response updateAccountChallenge(@PathParam("id") String login,
      @FormParam("challenge") String challenge,
      @FormParam("response") String response,
      @Context HttpServletRequest request) {

    // Make sure that the user owns the roles required for this operation
    User user = SecurityUtils.getUser();
    if (!SecurityUtils.userHasRole(user, SystemRole.SITEADMIN) && !user.getLogin().equals(login))
      return Response.status(Status.FORBIDDEN).build();

    JpaAccount account = null;
    Site site = getSite(request);
    try {
      account = directory.getAccount(site, login);
      if (account == null)
        return Response.status(Status.NOT_FOUND).build();

      // Set the challenge
      account.setChallenge(StringUtils.trimToNull(challenge));

      // Hash the response
      if (StringUtils.isNotBlank(response)) {
        logger.debug("Hashing response for user '{}@{}' using md5", login, site.getIdentifier());
        String digestResponse = PasswordEncoder.encode(StringUtils.trim(response));
        account.setResponse(digestResponse);
      } else {
        account.setResponse(response);
      }

      directory.updateAccount(account);
      return Response.ok().build();
    } catch (Throwable t) {
      return Response.serverError().build();
    }
  }

  @DELETE
  @Path("/account/{id}")
  public Response deleteAccount(@PathParam("id") String login,
      @Context HttpServletRequest request) {

    // Make sure that the user owns the roles required for this operation
    User user = SecurityUtils.getUser();
    if (!SecurityUtils.userHasRole(user, SystemRole.SITEADMIN) && !user.getLogin().equals(login))
      return Response.status(Status.FORBIDDEN).build();

    Site site = getSite(request);
    try {
      JpaAccount account = directory.getAccount(site, login);
      if (account == null)
        return Response.status(Status.NOT_FOUND).build();

      directory.removeAccount(site, login);
      return Response.ok().build();
    } catch (Throwable t) {
      logger.warn("Error removing account '{}': {}", login, t.getMessage());
      return Response.serverError().build();
    }
  }

  @PUT
  @Path("/account/{id}/status")
  public Response enableAccount(@PathParam("id") String login,
      @Context HttpServletRequest request) {

    // Make sure that the user owns the roles required for this operation
    User user = SecurityUtils.getUser();
    if (!SecurityUtils.userHasRole(user, SystemRole.SITEADMIN))
      return Response.status(Status.FORBIDDEN).build();

    JpaAccount account = null;
    Site site = getSite(request);
    try {
      account = directory.getAccount(site, login);
      if (account == null)
        return Response.status(Status.NOT_FOUND).build();
      if (account.isEnabled())
        return Response.status(Status.NOT_MODIFIED).build();

      directory.enableAccount(site, login);
      return Response.ok().build();
    } catch (Throwable t) {
      logger.warn("Error enabling account '{}': {}", login, t.getMessage());
      return Response.serverError().build();
    }
  }

  @DELETE
  @Path("/account/{id}/status")
  public Response disableAccount(@PathParam("id") String login,
      @Context HttpServletRequest request) {

    // Make sure that the user owns the roles required for this operation
    User user = SecurityUtils.getUser();
    if (!SecurityUtils.userHasRole(user, SystemRole.SITEADMIN))
      return Response.status(Status.FORBIDDEN).build();

    JpaAccount account = null;
    Site site = getSite(request);
    try {
      account = directory.getAccount(site, login);
      if (account == null)
        return Response.status(Status.NOT_FOUND).build();
      if (!account.isEnabled())
        return Response.status(Status.NOT_MODIFIED).build();

      directory.disableAccount(site, login);
      return Response.ok().build();
    } catch (Throwable t) {
      logger.warn("Error disabling account '{}': {}", login, t.getMessage());
      return Response.serverError().build();
    }
  }

  @POST
  @Path("/account/{id}/roles/{context}")
  public Response addRole(@PathParam("id") String login,
      @PathParam("context") String context, @FormParam("role") String role,
      @Context HttpServletRequest request) {

    // Make sure that the user owns the roles required for this operation
    User user = SecurityUtils.getUser();
    if (!SecurityUtils.userHasRole(user, SystemRole.SITEADMIN))
      return Response.status(Status.FORBIDDEN).build();

    // Make sure a role has been provided as part of the request
    if (StringUtils.isBlank(role))
      return Response.status(Status.BAD_REQUEST).build();

    JpaAccount account = null;
    Site site = getSite(request);
    try {
      account = directory.getAccount(site, login);
      if (account == null)
        return Response.status(Status.NOT_FOUND).build();

      if (account.hasRole(context, role))
        return Response.status(Status.NOT_MODIFIED).build();

      account.addRole(context, role);
      directory.updateAccount(account);
      return Response.ok().build();
    } catch (Throwable t) {
      logger.warn("Error adding role '{}:{}' to account {}: {}", new String[] {
          context,
          role,
          login,
          t.getMessage() });
      return Response.serverError().build();
    }
  }

  @DELETE
  @Path("/account/{id}/roles/{context}")
  public Response removeRole(@PathParam("id") String login,
      @PathParam("context") String context, @FormParam("role") String role,
      @Context HttpServletRequest request) {

    // Make sure that the user owns the roles required for this operation
    User user = SecurityUtils.getUser();
    if (!SecurityUtils.userHasRole(user, SystemRole.SITEADMIN))
      return Response.status(Status.FORBIDDEN).build();

    // Make sure a role has been provided as part of the request
    if (StringUtils.isBlank(role))
      return Response.status(Status.BAD_REQUEST).build();

    JpaAccount account = null;
    Site site = getSite(request);
    try {
      account = directory.getAccount(site, login);
      if (account == null)
        return Response.status(Status.NOT_FOUND).build();

      if (!account.hasRole(context, role))
        return Response.status(Status.NOT_MODIFIED).build();

      account.removeRole(context, role);
      directory.updateAccount(account);
      return Response.ok().build();
    } catch (Throwable t) {
      logger.warn("Error adding role '{}:{}' to account: {}", new String[] {
          context,
          role,
          login,
          t.getMessage() });
      return Response.serverError().build();
    }
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
      docs = SQLDirectoryProviderEndpointDocs.createDocumentation(servicePath);
    }
    return docs;
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
   * Callback for OSGi to set the site manager.
   * 
   * @param siteManager
   *          the site manager
   */
  void setSiteManager(SiteManager siteManager) {
    this.sites = siteManager;
  }

  /**
   * Callback from the OSGi declarative services environment that will pass in a
   * reference to the directory provider.
   * 
   * @param directoryProvider
   *          the sql directory provider
   */
  void setDiretoryProvider(SQLDirectoryProvider directoryProvider) {
    this.directory = directoryProvider;
  }

  /**
   * {@inheritDoc}
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "SQL directory rest endpoint";
  }

}
