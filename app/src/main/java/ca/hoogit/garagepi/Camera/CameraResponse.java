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

package ca.hoogit.garagepi.Camera;

/**
 * Created by jordon on 02/03/16.
 * Container for all {@link CameraSocket} responses
 */
public class CameraResponse {

    /**
     * JSON to POJO for {@link CameraSocket#getInfo()}
     */
    public class Info {

        public boolean ready;
        public boolean isCapturing;
        public Error error;
        public String message;

        public class Error {
            public String message;
            public boolean hasError;
        }

        @Override
        public String toString() {
            if (error != null) {
                return "Has error: " + error.message;
            }
            return "Ready: " + ready + ", is capturing: " + isCapturing + "\n" + message;
        }
    }

    /**
     * JSON to POJO container
     */

    /**
     * JSON to POJO container for {@link CameraSocket#onError}
     */
    public class Error {

        public String title;
        public String message;
        public Info info;

        public class Info {
            public String device;
        }

    }

}
