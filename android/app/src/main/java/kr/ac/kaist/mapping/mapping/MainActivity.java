package kr.ac.kaist.mapping.mapping;

import android.Manifest;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterItem;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.google.maps.android.ui.IconGenerator;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import kr.ac.kaist.mapping.mapping.model.Post;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback,
    ClusterManager.OnClusterClickListener<Post>,
    ClusterManager.OnClusterInfoWindowClickListener<Post>,
    ClusterManager.OnClusterItemClickListener<Post>,
    ClusterManager.OnClusterItemInfoWindowClickListener<Post> {
  private ClusterManager<Post> clusterManager;
  private CallbackManager callbackManager;

  private GoogleMap map;

  // Examples -- to be deleted later
  private static final LatLng Jinri = new LatLng(36.374687, 127.359165);
  private static final LatLng Sejong = new LatLng(36.371422, 127.366625);
  private static final LatLng Mir = new LatLng(36.370540, 127.355763);
  private static final LatLng Arum = new LatLng(36.373812, 127.356680);


  private static final int PERMISSION_REQUEST_CODE = 0;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    requestPermissions();

    ListViewAdapter adapter = new ListViewAdapter();
    ListView contactList = (ListView) findViewById(R.id.listContact);
    contactList.setAdapter(adapter);

    /* Facebook login process */
    callbackManager = CallbackManager.Factory.create();
    LoginButton loginButton = (LoginButton) findViewById(R.id.btnLogin);
    loginButton.setReadPermissions("email");
    loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
      @Override
      public void onSuccess(LoginResult loginResult) {
        Toast.makeText(MainActivity.this, "Successfully logged in", Toast.LENGTH_SHORT).show();
        Log.d("facebook", loginResult.getAccessToken().getToken());
      }

      @Override
      public void onCancel() {
        Toast.makeText(MainActivity.this, "Login canceled", Toast.LENGTH_SHORT).show();
      }

      @Override
      public void onError(FacebookException error) {
        Toast.makeText(MainActivity.this, "Error during login", Toast.LENGTH_SHORT).show();
      }
    });

    /* Mock data */
    adapter.addItem_Me(ContextCompat.getDrawable(this, R.drawable.contact),
        "KimYoonseo", "kimyoonseo@kaist.ac.kr");
    adapter.addItem(ContextCompat.getDrawable(this, R.drawable.contact),
        "KimYoonseo", "kimyoonseo@kaist.ac.kr");
    adapter.addItem(ContextCompat.getDrawable(this, R.drawable.contact),
        "KimYoonseo", "kimyoonseo@kaist.ac.kr");
    adapter.addItem(ContextCompat.getDrawable(this, R.drawable.contact),
        "KimYoonseo", "kimyoonseo@kaist.ac.kr");
    adapter.addItem(ContextCompat.getDrawable(this, R.drawable.contact),
        "KimYoonseo", "kimyoonseo@kaist.ac.kr");
    adapter.addItem(ContextCompat.getDrawable(this, R.drawable.contact),
        "KimYoonseo", "kimyoonseo@kaist.ac.kr");
    adapter.addItem(ContextCompat.getDrawable(this, R.drawable.contact),
        "KimYoonseo", "kimyoonseo@kaist.ac.kr");

    /* Contact button */
    ImageButton contactButton = (ImageButton) findViewById(R.id.btnContact);
    contactButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {

      }
    });

    /* Post button */
    ImageButton postBtn = (ImageButton) findViewById(R.id.post_button);
    postBtn.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Intent intent = new Intent(MainActivity.this, NewPostActivity.class);
        startActivity(intent);
      }
    });

    /* Move to current location */
    FloatingActionButton btnCurrentLocation =
        (FloatingActionButton) findViewById(R.id.btnCurrentLocation);
    btnCurrentLocation.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        moveToCurrentLocation();
      }
    });

    /* Set map */
    FragmentManager fragmentManager = getFragmentManager();
    MapFragment mapFragment = (MapFragment) fragmentManager.findFragmentById(R.id.fragMap);
    mapFragment.getMapAsync(this);
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    callbackManager.onActivityResult(requestCode, resultCode, data);
  }

  @Override
  public void onMapReady(final GoogleMap map) {
    this.map = map;

    LatLng location = GpsInfo.getInstance(MainActivity.this).getLocation();
    CameraPosition cp = new CameraPosition.Builder().target((location)).zoom(15).build();
    map.animateCamera(CameraUpdateFactory.newCameraPosition(cp));

    clusterManager = new ClusterManager<Post>(this, this.map);
    clusterManager.setRenderer(new PostRenderer());
    clusterManager.setOnClusterClickListener(this);
    clusterManager.setOnClusterInfoWindowClickListener(this);
    clusterManager.setOnClusterItemClickListener(this);
    clusterManager.setOnClusterItemInfoWindowClickListener(this);
    map.setOnCameraChangeListener(clusterManager);
    map.setOnMarkerClickListener(clusterManager);
    map.setOnInfoWindowClickListener(clusterManager);

    addPhotos();
    clusterManager.cluster();
  }

  @Override
  public boolean onClusterClick(Cluster<Post> cluster) {
    LatLngBounds.Builder builder = LatLngBounds.builder();
    for (ClusterItem item : cluster.getItems()) {
      builder.include(item.getPosition());
    }
    final LatLngBounds bounds = builder.build();

    map.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100));
    return true;
  }

  public void onClusterInfoWindowClick(Cluster<Post> cluster) {
    // Does nothing, but you could go to a list of the users.
  }

  public boolean onClusterItemClick(Post item) {
    // Does nothing, but you could go into the user's profile page, for example.
    return false;
  }

  public void onClusterItemInfoWindowClick(Post item) {
    // Does nothing, but you could go into the user's profile page, for example.
  }

  private void addPhotos() {
    clusterManager.addItem(new Post(Jinri, "Yoonseo", "AAA", R.drawable.jinri, new Date()));
    clusterManager.addItem(new Post(Arum, "Girl", "BBB", R.drawable.arum, new Date()));
    clusterManager.addItem(new Post(Mir, "Boy", "CCC", R.drawable.mir, new Date()));
    clusterManager.addItem(new Post(Sejong, "Anotehr girl", "DDD",
        R.drawable.sejong, new Date()));
  }

  /**
   * If outgoing calls permission is not granted, shows a dialog
   * to make user would grant it.
   */
  private void requestPermissions() {
    if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
        != PackageManager.PERMISSION_GRANTED) {
      ActivityCompat.requestPermissions(this,
          new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
          PERMISSION_REQUEST_CODE);
    }
  }

  @Override
  public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                         int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    switch (requestCode) {
      case PERMISSION_REQUEST_CODE:
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
          onPermissionGranted();
        } else {
          onPermissionDenied();
        }
        break;
      default:
        return;
    }
  }

  /**
   * If permissions are granted, do rest of jobs for initialization.
   */
  private void onPermissionGranted() {
    onMapReady(map);
  }

  /**
   * If permissions are denied, finishs the app and then moves
   * to the settings to configure permission by hands.
   */
  private void onPermissionDenied() {
    AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
    dialogBuilder.setTitle(R.string.permission_denied_title);
    dialogBuilder.setMessage(R.string.permission_denied_msg);
    dialogBuilder.setCancelable(false);
    dialogBuilder.setPositiveButton(R.string.permission_denied_confirm,
        new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            dialog.dismiss();
            final Intent intent = new Intent();
            intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.addCategory(Intent.CATEGORY_DEFAULT);
            intent.setData(Uri.parse("package:" + MainActivity.this.getPackageName()));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
            MainActivity.this.startActivity(intent);
            finishAndRemoveTask();
          }
        });
    dialogBuilder.show();
  }

  private void moveToCurrentLocation() {
    LatLng location = GpsInfo.getInstance(MainActivity.this).getLocation();
    CameraPosition cp = new CameraPosition.Builder().target((location)).zoom(15).build();
    map.animateCamera(CameraUpdateFactory.newCameraPosition(cp));
  }

  private class PostRenderer extends DefaultClusterRenderer<Post> {
    private final IconGenerator iconGenerator = new IconGenerator(getApplicationContext());
    private final IconGenerator clusterIconGenerator = new IconGenerator(getApplicationContext());
    private final ImageView imageView;
    private final ImageView clusterImageView;
    private final int dimension;

    public PostRenderer() {
      super(getApplicationContext(), map, clusterManager);

      View multiProfile = getLayoutInflater().inflate(R.layout.multi_profile, null);
      clusterIconGenerator.setContentView(multiProfile);
      clusterImageView = (ImageView) multiProfile.findViewById(R.id.image);

      imageView = new ImageView(getApplicationContext());
      dimension = (int) getResources().getDimension(R.dimen.custom_profile_image);
      imageView.setLayoutParams(new ViewGroup.LayoutParams(dimension, dimension));
      int padding = (int) getResources().getDimension(R.dimen.custom_profile_padding);
      imageView.setPadding(padding, padding, padding, padding);
      iconGenerator.setContentView(imageView);
    }

    protected void onBeforeClusterItemRendered(Post post, MarkerOptions markerOptions) {
      //Draw a single post
      //Set the info window to show their writer
      imageView.setImageResource(post.getPhoto());
      Bitmap icon = iconGenerator.makeIcon();
      markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon)).title(post.getWriter());
    }

    protected void onBeforeClusterRendered(Cluster<Post> cluster, MarkerOptions markerOptions) {
      //Draw multiple post
      // Note: this method runs on the UI thread. Don't spend too much time in here
      // (like in this example).
      List<Drawable> photos = new ArrayList<Drawable>(Math.min(4, cluster.getSize()));
      int width = dimension;
      int height = dimension;

      for (Post p : cluster.getItems()) {
        //draw 4 at most
        if (photos.size() == 4) {
          break;
        }
        Drawable drawable = getResources().getDrawable(p.getPhoto());
        drawable.setBounds(0, 0, width, height);
        photos.add(drawable);
      }
      MultiDrawable multiDrawable = new MultiDrawable(photos);
      multiDrawable.setBounds(0, 0, width, height);

      clusterImageView.setImageDrawable(multiDrawable);
      Bitmap icon = clusterIconGenerator.makeIcon(String.valueOf(cluster.getSize()));
      markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon));
    }

    protected boolean shouldRenderAsCluster(Cluster cluster) {
      //Always render clusters
      return cluster.getSize() > 1;
    }
  }
}
