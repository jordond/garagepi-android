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
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import ca.hoogit.garagepi.Auth.AuthManager;
import ca.hoogit.garagepi.Auth.AuthReceiver;
import ca.hoogit.garagepi.Auth.AuthService;
import ca.hoogit.garagepi.Auth.IAuthEvent;
import ca.hoogit.garagepi.Auth.User;
import ca.hoogit.garagepi.Auth.UserManager;
import ca.hoogit.garagepi.Controls.Door;
import ca.hoogit.garagepi.Controls.DoorManager;
import ca.hoogit.garagepi.Controls.DoorsFragment;
import ca.hoogit.garagepi.R;
import ca.hoogit.garagepi.Settings.SettingsActivity;
import ca.hoogit.garagepi.Socket.IConnectionEvent;
import ca.hoogit.garagepi.Socket.IDoorEvent;
import ca.hoogit.garagepi.Socket.SocketEvents;
import ca.hoogit.garagepi.Socket.SocketManager;
import ca.hoogit.garagepi.Update.UpdateManager;
import ca.hoogit.garagepi.Utils.Consts;
import ca.hoogit.garagepi.Utils.Helpers;
import ca.hoogit.garagepi.Utils.IBaseReceiver;
import ca.hoogit.garagepi.Utils.SharedPrefs;

public class MainActivity extends AppCompatActivity implements IAuthEvent, DoorManager.IQuery {

    @Bind(R.id.toolbar) Toolbar mToolbar;
    @Bind(R.id.container) ViewPager mViewPager;
    @Bind(R.id.tabs) TabLayout mTabLayout;

    private AuthManager mAuthManager;
    private UpdateManager mUpdateManager;
    private DoorManager mDoorManager;

    private SocketEvents mSocketEvents;

    private SectionsPagingAdapter mAdapter;
    private DoorsFragment mDoorsFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        mAuthManager = new AuthManager(this, this);
        mAuthManager.enableNotifications(mViewPager);
        mUpdateManager = new UpdateManager(this);
        mDoorManager = new DoorManager(this, this);


        // Set up the toolbar and the placeholder viewpager. // TODO Replace
        setSupportActionBar(mToolbar);
        mDoorsFragment = DoorsFragment.newInstance();
        mAdapter = new SectionsPagingAdapter(getSupportFragmentManager(), mDoorsFragment);
        mViewPager.setAdapter(mAdapter);
        mTabLayout.setupWithViewPager(mViewPager);

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

        mSocketEvents = new SocketEvents(this);
        mSocketEvents.on();
        mSocketEvents.onConnectionEvent(new IConnectionEvent() {
            @Override
            public void onConnected() {
                // TODO handle connection?
            }

            @Override
            public void onConnectionError(String message) {
                Toast.makeText(getApplication(), "Socket: " + message, Toast.LENGTH_LONG).show();
            }
        });
        mSocketEvents.onDoorEvent(changed -> {

        });

        SocketManager.getInstance().connect();
        SharedPrefs.getInstance().setFirstRun(false);
    }

    @Override
    public void onLogin(boolean wasSuccess, String message) {
        if (wasSuccess) {
            SocketManager.getInstance().connect();
            mDoorManager.query();
        }
        // TODO Start the socket and fragment views
        // Initialize the doors fragment, i.e. get pin data
        // mDoorsFragment.init()
        // If already init'd, mDoorFragment.refresh()
    }

    @Override
    public void onLogout(boolean wasSuccess, String message) {
        // TODO Handle logout by stopping all socket activity
        SocketManager.getInstance().disconnect();
    }

    @Override
    public void onError(String message) {

    }

    @Override
    public void onQuery(boolean wasSuccess, ArrayList<Door> response) {
        if (wasSuccess) {
            mDoorsFragment.setDoors(response);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Consts.RESULT_SETTINGS) {
            User user = UserManager.getInstance().user();
            if (user.canAuthenticate()) {
                mAuthManager.authenticate();
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
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent settings = new Intent(this, SettingsActivity.class);
            startActivityForResult(settings, Consts.RESULT_SETTINGS);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mAuthManager.register();
        mUpdateManager.register();
        mDoorManager.register();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mAuthManager.stop();
        mUpdateManager.stop();
        mDoorManager.stop();
        mSocketEvents.off();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SocketManager.getInstance().disconnect();
    }
}
