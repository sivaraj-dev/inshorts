package in.sdev.android.inshorts;

public final class Constants {
    public static final String CONTENT_AUTHORITY = "in.sdev.app.inshorts.news";
    public static final String ALL_NEWS = "All News";
    public static final String[] mNewsCategory = {ALL_NEWS, "b", "t", "e", "h" };
    public static final String[] mNewsCategoryText = {
            ALL_NEWS,
            "Business",
            "Science and Technology",
            "Entertainment",
            "Health"
    };
    public static final String PREF_NEWS_CATEGORY = "news_category";
    public static final String PREF_NEWS_PUBLISHER = "ref_type";
    public static final String PREF_REF_ID = "ref_id";
    public static final String PREF_PAGE_NO = "page_no";
    public static final String PREF_PAGE_SIZE = "page_size";
    public static final String[] SHARE_PACKAGE_NAMES = { "com.whatsapp", "com.twitter.android", "com.facebook.katana" };
    public static final String STATIC_NS_URL = "http://starlord.hackerearth.com";
}
