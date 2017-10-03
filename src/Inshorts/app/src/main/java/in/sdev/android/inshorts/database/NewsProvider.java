package in.sdev.android.inshorts.database;

import android.annotation.TargetApi;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import in.sdev.android.inshorts.database.NewsContract.NewsEntry;


import static in.sdev.android.inshorts.Constants.ALL_NEWS;
import static in.sdev.android.inshorts.Constants.CONTENT_AUTHORITY;
import static in.sdev.android.inshorts.database.NewsContract.PATH_NEWS;

public class NewsProvider extends ContentProvider {
    public static final int CODE_NEWS = 100;
    public static final int CODE_NEWS_BY_CATEGORY = 101;
    public static final int CODE_NEWS_WITH_FILTER = 102;
    public static final int CODE_NEWS_BY_ID = 103;
    public static final int CODE_NEWS_BY_BOOKMARK = 108;

    private static final UriMatcher sUriMatcher = buildUriMatcher();

    private final String LOG_TAG = NewsProvider.class.getSimpleName();
    private NewsDbHelper mOpenHelper;

    public static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = CONTENT_AUTHORITY;
        matcher.addURI(authority, PATH_NEWS, CODE_NEWS);
        matcher.addURI(authority, PATH_NEWS + "/by_category/*", CODE_NEWS_BY_CATEGORY);
        matcher.addURI(authority, PATH_NEWS + "/*", CODE_NEWS_BY_ID);
        matcher.addURI(authority, PATH_NEWS + "/*/*/*/*", CODE_NEWS_WITH_FILTER);
        matcher.addURI(authority, PATH_NEWS + "/*/*/*", CODE_NEWS_BY_BOOKMARK);
        return matcher;
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new NewsDbHelper(getContext());
        return false;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor cursor;
        String[] selectionArguments;
        String selectionQuery;
        String newsSelectionQuery =  " ";
        switch (sUriMatcher.match(uri)) {            
            case CODE_NEWS_WITH_FILTER: {
                String newsCategoryString = uri.getPathSegments().get(2);
                String newsRefTypeColumnString = uri.getPathSegments().get(3);
                String newsRefTypeString = uri.getPathSegments().get(4);
                
                selectionArguments = new String[]{};
                selectionQuery = "";
                cursor = mOpenHelper.getReadableDatabase().query(
                        NewsEntry.TABLE_NAME,
                        projection,
                        selectionQuery,
                        selectionArguments,
                        null,
                        null,
                        sortOrder);
                break;
            }
            case CODE_NEWS_BY_CATEGORY: {
                String newsCategoryString = uri.getPathSegments().get(2);
                selectionArguments = new String[]{newsCategoryString};
                selectionQuery = NewsEntry.COLUMN_CATEGORY + " = ? " + newsSelectionQuery;
                if (newsCategoryString.equalsIgnoreCase(ALL_NEWS) || newsCategoryString.equalsIgnoreCase("")) {
                    selectionArguments = new String[]{};
                    selectionQuery = " 1=1 " + newsSelectionQuery;
                }
                cursor = mOpenHelper.getReadableDatabase().query(
                        NewsEntry.TABLE_NAME,
                        projection,
                        selectionQuery,
                        selectionArguments,
                        null,
                        null,
                        sortOrder);
                break;
            }
            case CODE_NEWS: {
                cursor = mOpenHelper.getReadableDatabase().query(
                        NewsEntry.TABLE_NAME,
                        projection,
                        null,
                        null,
                        null,
                        null,
                        sortOrder);
                break;
            }
            case CODE_NEWS_BY_ID:
                String news_id = NewsEntry.getNewsIdFromUri(uri);
                selectionArguments = new String[]{news_id};
                cursor = mOpenHelper.getReadableDatabase().query(
                        NewsEntry.TABLE_NAME,
                        projection,
                        NewsEntry.COLUMN_NEWS_ID + "=?",
                        selectionArguments,
                        null,
                        null,
                        sortOrder);
                break;

            case CODE_NEWS_BY_BOOKMARK:
                selectionArguments = new String[]{"true", String.valueOf(true)};
                cursor = mOpenHelper.getReadableDatabase().query(
                        NewsEntry.TABLE_NAME,
                        projection,
                        NewsEntry.COLUMN_IS_BOOKMARKED + "= ? OR " + NewsEntry.COLUMN_IS_BOOKMARKED + "= ? ",
                        selectionArguments,
                        null,
                        null,
                        sortOrder);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        Log.v(LOG_TAG, "Uri " + uri.toString());
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }


    @Nullable
    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case CODE_NEWS:
                return NewsEntry.CONTENT_TYPE;
            case CODE_NEWS_BY_CATEGORY:
                return NewsEntry.CONTENT_TYPE;
            case CODE_NEWS_WITH_FILTER:
                return NewsEntry.CONTENT_TYPE;
            case CODE_NEWS_BY_ID:
                return NewsEntry.CONTENT_ITEM_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri = null;
        switch (match) {
            case CODE_NEWS_BY_ID: {
                long _id = db.insert(NewsEntry.TABLE_NAME, null, values);
                if (_id > 0)
                    returnUri = NewsEntry.buildNewsById(String.valueOf(_id));
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int bulkInsert(@NonNull Uri uri, @NonNull ContentValues[] values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int returnCount = 0;
        int rowsInserted = 0;
        switch (sUriMatcher.match(uri)) {
            case CODE_NEWS:
            case CODE_NEWS_BY_ID:
                db.beginTransaction();
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(NewsEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            rowsInserted++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                if (rowsInserted > 0) {
                    //Log.v(LOG_TAG, "uri " + uri.toString() + " rowsInserted: " + rowsInserted);
                    getContext().getContentResolver().notifyChange(uri, null);
                }
                return rowsInserted;
            default:
                return super.bulkInsert(uri, values);
        }
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        int numRowsDeleted;
        if (null == selection) selection = "1";
        switch (sUriMatcher.match(uri)) {
            case CODE_NEWS:
                numRowsDeleted = mOpenHelper.getWritableDatabase().delete(
                        NewsEntry.TABLE_NAME,
                        selection,
                        selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if (numRowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return numRowsDeleted;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues contentValues, @Nullable String selection, @Nullable String[] selectionArgs) {
        int numRowsUpdated;
        if (null == selection) selection = "1";
        Uri returnUri;
        switch (sUriMatcher.match(uri)) {
            case CODE_NEWS_BY_ID:
            case CODE_NEWS:
                numRowsUpdated = mOpenHelper.getWritableDatabase().update(
                        NewsEntry.TABLE_NAME,
                        contentValues,
                        selection,
                        selectionArgs);
                if (numRowsUpdated > 0) {
                    getContext().getContentResolver().notifyChange(uri, null);
                }
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if (numRowsUpdated >= 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return numRowsUpdated;
    }

    @Override
    @TargetApi(11)
    public void shutdown() {
        mOpenHelper.close();
        super.shutdown();
    }

    private void normalizeDate(ContentValues values) {
    }
}

