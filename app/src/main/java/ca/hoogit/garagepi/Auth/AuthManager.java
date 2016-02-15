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
import android.net.Uri;
import android.util.Log;

import com.google.gson.Gson;

import java.io.IOException;
import java.net.MalformedURLException;

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

    private final OkHttpClient mClient = new OkHttpClient();
    private final Gson mGson = new Gson();

    private Context mContext;
    private SharedPrefs mPrefs = SharedPrefs.getInstance();

    public AuthManager(Context context) {
        this.mContext = context;
    }

    public void authenticate(IAuthResult callback) {
        User user = UserManager.getInstance().get();
        if (!Helpers.isNetworkAvailable(mContext)) {
            callback.onFailure("No internet connection is available");
        } else if (!user.canAuthenticate()) {
            callback.onFailure("Email or password is empty");
        } else {
            RequestBody formBody = new FormBody.Builder()
                    .add("email", user.getEmail())
                    .add("password", user.getPassword())
                    .build();

            try {
                String loginPath = Helpers.urlBuilder(mPrefs.getAddress(), "auth", "local");
                Request request = new Request.Builder()
                        .url(loginPath)
                        .post(formBody)
                        .build();

                mClient.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        Log.e(TAG, "authenticate onFailure: " + e.getMessage(), e);
                        callback.onFailure("Server request failed");
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        if (!response.isSuccessful()) {
                            Log.i(TAG, "authenticate onResponse: User login attempt failed " + response.body().toString());
                            callback.onFailure("Login failed, invalid email or password");
                        } else {
                            Token token = mGson.fromJson(response.body().string(), Token.class);
                            Log.d(TAG, "authenticate onResponse: Successfully authenticated: " + token.token);
                            user.setToken(token.token);
                            user.setLastUpdated(System.currentTimeMillis());
                            user.save();
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
}
