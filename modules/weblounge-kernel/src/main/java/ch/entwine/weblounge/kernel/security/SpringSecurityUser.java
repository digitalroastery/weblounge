package ch.entwine.weblounge.kernel.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Set;

/**
 * Weblounge specific implementation of the Spring Security User Details, that
 * is capable of holding and passing on the weblounge user that has been created
 * during login.
 */
public class SpringSecurityUser extends User {

  /** The serial version uid */
  private static final long serialVersionUID = -2153871608501320500L;

  /** The weblounge user */
  private final ch.entwine.weblounge.common.security.User user;

  /**
   * Creates a new Spring Security
   * {@link org.springframework.security.core.userdetails.UserDetails}.
   * 
   * @param user
   *          the weblounge user
   * @param password
   *          the password
   * @param enabled
   *          <code>true</code> if the account is enabled
   * @param accountNonExpired
   *          <code>true</code> if the account is not expired
   * @param credentialsNonExpired
   *          <code>true</code> if the password is not expired
   * @param accountNonLocked
   *          <code>true</code> if the account is not locked
   * @param authorities
   *          the granted authorities
   */
  public SpringSecurityUser(ch.entwine.weblounge.common.security.User user,
      String password, boolean enabled, boolean accountNonExpired,
      boolean credentialsNonExpired, boolean accountNonLocked,
      Set<GrantedAuthority> authorities) {
    super(user.getLogin(), password, enabled, accountNonExpired, credentialsNonExpired, accountNonLocked, authorities);
    this.user = user;
  }

  /**
   * Returns the weblounge user that has been created during authentication.
   * 
   * @return the weblounge user
   */
  public ch.entwine.weblounge.common.security.User getUser() {
    return user;
  }

}
