package in.sdev.android.inshorts.utilities;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import in.sdev.android.inshorts.database.NewsContract;
import in.sdev.android.inshorts.database.NewsContract.NewsEntry;

public class NewsJsonUtils {
    private static final String LOG_TAG = NewsJsonUtils.class.getSimpleName();
    private static final String NS_ID = "ID";
    private static final String NS_TITLE = "TITLE";
    private static final String NS_URL = "URL";
    private static final String NS_PUBLISHER = "PUBLISHER";
    private static final String NS_CATEGORY = "CATEGORY";
    private static final String NS_HOSTNAME = "HOSTNAME";
    private static final String NS_TIMESTAMP = "TIMESTAMP";

    public static void updateNewsToLocal(Context context, String jsonStr)
            throws JSONException {
        JSONArray newsJsonArray = new JSONArray(jsonStr);
        ContentValues[] newsContentValues = new ContentValues[newsJsonArray.length()];
        for (int i = 0; i < newsJsonArray.length(); i++) {
            JSONObject newsObj = newsJsonArray.getJSONObject(i);
            ContentValues newsValues = new ContentValues();
            newsValues.put(NewsEntry.COLUMN_NEWS_ID, newsObj.getInt(NS_ID));
            newsValues.put(NewsEntry.COLUMN_TITLE, newsObj.getString(NS_TITLE));
            newsValues.put(NewsEntry.COLUMN_URL, newsObj.getString(NS_URL));
            newsValues.put(NewsEntry.COLUMN_PUBLISHER, newsObj.getString(NS_PUBLISHER));
            newsValues.put(NewsEntry.COLUMN_CATEGORY, newsObj.getString(NS_CATEGORY));
            newsValues.put(NewsEntry.COLUMN_HOSTNAME, newsObj.getString(NS_HOSTNAME));
            newsValues.put(NewsEntry.COLUMN_TIMESTAMP, newsObj.getString(NS_TIMESTAMP));
            newsContentValues[i] = newsValues;
        }
        if (newsContentValues != null && newsContentValues.length != 0) {
            ContentResolver feedContentResolver = context.getContentResolver();
            feedContentResolver.bulkInsert(
                    NewsContract.NewsEntry.CONTENT_URI,
                    newsContentValues);
        }
    }

}
