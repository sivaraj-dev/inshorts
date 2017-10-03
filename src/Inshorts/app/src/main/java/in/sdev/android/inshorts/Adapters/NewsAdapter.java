package in.sdev.android.inshorts.Adapters;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.ArrayList;
import java.util.List;

import in.sdev.android.inshorts.NewsApplication;
import in.sdev.android.inshorts.R;
import in.sdev.android.inshorts.activities.NewsActivity;
import in.sdev.android.inshorts.database.NewsDbHelper;
import in.sdev.android.inshorts.interfaces.APIService;
import in.sdev.android.inshorts.utilities.ApiUtils;

import static android.text.Html.fromHtml;
import static in.sdev.android.inshorts.utilities.DateUtils.getTimeAgo;
import static in.sdev.android.inshorts.utilities.NewsPreferences.routeLink;

public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.NewsAdapterViewHolder> {
    private static final String LOG_TAG = NewsAdapter.class.getSimpleName();
    private static final int VIEW_TYPE_NEWS = 1;
    private final Context mContext;
    final private NewsAdapterOnClickHandler mClickHandler;
    private Integer mAttempts = 0;
    private Cursor mCursor;
    private Activity mActivity;
    private FirebaseAnalytics mFTracker;
    private APIService mAPIService;
    private int mFrameLayoutId = 0;
    private NewsDbHelper mOpenHelper;
    private List<Integer> mList = new ArrayList<Integer>();

    public NewsAdapter(@NonNull Context context, NewsAdapterOnClickHandler clickHandler) {
        mContext = context;
        mActivity = (Activity) mContext;
        mClickHandler = clickHandler;
        mAPIService = ApiUtils.getAPIService(mContext);
        mOpenHelper = new NewsDbHelper(context);
        mFTracker = ((NewsApplication) ((Activity) mContext).getApplication()).getFirebaseAnalytics();
    }

    @Override
    public NewsAdapterViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        int layoutId;
        switch (viewType) {
            case VIEW_TYPE_NEWS:
                layoutId = R.layout.list_item_news;
                break;
            default:
                throw new IllegalArgumentException("Invalid view type, value of " + viewType);
        }
        View view = LayoutInflater.from(mContext).inflate(layoutId, viewGroup, false);
        view.setFocusable(true);
        return new NewsAdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final NewsAdapterViewHolder viewHolder, final int position) {
        mCursor.moveToPosition(position);
        int newsId = mCursor.getInt(NewsActivity.INDEX_NEWS_ID);
        String title = mCursor.getString(NewsActivity.INDEX_TITLE);
        String url = mCursor.getString(NewsActivity.INDEX_URL);
        String publisher = mCursor.getString(NewsActivity.INDEX_PUBLISHER);
        String category = mCursor.getString(NewsActivity.INDEX_CATEGORY);
        String hostName = mCursor.getString(NewsActivity.INDEX_HOSTNAME);
        Long timestamp = mCursor.getLong(NewsActivity.INDEX_TIMESTAMP);

        timestamp = timestamp/1000;

        viewHolder.mPublisherView.setText(publisher);
        viewHolder.mTitleView.setText(title);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            viewHolder.mTimestampView.setText(fromHtml(mContext.getString(R.string.format_bullet_with_time_ago, getTimeAgo(mContext, timestamp)), Html.FROM_HTML_MODE_LEGACY));
        } else {
            viewHolder.mTimestampView.setText(fromHtml(mContext.getString(R.string.format_bullet_with_time_ago, getTimeAgo(mContext, timestamp))));
        }


    }

    @Override
    public int getItemViewType(int position) {
        //mCursor.moveToPosition(position);
        //String category = mCursor.getString(NewsActivity.INDEX_CATEGORY);
        return VIEW_TYPE_NEWS;
    }

    @Override
    public int getItemCount() {
        if (null == mCursor) return 0;
        return mCursor.getCount();
    }

    public void swapCursor(Cursor newCursor) {
        mCursor = newCursor;
        notifyDataSetChanged();
    }



    public interface NewsAdapterOnClickHandler {
        void onClick(String weatherForDay);
    }

    public class NewsAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public final TextView mPublisherView;
        public final TextView mTitleView;
        public final TextView mTimestampView;
        public final ImageView mIvActionMore;


        public NewsAdapterViewHolder(View view) {
            super(view);
            mPublisherView = (TextView) view.findViewById(R.id.tv_news_publisher);
            mTitleView = (TextView) view.findViewById(R.id.tv_news_title);
            mTimestampView = (TextView) view.findViewById(R.id.tv_news_timestamp);
            mIvActionMore = (ImageView) view.findViewById(R.id.action_more);

            view.setOnClickListener(this);
            mPublisherView.setOnClickListener(this);
            mTitleView.setOnClickListener(this);
            mTimestampView.setOnClickListener(this);
            mIvActionMore.setOnClickListener(this);
        }

        @Override
        public void onClick(final View v) {
            int adapterPosition = getAdapterPosition();
            mCursor.moveToPosition(adapterPosition);
            routeLink(mActivity, mContext, "WEBVIEW", mCursor.getString(NewsActivity.INDEX_TITLE),  mCursor.getString(NewsActivity.INDEX_URL), "", "", "");
        }
    }
}
