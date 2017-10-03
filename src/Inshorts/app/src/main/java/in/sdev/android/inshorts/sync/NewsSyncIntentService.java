package in.sdev.android.inshorts.sync;

import android.app.IntentService;
import android.content.Intent;

public class NewsSyncIntentService extends IntentService {
    public NewsSyncIntentService() {
        super("FeedSyncIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        NewsSyncTask.syncFeed(this);
    }
}
