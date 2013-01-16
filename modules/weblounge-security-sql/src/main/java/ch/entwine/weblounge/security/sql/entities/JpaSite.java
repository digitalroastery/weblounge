package ch.entwine.weblounge.security.sql.entities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;

/**
 * Representation of a site in the directory provider.
 */
@Entity
@Table(name = "directory_site")
@NamedQueries({ @NamedQuery(name = "getSiteByName", query = "SELECT s FROM JpaSite s WHERE s.name = :site"), })
public class JpaSite implements Serializable {

  /** The serial version UID */
  private static final long serialVersionUID = 4773713282301469466L;

  @Id
  @GeneratedValue
  protected long siteId;
  
  @Column(unique = true, nullable = false)
  protected String name = null;

  /** True if logins into this site are enabled */
  protected boolean enabled = true;

  @OneToMany(mappedBy = "site", fetch = FetchType.LAZY, orphanRemoval = true)
  protected Collection<JpaAccount> accounts = null;

  /**
   * No argument constructor required by Open JPA.
   */
  JpaSite() {
    super();
  }

  /**
   * Creates a new database representation for the given site.
   * 
   * @param site
   *          the site identifier
   */
  public JpaSite(String site) {
    this.name = site;
  }

  /**
   * Returns the site identifier.
   * 
   * @return the site identifier
   */
  public String getName() {
    return name;
  }

  /**
   * Activates or deactivates login into this site.
   * 
   * @param enabled
   *          <code>true</code> to activate login into this site
   */
  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  /**
   * Returns <code>true</code> if the site is currently accepting logins.
   * 
   * @return <code>true</code> if logins for this site are enabled
   */
  public boolean isEnabled() {
    return enabled;
  }

  /**
   * Adds a user account to the site.
   * 
   * @param account
   *          the new account
   */
  public void addAccount(JpaAccount account) {
    if (accounts == null)
      accounts = new ArrayList<JpaAccount>();
    accounts.add(account);
  }

  /**
   * Removes the user account from the site.
   * 
   * @param account
   *          the account to remove
   */
  public void removeAccount(JpaAccount account) {
    if (accounts == null)
      return;
    accounts.remove(account);
  }

  /**
   * {@inheritDoc}
   * 
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    return name.hashCode();
  }

  /**
   * {@inheritDoc}
   * 
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof JpaSite))
      return false;
    return name.equals(((JpaSite) obj).name);
  }
  
  /**
   * {@inheritDoc}
   *
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return name;
  }

}
