package krasilnikov.alexey.cryptopass.sync;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;

import javax.inject.Inject;

import krasilnikov.alexey.cryptopass.Data;
import krasilnikov.alexey.cryptopass.R;

/**
 * This class contains methods for maintain ACTION_SEND intents.
 */
public class SendHelper {
    private final Activity mActivity;
    @Inject
    public SendHelper(Activity activity) {
        mActivity = activity;
    }

    public void sendInExtra(String data) {
        Intent intent = makeBaseIntent();
        intent.putExtra(Intent.EXTRA_TEXT, data);
        sendIntent(intent);
    }

    public void sendByProvider() {
        Intent intent = makeBaseIntent();
        intent.putExtra(Intent.EXTRA_STREAM, Data.makeExportUri(mActivity));
        sendIntent(intent);
    }

    private Intent makeBaseIntent() {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT,  mActivity.getString(R.string.export_file_name));
        return intent;
    }

    private void sendIntent(Intent intent) {
        try {
            mActivity.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            // If there are no apps that can handle SEND intent,
            // suggest user to install Google Drive.
            try {
                Intent marketIntent = new Intent(Intent.ACTION_VIEW);
                marketIntent.addCategory(Intent.CATEGORY_BROWSABLE);
                marketIntent.setData(Uri.parse("https://play.google.com/store/apps/details?id=com.google.android.apps.docs"));
                marketIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mActivity.startActivity(marketIntent);
            } catch (ActivityNotFoundException ignored) {
                // No Google Play and no web browser? Hm.
            }
        }
    }
}
