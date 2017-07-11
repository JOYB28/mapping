package kr.ac.kaist.mapping.mapping.model;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

import java.io.Serializable;
import java.util.Date;

public class Photo implements ClusterItem, Serializable {
  private long id;
  private User user;
  private String image;
  private String desc;
  private Date pub_date;
  private double latitude;
  private double longitude;
  private int grid;
  private boolean is_public;

  public Photo(long id, User user, String image, String desc, Date pub_date, double latitude, double longitude, int grid, boolean isPublic) {
    this.id = id;
    this.user = user;
    this.image = image;
    this.desc = desc;
    this.pub_date = pub_date;
    this.latitude = latitude;
    this.longitude = longitude;
    this.grid = grid;
    this.is_public = isPublic;
  }

  public long getId() {
    return id;
  }

  public User getUser() {
    return user;
  }

  public String getImage() {
    return image;
  }

  public String getDesc() {
    return desc;
  }

  public Date getPubDate() {
    return pub_date;
  }

  public double getLongitude() {
    return longitude;
  }

  public double getLatitude() {
    return latitude;
  }

  public int getGrid() {
    return grid;
  }

  public boolean isPublic() {
    return is_public;
  }

  @Override
  public LatLng getPosition() {
    return new LatLng(latitude, longitude);
  }
}
