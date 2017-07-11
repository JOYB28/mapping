package kr.ac.kaist.mapping.mapping.model;

import java.io.Serializable;

public class User implements Serializable {
  private long id;
  private String username;
  private String email;

  /**
   * Constructor for {@link User}.
   *
   * @param id id
   * @param username username
   * @param email email
   */
  public User(long id, String username, String email) {
    this.id = id;
    this.username = username;
    this.email = email;
  }

  public long getId() {
    return id;
  }

  public String getUsername() {
    return username;
  }

  public String getEmail() {
    return email;
  }
}
