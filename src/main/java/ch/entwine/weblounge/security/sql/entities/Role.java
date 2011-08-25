package ch.entwine.weblounge.security.sql.entities;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

/**
 * Entity implementation class for Entity: Role
 * 
 */
@Entity
@Table(name = "user_account_roles")
public class Role implements Serializable {

  private static final long serialVersionUID = -5479197872071441914L;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private int id = 0;

  private int userAccountId = 0;

  @OneToOne
  @JoinColumn(name = "user_account_id")
  private UserAccount userAccount = null;

  private String context = null;
  private String rolename = null;

  public Role() {
    super();
  }

  /**
   * @return the id
   */
  public int getId() {
    return id;
  }

  /**
   * @return the userAccount
   */
  public UserAccount getUserAccount() {
    return userAccount;
  }

  /**
   * @return the context
   */
  public String getContext() {
    return context;
  }

  /**
   * @return the rolename
   */
  public String getRolename() {
    return rolename;
  }

}
