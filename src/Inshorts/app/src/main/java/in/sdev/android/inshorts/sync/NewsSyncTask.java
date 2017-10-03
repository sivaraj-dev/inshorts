package in.sdev.android.inshorts.sync;

import android.content.Context;

import static in.sdev.android.inshorts.utilities.NewsPreferences.loadNews;


public class NewsSyncTask {
    private static final String LOG_TAG = NewsSyncTask.class.getSimpleName();

    synchronized public static void syncFeed(Context context) {
        try {
            loadNews(context, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
