package kr.ac.kaist.mapping.mapping;

import android.Manifest;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.LruCache;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
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
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterItem;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.google.maps.android.ui.IconGenerator;

import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import kr.ac.kaist.mapping.mapping.model.FacebookUser;
import kr.ac.kaist.mapping.mapping.model.Photo;
import kr.ac.kaist.mapping.mapping.model.User;
import okhttp3.Call;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback,
    ClusterManager.OnClusterClickListener<Photo>,
    ClusterManager.OnClusterInfoWindowClickListener<Photo>,
    ClusterManager.OnClusterItemClickListener<Photo>,
    ClusterManager.OnClusterItemInfoWindowClickListener<Photo> {
  private ClusterManager<Photo> clusterManager;
  private CallbackManager callbackManager;

  ListViewAdapter adapter;

  private GoogleMap map;
  private Collection<Photo> photos;

  private LruCache<String, Bitmap> imgCache;
  private HashMap<String, Bitmap> imgHash;

  private static final int PERMISSION_REQUEST_CODE = 0;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    requestPermissions();

    imgHash = new HashMap<>();

    final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
    final int cacheSize = maxMemory / 8;
    imgCache = new LruCache<String, Bitmap>(cacheSize) {
      @Override
      protected int sizeOf(String key, Bitmap bitmap) {
        return bitmap.getByteCount() / 1024;
      }
    };

    adapter = new ListViewAdapter();
    ListView contactList = (ListView) findViewById(R.id.listContact);
    contactList.setAdapter(adapter);

    AccessToken currentToken = AccessToken.getCurrentAccessToken();
    if (currentToken != null) {
      final Handler loginHandler = new Handler();
      ApiManager.getInstance(MainActivity.this).login(currentToken.getToken(),
          new ApiManager.Callback() {
            @Override
            public void onSuccess(Object obj) {
              loginHandler.post(new Runnable() {
                @Override
                public void run() {
                  getServerData();
                  getFacebookFriends();
                }
              });
            }

            @Override
            public void onFailure() {

            }
          });
    }

    /* Facebook login process */
    callbackManager = CallbackManager.Factory.create();
    LoginButton loginButton = (LoginButton) findViewById(R.id.btnLogin);
    loginButton.setReadPermissions("email");
    loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
      @Override
      public void onSuccess(LoginResult loginResult) {
        Toast.makeText(MainActivity.this, "Successfully logged in", Toast.LENGTH_SHORT).show();
        ApiManager.getInstance(MainActivity.this).login(loginResult.getAccessToken().getToken(),
            new ApiManager.Callback() {
              @Override
              public void onSuccess(Object obj) {
                getServerData();
                getFacebookFriends();
              }

              @Override
              public void onFailure() {

              }
            });
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
  }

  @Override
  public void onResume() {
    super.onResume();
    String photoHolderId = getIntent().getStringExtra("photoHolderId");
    if (photoHolderId != null) {
      final Photo photo = (Photo) DataHolder.popDataHolder(photoHolderId);
      final Handler handler = new Handler();
      new Thread(new Runnable() {
        @Override
        public void run() {
          try {
            URL url = new URL(photo.getImage());
            Bitmap bmp = BitmapFactory.decodeStream(url.openConnection().getInputStream());
            addBitmapToMemoryCache(photo.getImage(), bmp);
            handler.post(new Runnable() {
              @Override
              public void run() {
                clusterManager.addItem(photo);
              }
            });
          } catch (Exception e) {
            e.printStackTrace();
          }
        }
      }).start();
    }
  }

  public void getServerData() {
    /* Download images */
    final Handler handler = new Handler();
    new Thread(new Runnable() {
      @Override
      public void run() {
        ApiManager.getInstance(MainActivity.this).getPhotos(true, new ApiManager.Callback() {
          @Override
          public void onSuccess(Object obj) {
            photos = (Collection<Photo>) obj;
            for (final Photo photo : photos) {
              try {
                URL url = new URL(photo.getImage());
                final Bitmap bmp = BitmapFactory.decodeStream(url.openConnection().getInputStream());
                addBitmapToMemoryCache(photo.getImage(), bmp);
              } catch (Exception e) {
                e.printStackTrace();
              }
            }
            handler.post(new Runnable() {
              @Override
              public void run() {
                /* Set map */
                FragmentManager fragmentManager = getFragmentManager();
                MapFragment mapFragment = (MapFragment) fragmentManager.findFragmentById(R.id.fragMap);
                mapFragment.getMapAsync(MainActivity.this);
              }
            });
          }

          @Override
          public void onFailure() {

          }
        });
      }
    }).start();
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

    clusterManager = new ClusterManager<Photo>(this, this.map);
    clusterManager.setRenderer(new PhotoRender());
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
  public boolean onClusterClick(Cluster<Photo> cluster) {
    LatLngBounds.Builder builder = LatLngBounds.builder();
    for (ClusterItem item : cluster.getItems()) {
      builder.include(item.getPosition());
    }
    final LatLngBounds bounds = builder.build();

    map.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100));
    return true;
  }

  public void onClusterInfoWindowClick(Cluster<Photo> cluster) {
    // Does nothing, but you could go to a list of the users.
  }

  public boolean onClusterItemClick(Photo item) {
    // Does nothing, but you could go into the user's profile page, for example.
    Intent intent = new Intent(this, PhotoViewActivity.class);
    intent.putExtra("photoHolderId", DataHolder.putDataHolder(item));
    intent.putExtra("bitmapHolderId", DataHolder.putDataHolder(getBitmapFromMemCache(item.getImage())));
    startActivity(intent);
    return false;
  }

  public void onClusterItemInfoWindowClick(Photo item) {
    // Does nothing, but you could go into the user's profile page, for example.
  }

  private void addPhotos() {
    for (Photo photo : photos) {
      clusterManager.addItem(photo);
    }
  }

  /**nloading the image every time the Cluster Manager tries to load a marker, so if you have, for example, 100 markers you will downlo
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

  private class PhotoRender extends DefaultClusterRenderer<Photo> {
    private final IconGenerator iconGenerator = new IconGenerator(getApplicationContext());
    private final IconGenerator clusterIconGenerator = new IconGenerator(getApplicationContext());
    private final ImageView imageView;
    private final ImageView clusterImageView;
    private final int dimension;

    public PhotoRender() {
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

    @Override
    protected void onBeforeClusterItemRendered(final Photo photo, final MarkerOptions markerOptions) {
      Bitmap bitmap = getBitmapFromMemCache(photo.getImage());
      Log.d("asdf", photo.getImage());
      Log.d("asdf", (bitmap == null) + "");
      Log.d("asdf", imgCache.missCount() + " " + imgCache.putCount());
      if (bitmap == null) {
        return;
      }
      imageView.setImageBitmap(bitmap);
      Bitmap icon = iconGenerator.makeIcon();
      markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon))
          .title(photo.getUser().getUsername());
    }

    @Override
    protected void onBeforeClusterRendered(Cluster<Photo> cluster, MarkerOptions markerOptions) {
      //Draw multiple post
      // Note: this method runs on the UI thread. Don't spend too much time in here
      // (like in this example).
      List<Drawable> profilePhotos = new ArrayList<Drawable>(Math.min(4, cluster.getSize()));
      int width = dimension;
      int height = dimension;

      for (Photo photo : cluster.getItems()) {
        // Draw 4 at most.
        if (profilePhotos.size() == 4) break;
        Drawable drawable = new BitmapDrawable(getResources(), getBitmapFromMemCache(photo.getImage()));
        drawable.setBounds(0, 0, width, height);
        profilePhotos.add(drawable);
      }
      MultiDrawable multiDrawable = new MultiDrawable(profilePhotos);
      multiDrawable.setBounds(0, 0, width, height);

      clusterImageView.setImageDrawable(multiDrawable);
      Bitmap icon = iconGenerator.makeIcon(String.valueOf(cluster.getSize()));
      markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon));
    }

    @Override
    protected boolean shouldRenderAsCluster(Cluster cluster) {
      //Always render clusters
      return cluster.getSize() > 1;
    }
  }

  public void addBitmapToMemoryCache(String key, Bitmap bitmap) {
    /*
    if (getBitmapFromMemCache(key) == null) {
      imgCache.put(key, bitmap);
    }*/
    imgHash.put(key, bitmap);
  }

  public Bitmap getBitmapFromMemCache(String key) {
    //return imgCache.get(key);
    return imgHash.get(key);
  }

  private void getFacebookFriends() {
    GraphRequest graphRequest = GraphRequest.newMeRequest(AccessToken.getCurrentAccessToken(),
        new GraphRequest.GraphJSONObjectCallback() {
          @Override
          public void onCompleted(JSONObject object, GraphResponse response) {
            JsonParser jsonParser = new JsonParser();
            final JsonObject obj = (JsonObject) jsonParser.parse(object.toString());
            final String name = obj.getAsJsonPrimitive("name").getAsString();
            final String email = obj.getAsJsonPrimitive("email").getAsString();
            final String pictureUrl = obj.getAsJsonObject("picture").getAsJsonObject("data")
                .getAsJsonPrimitive("url").getAsString();

            final Handler handler = new Handler();
            new Thread(new Runnable() {
              @Override
              public void run() {
                try {
                  URL url = new URL(pictureUrl);
                  final Bitmap bmp = BitmapFactory.decodeStream(url.openConnection().getInputStream());
                  handler.post(new Runnable() {
                    @Override
                    public void run() {
                      adapter.addItem_Me(new BitmapDrawable(getResources(), bmp), name, email);
                      Collection<FacebookUser> friends = new Gson().fromJson(
                          obj.getAsJsonObject("friends").getAsJsonArray("data"),
                          new TypeToken<Collection<FacebookUser>>(){}.getType());
                      for (FacebookUser user : friends) {
                        adapter.addItem(ContextCompat.getDrawable(MainActivity.this, R.drawable.contact),
                            user);
                      }
                    }
                  });
                } catch (Exception e) {
                  e.printStackTrace();
                }
              }
            }).start();
          }
        });
    Bundle param = new Bundle();
    param.putString("fields", "friends,name,email,picture");
    graphRequest.setParameters(param);
    graphRequest.executeAsync();
  }
}
