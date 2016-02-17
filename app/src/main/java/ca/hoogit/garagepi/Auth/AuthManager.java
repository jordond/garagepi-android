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

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;

import java.io.IOException;
import java.net.MalformedURLException;

import ca.hoogit.garagepi.Networking.Client;
import ca.hoogit.garagepi.Utils.Helpers;
import ca.hoogit.garagepi.Utils.SharedPrefs;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by jordon on 15/02/16.
 * Handles the logging in and authenticating of the user token
 */
public class AuthManager {

    private static final String TAG = AuthManager.class.getSimpleName();

    private final Gson mGson = new Gson();

    private Context mContext;
    private SharedPrefs mPrefs = SharedPrefs.getInstance();
    private User mUser;
    private String mAddress = mPrefs.getAddress();

    public AuthManager(Context context) {
        this.mContext = context;
        this.mUser = UserManager.getInstance().user();
    }

    private boolean hasNetwork() {
        return Helpers.isNetworkAvailable(mContext);
    }

    public void logout(IAuthResult callback) {
        if (hasNetwork() && mUser.canAuthenticate()) {
            try {
                OkHttpClient client = Client.authClient(mUser.getToken());
                Request request = new Request.Builder()
                        .url(Helpers.urlBuilder(mAddress, "auth", "logout"))
                        .build();

                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        Log.e(TAG, "onFailure: ", e);
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        Log.d(TAG, "onResponse: Logout status " + response.isSuccessful());
                    }
                });

            } catch (MalformedURLException e) {
                callback.onComplete(false, "Invalid server address");
            }
        }
        mUser = UserManager.getInstance().clear();
        callback.onComplete(true, "Successfully logged out");
    }

    public void login(IAuthResult callback) {
        if (mUser.getToken().isEmpty() || "None".equals(mUser.getToken())) {
            authenticate(callback);
        } else {
            validate((isValid, message) -> {
                if (isValid) {
                    Log.d(TAG, "login: Token is valid, will refresh instead of log in.");
                    refreshToken((wasSuccess, responseMessage) -> {
                        if (wasSuccess) {
                            callback.onComplete(true, responseMessage);
                        } else {
                            Log.d(TAG, "login: Refresh failed, attempting full authenticate");
                            authenticate(callback);
                        }
                    });
                } else {
                    Log.d(TAG, "login: Token was invalid, trying to log in.");
                    authenticate(callback);
                }
            });
        }
    }

    public void validate(IAuthResult callback) {
        if (!hasNetwork()) {
            callback.onComplete(false, "No internet connection is available");
        } else {
            try {
                OkHttpClient client = Client.authClient(mUser.getToken());
                Request request = new Request.Builder()
                        .url(Helpers.urlBuilder(mAddress, "auth", "valid"))
                        .build();

                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        Log.e(TAG, "authenticate onFailure: " + e.getMessage(), e);
                        callback.onComplete(false, "Server request failed");
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        callback.onComplete(response.isSuccessful(), response.message());
                    }
                });
            } catch (MalformedURLException e) {
                Log.e(TAG, "authenticate: Invalid server address" + e.getMessage(), e);
                callback.onComplete(false, "Invalid server address");
            }
        }
    }

    public void refreshToken(IAuthResult callback) {
        if (!hasNetwork()) {
            callback.onComplete(false, "No internet connection is available");
        } else {
            try {
                OkHttpClient client = Client.authClient(mUser.getToken());
                Request request = new Request.Builder()
                        .url(Helpers.urlBuilder(mAddress, "auth", "refresh"))
                        .build();
                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        callback.onComplete(false, e.getMessage());
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        String message = "Refreshing token failed";
                        if (response.isSuccessful()) {
                            Token token = mGson.fromJson(response.body().string(), Token.class);
                            Log.d(TAG, "refresh onResponse: Successfully refreshed: " + token.token);
                            message = "Successfully refreshed auth token";
                            mUser.setToken(token.token);
                            mUser.save();
                        }
                        callback.onComplete(response.isSuccessful(), message);
                    }
                });
            } catch (MalformedURLException e) {
                Log.e(TAG, "authenticate: Invalid server address" + e.getMessage(), e);
                callback.onComplete(false, e.getMessage());
            }
        }
    }

    public void authenticate(IAuthResult callback) {
        if (!Helpers.isNetworkAvailable(mContext)) {
            callback.onComplete(false, "No internet connection is available");
        } else if (!mUser.canAuthenticate()) {
            callback.onComplete(false, "Email or password is empty");
        } else {
            RequestBody formBody = new FormBody.Builder()
                    .add("email", mUser.getEmail())
                    .add("password", mUser.getPassword())
                    .build();
            try {
                String loginPath = Helpers.urlBuilder(mAddress, "auth", "local");
                Request request = new Request.Builder()
                        .url(loginPath)
                        .post(formBody)
                        .build();

                Client.get().newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        Log.e(TAG, "authenticate onFailure: " + e.getMessage(), e);
                        callback.onComplete(false, "Server request failed");
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        Token token = new Token();
                        String message;
                        if (!response.isSuccessful()) {
                            Log.i(TAG, "authenticate onResponse: User login attempt failed " + response.body().toString());
                            message = "Login failed, invalid email or password";
                        } else {
                            token = mGson.fromJson(response.body().string(), Token.class);
                            Log.d(TAG, "authenticate onResponse: Successfully authenticated: " + token.token);
                            message = "Login was successful";
                        }
                        mUser.setToken(token.token);
                        mUser.save();
                        callback.onComplete(response.isSuccessful(), message);
                    }
                });
            } catch (MalformedURLException e) {
                Log.e(TAG, "authenticate: Invalid server address" + e.getMessage(), e);
                callback.onComplete(false, "Invalid server address");
            } catch (Exception e) {
                Log.e(TAG, "authenticate: Exception: " + e.getMessage(), e);
                callback.onComplete(false, "Something went wrong");
            }
        }
    }

    private class Token {
        public String token = "";
    }

    public interface IAuthResult {
        void onComplete(boolean wasSuccess, String responseMessage);
    }
}
