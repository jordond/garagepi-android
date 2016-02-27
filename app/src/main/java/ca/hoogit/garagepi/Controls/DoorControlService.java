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

package ca.hoogit.garagepi.Controls;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import ca.hoogit.garagepi.Networking.Client;
import ca.hoogit.garagepi.R;
import ca.hoogit.garagepi.Utils.Consts;
import ca.hoogit.garagepi.Utils.Helpers;
import ca.hoogit.garagepi.Utils.SharedPrefs;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class DoorControlService extends IntentService {

    private static final String TAG = DoorControlService.class.getSimpleName();

    public DoorControlService() {
        super("DoorControlService");
    }

    private final Gson mGson = new Gson();

    public static void startActionQuery(Context context) {
        Intent intent = new Intent(context, DoorControlService.class);
        intent.setAction(Consts.ACTION_DOORS_QUERY);
        context.startService(intent);
    }

    public static void startActionToggle(Context context, String name) {
        Intent intent = new Intent(context, DoorControlService.class);
        intent.setAction(Consts.ACTION_DOORS_TOGGLE);
        intent.putExtra(Consts.KEY_DOOR_ID, name);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (Consts.ACTION_DOORS_QUERY.equals(action)) {
                handleActionQuery();
            } else if (Consts.ACTION_DOORS_TOGGLE.equals(action)) {
                String name = intent.getStringExtra(Consts.KEY_DOOR_ID);
                handleActionToggle(name);
            }
        }
    }

    private void handleActionQuery() {
        try {
            OkHttpClient client = Client.authClient(SharedPrefs.getInstance().getToken());
            Request request = new Request.Builder().url(Helpers.getApiRoute("api", "gpios")).build();

            Response response = client.newCall(request).execute();
            boolean success = response.isSuccessful();
            if (!success) {
                throw new IOException(getString(R.string.request_failed));
            }
            Door[] doors = mGson.fromJson(response.body().string(), Door[].class);
            response.body().close();
            ArrayList<Door> doorsList = new ArrayList<>();
            Collections.addAll(doorsList, doors);

            Intent intent = new Intent(Consts.INTENT_MESSAGE_DOORS);
            intent.putExtra(Consts.KEY_BROADCAST_ACTION, Consts.ACTION_DOORS_QUERY);
            intent.putExtra(Consts.KEY_BROADCAST_SUCCESS, true);
            intent.putExtra(Consts.KEY_DOORS, doorsList);
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
            Log.d(TAG, "handleActionQuery: Broadcasting door information");
        } catch (IOException e) {
            Log.e(TAG, "handleActionQuery: Request failed " + e.getMessage());
            Helpers.broadcast(this, Consts.INTENT_MESSAGE_DOORS, Consts.ACTION_DOORS_QUERY, false, e.getMessage());
        }
    }

    private void handleActionToggle(String name) {
        try {
            OkHttpClient client = Client.authClient(SharedPrefs.getInstance().getToken());
            Request request = new Request.Builder()
                    .url(Helpers.getApiRoute("api", "gpios", name.toLowerCase()))
                    .build();
            Response response = client.newCall(request).execute();
            boolean success = response.isSuccessful();
            if (!success) {
                throw new IOException(getString(R.string.request_failed));
            }
            ToggleResponse toggled = mGson.fromJson(response.body().string(), ToggleResponse.class);
            response.body().close();
            Intent intent = new Intent(Consts.INTENT_MESSAGE_DOORS);
            intent.putExtra(Consts.KEY_BROADCAST_ACTION, Consts.ACTION_DOORS_TOGGLE);
            intent.putExtra(Consts.KEY_BROADCAST_SUCCESS, toggled.toggled);
            intent.putExtra(Consts.KEY_DOOR_ID, name);
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        } catch (Exception e) {
            Log.e(TAG, "handleActionToggle: Request failed " + e.getMessage());
            Helpers.broadcast(this, Consts.INTENT_MESSAGE_DOORS, Consts.ACTION_DOORS_TOGGLE, false, e.getMessage());
        }
    }

    private class ToggleResponse {
        boolean toggled;
    }
}
