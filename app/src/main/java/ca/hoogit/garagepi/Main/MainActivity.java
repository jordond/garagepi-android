/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2016 Jordon de Hoog
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package ca.hoogit.garagepi.Main;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;

import butterknife.Bind;
import butterknife.ButterKnife;
import ca.hoogit.garagepi.Auth.AuthManager;
import ca.hoogit.garagepi.Auth.IAuthEvent;
import ca.hoogit.garagepi.Auth.User;
import ca.hoogit.garagepi.Auth.UserManager;
import ca.hoogit.garagepi.Controls.DoorManager;
import ca.hoogit.garagepi.R;
import ca.hoogit.garagepi.Settings.SettingsActivity;
import ca.hoogit.garagepi.Socket.IConnectionEvent;
import ca.hoogit.garagepi.Socket.SocketManager;
import ca.hoogit.garagepi.Update.UpdateManager;
import ca.hoogit.garagepi.Utils.Consts;
import ca.hoogit.garagepi.Utils.SharedPrefs;

public class MainActivity extends AppCompatActivity implements IAuthEvent {

    private static final String TAG = MainActivity.class.getSimpleName();

    @Bind(R.id.container) ViewPager mViewPager;

    private AuthManager mAuthManager;
    private UpdateManager mUpdateManager;
    private DoorManager mDoorManager;

    private SocketManager mSocketManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        mAuthManager = new AuthManager(this, this);
        mAuthManager.enableNotifications(mViewPager);
        mUpdateManager = new UpdateManager(this);
        mDoorManager = new DoorManager(this);

        // Set up the toolbar and the placeholder viewpager.
        SectionsPagingAdapter mAdapter = new SectionsPagingAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mAdapter);

        // Check to make sure the user is authenticated
        User user = UserManager.getInstance().user();
        if (!user.canAuthenticate() || SharedPrefs.getInstance().isFirstRun()) {
            showCredentialsDialog(R.string.dialog_no_user_title, R.string.dialog_no_user_content);
        } else {
            if (savedInstanceState == null) {
                if (!mAuthManager.authenticate()) {
                    mDoorManager.query();
                }
                mUpdateManager.check();
            }
        }

        mSocketManager = new SocketManager(this);
        mSocketManager.onConnectionEvent(new IConnectionEvent() {
            @Override
            public void onConnected() {
                // TODO handle connection?
            }

            @Override
            public void onConnectionError(String message) {
                Toast.makeText(getApplication(), "Socket: " + message, Toast.LENGTH_LONG).show();
            }
        });
        mSocketManager.connect();

        SharedPrefs.getInstance().setFirstRun(false);
    }

    @Override
    public void onLogin(boolean wasSuccess, String message) {
        if (wasSuccess) {
            mSocketManager.refresh();
            mDoorManager.query();
        }
    }

    @Override
    public void onLogout(boolean wasSuccess, String message) {
        // TODO hide the fragment views and replace with placeholder image/text
        mSocketManager.disconnect();
    }

    @Override
    public void onError(String message) {

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Consts.RESULT_SETTINGS) {
            User user = UserManager.getInstance().user();
            if (user.canAuthenticate()) {
                if (!mAuthManager.authenticate()) {
                    mDoorManager.query();
                }
            } else {
                showCredentialsDialog(R.string.dialog_missing_cred, R.string.dialog_missing_cred_content);
            }
        }
    }

    private void showCredentialsDialog(int titleId, int contentID) {
        new MaterialDialog.Builder(this)
                .title(titleId)
                .content(contentID)
                .positiveText(R.string.dialog_okay)
                .cancelable(false)
                .onPositive((dialog, which) -> {
                    Intent settings = new Intent(this, SettingsActivity.class);
                    startActivityForResult(settings, Consts.RESULT_SETTINGS);
                }).build().show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent settings = new Intent(this, SettingsActivity.class);
            startActivityForResult(settings, Consts.RESULT_SETTINGS);
        } else if (id == R.id.action_fullscreen) {
            toggleFullscreen();
        }

        return super.onOptionsItemSelected(item);
    }

    public void toggleFullscreen() {
        int newUiOptions = getWindow().getDecorView().getSystemUiVisibility();
        newUiOptions ^= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        newUiOptions ^= View.SYSTEM_UI_FLAG_FULLSCREEN;
        newUiOptions ^= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;

        getWindow().getDecorView().setSystemUiVisibility(newUiOptions);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mAuthManager.register();
        mUpdateManager.register();
        mSocketManager.on();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mAuthManager.stop();
        mUpdateManager.stop();
        mSocketManager.off();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mSocketManager.disconnect();
    }
}