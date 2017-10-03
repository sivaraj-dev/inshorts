package in.sdev.android.inshorts.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import in.sdev.android.inshorts.database.NewsContract.NewsEntry;

public class NewsDbHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "inshorts.db";
    private static final int DATABASE_VERSION = 1;

    public NewsDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        final String SQL_CREATE_NEWS_TABLE =
                "CREATE TABLE " + NewsEntry.TABLE_NAME + " (" +
                        NewsEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        NewsEntry.COLUMN_NEWS_ID + " INTEGER NOT NULL, " +
                        NewsEntry.COLUMN_TITLE + " TEXT NOT NULL, " +
                        NewsEntry.COLUMN_URL + " TEXT NOT NULL, " +
                        NewsEntry.COLUMN_PUBLISHER + " TEXT NOT NULL, " +
                        NewsEntry.COLUMN_CATEGORY + " TEXT NULL, " +
                        NewsEntry.COLUMN_HOSTNAME  + " TEXT NULL, " +
                        NewsEntry.COLUMN_CONTENT  + " TEXT NULL, " +
                        NewsEntry.COLUMN_IS_BOOKMARKED + " BOOLEAN NOT NULL DEFAULT FALSE, " +
                        NewsEntry.COLUMN_TIMESTAMP + " LONG NOT NULL, " +
                        " UNIQUE (" + NewsEntry.COLUMN_NEWS_ID + ") ON CONFLICT REPLACE);";
        sqLiteDatabase.execSQL(SQL_CREATE_NEWS_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + NewsEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}
