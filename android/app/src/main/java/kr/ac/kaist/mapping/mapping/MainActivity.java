package kr.ac.kaist.mapping.mapping;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.app.FragmentManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, ClusterManager.OnClusterClickListener<Post>,
        ClusterManager.OnClusterInfoWindowClickListener<Post>, ClusterManager.OnClusterItemClickListener<Post>,
        ClusterManager.OnClusterItemInfoWindowClickListener<Post>
{
  private ClusterManager<Post> clusterManager;
  private CallbackManager callbackManager;

  // Examples -- to be deleted later
  private static final LatLng Jinri = new LatLng(36.374687, 127.359165);
  private static final LatLng Sejong = new LatLng(36.371422, 127.366625);
  private static final LatLng Mir = new LatLng(36.370540, 127.355763);
  private static final LatLng Arum = new LatLng(36.373812, 127.356680);

  private GoogleMap map;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    callbackManager = CallbackManager.Factory.create();
    LoginButton loginButton = (LoginButton) findViewById(R.id.login_button);
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

    FragmentManager fragmentManager = getFragmentManager();
    MapFragment mapFragment = (MapFragment)fragmentManager.findFragmentById(R.id.map);
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
    // Zoom to KAIST
    LatLng address = new LatLng(36.372253, 127.360398);
    CameraPosition cp = new CameraPosition.Builder().target((address )).zoom(15).build();
    map.animateCamera(CameraUpdateFactory.newCameraPosition(cp));

    clusterManager = new ClusterManager<Post>(this, this.map);
    clusterManager.setRenderer(new PostRenderer());
    this.map.setOnCameraChangeListener(clusterManager);
    this.map.setOnMarkerClickListener(clusterManager);
    this.map.setOnInfoWindowClickListener(clusterManager);
    clusterManager.setOnClusterClickListener(this);
    clusterManager.setOnClusterInfoWindowClickListener(this);
    clusterManager.setOnClusterItemClickListener(this);
    clusterManager.setOnClusterItemInfoWindowClickListener(this);

    addItems();
    clusterManager.cluster();

    /*
    // Examples
    Bitmap orgImage1 = BitmapFactory.decodeResource(getResources(),R.drawable.arum);
    Bitmap bmp = makeMarker(orgImage1);
    map.addMarker(new MarkerOptions().position(Arum)
            .icon(BitmapDescriptorFactory.fromBitmap(bmp))
            // Specifies the anchor to be at a particular point in the marker image.
            .anchor(0.5f, 1));

    Bitmap orgImage2 = BitmapFactory.decodeResource(getResources(),R.drawable.jinri);
    Bitmap bmp2 = makeMarker(orgImage2);
    map.addMarker(new MarkerOptions().position(Jinri)
            .icon(BitmapDescriptorFactory.fromBitmap(bmp2))
            // Specifies the anchor to be at a particular point in the marker image.
            .anchor(0.5f, 1));

    Bitmap orgImage3 = BitmapFactory.decodeResource(getResources(),R.drawable.mir);
    Bitmap bmp3 = makeMarker(orgImage3);
    map.addMarker(new MarkerOptions().position(Mir)
            .icon(BitmapDescriptorFactory.fromBitmap(bmp3))
            // Specifies the anchor to be at a particular point in the marker image.
            .anchor(0.5f, 1));

    Bitmap orgImage4 = BitmapFactory.decodeResource(getResources(),R.drawable.sejong);
    Bitmap bmp4 = makeMarker(orgImage4);
    map.addMarker(new MarkerOptions().position(Sejong)
            .icon(BitmapDescriptorFactory.fromBitmap(bmp4))
            // Specifies the anchor to be at a particular point in the marker image.
            .anchor(0.5f, 1));
    */

  }

  /*
  public Bitmap makeMarker(Bitmap orgImage){
    Bitmap.Config conf = Bitmap.Config.ARGB_8888;
    Bitmap bmp = Bitmap.createBitmap(200, 200, conf);
    Canvas canvas1 = new Canvas(bmp);

    Paint color = new Paint();
    color.setTextSize(35);
    color.setColor(Color.BLACK);

    //Bitmap orgImage = BitmapFactory.decodeResource(getResources(),R.drawable.arum);
    Bitmap resize = Bitmap.createScaledBitmap(orgImage,150,150,true);
    canvas1.drawBitmap(resize,0,0,color);

    return bmp;
  }
  */

  private class PostRenderer extends DefaultClusterRenderer<Post>{
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
    protected void onBeforeClusterItemRendered(Post post, MarkerOptions markerOptions){
      //Draw a single post
      //Set the info window to show their writer
      imageView.setImageResource(post.getPhoto());
      Bitmap icon = iconGenerator.makeIcon();
      markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon)).title(post.getWriter());
    }

    protected void onBeforeClusterRendered(Cluster<Post> cluster, MarkerOptions markerOptions){
      //Draw multiple post
      // Note: this method runs on the UI thread. Don't spend too much time in here (like in this example).
      List<Drawable> photos = new ArrayList<Drawable>(Math.min(4, cluster.getSize()));
      int width = dimension;
      int height = dimension;

      for (Post p : cluster.getItems()){
        //draw 4 at most
        if(photos.size()==4) break;
        Drawable drawable = getResources().getDrawable(p.getPhoto());
        drawable.setBounds(0,0,width,height);
        photos.add(drawable);
      }
      MultiDrawable multiDrawable = new MultiDrawable(photos);
      multiDrawable.setBounds(0,0, width, height);

      clusterImageView.setImageDrawable(multiDrawable);
      Bitmap icon = clusterIconGenerator.makeIcon(String.valueOf(cluster.getSize()));
      markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon));
    }

    protected boolean shouldRenderAsCluster(Cluster cluster){
      //Always render clusters
      return cluster.getSize()>1;
    }
  }

  public boolean onClusterClick(Cluster<Post> cluster){
    //show a toast with some info when the cluster is clicked
    String writer = cluster.getItems().iterator().next().getWriter();
    Toast.makeText(this, cluster.getSize() + "(including "+ writer + ")", Toast.LENGTH_SHORT).show();
    // Zoom in the cluster. Need to create LatLngBounds and including all the cluster items
    // inside of bounds, then animate to center of the bounds.

    // Create the builder to collect all essential cluster items for the bounds.
    LatLngBounds.Builder builder = LatLngBounds.builder();
    for(ClusterItem item : cluster.getItems()){
      builder.include(item.getPosition());
    }
    //Get the LatLngBounds
    final LatLngBounds bounds = builder.build();

    //Animate camera to the bounds
    try{
      map.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds,100));
    }catch (Exception e){
      e.printStackTrace();
    }
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

  private void addItems(){
    clusterManager.addItem(new Post(Jinri,"Yoonseo","AAA",R.drawable.jinri, new Date()));
    clusterManager.addItem(new Post(Arum,"Girl","BBB", R.drawable.arum, new Date()));
    clusterManager.addItem(new Post(Mir,"Boy","CCC", R.drawable.mir, new Date()));
    clusterManager.addItem(new Post(Sejong,"Anotehr girl","DDD", R.drawable.sejong, new Date()));
  }
}
