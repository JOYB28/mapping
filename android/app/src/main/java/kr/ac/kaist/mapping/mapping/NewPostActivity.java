package kr.ac.kaist.mapping.mapping;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Date;

import kr.ac.kaist.mapping.mapping.model.Photo;

public class NewPostActivity extends AppCompatActivity {
  private static final int REQ_CODE_SELECT_IMAGE = 100;

  private String imgPath;
  private String imgData;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_new_post);

    /* Return to main page */
    ImageButton btnCancel = (ImageButton) findViewById(R.id.btnCancel);
    btnCancel.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        onBackPressed();
      }
    });

    final EditText txtDesc = (EditText) findViewById(R.id.txtDesc);
    final CheckedTextView btnIsPublic = (CheckedTextView) findViewById(R.id.btnIsPublic);
    btnIsPublic.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        String value;
        if (btnIsPublic.isChecked()) {
          value = "only friends";
          btnIsPublic.setCheckMarkDrawable(R.drawable.blank);
          btnIsPublic.setChecked(false);
        } else {
          value = "public";
          btnIsPublic.setCheckMarkDrawable(R.drawable.check);
          btnIsPublic.setChecked(true);
        }
        Toast.makeText(getBaseContext(),value, Toast.LENGTH_SHORT).show();
      }
    });

    /* Post a new photo to the server */
    ImageButton btnPost = (ImageButton) findViewById(R.id.btnPost);
    btnPost.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        if (imgData == null) {
          new AlertDialog.Builder(NewPostActivity.this)
              .setTitle(R.string.img_not_selected_warning_title)
              .setMessage(R.string.img_not_selected_warning_msg)
              .setPositiveButton(R.string.img_not_selected_warning_confirm,
                  new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                      moveToGallery();
                    }
                  })
              .setNegativeButton(R.string.img_not_selected_warning_cancel, null)
              .show();
        }

        LatLng location = GpsInfo.getInstance(NewPostActivity.this).getLocation();
        final Photo photo = new Photo(0, null, imgData, txtDesc.getText().toString(), new Date(),
            (double) Math.round(location.latitude * 1000) / 1000,
            (double) Math.round(location.longitude * 1000) / 1000, 0, btnIsPublic.isChecked());
        ApiManager.getInstance(NewPostActivity.this).postPhoto(photo, new ApiManager.Callback() {
          @Override
          public void onSuccess(Object obj) {
            Intent intent = new Intent(NewPostActivity.this, MainActivity.class);
            String photoHolderId = DataHolder.putDataHolder(photo);
            intent.putExtra("photoHolderId", photoHolderId);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
          }

          @Override
          public void onFailure() {

          }
        });
      }
    });

    Button btnGallery;
    btnGallery = (Button) findViewById(R.id.btnGallery);
    btnGallery.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        moveToGallery();
      }
    });
  }

  public Uri getImageUri(Context inContext, Bitmap inImage) {
    Log.d("asdf", "asdf");
    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
    Log.d("asdf", "asdf");
    inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
    Log.d("asdf", "asdf");
    String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "temp.jpg", null);
    Log.d("asdf", path);
    return Uri.parse(path);
  }

  public String getRealPathFromURI(Uri uri) {
    Cursor cursor = getContentResolver().query(uri, null, null, null, null);
    cursor.moveToFirst();
    int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
    String asdf = cursor.getString(idx);
    Log.d("asdf", asdf);

    return asdf;
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    Toast.makeText(getBaseContext(), "resultCode : " + resultCode,Toast.LENGTH_SHORT).show();

    if (requestCode == REQ_CODE_SELECT_IMAGE) {
      if (resultCode == Activity.RESULT_OK) {
        try {
          //이미지 데이터를 비트맵으로 받아온다.
          Bitmap bitmap = android.provider.MediaStore.Images.Media
              .getBitmap(getContentResolver(), data.getData());
          ImageView imgSelected = (ImageView) findViewById(R.id.imgSelected);

          imgSelected.setImageBitmap(bitmap);

          //Uri tempUri = getImageUri(getApplicationContext(), image);
          //imgPath = getRealPathFromURI(tempUri);
          ByteArrayOutputStream baos = new ByteArrayOutputStream();
          bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
          byte[] imageBytes = baos.toByteArray();
          imgData = Base64.encodeToString(imageBytes, Base64.DEFAULT);
        } catch (FileNotFoundException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        } catch (IOException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    }
  }

  @Override
  public void onBackPressed() {
    super.onBackPressed();
    Intent intent = new Intent(NewPostActivity.this, MainActivity.class);
    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
    startActivity(intent);
  }

  private void moveToGallery() {
    Intent intent = new Intent(Intent.ACTION_PICK);
    intent.setType(android.provider.MediaStore.Images.Media.CONTENT_TYPE);
    intent.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
    startActivityForResult(intent, REQ_CODE_SELECT_IMAGE);
  }
}
