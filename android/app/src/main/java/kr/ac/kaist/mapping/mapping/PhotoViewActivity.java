package kr.ac.kaist.mapping.mapping;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import kr.ac.kaist.mapping.mapping.model.Photo;

public class PhotoViewActivity extends AppCompatActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_photo_view);

    Intent intent = getIntent();
    String photoHolderId = intent.getStringExtra("photoHolderId");
    String bitmapHolderId = intent.getStringExtra("bitmapHolderId");

    final Photo photo = (Photo) DataHolder.popDataHolder(photoHolderId);
    final Bitmap bitmap = (Bitmap) DataHolder.popDataHolder(bitmapHolderId);

    final TextView txtPhotoViewTitle = (TextView) findViewById(R.id.txtPhotoViewTitle);
    final ImageView imgView = (ImageView) findViewById(R.id.imgView);
    final TextView txtPhotoViewDesc = (TextView) findViewById(R.id.txtPhotoDesc);

    txtPhotoViewTitle.setText(photo.getUser().getUsername());
    imgView.setImageBitmap(bitmap);
    txtPhotoViewDesc.setText(photo.getDesc()
        + " (" + new SimpleDateFormat("yyyy-MM-dd").format(photo.getPubDate()) + ")");
  }
}
