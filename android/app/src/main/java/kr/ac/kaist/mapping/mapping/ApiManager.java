package kr.ac.kaist.mapping.mapping;

import android.content.Context;
import android.content.res.Resources;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collection;

import kr.ac.kaist.mapping.mapping.model.Photo;
import kr.ac.kaist.mapping.mapping.model.User;
import okhttp3.Call;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ApiManager {
  private static final String URL_SCHEME = "http";
  private static final String URL_HOST = "52.78.113.241";
  private static final String URL_AUTH = "auth";
  private static final String URL_API = "api";

  private final String clientId;
  private final String clientSecret;

  private static ApiManager instance;
  private OkHttpClient client;

  private Context context;

  private String token;

  public static synchronized ApiManager getInstance(Context context) {
    if (instance == null) {
      instance = new ApiManager(context);
    }
    return instance;
  }

  private ApiManager(Context context) {
    this.clientId = context.getString(R.string.mapping_client_id);
    this.clientSecret = context.getString(R.string.mapping_client_secret);
    this.client = new OkHttpClient();
    this.context = context;
  }

  public void login(final String fbToken, final Callback callback) {
    Request request = new Request.Builder()
        .url(new HttpUrl.Builder()
            .scheme(URL_SCHEME)
            .host(URL_HOST)
            .addPathSegment(URL_AUTH)
            .addPathSegment("convert-token")
            .build())
        .post(new FormBody.Builder()
            .add("grant_type", "convert_token")
            .add("client_id", clientId)
            .add("client_secret", clientSecret)
            .add("backend", "facebook")
            .add("token", fbToken)
            .build())
        .build();
    client.newCall(request).enqueue(new okhttp3.Callback() {
      @Override
      public void onFailure(Call call, IOException e) {
        callback.onFailure();
      }

      @Override
      public void onResponse(Call call, Response response) throws IOException {
        String res = response.body().string();
        Log.d("asdf", res);

        JsonElement json = new JsonParser().parse(res);
        token = json.getAsJsonObject().get("access_token").getAsString();
        callback.onSuccess(token);
      }
    });
  }

  public void getUser(int id, final Callback callback) {
    Request request = getRequestBuilder(getUrlBuilder()
        .addPathSegment("users")
        .addPathSegment(String.valueOf(id))
        .build()).build();
    client.newCall(request).enqueue(new okhttp3.Callback() {
      @Override
      public void onFailure(Call call, IOException e) {
        callback.onFailure();
      }

      @Override
      public void onResponse(Call call, Response response) throws IOException {
        if (response.code() != 200) {
          callback.onFailure();
          return;
        }
        Gson gson = new Gson();
        User user = gson.fromJson(response.body().string(), User.class);
        callback.onSuccess(user);
      }
    });
  }

  public void getCurrentUser(final Callback callback) {
    Request request = getRequestBuilder(getUrlBuilder()
        .addPathSegment("me")
        .build()).build();
    client.newCall(request).enqueue(new okhttp3.Callback() {
      @Override
      public void onFailure(Call call, IOException e) {
        callback.onFailure();
      }

      @Override
      public void onResponse(Call call, Response response) throws IOException {
        if (response.code() != 200) {
          callback.onFailure();
          return;
        }
        Gson gson = new Gson();
        User user = gson.fromJson(response.body().string(), User.class);
        callback.onSuccess(user);
      }
    });
  }

  public void getUserPhotos(int id, final Callback callback) {
    Request request = getRequestBuilder(getUrlBuilder()
        .addPathSegment("users")
        .addPathSegment(String.valueOf(id))
        .addPathSegment("photos")
        .build()).build();
    client.newCall(request).enqueue(new okhttp3.Callback() {
      @Override
      public void onFailure(Call call, IOException e) {
        callback.onFailure();
      }

      @Override
      public void onResponse(Call call, Response response) throws IOException {
        if (response.code() != 200) {
          callback.onFailure();
          return;
        }
        Gson gson = new Gson();
        Collection<Photo> photos = gson.fromJson(response.body().string(),
            new TypeToken<Collection<Photo>>() {}.getType());
        callback.onSuccess(photos);
      }
    });
  }

  public void getCurrentUserPhotos(final Callback callback) {
    Request request = getRequestBuilder(getUrlBuilder()
        .addPathSegment("me")
        .addPathSegment("photos")
        .build()).build();
    client.newCall(request).enqueue(new okhttp3.Callback() {
      @Override
      public void onFailure(Call call, IOException e) {
        callback.onFailure();
      }

      @Override
      public void onResponse(Call call, Response response) throws IOException {
        if (response.code() != 200) {
          callback.onFailure();
          return;
        }
        Gson gson = new Gson();
        Collection<Photo> photos = gson.fromJson(response.body().string(),
            new TypeToken<Collection<Photo>>() {}.getType());
        callback.onSuccess(photos);
      }
    });
  }

  public void postPhoto(Photo photo, final Callback callback) {
    RequestBody requestBody = new MultipartBody.Builder()
        .setType(MultipartBody.FORM)
        .addFormDataPart("image", photo.getImage())
        .addFormDataPart("desc", photo.getDesc())
        .addFormDataPart("pub_date", photo.getPubDate().toString())
        .addFormDataPart("latitude", String.valueOf(photo.getLatitude()))
        .addFormDataPart("longitude", String.valueOf(photo.getLongitude()))
        .addFormDataPart("grid", String.valueOf(photo.getGrid()))
        .addFormDataPart("is_public", String.valueOf(photo.isPublic()))
        .build();
    Request request = getRequestBuilder(getUrlBuilder()
        .addPathSegment("me")
        .addPathSegment("photos")
        .build()).post(requestBody).build();
    client.newCall(request).enqueue(new okhttp3.Callback() {
      @Override
      public void onFailure(Call call, IOException e) {
        callback.onFailure();
      }

      @Override
      public void onResponse(Call call, Response response) throws IOException {
        if (response.code() != 201) {
          callback.onFailure();
          return;
        }
        callback.onSuccess(null);
      }
    });
  }

  public void updateCurrentUserFriends() {
    Request request = getRequestBuilder(getUrlBuilder()
        .addPathSegment("me")
        .addPathSegment("friends")
        .build()).post(RequestBody.create(null, "")).build();
    client.newCall(request).enqueue(new okhttp3.Callback() {
      @Override
      public void onFailure(Call call, IOException e) {
      }

      @Override
      public void onResponse(Call call, Response response) throws IOException {
      }
    });
  }

  public void getPhotos(boolean showAll, final Callback callback) {
    getPhotos(showAll, null, callback);
  }

  public void getPhotos(boolean showAll, int grid, final Callback callback) {
    getPhotos(showAll, new Integer(grid), callback);
  }

  private void getPhotos(boolean showAll, Integer grid, final Callback callback) {
    HttpUrl.Builder urlBuilder = getUrlBuilder().addPathSegment("photos");
    if (showAll) {
      urlBuilder = urlBuilder.addQueryParameter("show", "all");
    }
    if (grid != null) {
      urlBuilder = urlBuilder.addQueryParameter("grid", grid.toString());
    }
    Request request = getRequestBuilder(urlBuilder.build()).build();
    client.newCall(request).enqueue(new okhttp3.Callback() {
      @Override
      public void onFailure(Call call, IOException e) {
        callback.onFailure();
      }

      @Override
      public void onResponse(Call call, Response response) throws IOException {
        if (response.code() != 200) {
          Log.d("asdf", response.body().string());
          callback.onFailure();
          return;
        }
        Gson gson = new Gson();
        String result = response.body().string();
        Log.d("asdf", result);
        Collection<Photo> photos = gson.fromJson(result,
            new TypeToken<Collection<Photo>>() {}.getType());
        callback.onSuccess(photos);
      }
    });
  }

  public void getPhoto(long id, final Callback callback) {
    Request request = getRequestBuilder(getUrlBuilder()
        .addPathSegment("photos")
        .addPathSegment(String.valueOf(id))
        .build()).build();
    client.newCall(request).enqueue(new okhttp3.Callback() {
      @Override
      public void onFailure(Call call, IOException e) {
        callback.onFailure();
      }

      @Override
      public void onResponse(Call call, Response response) throws IOException {
        if (response.code() != 200) {
          callback.onFailure();
          return;
        }
        Gson gson = new Gson();
        Photo photo = gson.fromJson(response.body().string(), Photo.class);
        callback.onSuccess(photo);
      }
    });
  }

  private HttpUrl.Builder getUrlBuilder() {
    return new HttpUrl.Builder()
        .scheme(URL_SCHEME)
        .host(URL_HOST)
        .addPathSegment(URL_API);
  }

  private Request.Builder getRequestBuilder(HttpUrl url) {
    return new Request.Builder()
        .url(url)
        .addHeader("Authorization", "Bearer " + token);
  }

  public interface Callback {
    void onSuccess(Object obj);
    void onFailure();
  }
}
