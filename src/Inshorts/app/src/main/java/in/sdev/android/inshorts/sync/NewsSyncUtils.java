package in.sdev.android.inshorts.sync;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.NonNull;

import in.sdev.android.inshorts.activities.NewsActivity;
import in.sdev.android.inshorts.database.NewsContract;

public class NewsSyncUtils {
    private static boolean sInitialized;
    private static boolean sInitializedProfile;

    synchronized public static void initialize(@NonNull final Context context) {
        if (sInitialized) return;
        sInitialized = true;
        new AsyncTask<Void, Void, Void>() {
            @Override
            public Void doInBackground(Void... voids) {
                Uri newsQueryUri = NewsContract.NewsEntry.CONTENT_URI;
                String[] projectionColumns = { NewsContract.NewsEntry._ID };
                Cursor cursor = context.getContentResolver().query(
                        newsQueryUri,
                        NewsActivity.NEWS_FEED_PROJECTION,
                        null,
                        null,
                        null);
                if (null == cursor || cursor.getCount() == 0) {
                    startImmediateSync(context);
                }
                cursor.close();
                return null;
            }
        }.execute();
    }

    public static void startImmediateSync(@NonNull final Context context) {
        Intent intentToSyncImmediately = new Intent(context, NewsSyncIntentService.class);
        context.startService(intentToSyncImmediately);
    }
}
