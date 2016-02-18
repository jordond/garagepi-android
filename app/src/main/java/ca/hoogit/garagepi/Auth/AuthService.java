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

package ca.hoogit.garagepi.Auth;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.gson.Gson;

import java.io.IOException;
import java.net.MalformedURLException;

import ca.hoogit.garagepi.Networking.Client;
import ca.hoogit.garagepi.R;
import ca.hoogit.garagepi.Utils.Consts;
import ca.hoogit.garagepi.Utils.Helpers;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Service for handling all of the server side authentication
 * Called using the static helper functions
 */
public class AuthService extends IntentService {

    private static final String TAG = AuthService.class.getSimpleName();

    private final Gson mGson = new Gson();

    public AuthService() {
        super("AuthService");
    }

    /**
     * Start the service with the intent to login
     * @param context Calling activity reference
     */
    public static void startLogin(Context context) {
        startAuthService(context, Consts.ACTION_AUTH_LOGIN);
    }

    /**
     * Start the service with the intent to logout
     * @param context Calling activity reference
     */
    public static void startLogout(Context context) {
        startAuthService(context, Consts.ACTION_AUTH_LOGOUT);
    }

    /**
     * Helper method to handle launching of the service
     * @param context Calling activity reference
     * @param action Service action
     */
    private static void startAuthService(Context context, String action) {
        Intent intent = new Intent(context, AuthService.class);
        intent.setAction(action);
        context.startService(intent);
    }

    /**
     * Call the appropriate message based on the Intent action
     * If login, check for network and user, then call login handler,
     * for logout always call logout regardless
     * @param intent Contains intended action
     */
    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final boolean hasInternet = Helpers.isNetworkAvailable(getApplicationContext());
            final String action = intent.getAction();
            User user = UserManager.getInstance().user();

            if (action.equals(Consts.ACTION_AUTH_LOGIN)) {
                if (!hasInternet) {
                    Helpers.broadcast(this, Consts.ERROR, false, getString(R.string.auth_message_no_internet));
                } else if (!user.canAuthenticate()) {
                    Helpers.broadcast(this, Consts.ERROR, false, getString(R.string.auth_message_invalid_credentials));
                } else {
                    handleActionLogin(user);
                }
            } else if (action.equals(Consts.ACTION_AUTH_LOGOUT)) {
                handleActionLogout(user);
            }
        }
    }

    /**
     * Attempt to log the user in with the stored credentials.
     * If there is no user token then always try to authenticate.  If a token exists check to see
     * if it is still a valid token, if so refresh that token, and if not then re-authenticate.
     * Broadcast the success or failure
     * @param user Credentials for server
     */
    private void handleActionLogin(User user) {
        if (user.getToken().isEmpty() || "None".equals(user.getToken())) {
            authenticate(user);
        } else {
            if (validate(user)) {
                Log.d(TAG, "handleActionLogin: Token is valid, will try refreshing.");
                if (refresh(user)) {
                    Log.d(TAG, "handleActionLogin: Refresh was successful");
                    Helpers.broadcast(this, Consts.ACTION_AUTH_LOGIN, true, getString(R.string.success_login));
                } else {
                    Log.d(TAG, "handleActionLogin: Refresh failed, attempting full authenticate");
                    authenticate(user);
                }
            } else {
                Log.d(TAG, "handleActionLogin: Token was invalid, trying to log in.");
                authenticate(user);
            }
        }
    }

    /**
     * Attempt to gracefully log the user out, if there is not network than it will fail, but that
     * will not affect this application.  A successful attempt is preferred as it will correctly
     * log the user out on the server.
     * @param user Credentials for user
     */
    private void handleActionLogout(User user) {
        try {
            OkHttpClient client = Client.authClient(user.getToken());
            Request.Builder request = buildRequest(Consts.ACTION_AUTH_LOGOUT);
            if (request != null) {
                Response response = client.newCall(request.build()).execute();
                UserManager.getInstance().clear();
                Log.d(TAG, "handleActionLogout: response: " + response.isSuccessful() + " " + response.message());
                Helpers.broadcast(this, Consts.ACTION_AUTH_LOGOUT, response.isSuccessful(), getString(R.string.success_logout));
            }
        } catch (IOException e) {
            handleException(Consts.ACTION_AUTH_LOGOUT, e);
        }
    }

    /**
     * Authenticate the user by building form-encoded fields, broadcast the status of the attempt.
     * @param user
     */
    private void authenticate(User user) {
        try {
            OkHttpClient client = Client.get();
            RequestBody formBody = new FormBody.Builder()
                    .add(Consts.FIELD_LOGIN, user.getEmail())
                    .add(Consts.FIELD_PASSWORD, user.getPassword())
                    .build();
            Request.Builder request = buildRequest(Consts.ACTION_AUTH_LOGIN);
            if (request != null) {
                request.post(formBody).build();
                Response response = client.newCall(request.build()).execute();
                String message = getString(R.string.error_auth_invalid);
                if (response.isSuccessful()) {
                    TokenResponse t = mGson.fromJson(response.body().string(), TokenResponse.class);
                    message = getString(R.string.success_auth);
                    user.setToken(t.token);
                    user.save();
                }
                Log.d(TAG, "authenticate: status: " + response.isSuccessful() + " " + response.message());
                Helpers.broadcast(this, Consts.ACTION_AUTH_LOGIN, response.isSuccessful(), message);
            }
        } catch (IOException e) {
            handleException(Consts.ACTION_AUTH_LOGIN, e);
        }
    }

    /**
     * Check to see if the token is still valid by sending it to the server.
     * @param user Credentials for server
     * @return boolean Token is valid or invalid
     */
    private boolean validate(User user) {
        try {
            OkHttpClient client = Client.authClient(user.getToken());
            Request.Builder request = buildRequest(Consts.ACTION_AUTH_TOKEN_VALIDATE);
            if (request != null) {
                Response response = client.newCall(request.build()).execute();
                Log.d(TAG, "validate: response: " + response.isSuccessful() + " " + response.message());
                return response.isSuccessful();
            }
        } catch (IOException e) {
            handleException(Consts.ACTION_AUTH_TOKEN_VALIDATE, e);
        }
        return false;
    }

    /**
     * Attempt to refresh the user token.
     * If it is successful than edit the user object and save it.
     * @param user Credentials for server
     * @return boolean Status of token refresh
     */
    private boolean refresh(User user) {
        try {
            OkHttpClient client = Client.authClient(user.getToken());
            Request.Builder request = buildRequest(Consts.ACTION_AUTH_TOKEN_REFRESH);
            if (request != null) {
                Response response = client.newCall(request.build()).execute();
                String message = getString(R.string.error_token_refresh);
                if (response.isSuccessful()) {
                    TokenResponse t = mGson.fromJson(response.body().string(), TokenResponse.class);
                    message = getString(R.string.success_token_refresh);
                    user.setToken(t.token);
                    user.save();
                }
                Log.d(TAG, "refresh: " + message + " - " + response.message());
                return response.isSuccessful();
            }
        } catch (IOException e) {
            handleException(Consts.ACTION_AUTH_TOKEN_REFRESH, e);
        }
        return false;
    }

    /**
     * Helper method to parse the user entered server address, adding the action to the
     * getApiRoute() builder function.  Catch any errors with a malformed URL.
     * @param action Server action
     * @return Request.Builder with the proper url
     */
    private Request.Builder buildRequest(String action) {
        try {
            return new Request.Builder().url(Helpers.getApiRoute("auth", action));
        } catch (MalformedURLException e) {
            Log.e(TAG, "buildRequest: Invalid server address " + e.getMessage(), e);
            Helpers.broadcast(this, Consts.ERROR, false, getString(R.string.error_invalid_address));
        }
        return null;
    }

    /**
     * Helper method to handle any IO exceptions
     * @param action Action where the exception occurred
     * @param e Caught exception
     */
    private void handleException(String action, Exception e) {
        Log.e(TAG, "validate: Request failed " + e.getMessage(), e);
        Helpers.broadcast(this, action, false, e.getMessage());
    }
}
