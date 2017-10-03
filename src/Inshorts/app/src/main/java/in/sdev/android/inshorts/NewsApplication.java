package in.sdev.android.inshorts;

import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;

import com.google.firebase.analytics.FirebaseAnalytics;

import in.sdev.android.inshorts.utilities.NewsPreferences;

public class NewsApplication extends Application {
    private static NewsApplication mInstance;
    public FirebaseAnalytics mFirebaseAnalytics;

    public static synchronized NewsApplication getInstance() {
        return mInstance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
        NewsPreferences.resetFilterDetails(this);
        NewsPreferences.resetNewsCategory(this);
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(base);
    }

    public FirebaseAnalytics getFirebaseAnalytics() {
        return FirebaseAnalytics.getInstance(this);
    }


}
