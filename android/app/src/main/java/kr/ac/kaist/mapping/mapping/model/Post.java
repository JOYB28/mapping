package kr.ac.kaist.mapping.mapping.model;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

import java.util.Date;

public class Post implements ClusterItem {
  private final int photo;
  private final String writer;
  private final LatLng position;
  private final String content;
  private final Date date;

  /**
   * Constructor for Post.
   *
   * @param position position
   * @param writer author of the post
   * @param content content of the post
   * @param pictureResource resource id for the picture of the post
   * @param date publish date
   */
  public Post(LatLng position, String writer, String content, int pictureResource, Date date) {
    this.writer = writer;
    this.photo = pictureResource;
    this.position = position;
    this.content = content;
    this.date = date;
  }

  public int getPhoto() {
    return photo;
  }

  public String getWriter() {
    return writer;
  }

  @Override
  public LatLng getPosition() {
    return position;
  }

  public String getContent() {
    return content;
  }

  public Date getDate() {
    return date;
  }
}
