package ch.entwine.weblounge.security.sql.entities;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Database entity SiteUser:
 */
@Entity
@Table(name = "user_accounts")
public class UserAccount implements Serializable {

  private static final long serialVersionUID = -3709284981425807732L;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private int id = 0;

  private String siteId = null;

  private String userId = null;
  private boolean enabled = true;
  private Date lastLoginDate = null;
  private String lastLoginIP = null;

  public UserAccount() {
    super();
  }

}
