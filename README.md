# Android-Hotpatch
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
