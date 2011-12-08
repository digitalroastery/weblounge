package ch.entwine.weblounge.kernel.security;

import ch.entwine.weblounge.common.security.User;
import ch.entwine.weblounge.common.url.PathUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.SimpleUrlLogoutSuccessHandler;

import java.io.IOException;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class WebloungeLogoutSuccessHandler extends SimpleUrlLogoutSuccessHandler {

  /** The logging facility */
  private static Logger logger = LoggerFactory.getLogger(RoleBasedLoginSuccessHandler.class);

  /** Parameter name for the path to got to after logout */
  private static final String PATH_PARAMETER_NAME = "path";

  /**
   * {@inheritDoc}
   * 
   * @see org.springframework.security.web.authentication.logout.SimpleUrlLogoutSuccessHandler#onLogoutSuccess(javax.servlet.http.HttpServletRequest,
   *      javax.servlet.http.HttpServletResponse,
   *      org.springframework.security.core.Authentication)
   */
  @Override
  public void onLogoutSuccess(HttpServletRequest request,
      HttpServletResponse response, Authentication authentication)
      throws IOException, ServletException {

    String targetUrl = "/";
    if (request.getParameter(PATH_PARAMETER_NAME) != null) {
      targetUrl = PathUtils.concat("/", request.getParameter(PATH_PARAMETER_NAME));
    }

    // Authentication can be null, e. g. if a user presses "logout" even though
    // his session has already been expired
    if (authentication != null) {
      Object principal = authentication.getPrincipal();
      if (!(principal instanceof SpringSecurityUser)) {
        User user = ((SpringSecurityUser) principal).getUser();
        logger.info("{} logged out", user);
      } else {
        logger.info("{} logged out", authentication.getName());
      }
    }
    setDefaultTargetUrl(addTimeStamp(targetUrl));
    super.onLogoutSuccess(request, response, authentication);
  }

  /**
   * Add a timestamp parameter to the url location
   * 
   * @param location
   *          the url
   * @return the page with a timestamp
   */
  private String addTimeStamp(String location) {
    long timeStamp = new Date().getTime();
    if (location.contains("?")) {
      return location.concat("&_=" + timeStamp);
    } else {
      return location.concat("?_=" + timeStamp);
    }
  }

}
