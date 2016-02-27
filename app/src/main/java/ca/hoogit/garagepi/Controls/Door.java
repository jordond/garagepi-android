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

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

/**
 * Created by jordon on 23/02/16.
 * Door model object
 */
public class Door implements Serializable, Parcelable {

    public String name;
    public Pin input;
    public Pin output;

    public class Pin implements Serializable {
        public int pin;
        public boolean value;
    }

    public String getStatus() {
        return this.input.value ? "Opened" : "Closed";
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (getClass() != o.getClass()) {
            return false;
        }
        final Door other = (Door) o;
        return !((this.name == null) ? (other.name != null) : !this.name.equals(other.name));
    }

    protected Door(Parcel in) {
        name = in.readString();
        input = (Pin) in.readValue(Pin.class.getClassLoader());
        output = (Pin) in.readValue(Pin.class.getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeValue(input);
        dest.writeValue(output);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<Door> CREATOR = new Parcelable.Creator<Door>() {
        @Override
        public Door createFromParcel(Parcel in) {
            return new Door(in);
        }

        @Override
        public Door[] newArray(int size) {
            return new Door[size];
        }
    };
}
