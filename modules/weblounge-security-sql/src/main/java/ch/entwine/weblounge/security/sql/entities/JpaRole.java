package ch.entwine.weblounge.security.sql.entities;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
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

  @OneToOne(orphanRemoval = true)
  private JpaAccount account = null;

  /** The role context */
  @Column(nullable = false)
  private String context = null;

  /** The role name */
  @Column(nullable = false)
  private String rolename = null;

  /**
   * No argument constructor required by OpenJPA.
   */
  JpaRole() {
    super();
  }

  /**
   * Creates a new role with the given context.
   * 
   * @param account
   *          the account that this role belongs to
   * @param context
   *          the context
   * @param role
   *          the role
   */
  public JpaRole(JpaAccount account, String context, String role) {
    this.account = account;
    this.context = context;
    this.rolename = role;
  }

  /**
   * Returns the user account.
   * 
   * @return the account
   */
  public JpaAccount getAccount() {
    return account;
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
