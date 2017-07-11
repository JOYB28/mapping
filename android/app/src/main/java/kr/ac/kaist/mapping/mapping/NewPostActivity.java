package kr.ac.kaist.mapping.mapping;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import java.io.FileNotFoundException;
import java.io.IOException;

public class NewPostActivity extends AppCompatActivity {
  final int reqCodeSelectImage = 100;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_new_post);

    /* Return to main page */
    ImageButton cancel = (ImageButton) findViewById(R.id.cancel_button);
    cancel.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        onBackPressed();
      }
    });

    /* Post a new photo to the server */
    ImageButton post = (ImageButton) findViewById(R.id.post_button);
    post.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        // TODO: Implement server communication
      }
    });

    final CheckedTextView isPublic = (CheckedTextView) findViewById(R.id.isPublic);
    isPublic.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        String value;
        if (isPublic.isChecked()) {
          value = "only friends";
          isPublic.setCheckMarkDrawable(R.drawable.blank);
          isPublic.setChecked(false);
        } else {
          value = "public";
          isPublic.setCheckMarkDrawable(R.drawable.check);
          isPublic.setChecked(true);
        }
        Toast.makeText(getBaseContext(),value, Toast.LENGTH_SHORT).show();
      }
    });

    Button photo;
    photo = (Button) findViewById(R.id.gallery);
    photo.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType(android.provider.MediaStore.Images.Media.CONTENT_TYPE);
        intent.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, reqCodeSelectImage);
      }
    });



  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    Toast.makeText(getBaseContext(), "resultCode : " + resultCode,Toast.LENGTH_SHORT).show();

    if (requestCode == reqCodeSelectImage) {
      if (resultCode == Activity.RESULT_OK) {
        try {
          //Uri에서 이미지 이름을 얻어온다.
          //String name_Str = getImageNameToUri(data.getData());

          //이미지 데이터를 비트맵으로 받아온다.
          Bitmap imageBitmap = android.provider.MediaStore.Images.Media
              .getBitmap(getContentResolver(), data.getData());
          ImageView image = (ImageView)findViewById(R.id.image);

          image.setImageBitmap(imageBitmap);

          //Toast.makeText(getBaseContext(), "name_Str : "+name_Str , Toast.LENGTH_SHORT).show();
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
}
