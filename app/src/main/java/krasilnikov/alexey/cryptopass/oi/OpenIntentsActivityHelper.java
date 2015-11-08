package krasilnikov.alexey.cryptopass.oi;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;

import javax.inject.Inject;

import krasilnikov.alexey.cryptopass.ActionService;
import krasilnikov.alexey.cryptopass.Data;
import krasilnikov.alexey.cryptopass.scope.ActivityScoped;

@ActivityScoped
public class OpenIntentsActivityHelper {
    private static final int REQUEST_CODE_BASE = 100000;
    private static final int REQUEST_CODE_OI_EXPORT = REQUEST_CODE_BASE + 2;
    private static final int REQUEST_CODE_OI_IMPORT = REQUEST_CODE_BASE + 3;

    private final Activity mActivity;

    @Inject
    public OpenIntentsActivityHelper(Activity activity) {
        mActivity = activity;
    }

    public void startImport() {
        Intent intent = new Intent(OpenIntents.ACTION_PICK_FILE);
        intent.putExtra(OpenIntents.EXTRA_TITLE, "Cryptopass: Import bookmarks");
        intent.putExtra(OpenIntents.EXTRA_BUTTON_TEXT, "Open");
        tryStartOIFileManager(intent, REQUEST_CODE_OI_IMPORT);
    }

    public void startExport() {
        Intent intent = new Intent(OpenIntents.ACTION_PICK_FILE);
        intent.setData(Uri.parse("file:///bookmarks.json"));
        intent.putExtra(OpenIntents.EXTRA_TITLE, "Cryptopass: Export bookmarks");
        intent.putExtra(OpenIntents.EXTRA_BUTTON_TEXT, "Save");
        tryStartOIFileManager(intent, REQUEST_CODE_OI_EXPORT);
    }

    private void tryStartOIFileManager(Intent intent, int requestCode) {
        try {
            mActivity.startActivityForResult(intent, requestCode);
        } catch (ActivityNotFoundException ex) {
            Intent marketIntent = new Intent(Intent.ACTION_VIEW);
            marketIntent.addCategory(Intent.CATEGORY_BROWSABLE);
            marketIntent.setData(OpenIntents.FILEMANAGER_ON_MARKET);
            marketIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mActivity.startActivity(marketIntent);
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (Activity.RESULT_OK == resultCode) {
            switch (requestCode) {
                case REQUEST_CODE_OI_IMPORT: {
                    Intent intent = new Intent(Data.ACTION_IMPORT, data.getData());
                    intent.setClass(mActivity, ActionService.class);
                    mActivity.startService(intent);
                }
                break;
                case REQUEST_CODE_OI_EXPORT: {
                    Intent intent = new Intent(Data.ACTION_EXPORT, data.getData());
                    intent.setClass(mActivity, ActionService.class);
                    mActivity.startService(intent);
                }

                break;
            }
        }
    }
}
