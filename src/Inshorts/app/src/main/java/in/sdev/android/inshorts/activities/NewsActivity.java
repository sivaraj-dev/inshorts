package in.sdev.android.inshorts.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources.Theme;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.ThemedSpinnerAdapter;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.analytics.FirebaseAnalytics;

import in.sdev.android.inshorts.Adapters.NewsAdapter;
import in.sdev.android.inshorts.NewsApplication;
import in.sdev.android.inshorts.R;
import in.sdev.android.inshorts.database.NewsContract.NewsEntry;
import in.sdev.android.inshorts.sync.NewsSyncUtils;
import in.sdev.android.inshorts.utilities.NewsPreferences;

import static in.sdev.android.inshorts.Constants.mNewsCategory;
import static in.sdev.android.inshorts.Constants.mNewsCategoryText;
import static in.sdev.android.inshorts.R.id.spinner;
import static in.sdev.android.inshorts.sync.NewsSyncUtils.startImmediateSync;
import static in.sdev.android.inshorts.utilities.NewsPreferences.getNewsCategory;
import static in.sdev.android.inshorts.utilities.NewsPreferences.isFilterApplied;
import static in.sdev.android.inshorts.utilities.NewsPreferences.isNetworkAvailable;
import static in.sdev.android.inshorts.utilities.NewsPreferences.loadNews;
import static in.sdev.android.inshorts.utilities.NewsPreferences.sendEventTracker;
import static in.sdev.android.inshorts.utilities.NewsPreferences.setNewsPageDetails;

public class NewsActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    public static final String[] NEWS_FEED_PROJECTION = {
            NewsEntry.COLUMN_NEWS_ID,
            NewsEntry.COLUMN_TITLE,
            NewsEntry.COLUMN_URL,
            NewsEntry.COLUMN_PUBLISHER,
            NewsEntry.COLUMN_CATEGORY,
            NewsEntry.COLUMN_IS_BOOKMARKED,
            NewsEntry.COLUMN_TIMESTAMP
    };
    public static final int INDEX_NEWS_ID = 0;
    public static final int INDEX_TITLE = 1;
    public static final int INDEX_URL = 2;
    public static final int INDEX_PUBLISHER = 3;
    public static final int INDEX_CATEGORY = 4;
    public static final int INDEX_HOSTNAME = 5;
    public static final int INDEX_TIMESTAMP = 6;

    public static Boolean mHardReload = false;
    Spinner mNewsCategorySpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        // Setup spinner
        mNewsCategorySpinner = (Spinner) findViewById(spinner);
        mNewsCategorySpinner.setAdapter(new NewsCategorySpinnerAdapter(toolbar.getContext(), mNewsCategoryText));

        mNewsCategorySpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // When the given dropdown item is selected, show its contents in the
                // container view.
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.container, NewsFragment.newInstance(NewsActivity.this, position))
                        .commit();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_news, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();


        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int position = -1;
        switch (item.getItemId()){
            case R.id.nav_all_news:
                position=0;
                break;
            case R.id.nav_category_b:
                position=1;
                break;
            case R.id.nav_category_t:
                position=2;
                break;
            case R.id.nav_category_e:
                position=3;
                break;
            case R.id.nav_category_m:
                position=4;
                break;
        }
        Log.v("NewsActivity", "Position "+ position);
        if(position!=-1){
            mNewsCategorySpinner.setSelection(position);
            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
            drawer.closeDrawer(GravityCompat.START);
        }
        return true;
    }


    private static class NewsCategorySpinnerAdapter extends ArrayAdapter<String> implements ThemedSpinnerAdapter {
        private final ThemedSpinnerAdapter.Helper mDropDownHelper;

        public NewsCategorySpinnerAdapter(Context context, String[] objects) {
            super(context, android.R.layout.simple_list_item_1, objects);
            mDropDownHelper = new ThemedSpinnerAdapter.Helper(context);
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            View view;

            if (convertView == null) {
                // Inflate the drop down using the helper's LayoutInflater
                LayoutInflater inflater = mDropDownHelper.getDropDownViewInflater();
                view = inflater.inflate(android.R.layout.simple_list_item_1, parent, false);
            } else {
                view = convertView;
            }

            TextView textView = (TextView) view.findViewById(android.R.id.text1);
            textView.setText(getItem(position));

            return view;
        }

        @Override
        public Theme getDropDownViewTheme() {
            return mDropDownHelper.getDropDownViewTheme();
        }

        @Override
        public void setDropDownViewTheme(Theme theme) {
            mDropDownHelper.setDropDownViewTheme(theme);
        }
    }

    public static class NewsFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>,
            NewsAdapter.NewsAdapterOnClickHandler, SwipeRefreshLayout.OnRefreshListener, SharedPreferences.OnSharedPreferenceChangeListener {
        public final static int CODE_FILTER_CHANGE = 200;
        public static final int PAGE_SIZE = 25;
        public static final String KEY_TRANSITION = "KEY_TRANSITION";
        private static final String LOG_TAG = NewsFragment.class.getSimpleName();
        private static final String ARG_SECTION_NUMBER = "section_number";
        private static final int ID_FEED_LOADER = 1;
        static SwipeRefreshLayout mSwipeRefreshLayout;
        private static boolean PREFERENCES_HAVE_BEEN_UPDATED = false;
        private static Uri mUri;
        private static int mType;
        private static LinearLayout ll_error_message_display;
        Context mContext;
        FirebaseAnalytics mFTracker;
        private RecyclerView mRecyclerView;
        private NewsAdapter mNewsAdapter;
        private TextView mErrorMessageDisplay;
        private ProgressBar mLoadingIndicator;
        private int mPosition = RecyclerView.NO_POSITION;
        private MenuItem mMenuFilter;
        private LinearLayoutManager layoutManager;
        private boolean isLastPage = false;
        private int currentPage = 1;
        private int selectedSortByKey = 0;
        private int selectedSortOrderKey = 1;
        private boolean isLoading = false;
        private String sortByValue = "relevant";
        private String sortOrderValue = "desc";
        private String filter;
        private String query;
        private RecyclerView.OnScrollListener recyclerViewOnScrollListener = new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int visibleItemCount = layoutManager.getChildCount();
                int totalItemCount = layoutManager.getItemCount();
                int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();
                currentPage = (firstVisibleItemPosition / PAGE_SIZE) + 1;
                if (!isLoading && !isLastPage) {
                    if ((visibleItemCount + firstVisibleItemPosition) >= (totalItemCount - 1)
                            && firstVisibleItemPosition >= 0) {
                        loadMoreItems();
                    }
                }
            }
        };

        public NewsFragment() {
        }

        public static NewsFragment newInstance(Context context, int sectionNumber) {
            NewsFragment fragment = new NewsFragment();
            mUri = NewsEntry.CONTENT_URI;
            NewsPreferences.setNewsCategory(context, mNewsCategory[sectionNumber]);
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            mHardReload = true;

            return fragment;
        }

        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            switch (resultCode) {
                case CODE_FILTER_CHANGE:
                    getLoaderManager().restartLoader(ID_FEED_LOADER, null, this);
                    toggleFilterMenu();
                    mHardReload = true;
                    updateNews(getContext());
                    mSwipeRefreshLayout.setRefreshing(true);
                    break;
            }
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_news, container, false);
            mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerview_news);
            mSwipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipeRefreshLayout);
            mErrorMessageDisplay = (TextView) rootView.findViewById(R.id.tv_error_message_display);
            mLoadingIndicator = (ProgressBar) rootView.findViewById(R.id.pb_loading_indicator);
            ll_error_message_display = (LinearLayout) rootView.findViewById(R.id.ll_error_message_display);
            layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
            mRecyclerView.setLayoutManager(layoutManager);
            mNewsAdapter = new NewsAdapter(getActivity(), NewsFragment.this);
            mRecyclerView.setAdapter(mNewsAdapter);
            mRecyclerView.setHasFixedSize(true);
            mRecyclerView.addOnScrollListener(recyclerViewOnScrollListener);
            mSwipeRefreshLayout.setOnRefreshListener(this);
            if (mHardReload) {
                updateNews(getContext());
                mSwipeRefreshLayout.setRefreshing(true);
            }
            showLoading();
            setHasOptionsMenu(true);
            mContext = getContext();

            mFTracker = ((NewsApplication) getActivity().getApplication()).getFirebaseAnalytics();
            return rootView;
        }

        @Override
        public void onActivityCreated(@Nullable Bundle savedInstanceState) {
            getLoaderManager().initLoader(ID_FEED_LOADER, null, this);
            NewsSyncUtils.initialize(getContext());
            super.onActivityCreated(savedInstanceState);
        }

        @Override
        public void onStart() {
            super.onStart();
            if (PREFERENCES_HAVE_BEEN_UPDATED) {
                Log.d(LOG_TAG, "onStart: preferences were updated");
                getLoaderManager().initLoader(ID_FEED_LOADER, null, this);
                PREFERENCES_HAVE_BEEN_UPDATED = false;
            }
        }

        @Override
        public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
            inflater.inflate(R.menu.menu_news, menu);
            mMenuFilter = menu.findItem(R.id.action_feed_filter);
            super.onCreateOptionsMenu(menu, inflater);
            toggleFilterMenu();
        }

        @Override
        public void onPrepareOptionsMenu(Menu menu) {
            super.onPrepareOptionsMenu(menu);
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            switch (item.getItemId()) {
                case R.id.action_feed_filter:
                    Intent intentToStartDetailActivity = new Intent(getContext(), FilterActivity.class);
                    startActivityForResult(intentToStartDetailActivity, CODE_FILTER_CHANGE);
                    break;
                case R.id.action_bookmarks:
                    Intent intentToStartBookmarkActivity = new Intent(getContext(), BookmarkActivity.class);
                    startActivity(intentToStartBookmarkActivity);
                    sendEventTracker(mFTracker, mContext.getResources().getString(R.string.tracker_viewbookmarks, LOG_TAG), mContext.getResources().getString(R.string.track_click), "");
                    break;
            }
            return false;
        }

        private void showLoading() {
            mSwipeRefreshLayout.setVisibility(View.INVISIBLE);
            mLoadingIndicator.setVisibility(View.VISIBLE);
            ll_error_message_display.setVisibility(View.GONE);
        }

        public void toggleFilterMenu() {
            if (NewsPreferences.isTagWithNewsCategoryApplied(getContext())) {
                //mMenuFilter.setIcon(R.drawable.ic_filter_applied);
            } else {
                //mMenuFilter.setIcon(R.drawable.ic_filter_feed);
            }
        }

        public void updateNews(Context context) {
            if (!isNetworkAvailable(context)) {
                mSwipeRefreshLayout.setRefreshing(false);
                Toast.makeText(context, context.getResources().getString(R.string.error_no_internet_connection), Toast.LENGTH_SHORT).show();
                isLoading = false;
                return;
            }
            currentPage = 0;
            isLoading = true;

            startImmediateSync(context);
        }

        /*
        * Method which help the recyler view to go to top on clicking home when present in feed fragment
        * */
        public void gotoTop() {
            LinearLayoutManager linearLayoutManager = (LinearLayoutManager) mRecyclerView.getLayoutManager();
            linearLayoutManager.smoothScrollToPosition(mRecyclerView, null, 0);
        }

        private void loadMoreItems() {
            isLoading = true;
            currentPage += 1;
            //Log.v(LOG_TAG, "Loadmore items " + currentPage);
            setNewsPageDetails(mContext, String.valueOf(currentPage), String.valueOf(PAGE_SIZE));
            loadNews(mContext, false);
        }

        @Override
        public Loader<Cursor> onCreateLoader(int loaderId, Bundle bundle) {
            switch (loaderId) {
                case ID_FEED_LOADER:
                    String selection = "";
                    String[] selectionArgs = null;
                    String sortOrder = NewsEntry.COLUMN_TIMESTAMP + " DESC";
                    if (isFilterApplied(getContext())) {
                        String feedType = getNewsCategory(getContext());
                        mUri = NewsEntry.buildNewsByType(feedType);
                        Log.v(LOG_TAG, "Uri filter applied " + mUri);
                    }
                    return new CursorLoader(getActivity(),
                            mUri,
                            NEWS_FEED_PROJECTION,
                            null,
                            null,
                            sortOrder);
                default:
                    throw new RuntimeException("Loader Not Implemented: " + loaderId);
            }
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            mNewsAdapter.swapCursor(data);
            isLoading = false;
            if (mPosition == RecyclerView.NO_POSITION) mPosition = 0;
            if (mHardReload) {
                mSwipeRefreshLayout.setRefreshing(false);
                mHardReload = false;
            }
            if (data.getCount() != 0) showNewsDataView();
            else {
                if (!isLoading) {
                    mLoadingIndicator.setVisibility(View.GONE);
                    showErrorMessage();
                }
            }
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
            mNewsAdapter.swapCursor(null);
        }

        private void showNewsDataView() {
            ll_error_message_display.setVisibility(View.GONE);
            mSwipeRefreshLayout.setVisibility(View.VISIBLE);
            mLoadingIndicator.setVisibility(View.GONE);
        }

        private void showErrorMessage() {
            mSwipeRefreshLayout.setVisibility(View.GONE);
            ll_error_message_display.setVisibility(View.VISIBLE);
        }

        @Override
        public void onClick(String feedId) {
            Context context = getActivity();
            Class destinationClass = WebViewActivity.class;
            Intent intentToStartDetailActivity = new Intent(context, destinationClass);
            intentToStartDetailActivity.putExtra(Intent.EXTRA_TEXT, feedId);
            startActivity(intentToStartDetailActivity);
        }

        @Override
        public void onRefresh() {
            mHardReload = true;
            updateNews(getContext());
            // clear_cache(getContext());
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
            PREFERENCES_HAVE_BEEN_UPDATED = true;
        }
    }

}
