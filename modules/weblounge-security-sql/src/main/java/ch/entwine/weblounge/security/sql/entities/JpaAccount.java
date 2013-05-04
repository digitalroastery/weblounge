package ch.entwine.weblounge.security.sql.entities;

import ch.entwine.weblounge.common.language.Language;

import org.apache.commons.lang.StringUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

/**
 * A user account that enables login of a Weblounge user into a specific sites
 * and at the same time ties in the roles that the user is being assigned.
 */
@Entity
@Table(name = "directory_account", uniqueConstraints = @UniqueConstraint(columnNames = { "login", "site_id" }))
@NamedQueries({
    @NamedQuery(name = "getAccount", query = "SELECT a FROM JpaAccount a WHERE a.site.name = :siteId AND a.login = :userId"),
    @NamedQuery(name = "getActiveAccount", query = "SELECT a FROM JpaAccount a WHERE a.site.name = :siteId AND a.login = :userId AND a.enabled = true and a.site.enabled = true"),
    @NamedQuery(name = "getAccounts", query = "SELECT a FROM JpaAccount a WHERE a.site.name = :siteId") })
public class JpaAccount implements Serializable {

  /** Serial version UID */
  private static final long serialVersionUID = -3709284981425807732L;

  @Id
  @GeneratedValue
  protected long id;

  /** The site that this user account belongs to */
  @ManyToOne
  @Column(name = "site_id", nullable = false)
  protected JpaSite site = null;

  /** The user login */
  @Column(name = "login", nullable = false)
  protected String login = null;

  /** The user's first name */
  protected String firstname = null;

  /** The user's last name */
  protected String lastname = null;

  /** The user's password hash */
  protected String password = null;

  /** The user's initials */
  protected String initials = null;

  /** The user's e-mail address */
  protected String email = null;

  /** The activation code */
  protected String activationCode = null;

  /** The challenge */
  protected String challenge = null;

  /** The response to the challenge */
  protected String response = null;

  /** The preferred language */
  protected String language = null;

  /** The user roles */
  @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
  protected Collection<JpaRole> roles = null;

  /** True if this account is enabled */
  protected boolean enabled = true;

  /** The date this account was last used */
  protected Date lastLoginDate = null;

  /** The IP that was used during the last login */
  protected String lastLoginIP = null;

  /**
   * No argument constructor required by OpenJPA.
   */
  JpaAccount() {
    super();
  }

  /**
   * Creates a new user account for the given user in the specified site.
   * 
   * @param site
   *          the site
   * @param login
   *          the user name
   * @param password
   *          the password
   */
  public JpaAccount(JpaSite site, String login, String password) {
    this.site = site;
    this.login = login;
    this.password = password;
  }

  /**
   * Returns the site.
   * 
   * @return the site
   */
  public JpaSite getSite() {
    return site;
  }

  /**
   * Returns the login name.
   * 
   * @return the login
   */
  public String getLogin() {
    return login;
  }

  /**
   * Returns the user's first name or <code>null</code> if no first name has
   * been specified.
   * 
   * @return the first name
   */
  public String getFirstname() {
    return firstname;
  }

  /**
   * Sets the user's first name.
   * 
   * @param firstname
   *          the first name
   */
  public void setFirstname(String firstname) {
    this.firstname = firstname;
  }

  /**
   * Returns the user's last name or <code>null</code> if no last name has been
   * specified.
   * 
   * @return the last name
   */
  public String getLastname() {
    return lastname;
  }

  /**
   * Sets the user's last name.
   * 
   * @param lastname
   *          the last name
   */
  public void setLastname(String lastname) {
    this.lastname = lastname;
  }

  /**
   * Returns the user's password or <code>null</code> if no password has been
   * specified.
   * 
   * @return the password
   */
  public String getPassword() {
    return password;
  }

  /**
   * Sets the password.
   * 
   * @param password
   *          the password to set
   */
  public void setPassword(String password) {
    this.password = password;
  }

  /**
   * Returns the user's initials or <code>null</code> if no initials have been
   * specified.
   * 
   * @return the initials
   */
  public String getInitials() {
    return initials;
  }

  /**
   * Sets the user's initials.
   * 
   * @param initials
   *          the initials
   */
  public void setInitials(String initials) {
    this.initials = initials;
  }

  /**
   * Returns the user's e-mail or <code>null</code> if no e-mail address has
   * been specified.
   * 
   * @return the email address
   */
  public String getEmail() {
    return email;
  }

  /**
   * Sets the user's e-mail address.
   * 
   * @param email
   *          the email address
   */
  public void setEmail(String email) {
    this.email = email;
  }

  /**
   * Returns the user's preferred language.
   * 
   * @return the language
   */
  public String getLanguage() {
    return language;
  }

  /**
   * Sets the user's preferred language.
   * 
   * @param language
   *          the language
   */
  public void setLanguage(Language language) {
    this.language = language.getIdentifier();
  }

  /**
   * Enables or disables login into this account.
   * 
   * @param enabled
   *          <code>true</code> to allow login into this account
   */
  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  /**
   * Returns <code>true</code> if this account can be logged into.
   * 
   * @return <code>true</code> if the account is enabled
   */
  public boolean isEnabled() {
    return enabled;
  }

  /**
   * Sets the challenge, which is a question that can be posed to wannabe
   * account owners if they forget their password.
   * 
   * @param challenge
   *          the challenge
   */
  public void setChallenge(String challenge) {
    this.challenge = challenge;
  }

  /**
   * Returns the challenge used to verify that the person asking to reset the
   * password actually is the account owner.
   * 
   * @return the challenge
   */
  public String getChallenge() {
    return challenge;
  }

  /**
   * Sets the response, which is the response to the question that can be posed
   * to wannabe account owners if they forget their password.
   * 
   * @param response
   *          the challenge
   */
  public void setResponse(String response) {
    this.response = response;
  }

  /**
   * Returns the response to the challenge used to verify that the person asking
   * to reset the password actually is the account owner.
   * 
   * @return the response
   */
  public String getResponse() {
    return response;
  }

  /**
   * Sets the activation code that is used to verify a new account owner's
   * e-mail address.
   * 
   * @param code
   *          the activation code
   */
  public void setActivationCode(String code) {
    this.activationCode = code;
  }

  /**
   * Returns the activation code that is used to verify a new account owner's
   * e-mail address.
   * 
   * @return the activation code
   */
  public String getActivationCode() {
    return activationCode;
  }

  /**
   * Sets the date where this account has been accessed.
   * 
   * @param date
   *          the date
   */
  public void setLastLoginDate(Date date) {
    this.lastLoginDate = date;
  }

  /**
   * Returns the date where this account has been accessed for the last time or
   * <code>null</code> if the account has never been accessed.
   * 
   * @return the date of the last login
   */
  public Date getLastLoginDate() {
    return lastLoginDate;
  }

  /**
   * Sets the IP address where this account has been accessed from.
   * 
   * @param ip
   *          the IP address
   */
  public void setLastLoginFrom(String ip) {
    this.lastLoginIP = ip;
  }

  /**
   * Returns the IP address where this account has been accessed from last time
   * or <code>null</code> if the account has never been accessed.
   * 
   * @return the IP address used to login in
   */
  public String getLastLoginFrom() {
    return this.lastLoginIP;
  }

  /**
   * Adds the role to the list of roles that are assigned to this account.
   * 
   * @param context
   *          the role context
   * @param role
   *          the role name
   */
  public void addRole(String context, String role) {
    if (roles == null)
      roles = new ArrayList<JpaRole>();
    this.roles.add(new JpaRole(this, context, role));
  }

  /**
   * Removes the given role from the list of roles that are assigned to this
   * account.
   * 
   * @param context
   *          the role context
   * @param role
   *          the role
   */
  public void removeRole(String context, String role) {
    if (roles == null)
      return;
    if (StringUtils.isBlank(context))
      throw new IllegalArgumentException("Role context must not be blank");
    if (StringUtils.isBlank(role))
      throw new IllegalArgumentException("Role name must not be blank");
    Iterator<JpaRole> ri = roles.iterator();
    while (ri.hasNext()) {
      JpaRole r = ri.next();
      if (context.equals(r.getContext()) && role.equals(r.getRolename())) {
        ri.remove();
        return;
      }
    }
  }

  /**
   * Returns the roles that are assigned to this user account.
   * 
   * @return
   */
  public Collection<JpaRole> getRoles() {
    return roles;
  }

  /**
   * Returns <code>true</code> if the user account has been given the role.
   * 
   * @param context
   *          the role context
   * @param role
   *          the role name
   * @return <code>true</code> if this account owns the role
   * @throws IllegalArgumentException
   *           if either one of <code>context</code> or <code>role</code> is
   *           blank
   */
  public boolean hasRole(String context, String role) {
    if (StringUtils.isBlank(context))
      throw new IllegalArgumentException("Role context must not be null");
    if (StringUtils.isBlank(role))
      throw new IllegalArgumentException("Role name must not be null");
    for (JpaRole r : roles) {
      if (context.equals(r.getContext()) && role.equals(r.getRolename()))
        return true;
    }
    return false;
  }

}
