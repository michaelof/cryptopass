package krasilnikov.alexey.cryptopass.v14;

import android.app.Activity;
import android.view.Window;

import javax.inject.Inject;

class ProgressControllerSandwitch implements ProgressController {
    private final Activity mActivity;

    @Inject
    public ProgressControllerSandwitch(Activity activity) {
        mActivity = activity;
    }

    @Override
    public void onCreate() {
        mActivity.requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        mActivity.setProgressBarIndeterminateVisibility(false);
    }

    @Override
    public void setVisibility(boolean inOperation) {
        mActivity.setProgressBarIndeterminateVisibility(inOperation);
    }
}
