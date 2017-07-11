package kr.ac.kaist.mapping.mapping;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import kr.ac.kaist.mapping.mapping.model.FacebookUser;

/**
 * Created by q on 2017-07-10.
 */

public class ListViewAdapter extends BaseAdapter {
  private ArrayList<ListViewItem> listViewItemList = new ArrayList<ListViewItem>();

  public ListViewAdapter(){
  }

  public int getCount() {
    return listViewItemList.size();
  }

  public int getViewTypeCount() {
    return ListViewItem.ITEM_VIEW_TYPES_MAX;
  }

  public int getItemViewType(int position) {
    return listViewItemList.get(position).getType();
  }

  /**
   *  Get view.
   */
  public View getView(int position, View convertView, ViewGroup parent) {
    final Context context = parent.getContext();
    int viewType = getItemViewType(position);

    if (convertView == null) {
      LayoutInflater inflater =
          (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
      ListViewItem listViewItem = listViewItemList.get(position);
      switch (viewType) {
        case ListViewItem.ITEM_VIEW_TYPES_CONTACT:
          convertView = inflater.inflate(R.layout.listview_item, parent, false);
          ImageView icon = (ImageView) convertView.findViewById(R.id.icon);
          TextView name = (TextView) convertView.findViewById(R.id.name);
          TextView email = (TextView) convertView.findViewById(R.id.email);

          icon.setImageDrawable(listViewItem.getIcon());
          name.setText(listViewItem.getName());
          break;
        case ListViewItem.ITEM_VIEW_TYPES_ME:
          convertView = inflater.inflate(R.layout.login_item, parent, false);
          ImageView iconMe = (ImageView) convertView.findViewById(R.id.icon_me);
          TextView nameMe = (TextView) convertView.findViewById(R.id.name_me);
          TextView emailMe = (TextView) convertView.findViewById(R.id.email_me);
          iconMe.setImageDrawable(listViewItem.getIcon());
          nameMe.setText(listViewItem.getName());
          emailMe.setText(listViewItem.getEmail());
          break;
        default:
          break;
      }
    }


    return convertView;
  }

  public long getItemId(int position) {
    return position;
  }

  public Object getItem(int position) {
    return listViewItemList.get(position);
  }

  /**
   *  Add friends' profile.
   */
  public void addItem(Drawable icon, FacebookUser user) {
    ListViewItem item = new ListViewItem();

    item.setIcon(icon);
    item.setName(user.getName());
    item.setType(0);

    listViewItemList.add(item);
    notifyDataSetChanged();
  }

  /**
   *  Add my profile.
   */
  public void addItem_Me(Drawable icon, String name, String email) {
    ListViewItem item = new ListViewItem();

    item.setIcon(icon);
    item.setName(name);
    item.setEmail(email);
    item.setType(1);

    listViewItemList.add(item);
    notifyDataSetChanged();
  }


}
