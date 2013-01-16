package ch.entwine.weblounge.security.sql.entities;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

/**
 * Implements a user's role owned as part of a site's user account.
 */
@Entity
@Table(name = "directory_role")
public class JpaRole implements Serializable {

  /** Serial version UID */
  private static final long serialVersionUID = -5479197872071441914L;

  @OneToOne
  @JoinColumn(name = "user_account_id")
  private JpaAccount userAccount = null;

  private String context = null;
  private String rolename = null;

  public JpaRole() {
    super();
  }

  /**
   * Returns the user account.
   * 
   * @return the account
   */
  public JpaAccount getUserAccount() {
    return userAccount;
  }

  /**
   * Returns the role context.
   * 
   * @return the context
   */
  public String getContext() {
    return context;
  }

  /**
   * Returns the role identifier.
   * 
   * @return the role identifier
   */
  public String getRolename() {
    return rolename;
  }

  /**
   * {@inheritDoc}
   * 
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    return rolename.hashCode();
  }

  /**
   * {@inheritDoc}
   * 
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof JpaRole))
      return false;
    JpaRole r = (JpaRole) obj;
    return context.equals(r.context) && rolename.equals(r.rolename);
  }

  /**
   * {@inheritDoc}
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return context + ":" + rolename;
  }

}
