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

package ca.hoogit.garagepi.Update;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

import ca.hoogit.garagepi.Networking.Client;
import ca.hoogit.garagepi.R;
import ca.hoogit.garagepi.Utils.Consts;
import ca.hoogit.garagepi.Utils.Helpers;
import ca.hoogit.garagepi.Utils.SharedPrefs;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okio.BufferedSink;
import okio.Okio;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class UpdateService extends IntentService {

    private static final String TAG = UpdateService.class.getSimpleName();
    private final Gson mGson = new Gson();

    public UpdateService() {
        super("UpdateService");
    }

    public static void startUpdateCheck(Context context) {
        startService(context, Consts.ACTION_UPDATE_CHECK);
    }

    public static void startDownload(Context context) {
        startService(context, Consts.ACTION_UPDATE_DOWNLOAD);
    }

    private static void startService(Context context, String action) {
        Intent intent = new Intent(context, UpdateService.class);
        intent.setAction(action);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (Consts.ACTION_UPDATE_CHECK.equals(action)) {
                handleActionCheck();
            } else if (Consts.ACTION_UPDATE_DOWNLOAD.equals(action)) {
                handleActionDownload();
            }
        }
    }

    private void handleActionCheck() {
        try {
            OkHttpClient client = Client.get();
            Version currentVersion = new Version();
            Request request = new Request.Builder()
                    .url(getString(R.string.github_api_root) +
                            getString(R.string.github_repo) +
                            getString(R.string.github_api_path) +
                            currentVersion.getBranch())
                    .build();

            Response response = client.newCall(request).execute();
            if (!response.isSuccessful()) {
                throw new IOException("Update check failed");
            }
            GitApiResponse result = mGson.fromJson(response.body().string(), GitApiResponse.class);
            response.body().close();
            String message = getString(R.string.no_update);
            boolean hasNewerVersion = currentVersion.isNewer(result.object.sha);
            if (hasNewerVersion) {
                message = getString(R.string.update_available);
            }
            Log.i(TAG, "handleActionCheck: " + message);
            Helpers.broadcast(this, Consts.ACTION_UPDATE_CHECK, hasNewerVersion, message);
        } catch (IOException e) {
            Log.e(TAG, "handleActionCheck: Error has occurred", e);
            Helpers.broadcast(this, Consts.ERROR, false, e.getMessage());
        }
    }

    private void handleActionDownload() {
        // TODO store a list of downloaded git hash's, that way the same one isn't always downloaded, say if the CI build fails
        try {
            OkHttpClient client = Client.get();
            String url = getString(R.string.download_root) + Version.getBuildBranch() + getString(R.string.download_paths);
            Request request = new Request.Builder().url(url).build();

            Response response = client.newCall(request).execute();
            if (!response.isSuccessful()) {
                throw new IOException("Download failed" + response.message());
            }
            Helpers.broadcast(this, Consts.ACTION_UPDATE_DOWNLOAD_STARTED, true, "Update download has started");

            File cacheDir = getExternalCacheDir();
            if (cacheDir == null) {
                throw new IOException("Could not get cache directory");
            }
            File downloadedFile = new File(cacheDir.getAbsolutePath(), getString(R.string.download_filename));
            BufferedSink sink = Okio.buffer(Okio.sink(downloadedFile));
            sink.writeAll(response.body().source());
            sink.close();

            Intent install = new Intent(Intent.ACTION_VIEW);
            install.setDataAndType(Uri.fromFile(downloadedFile), "application/vnd.android.package-archive");
            install.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(install);

            Helpers.broadcast(this, Consts.ACTION_UPDATE_DOWNLOAD_FINISHED, true, "Update download has finished");
            response.body().close();
        } catch (IOException e) {
            Log.e(TAG, "handleActionCheck: Error has occurred", e);
            Helpers.broadcast(this, Consts.ACTION_UPDATE_DOWNLOAD_FINISHED, false, e.getMessage());
        }
    }
}
