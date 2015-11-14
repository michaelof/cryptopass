package krasilnikov.alexey.cryptopass.v21;

import android.app.Activity;
import android.view.View;

import javax.inject.Inject;

import krasilnikov.alexey.cryptopass.R;
import krasilnikov.alexey.cryptopass.v14.ProgressController;

public class ProgressControllerLollipop implements ProgressController {
    private final Activity mActivity;

    @Inject
    public ProgressControllerLollipop(Activity activity) {
        mActivity = activity;
    }

    @Override
    public void onCreate() {
    }

    private View mProgressBar;

    @Override
    public void setVisibility(boolean inOperation) {
        if (mProgressBar == null) {
            mProgressBar = mActivity.findViewById(R.id.progress);
        }

        mProgressBar.setVisibility(inOperation ? View.VISIBLE : View.INVISIBLE);
    }
}
