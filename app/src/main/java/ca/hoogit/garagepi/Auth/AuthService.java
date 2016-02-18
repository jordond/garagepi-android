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

public class AuthService extends IntentService {

    private static final String TAG = AuthService.class.getSimpleName();

    private final Gson mGson = new Gson();

    public AuthService() {
        super("AuthService");
    }

    public static void startLogin(Context context) {
        startAuthService(context, Consts.ACTION_AUTH_LOGIN);
    }

    public static void startLogout(Context context) {
        startAuthService(context, Consts.ACTION_AUTH_LOGOUT);
    }

    private static void startAuthService(Context context, String action) {
        Intent intent = new Intent(context, AuthService.class);
        intent.setAction(action);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final boolean hasInternet = Helpers.isNetworkAvailable(getApplicationContext());
            final String action = intent.getAction();
            User user = UserManager.getInstance().user();

            if (action.equals(Consts.ACTION_AUTH_LOGIN)) {
                if (!hasInternet) {
                    broadcast(Consts.ERROR, false, getString(R.string.auth_message_no_internet));
                } else if (!user.canAuthenticate()) {
                    broadcast(Consts.ERROR, false, getString(R.string.auth_message_invalid_credentials));
                } else {
                    handleActionLogin(user);
                }
            } else if (action.equals(Consts.ACTION_AUTH_LOGOUT)) {
                handleActionLogout(user);
            }
        }
    }

    private void handleActionLogin(User user) {
        if (user.getToken().isEmpty() || "None".equals(user.getToken())) {
            authenticate(user);
        } else {
            if (validate(user)) {
                Log.d(TAG, "handleActionLogin: Token is valid, will try refreshing.");
                if (refresh(user)) {
                    Log.d(TAG, "handleActionLogin: Refresh was successful");
                    broadcast(Consts.ACTION_AUTH_LOGIN, true, getString(R.string.success_login));
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

    private void handleActionLogout(User user) {
        try {
            OkHttpClient client = Client.authClient(user.getToken());
            Request.Builder request = buildRequest(Consts.ACTION_AUTH_LOGOUT);
            if (request != null) {
                Response response = client.newCall(request.build()).execute();
                UserManager.getInstance().clear();
                Log.d(TAG, "handleActionLogout: response: " + response.isSuccessful() + " " + response.message());
                broadcast(Consts.ACTION_AUTH_LOGOUT, response.isSuccessful(), getString(R.string.success_logout));
            }
        } catch (IOException e) {
            handleException(Consts.ACTION_AUTH_LOGOUT, e);
        }
    }

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
                String message = "Login failed, invalid email or password.";
                if (response.isSuccessful()) {
                    TokenResponse t = mGson.fromJson(response.body().string(), TokenResponse.class);
                    message = "Successfully logged in.";
                    user.setToken(t.token);
                    user.save();
                }
                Log.d(TAG, "authenticate: status: " + response.isSuccessful() + " " + response.message());
                broadcast(Consts.ACTION_AUTH_LOGIN, response.isSuccessful(), message);
            }
        } catch (IOException e) {
            handleException(Consts.ACTION_AUTH_LOGIN, e);
        }
    }

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

    private Request.Builder buildRequest(String action) {
        try {
            return new Request.Builder().url(Helpers.getApiRoute("auth", action));
        } catch (MalformedURLException e) {
            Log.e(TAG, "buildRequest: Invalid server address " + e.getMessage(), e);
            broadcast(Consts.ERROR, false, getString(R.string.error_invalid_address));
        }
        return null;
    }

    private void handleException(String action, Exception e) {
        Log.e(TAG, "validate: Request failed " + e.getMessage(), e);
        broadcast(action, false, e.getMessage());
    }

    private void broadcast(String action, boolean wasSuccess, String message) {
        Intent authResponse = new Intent(Consts.INTENT_MESSAGE_AUTH);
        authResponse.putExtra(Consts.KEY_MESSAGE_AUTH_ACTION, action);
        authResponse.putExtra(Consts.KEY_MESSAGE_AUTH_SUCCESS, wasSuccess);
        authResponse.putExtra(Consts.KEY_MESSAGE_AUTH_MESSAGE, message);
        LocalBroadcastManager.getInstance(getApplication()).sendBroadcast(authResponse);
        Log.i(TAG, "broadcast: Auth status: " + wasSuccess + " - " + message);
    }
}
