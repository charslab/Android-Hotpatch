# Android-Hotpatch
[![Github All Releases](https://img.shields.io/github/downloads/charslab/Android-Hotpatch/total.svg)](https://github.com/charslab/Android-Hotpatch/releases)
[![license](https://img.shields.io/github/license/mashape/apistatus.svg)](https://github.com/charslab/Android-Hotpatch/blob/master/LICENSE)
[![gitcheese.com](https://s3.amazonaws.com/gitcheese-ui-master/images/badge.svg)](https://www.gitcheese.com/donate/users/13789437/repos/87186100)

Update or fix an android app on the fly, without having to publish a new APK.


## Usage: 

1. Make a .jar library with your app's classes and methods that you want to be updatable (see [compiling your application as a library](https://github.com/charslab/Android-Hotpatch/blob/master/README.md#compiling-an-application-as-a-library-android-studio--eclipse))
2. Grab [Hotpatch.java](https://github.com/charslab/Android-Hotpatch/blob/master/app/src/main/java/com/chars/android_hotpatch/Hotpatch.java) and add it to your project
3. Load the .jar library you built earlier 

You might need to do a small refactor of your app's code, but **the advantages are many:**

- Quickly fix & deploy a patch for a method
- Add methods to classes
- Hotpatch does not need the app to restart
- Updating an app using Hotpatch **does not require root!**


## Quick usage demo:

Let's say we have a class that we want to use in our Android app, defined this way:

```JAVA
package com.chars.testlib.TestLib;

public class TestLib {
     public String getVersionString() {
        return "libversion 1.0";
     }
}
```

After making a .jar library of that class, deploy it to you device i.e in */sdcard/TestLib.jar*

In order to use it in your Android app, you must load it with Hotpatch

```JAVA
final String className = "com.chars.testlib.TestLib";
final String methods[] = {"getVersionString"};

final Hotpatch hotpatch = new Hotpatch();

try {
    hotpatch.loadLibrary("/sdcard/TestLib.jar", getApplicationContext());
    hotpatch.loadClass(className);
    hotpatch.loadMethods(className, methods);

    String result = (String)hotpatch.call(className, methods[0]);
    Log.d("AndroidHotpatch", result);

} catch (Exception e) {
    Log.e("AndroidHotpatch", Log.getStackTraceString(e));
}
```

The line
        
        String result = (String)hotpatch.call(className, methods[0]);
        
will execute the *getVersionString()* method, defined in class *TestLib*.

To update the library, just make a new .jar from an updated version of the class. For example:

```JAVA
package com.chars.testlib.TestLib;

public class TestLib {
     public String getVersionString() {
        return "libversion 2.0";
     }
}
```
Push the updated .jar to the same path as the previous. In your Android app, you can just call

        hotpatch.reload();
        
and you'll have your updated library loaded into the app. Now, whenever you execute *getVersionString()* you will get *"libversion 2.0"*

## Compiling an application as a library (Android Studio / Eclipse):

1. Start a new android project
2. Add the classes that you want to be updatable
3. Build an APK
4. Rename the .apk file to .jar


## Changelog

- v0.1 Alpha:
     - Support for methods
     - Implemented Hotpatch.loadLibrary()
     - Implemented Hotpatch.loadClass()
     - Implemented Hotpatch.loadMethods()
     - Implemented Hotpatch.reload()
     - Implemented Hotpatch.call()
