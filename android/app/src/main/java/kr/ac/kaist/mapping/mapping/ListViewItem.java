package kr.ac.kaist.mapping.mapping;

import android.graphics.drawable.Drawable;

/**
 * Created by q on 2017-07-10.
 */

public class ListViewItem {
  public static final int ITEM_VIEW_TYPES_CONTACT = 0;
  public static final int ITEM_VIEW_TYPES_ME = 1;
  public static final int ITEM_VIEW_TYPES_MAX = 2;

  private int type;
  private Drawable icon;
  private String name;
  private String email;

  public void setType(int type) {
    this.type = type;
  }

  public void setIcon(Drawable icon) {
    this.icon = icon;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public int getType() {
    return this.type;
  }

  public Drawable getIcon() {
    return this.icon;
  }

  public String getName() {
    return this.name;
  }

  public String getEmail() {
    return this.email;
  }

}
