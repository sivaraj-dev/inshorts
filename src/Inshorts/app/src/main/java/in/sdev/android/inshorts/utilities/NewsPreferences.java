package in.sdev.android.inshorts.utilities;

import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;

import in.sdev.android.inshorts.Constants;
import in.sdev.android.inshorts.R;
import in.sdev.android.inshorts.activities.WebViewActivity;
import in.sdev.android.inshorts.database.NewsContract;
import in.sdev.android.inshorts.database.NewsDbHelper;
import in.sdev.android.inshorts.interfaces.APIService;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;

import static in.sdev.android.inshorts.utilities.NewsJsonUtils.updateNewsToLocal;


public final class NewsPreferences {
    private static final String LOG_TAG = NewsPreferences.class.getSimpleName();

    /*
    * Method to get the news type which he had set in news activity
    * */
    public static String getNewsCategory(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getString(Constants.PREF_NEWS_CATEGORY, context.getString(R.string.pref_news_category_default));
    }



    /*
    * Loads more news when needed
    * Method called when the user scrolls down in news activity
    * */
    public static void loadNews(final Context context, Boolean getLastest) {
        try {
            APIService mAPIService = ApiUtils.getAPIService(context);
            String[] pageNews = NewsPreferences.getNewsPageDetails(context);
            String[] preferredCoordinates = NewsPreferences.getFilterDetails(context);
            String news_category = preferredCoordinates[0];
            String news_publisher = preferredCoordinates[1];
            mAPIService.getNews(news_category, news_publisher, getLastest?"1":pageNews[0], pageNews[1]).enqueue(new retrofit2.Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    if (response.isSuccessful()) {
                        try {
                            String data = response.body().string();
                            updateNewsToLocal(context, data);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    Log.e(LOG_TAG, "Unable to submit post to API.");
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*
    * Get the current page in the news activity and passes into the loadMoreNews method
    * */
    private static String[] getNewsPageDetails(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        String[] pageDetails = new String[5];
        pageDetails[0] = sp.getString(Constants.PREF_PAGE_NO, "1");
        pageDetails[1] = sp.getString(Constants.PREF_PAGE_SIZE, "5");
        return pageDetails;
    }

    /*
    * Get the current set filter details
    * */
    public static String[] getFilterDetails(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        String[] userDetails = new String[3];
        userDetails[0] = sp.getString(Constants.PREF_NEWS_CATEGORY, context.getString(R.string
                .pref_news_category_default));
        userDetails[1] = sp.getString(Constants.PREF_NEWS_PUBLISHER, context.getString(R.string
                .pref_news_publisher_default));
        return userDetails;
    }

    /*
    * Get the current version code
    * */
    @NonNull
    public static String getVersionCode(Context context) {
        int versionCode = 0;
        String versionName = "0";
        PackageManager manager = context.getPackageManager();
        try {
            PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
            versionName = info.versionName;
            versionCode = info.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return String.valueOf(versionCode);
    }


    /*
    * Reset the news type
    * We call this method when he reopens the app
    * */
    public static void resetNewsCategory(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sp.edit();
        editor.remove(Constants.PREF_NEWS_CATEGORY);
        editor.apply();
    }

    /*
    * Reset the filter details
    * We call this method when he reopens the app
    * */
    public static void resetFilterDetails(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sp.edit();
        editor.remove(Constants.PREF_NEWS_PUBLISHER);
        editor.remove(Constants.PREF_REF_ID);
        editor.apply();
    }

    /*
    * Method to check the string null and is not empty
    * */
    public static boolean isEmptyString(final String s) {
        return s == null || s.trim().isEmpty() || s.equalsIgnoreCase("null");
    }


    /*
  * Set the page-size and page-number in preferences
  * */
    public static void setNewsPageDetails(Context context, String page_no, String page_size) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(Constants.PREF_PAGE_NO, page_no);
        editor.putString(Constants.PREF_PAGE_SIZE, page_size);
        editor.apply();
    }

    /*
    * Checks weather the news-type along tag is applied or not
    * */
    public static boolean isTagWithNewsCategoryApplied(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        boolean spContainNews_type = sp.contains(Constants.PREF_NEWS_CATEGORY);
        boolean spContainRef_type = sp.contains(Constants.PREF_NEWS_PUBLISHER);
        boolean spContainRef_id = sp.contains(Constants.PREF_REF_ID);
        boolean spContainBothNewsCategoryAndPublisher = false;
        if (spContainNews_type && spContainRef_type && spContainRef_type) {
            spContainBothNewsCategoryAndPublisher = true;
        }
        return spContainBothNewsCategoryAndPublisher;
    }

    /*
    * Checks weather the tag is applied or not
    * */
    public static boolean isFilterApplied(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.contains(Constants.PREF_NEWS_CATEGORY);
    }


    /*
    * Method to get the version name
    * */
    public static String getVersionName(Context context) {
        int versionCode = 0;
        String versionName = "";
        PackageManager manager = context.getPackageManager();
        try {
            PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
            versionName = "v" + info.versionName;
            versionCode = info.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return String.valueOf(versionName);
    }


    /*
    * Method to send the data to other apps
    * */
    public static void shareDataToOtherApps(final Context context, FirebaseAnalytics firebaseAnalytics, final int packageType, String subject, final String body, final String url, String imageUrl, String videoUrl) {
        Toast.makeText(context, "Loading...", Toast.LENGTH_SHORT).show();
        final Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        if (!isEmptyString(subject)) {
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
        }
        if (!isEmptyString(imageUrl)) {
            isStoragePermissionGranted(context);
            ImageView iv = new ImageView(context);
            Picasso.with(context)
                    .load(imageUrl)
                    .into(new Target() {
                        @Override
                        public void onBitmapLoaded(final Bitmap bitmap, Picasso.LoadedFrom from) {
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    File fileDir = new File(Environment.getExternalStorageDirectory(), "Inshorts");
                                    if (!fileDir.exists())
                                        fileDir.mkdir();
                                    File file = new File(Environment.getExternalStorageDirectory(), "Inshorts/inshorts.png");
                                    try {
                                        file.createNewFile();
                                        FileOutputStream ostream = new FileOutputStream(file);
                                        bitmap.compress(Bitmap.CompressFormat.JPEG, 75, ostream);
                                        ostream.close();
                                        shareIntent.setType("image/*");
                                        shareIntent.putExtra(Intent.EXTRA_STREAM, getImageUri(context, bitmap));
                                        shareIntent.putExtra(Intent.EXTRA_TEXT, url + " \n " + body);
                                        shareIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                        if (packageType == -1 || packageType > 2) {
                                            context.startActivity(Intent.createChooser(shareIntent, "Share via"));
                                        } else {
                                            shareIntent.setPackage(Constants.SHARE_PACKAGE_NAMES[packageType]);
                                            context.startActivity(shareIntent);
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            }).start();
                        }

                        @Override
                        public void onBitmapFailed(Drawable errorDrawable) {
                        }

                        @Override
                        public void onPrepareLoad(Drawable placeHolderDrawable) {
                        }
                    });
        } else {
            shareIntent.putExtra(Intent.EXTRA_TEXT, url + " \n " + body);
            shareIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            if (packageType == -1 || packageType > 2) {
                sendEventTracker(firebaseAnalytics, "Share", "Share", "News Share");
                context.startActivity(Intent.createChooser(shareIntent, "Share via"));
            } else {
                sendEventTracker(firebaseAnalytics, "Share", "Share", "Constants.SHARE_PACKAGE_NAMES[packageType]");
                shareIntent.setPackage(Constants.SHARE_PACKAGE_NAMES[packageType]);
                context.startActivity(shareIntent);
            }
        }
    }

    /*
    * Method to check weather the required permissions are granted
    * */
    public static boolean isStoragePermissionGranted(Context context) {
        if (Build.VERSION.SDK_INT >= 23) {
            if (context.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                //Log.v("isSPGranted", "Permission is granted");
                return true;
            } else {
                //Log.v("isSPGranted", "Permission is revoked");
                ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return false;
            }
        } else {
            //Log.v("isSPGranted", "Permission is granted");
            return true;
        }
    }

    /*
    * Get the image uri form bitmap
    * */
    private static Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }


    public static void sendEventTracker(FirebaseAnalytics firebaseAnalytics, String category, String action, String label) {
        if (null != firebaseAnalytics) {
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_CATEGORY, category);
            bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, action);
            bundle.putString(FirebaseAnalytics.Param.CONTENT, label);
            firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
        }
    }

    /*
    * Set the tag details
    * */
    public static void setFilterDetails(Context context, String news_category, String ref_type, String ref_id) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(Constants.PREF_NEWS_CATEGORY, news_category);
        editor.putString(Constants.PREF_NEWS_PUBLISHER, ref_type);
        editor.putString(Constants.PREF_REF_ID, ref_id);
        editor.apply();
    }

    /*
    * Set the news-type
    * */
    public static void setNewsCategory(Context context, String news_category) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(Constants.PREF_NEWS_CATEGORY, news_category);
        editor.apply();
    }

    /*
    * Method that takes the user to playstore
    * */
    private static void playStore(Context context) {
        Uri uri = Uri.parse("market://details?id=" + context.getPackageName());
        Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
        goToMarket.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            context.startActivity(goToMarket);
        } catch (ActivityNotFoundException e) {
            context.startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("http://play.google.com/store/apps/details?id=" + context.getPackageName())));
        }
        ((Activity) context).finish();
    }

    /*
    * Method to control the Toggle of bookmark
    * */
    public static ImageView toggleBookmark(Context context, Boolean is_bookmarked, ImageView actionToggle) {
        if (is_bookmarked) {
            actionToggle.setImageResource(R.drawable.ic_user_bookmarked);
        } else {
            actionToggle.setImageResource(R.drawable.ic_user_bookmark);
        }
        return actionToggle;
    }

    public static float dp2px(Context context, float dipValue) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dipValue, metrics);
    }

    /*
    * Method used to check weather the apps are installed or not to show in the share layout
    * */
    public static void checkVisibilityOfSharePackages(Context context, ImageView ivWhatsapp, ImageView ivTwitter, ImageView ivFacebook) {
        if (appInstalled(context, Constants.SHARE_PACKAGE_NAMES[0]))
            ivWhatsapp.setVisibility(View.VISIBLE);
        else ivWhatsapp.setVisibility(View.GONE);
        if (appInstalled(context, Constants.SHARE_PACKAGE_NAMES[1]))
            ivTwitter.setVisibility(View.VISIBLE);
        else ivTwitter.setVisibility(View.GONE);
        if (appInstalled(context, Constants.SHARE_PACKAGE_NAMES[2]))
            ivFacebook.setVisibility(View.VISIBLE);
        else ivFacebook.setVisibility(View.GONE);
    }

    private static boolean appInstalled(Context context, String app_package) {
        PackageManager pm = context.getPackageManager();
        boolean app_installed;
        try {
            pm.getPackageInfo(app_package, PackageManager.GET_ACTIVITIES);
            app_installed = true;
        } catch (PackageManager.NameNotFoundException e) {
            app_installed = false;
        }
        return app_installed;
    }

    /*
    * Get icon of the news category
    * */
    public static int getIconResourceForCategory(String cart_type) {
        switch (cart_type) {
            case "b":
                return R.drawable.ic_category_b;
            case "e":
                return R.drawable.ic_category_e;
            case "m":
                return R.drawable.ic_category_m;
            case "t":
                return R.drawable.ic_category_t;
            default:
                return R.drawable.ic_category_all;
        }
    }

    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public static void ToggleAction(final Context context, APIService apiService, final String action_id, String ref_id, String status, final String ref1, final String ref2, String uid, String fcv) {

    }

    /*
    * Function converts string into a RequestBody
    * */
    public static RequestBody convertintoRequestBody(String s) {
        return RequestBody.create(MultipartBody.FORM, s + "");
    }


    public static void clear_cache(Context context) {
        NewsDbHelper mOpenHelper = new NewsDbHelper(context);
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        db.delete(NewsContract.NewsEntry.TABLE_NAME, null, null);
    }

    /**
    * Method to route into the app when clicked on notification
    * */
    public static void routeLink(Activity activity, Context context, String type, String title, String ref1, String ref2, String ref3, String img_url) {

        switch (type) {
            case "WEBVIEW":
                Intent intentWebView = new Intent(context, WebViewActivity.class);
                intentWebView.putExtra("web_title", title);
                intentWebView.putExtra("web_url", ref1);
                context.startActivity(intentWebView);
                activity.overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
                break;
        }
    }

}



