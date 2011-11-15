package ch.entwine.weblounge.kernel.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Set;

public class SpringSecurityUser extends User implements UserDetails {

  private static final long serialVersionUID = -2153871608501320500L;

  private final ch.entwine.weblounge.common.security.User user;

  public SpringSecurityUser(ch.entwine.weblounge.common.security.User user,
      String password, boolean enabled, boolean accountNonExpired,
      boolean credentialsNonExpired, boolean accountNonLocked,
      Set<GrantedAuthority> authorities) {
    super(user.getLogin(), password, enabled, accountNonExpired, credentialsNonExpired, accountNonLocked, authorities);
    this.user = user;
  }

  public ch.entwine.weblounge.common.security.User getUser() {
    return user;
  }

}
