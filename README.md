# Garage Pi for Android
Android client for garage pi control system.  To be paired with the [jordond/garagepi](http://github.com/jordond/garagepi) project.

Checkout the develop branch for a more up-to-date version.

[![Build Status](https://ci.hoogit.ca/buildStatus/icon?job=GaragePi.android.master)](https://ci.hoogit.ca/job/GaragePi.android.master/)

# Installing
This will not be released on the Play Store as it is for a very specific setup.  Therefore I have made an update feature of the app that will download the apk itself.

1. Grab the apk from [here](https://ci.hoogit.ca/job/GaragePi.android.master/lastSuccessfulBuild/artifact/app/build/outputs/apk/app-release.apk)
  1. Or the unstable version from [here](https://ci.hoogit.ca/job/GaragePi.android.develop/lastSuccessfulBuild/artifact/app/build/outputs/apk/app-release.apk)
2. Allow your device to install apps from unknown sources.
  2. `Settings > Security > Unknown Sources`
3. Open the apk and install.
4. On first run you will be prompted to enter your server credentials.

# Updating
1. In the settings you can enable the unstable branch.
2. The app will check for an update and prompt to download if there is one available.

# License

```
The MIT License (MIT)
Copyright (c) 2016 Jordon de Hoog

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```
