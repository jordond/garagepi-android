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

    public AuthManager(Context context) {
        this.mContext = context;
    }

    private boolean hasNetwork() {
        return Helpers.isNetworkAvailable(mContext);
    }

    public void login(IAuthResult callback) {
        mUser = UserManager.getInstance().user();
        if (mUser.getToken().isEmpty() || "None".equals(mUser.getToken())) {
            authenticate(callback);
        } else {
            validate((isValid, message) -> {
                if (isValid) {
                    Log.d(TAG, "login: Token is valid, will refresh instead of log in.");
                    refreshToken(success -> {
                        if (success) {
                            callback.onSuccess("Successfully refreshed token");
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

    public void validate(IValidateResult callback) {
        if (!hasNetwork()) {
            callback.onResponse(false, "No internet connection is available");
        } else {
            try {
                OkHttpClient client = Client.authClient(mPrefs.getToken());
                Request request = new Request.Builder()
                        .url(Helpers.urlBuilder(mPrefs.getAddress(), "auth", "valid"))
                        .build();

                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        Log.e(TAG, "authenticate onFailure: " + e.getMessage(), e);
                        callback.onResponse(false, "Server request failed");
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        callback.onResponse(response.isSuccessful(), response.message());
                    }
                });
            } catch (MalformedURLException e) {
                Log.e(TAG, "authenticate: Invalid server address" + e.getMessage(), e);
                callback.onResponse(false, "Invalid server address");
            }
        }
    }

    public void refreshToken(IRefreshResult callback) {
        if (!hasNetwork()) {
            callback.onResponse(false);
        } else {
            try {
                OkHttpClient client = Client.authClient(mPrefs.getToken());
                Request request = new Request.Builder()
                        .url(Helpers.urlBuilder(mPrefs.getAddress(), "auth", "refresh"))
                        .build();
                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        callback.onResponse(false);
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        if (response.isSuccessful()) {
                            Token token = mGson.fromJson(response.body().string(), Token.class);
                            Log.d(TAG, "refresh onResponse: Successfully refreshed: " + token.token);
                            mUser.setToken(token.token);
                            mUser.save();
                        }
                        callback.onResponse(response.isSuccessful());
                    }
                });
            } catch (MalformedURLException e) {
                Log.e(TAG, "authenticate: Invalid server address" + e.getMessage(), e);
                callback.onResponse(false);
            }
        }
    }

    public void authenticate(IAuthResult callback) {
        if (!Helpers.isNetworkAvailable(mContext)) {
            callback.onFailure("No internet connection is available");
        } else if (!mUser.canAuthenticate()) {
            callback.onFailure("Email or password is empty");
            // } else if (/* Token exists try validating/refreshing first */) { // TODO Implement
        } else {
            RequestBody formBody = new FormBody.Builder()
                    .add("email", mUser.getEmail())
                    .add("password", mUser.getPassword())
                    .build();
            try {
                String loginPath = Helpers.urlBuilder(mPrefs.getAddress(), "auth", "local");
                Request request = new Request.Builder()
                        .url(loginPath)
                        .post(formBody)
                        .build();

                Client.get().newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        Log.e(TAG, "authenticate onFailure: " + e.getMessage(), e);
                        callback.onFailure("Server request failed");
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        if (!response.isSuccessful()) {
                            Log.i(TAG, "authenticate onResponse: User login attempt failed " + response.body().toString());
                            mUser.setToken("");
                            mUser.save();
                            callback.onFailure("Login failed, invalid email or password");
                        } else {
                            Token token = mGson.fromJson(response.body().string(), Token.class);
                            Log.d(TAG, "authenticate onResponse: Successfully authenticated: " + token.token);
                            mUser.setToken(token.token);
                            mUser.save();
                            callback.onSuccess("Login was successful!");
                        }
                    }
                });
            } catch (MalformedURLException e) {
                Log.e(TAG, "authenticate: Invalid server address" + e.getMessage(), e);
                callback.onFailure("Invalid server address");
            } catch (Exception e) {
                Log.e(TAG, "authenticate: Exception: " + e.getMessage(), e);
                callback.onFailure("Something went wrong");
            }
        }
    }

    private class Token {
        public String token;
    }

    public interface IAuthResult {
        void onSuccess(String message);

        void onFailure(String error);
    }

    public interface IValidateResult {
        void onResponse(boolean isValid, String message);
    }

    public interface IRefreshResult {
        void onResponse(boolean success);
    }
}
