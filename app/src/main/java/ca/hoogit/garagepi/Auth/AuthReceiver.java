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

import ca.hoogit.garagepi.Utils.BaseReceiver;
import ca.hoogit.garagepi.Utils.Consts;

/**
 * Created by jordon on 18/02/16.
 * Receiver class for auth broadcasts
 */
public class AuthReceiver extends BaseReceiver {

    private IAuthEvent mListener;

    public AuthReceiver(Context context) {
        super(context);
    }

    public AuthReceiver(Context context, IAuthEvent listener) {
        super(context);
        this.mListener = listener;
    }

    public void setListener(IAuthEvent listener) {
        this.mListener = listener;
    }

    @Override
    public String getFilterName() {
        return Consts.INTENT_MESSAGE_AUTH;
    }

    @Override
    public void messageReceived(String action, boolean status, String message) {
        if (mListener != null) {
            switch (action) {
                case Consts.ACTION_AUTH_LOGIN:
                    mListener.onLogin(status, message);
                    break;
                case Consts.ACTION_AUTH_LOGOUT:
                    mListener.onLogout(status, message);
                    break;
                case Consts.ERROR:
                    mListener.onError(message);
                    break;
            }
        }
    }
}
