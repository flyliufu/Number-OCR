package com.lavor.functionsdemo;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.DisplayMetrics;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by sky on 2015/7/6.
 */
public class App extends Application {

  protected static App mInstance;
  private static HttpAPI mHttpAPI;
  private DisplayMetrics displayMetrics = null;
  private static SharedPreferences mSp = null;

  public App() {
    mInstance = this;
  }

  public static OkHttpClient buildOKHTTP() {

    X509TrustManager xtm = new X509TrustManager() {
      @Override public void checkClientTrusted(X509Certificate[] chain, String authType) {
      }

      @Override public void checkServerTrusted(X509Certificate[] chain, String authType) {
      }

      @Override public X509Certificate[] getAcceptedIssuers() {
        X509Certificate[] x509Certificates = new X509Certificate[0];
        return x509Certificates;
      }
    };

    SSLContext sslContext = null;
    try {
      sslContext = SSLContext.getInstance("SSL");
      sslContext.init(null, new TrustManager[] { xtm }, new SecureRandom());
    } catch (NoSuchAlgorithmException e) {
      e.printStackTrace();
    } catch (KeyManagementException e) {
      e.printStackTrace();
    }

    HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor();
    httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
    // httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.NONE);

    return new OkHttpClient.Builder().connectTimeout(58000L, TimeUnit.MILLISECONDS)
        .readTimeout(58000L, TimeUnit.MILLISECONDS)
        .addInterceptor(httpLoggingInterceptor)
        .sslSocketFactory(sslContext.getSocketFactory())
        .hostnameVerifier(new HostnameVerifier() {
          @Override public boolean verify(String hostname, SSLSession session) {
            return true;
          }
        })
        .build();
  }

  private static Retrofit.Builder retBuilder = new Retrofit.Builder().client(buildOKHTTP())
      .addConverterFactory(GsonConverterFactory.create())
      .addCallAdapterFactory(RxJavaCallAdapterFactory.create());

  private static String URL = "https://v.lefu8.com/custserver/";

  public static HttpAPI getHttpAPI() {
    if (mHttpAPI == null) {
      mHttpAPI = retBuilder.baseUrl(URL).build().create(HttpAPI.class);
    }
    return mHttpAPI;
  }

  @Override public void onCreate() {
    super.onCreate();
    mInstance = this;
    mSp = getSharedPreferences("tessdata", Context.MODE_PRIVATE);
  }

  public float getScreenDensity() {
    if (this.displayMetrics == null) {
      setDisplayMetrics(getResources().getDisplayMetrics());
    }
    return this.displayMetrics.density;
  }

  public int getScreenHeight() {
    if (this.displayMetrics == null) {
      setDisplayMetrics(getResources().getDisplayMetrics());
    }
    return this.displayMetrics.heightPixels;
  }

  public int getScreenWidth() {
    if (this.displayMetrics == null) {
      setDisplayMetrics(getResources().getDisplayMetrics());
    }
    return this.displayMetrics.widthPixels;
  }

  public void setDisplayMetrics(DisplayMetrics DisplayMetrics) {
    this.displayMetrics = DisplayMetrics;
  }

  public int dp2px(float f) {
    return (int) (0.5F + f * getScreenDensity());
  }

  public int px2dp(float pxValue) {
    return (int) (pxValue / getScreenDensity() + 0.5f);
  }

  //获取应用的data/data/....File目录
  public String getFilesDirPath() {
    return getFilesDir().getAbsolutePath();
  }

  //获取应用的data/data/....Cache目录
  public String getCacheDirPath() {
    return getCacheDir().getAbsolutePath();
  }

  public static void setFlag(Context context, Boolean b) {
    mSp.edit().putBoolean("flag", b).apply();
  }

  public static Boolean getFlag() {
    return mSp.getBoolean("flag", false);
  }
}
