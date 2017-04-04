# Android-Hotpatch
[![Github All Releases](https://img.shields.io/github/downloads/charslab/Android-Hotpatch/total.svg)](https://github.com/charslab/Android-Hotpatch/releases)
[![license](https://img.shields.io/github/license/mashape/apistatus.svg)](https://github.com/charslab/Android-Hotpatch/blob/master/LICENSE)

Autopdate an android app on the fly

## Usage: 

## Example:


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



