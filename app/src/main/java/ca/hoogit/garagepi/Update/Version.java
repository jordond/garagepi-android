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

import android.text.TextUtils;

import ca.hoogit.garagepi.BuildConfig;
import ca.hoogit.garagepi.Utils.SharedPrefs;

/**
 * Created by jordon on 16/02/16.
 * Model object for version
 */
public class Version {

    private String name;
    private String hash;
    private String branch;

    public Version() {
        this.name = BuildConfig.VERSION_NAME;
        this.hash = BuildConfig.GitHash;
        this.branch = SharedPrefs.getInstance().getBranch();
    }

    public Version(String name, String hash, String branch) {
        this.name = name;
        this.hash = hash;
        this.branch = branch;
    }

    public String getName() {
        return name;
    }

    public String getHash() {
        return hash;
    }

    public String getBranch() {
        return branch;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

    public String toString() {
        String name = "Name: " + this.name;
        String hash = "Hash: " + this.hash;
        String branch = "Branch: " + this.branch;
        return TextUtils.join("\n", new String[]{name, hash, branch});
    }

    public boolean isNewer(String hash) {
        if (hash.length() > 7) {
            hash = hash.substring(0, 7);
        }
        return !this.hash.equals(hash);
    }

    public static String output() {
        String currentVersion = "Name: " + BuildConfig.VERSION_NAME;
        String hash = "Hash: " + BuildConfig.GitHash;
        String branch = "Branch: " + SharedPrefs.getInstance().getBranch();
        return TextUtils.join("\n", new String[]{currentVersion, hash, branch});
    }
}
