package krasilnikov.alexey.cryptopass.sync;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.MenuItem;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResolvingResultCallbacks;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.drive.Drive;

import javax.inject.Inject;

import krasilnikov.alexey.cryptopass.scope.ActivityScoped;

/**
 * Created by zeac on 24.02.16.
 */
@ActivityScoped
public class SyncAuthController {
    private static final int RESULT_SIGN_IN = 2000;
    private static final int RESULT_CONNECTION_RESOLUTION = 2001;

    private final Activity mActivity;
    private final GoogleApiClient mGoogleApiClient;

    @Nullable
    private GoogleSignInAccount mSignedAccount;

    @Nullable
    private Status mUnresolvableFailure;

    @Nullable
    private ConnectionResult mConnectionFailed;

    @Inject
    public SyncAuthController(Activity activity) {
        mActivity = activity;

        GoogleSignInOptions gso =
                new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).
                        requestScopes(Drive.SCOPE_APPFOLDER).
                        requestEmail().
                        build();

        mGoogleApiClient = new GoogleApiClient.Builder(activity).
                addApi(Auth.GOOGLE_SIGN_IN_API, gso).
                addOnConnectionFailedListener(mConnectionFailedListener).
                build();

        Auth.GoogleSignInApi.silentSignIn(mGoogleApiClient).
                setResultCallback(new ResolvingCallback(mActivity, RESULT_SIGN_IN));
    }

    GoogleApiClient.OnConnectionFailedListener mConnectionFailedListener =
            new GoogleApiClient.OnConnectionFailedListener() {
                @Override
                public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                    handleConnectionFailed(connectionResult);
                }
            };

    private class ResolvingCallback extends ResolvingResultCallbacks<GoogleSignInResult> {
        protected ResolvingCallback(@NonNull Activity activity, int requestCode) {
            super(activity, requestCode);
        }

        @Override
        public void onSuccess(@NonNull GoogleSignInResult googleSignInResult) {
            handleSignResult(googleSignInResult);
        }

        @Override
        public void onUnresolvableFailure(@NonNull Status status) {
            handleUnresolvableFailure(status);
        }
    }

    private void connect() {
        mGoogleApiClient.connect(GoogleApiClient.SIGN_IN_MODE_OPTIONAL);
    }

    private void disconnect() {
        mGoogleApiClient.disconnect();
    }

    private void reconnect() {
        disconnect();
        connect();
    }

    void handleConnectionFailed(ConnectionResult result) {
        mConnectionFailed = result;
    }

    void handleSignResult(GoogleSignInResult result) {
        mUnresolvableFailure = null;
        if (result != null && result.isSuccess()) {
            mSignedAccount = result.getSignInAccount();
        } else {
            mSignedAccount = null;
        }
    }

    void handleUnresolvableFailure(Status status) {
        mUnresolvableFailure = status;
        if (status.getStatusCode() == ConnectionResult.SIGN_IN_REQUIRED) {
            // This is resolvable failure
            mUnresolvableFailure = null;
        }
    }

    public void onStart() {
        connect();
    }

    public void onStop() {
        disconnect();
    }

    public void onPrepareOptionsMenu(MenuItem item) {
        if (mGoogleApiClient.isConnecting()) {
            item.setTitle("Checking");
        } else if (mConnectionFailed != null) {
            // On devices without google play services
            if (mConnectionFailed.getErrorCode() == ConnectionResult.SERVICE_MISSING) {
                item.setVisible(false);
            }
            item.setTitle(mConnectionFailed.toString());
        } else if (mUnresolvableFailure != null) {
            switch (mUnresolvableFailure.getStatusCode()) {
                default:
                    item.setTitle(mUnresolvableFailure.getStatusMessage());
                    break;
            }
        } else {
            if (mSignedAccount != null) {
                item.setTitle("Disable synchronisation on " + mSignedAccount.getDisplayName());
            } else {
                item.setTitle("Enable synchronisation (Google)");
            }
        }
    }

    public void onItemSelected() {
        if (mConnectionFailed != null) {
            if (mConnectionFailed.hasResolution()) {
                try {
                    mConnectionFailed.startResolutionForResult(mActivity,
                            RESULT_CONNECTION_RESOLUTION);
                } catch (IntentSender.SendIntentException e) {
                    // And what we can do?
                }
            }
        } else if (mUnresolvableFailure != null) {
            if (mUnresolvableFailure.hasResolution()) {
                try {
                    mUnresolvableFailure.startResolutionForResult(mActivity, 1001);
                } catch (IntentSender.SendIntentException e) {
                    mUnresolvableFailure = null;
                    reconnect();
                }
            }
        } else if (mSignedAccount != null) {
            Auth.GoogleSignInApi.signOut(mGoogleApiClient);

            handleSignResult(null);
            reconnect();
        } else {
            Intent intent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);

            mActivity.startActivityForResult(intent, RESULT_SIGN_IN);
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (Activity.RESULT_CANCELED == resultCode)
            return;

        if (RESULT_SIGN_IN == requestCode) {
            handleSignResult(Auth.GoogleSignInApi.getSignInResultFromIntent(data));
        } else if (RESULT_CONNECTION_RESOLUTION == requestCode) {
            reconnect();
        }
    }
}
