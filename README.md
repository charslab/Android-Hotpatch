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

## Dependencies:
     
 okhttp (>= 3.8.1)
 
 Add it to your gradle files:
 
     compile 'com.squareup.okhttp3:okhttp:3.8.1'

## Quick usage demo:

Let's say we have a class that we want to use in our Android app, defined this way:

```JAVA
package com.chars.testlib;

public class TestLib {
     public String getVersionString() {
        return "libversion 1.0";
     }
}
```

After making a .jar library of that class, deploy it to your device i.e in your app private storage path (*getFilesDir()*)

In order to use it in your Android app, you must load it with Hotpatch

```JAVA
final String className = "com.chars.testlib.TestLib";
final String methods[] = {"getVersionString"};

final Hotpatch hotpatch = new Hotpatch();

try {
    hotpatch.loadLibrary(getFilesDir() + "/TestLib.jar", getApplicationContext());
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

## OTA Update

To patch or update your app remotely, you have to setup trusted domains first. This prevents attacks like DNS spoofing.
While downloading the patch file, Android-Hotpatch will perform certificate pinning, to make sure the patch is being dowloaded from **your** server. 

1. **Obtain your server's certificate publick key:**
     
     You can obtain the sha256-hashed public key of your certificate with: 
     
       ./get_key_sha256.sh yourdomain.com
     
     For example, using github.com:
               
       ./get_key_sha256.sh github.com
               
     The output will be: 
     
       /businessCategory=Private Organization/jurisdictionC=US/jurisdictionST=Delaware/serialNumber=5157550/street=88 Colin P Kelly, Jr Street/postalCode=94107/C=US/ST=California/L=San Francisco/O=GitHub, Inc./CN=github.com
       pL1+qb9HTMRZJmuC/bB/ZI9d302BYrrqiVuRyW+DGrU=
       /C=US/O=DigiCert Inc/OU=www.digicert.com/CN=DigiCert SHA2 Extended Validation Server CA
       RRM1dGqnDFsCJXBTHky16vi1obOlCgFFn/yOhI/y+ho=
     
     *pL1+qb9HTMRZJmuC/bB/ZI9d302BYrrqiVuRyW+DGrU=* is the hashed public key. 
     
2. **Add trusted domain name to Android-Hotpatch domains list**

     Add the domain-key pair to Android-Hotpatch:
     
     ```JAVA
     hotpatch.addSecureDomain("https://github.com", "sha256/pL1+qb9HTMRZJmuC/bB/ZI9d302BYrrqiVuRyW+DGrU=");
     ```
     *Note:* prepend the hash you obtained in the previous step with 'sha256/'
     
3. **Download and apply patch file**

     You can now download your updated patch from your domain with
          
     ```JAVA
     hotpatch.downloadHotpatch("https://yourdomain.com/url/to/patch.jar", path, callback);
     ```
          
     Example:
          
     ```JAVA
     hotpatch.downloadHotpatch("https://github.com/charslab/Android-Hotpatch/raw/master/TestLib/testlib_v2.0.jar",
                       hotpatchPath,
                       new Hotpatch.Callback() {
                           @Override
                           public void run() {
                               try {
                                   hotpatch.reload();
                                   Log.d("AndroidHotpatch", "Hotpatch update completed");

                                   String result = (String) hotpatch.call(className, methods[0]);
                                   Intent update_text = new Intent("update-textview");
                                   update_text.putExtra("version", result);
                                   sendBroadcast(update_text);
                               } catch (Exception e) {
                                   Log.e("AndroidHotpatch", Log.getStackTraceString(e));
                                   textViewResult.setText(e.getMessage());
                               }

                           }
     });
     ```

See [MainActivity.java](https://github.com/charslab/Android-Hotpatch/blob/master/app/src/main/java/com/chars/android_hotpatch/MainActivity.java) for usage example.

## Compiling an application as a library (Android Studio / Eclipse):

1. Start a new android project
2. Add the classes that you want to be updatable
3. Build an APK
4. Rename the .apk file to .jar


## Changelog
- v1.0 Beta:
     - Certificate pinning for OTA updates
     - Bugfixes
     
- v1.0 Alpha:
     - Support for methods
     - Implemented Hotpatch.loadLibrary()
     - Implemented Hotpatch.loadClass()
     - Implemented Hotpatch.loadMethods()
     - Implemented Hotpatch.reload()
     - Implemented Hotpatch.call()
