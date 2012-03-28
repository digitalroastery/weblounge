package ch.entwine.weblounge.security.sql.entities;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * Entity implementation class for Entity: Site
 * 
 */
@Entity
public class Site implements Serializable {

  private static final long serialVersionUID = 4773713282301469466L;

  /* Private fields */
  private String id = null;
  private String name = null;

  public Site() {
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
   * @return the name
   */
  public String getName() {
    return name;
  }

  /**
   * @param name
   *          the name to set
   */
  public void setName(String name) {
    this.name = name;
  }

}
