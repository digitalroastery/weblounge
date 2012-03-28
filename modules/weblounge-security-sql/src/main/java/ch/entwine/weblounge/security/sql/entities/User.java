package ch.entwine.weblounge.security.sql.entities;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Entity implementation class for Entity: User
 * 
 */
@Entity
@Table(name = "users")
public class User implements Serializable {

  private static final long serialVersionUID = -7832991492009113260L;

  /* Private fields */
  private String id = null;
  private String firstname = null;
  private String lastname = null;
  private String passwordHash = null;
  private String initials = null;
  private String email = null;

  public User() {
    super();
  }

  /**
   * @return the id
   */
  @Id
  public String getId() {
    return id;
  }

  /**
   * @param id
   *          the id to set
   */
  public void setId(String id) {
    this.id = id;
  }

  /**
   * @return the firstname
   */
  public String getFirstname() {
    return firstname;
  }

  /**
   * @param firstname
   *          the firstname to set
   */
  public void setFirstname(String firstname) {
    this.firstname = firstname;
  }

  /**
   * @return the lastname
   */
  public String getLastname() {
    return lastname;
  }

  /**
   * @param lastname
   *          the lastname to set
   */
  public void setLastname(String lastname) {
    this.lastname = lastname;
  }

  /**
   * @return the passwordHash
   */
  public String getPasswordHash() {
    return passwordHash;
  }

  /**
   * @param passwordHash
   *          the passwordHash to set
   */
  public void setPasswordHash(String passwordHash) {
    this.passwordHash = passwordHash;
  }

  /**
   * @return the initials
   */
  public String getInitials() {
    return initials;
  }

  /**
   * @param initials
   *          the initials to set
   */
  public void setInitials(String initials) {
    this.initials = initials;
  }

  /**
   * @return the email
   */
  public String getEmail() {
    return email;
  }

  /**
   * @param email
   *          the email to set
   */
  public void setEmail(String email) {
    this.email = email;
  }

}
