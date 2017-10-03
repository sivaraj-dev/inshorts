package in.sdev.android.inshorts.database;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

import static in.sdev.android.inshorts.Constants.CONTENT_AUTHORITY;

public class NewsContract {

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_NEWS = "news";


    public static final class NewsEntry implements BaseColumns {
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_NEWS).build();
        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_NEWS;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_NEWS;
        public static final String TABLE_NAME = "news";
        public static final String COLUMN_NEWS_ID = "news_id";
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_URL = "url";
        public static final String COLUMN_PUBLISHER = "publisher";
        public static final String COLUMN_CATEGORY = "category";
        public static final String COLUMN_HOSTNAME = "hostname";
        public static final String COLUMN_CONTENT = "content";
        public static final String COLUMN_IS_BOOKMARKED = "is_bookmarked";
        public static final String COLUMN_TIMESTAMP = "timestamp";


        public static Uri buildNewsUriWithFilter(String newsCategory, String filter_type, String filter_id) {
            return CONTENT_URI.buildUpon()
                    .appendPath("filter")
                    .appendPath(newsCategory)
                    .appendPath(filter_type)
                    .appendPath(filter_id)
                    .build();
        }

        public static Uri buildNewsByType(String newsCategory) {
            return CONTENT_URI.buildUpon()
                    .appendPath("by_category")
                    .appendPath(newsCategory)
                    .build();
        }

        public static Uri buildNewsById(String id) {
            return CONTENT_URI.buildUpon()
                    .appendPath(id)
                    .build();
        }

        public static String getNewsIdFromUri(Uri uri) {
            return uri.getPathSegments().get(1);
        }

        public static Uri buildNewsByUid(String uid) {
            return CONTENT_URI.buildUpon()
                    .appendPath("by_uid")
                    .appendPath(uid)
                    .build();
        }

        public static Uri buildNewsByBookmark(String uid) {
            return CONTENT_URI.buildUpon()
                    .appendPath("my")
                    .appendPath("bookmark")
                    .appendPath(uid)
                    .build();
        }

        public static String getUidFromUri(Uri uri) {
            return uri.getPathSegments().get(2);
        }
    }


}
