/**
 Created by chars on 04/04/17.

 MIT License

 Copyright (c) 2017 Chars Lab

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
 */

package com.chars.android_hotpatch;

import android.content.Context;
import android.util.Log;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Set;

import dalvik.system.DexClassLoader;
import dalvik.system.DexFile;
import okhttp3.Call;
import okhttp3.CertificatePinner;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class Hotpatch {
    public static final String TAG = "AndroidHotpatch";
    public static final String VERSION = "android-hotpatch:v1.0.0b";

    private Context                         context;
    private String                          jarpath;
    private DexClassLoader                  dexClassLoader;
    private HashMap<String, Class<Object> > classLoaded;
    private HashMap<String, Object>         classInstance;
    private HashMap<String, Field>          fields;
    private HashMap<String, Method>         method;

    private HashMap<String, String>         domains;


    public void Hotpatch() {
        jarpath = null;
        dexClassLoader = null;
        classLoaded = null;
        classInstance = null;
        method = null;
        fields = null;
    }

    public void Hotpatch(String jarpath, Context context) {
        Hotpatch();
        loadLibrary(jarpath, context);
    }

    public void loadLibrary(String jarpath, Context context) {
        this.context = context;

        File jarfile = new File(jarpath);
        if(!jarfile.exists())
            throw new IllegalArgumentException("Could not find library: " + jarpath);


        this.jarpath = jarpath;

        File optimizedLibrarypath = context.getDir("dex", 0);

        File cachedDex = new File(optimizedLibrarypath.getPath() + "/" + jarpath.replace("jar", "dex"));
        Log.d(TAG, "Checking for cached version in " + optimizedLibrarypath.getAbsolutePath());

        File files[] = optimizedLibrarypath.listFiles();
        for (int i = 0;i < files.length; i++) {
            Log.d(TAG, "File: " + files[i].getName());

            if (files[i].getName().equals(jarfile.getName().replace("jar", "dex"))) {
                Log.d(TAG, "A cached " + files[i].getName() + " exists in " + optimizedLibrarypath.getAbsolutePath() + ". Removing it");
                files[i].delete();
            }
        }

        dexClassLoader = new DexClassLoader(jarpath, optimizedLibrarypath.getAbsolutePath(),
                null, context.getClassLoader());

    }

    @SuppressWarnings("unchecked")
    public void loadClass(String className) throws ClassNotFoundException,
                                                   IllegalAccessException,
                                                   InstantiationException {
        if (classLoaded == null)
            classLoaded = new HashMap<>();

        if (classLoaded.containsKey(className)) {
            Log.d(TAG, "Class " + className + " is already loaded. Unloading it");
            classLoaded.remove(className);
        }

        Log.d(TAG, "Loading class " + className);

        Class<Object> tmpClass = (Class<Object>)dexClassLoader.loadClass(className);
        classLoaded.put(className, tmpClass);

        if(classInstance == null)
            classInstance = new HashMap<>();

        Object tmpClassInstance = tmpClass.newInstance();
        classInstance.put(className, tmpClassInstance);


    }

    public void loadClasses(String classNames[]) throws IOException,
                                                        ClassNotFoundException,
                                                        IllegalAccessException,
                                                        InstantiationException {
        for (String className : classNames) {
            this.loadClass(className);
        }

    }

    public void loadClasses() throws IOException,
                                     ClassNotFoundException,
                                     IllegalAccessException,
                                     InstantiationException {
        DexFile dexFile = DexFile.loadDex(this.jarpath, File.createTempFile("opt", "dex", this.context.getCacheDir()).getPath(),
                0);


        String className = "";
        for(Enumeration<String> classNames =  dexFile.entries(); classNames.hasMoreElements();
            className = classNames.nextElement()) {

            if(!className.isEmpty()) {

                //Remove partial classes <class>$1..
                if(className.contains("$"))
                    className = className.substring(0, className.indexOf("$"));

                this.loadClass(className);
            }
        }
    }

    public void loadFields(String className) {
        Field fields[] = classLoaded.get(className).getFields();

        if (this.fields == null)
            this.fields = new HashMap<>();

        for (Field field : fields) {
            Log.d(TAG, "Field " + field.getName() + ": " + field.toGenericString());
            this.fields.put(className + ":" + field.getName(), field);
        }

    }

    public void loadMethods(String className, String methods[], Class<?>... parameterTypes) throws NoSuchMethodException {
        if (!classLoaded.containsKey(className))
            throw new IllegalArgumentException("Class " + className + " is not loaded");

        if (this.method == null)
            this.method = new HashMap<>();

        for (String methodName: methods) {
            Log.d(TAG, "Loading method " + className + "." + methodName);
            Method tmpMethod = classLoaded.get(className).getMethod(methodName);
            method.put(className + ":" + methodName, tmpMethod);
            Log.d(TAG, "Done");
        }
    }

    public void loadMethods(String className) {
        /* TOOD: fix java.lang.NoSuchMethodException: equals [] */

        if (!classLoaded.containsKey(className))
            throw new IllegalArgumentException("Class " + className + " is not loaded");

        if (this.method == null)
            this.method = new HashMap<>();

        Method methods[] = classLoaded.get(className).getMethods();

        for (Method method : methods) {
            String name = method.getName();

            this.method.put(className + ":" + name, method);
        }
    }

    public Object call(String className, String methodName, Object... args) throws IllegalAccessException,
            InvocationTargetException {

        if (!this.method.containsKey(className + ":" + methodName))
            throw new IllegalArgumentException("No such method " + methodName + " for class " + className);

        if (!this.classInstance.containsKey(className))
            throw new IllegalArgumentException("No instance for class " + className);

        Object classInstance = this.classInstance.get(className);
        Method method = this.method.get(className + ":" + methodName);

        return method.invoke(classInstance, args);
    }


    public void reload() throws InstantiationException, IllegalAccessException, ClassNotFoundException,
            NoSuchMethodException {
        this.loadLibrary(jarpath, context);

        Set<String> methodNames = method.keySet();

        for (String method_signature : methodNames) {
            String curr_class = method_signature.split(":")[0];
            String curr_method = method_signature.split(":")[1];

            String method_array[] =  { curr_method };

            this.loadClass(curr_class);
            this.loadMethods(curr_class, method_array);
        }

    }

    public void autoload() throws IOException,
                                  ClassNotFoundException,
                                  IllegalAccessException,
                                  InstantiationException {

        this.loadClasses();
        //load methods
        //load fields
    }



    public static class Callback {
        public void run() {

        }
    }

    public void addSecureDomain(String domain, String publicKeySha256) {
        if (domain.isEmpty())
            throw new IllegalArgumentException("Domain cannot be empty");

        if (publicKeySha256 == null)
            throw new IllegalArgumentException("Public key hash cannot be empty");


        if (this.domains == null) {
            this.domains = new HashMap<String, String>();
        }

        this.domains.put(domain, publicKeySha256);
    }

    public void downloadHotpatch(final String sourceUrl, final String dest, final Callback downloadedCallback) throws IOException {

        String domain = "";
        Set d = domains.keySet();
        for (Object cd : d) {
            if (sourceUrl.startsWith((String)cd)) {
                Log.d(TAG, "Found match, domain: " + cd);
                domain = (String) cd;
                break;
            }
        }

        if (domain.isEmpty())
            throw new IllegalArgumentException("Unkown domain in " + sourceUrl);



        String publicKey = domains.get(domain);
        CertificatePinner certificatePinner = new CertificatePinner.Builder()
                                                    .add(domain, publicKey)
                                                    .build();
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                                            .certificatePinner(certificatePinner)
                                            .build();

        Request request = new Request.Builder().url(sourceUrl).build();
        Log.d(TAG, "Downloading patch to " + dest);

        okHttpClient.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "Failed to download file: " + Log.getStackTraceString(e));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful())
                    throw new IOException("Failed to dowload file: " + response);

                File file = new File(dest);
                if (file.exists())
                    file.delete();

                FileOutputStream outputStream = new FileOutputStream(dest);
                DataOutputStream dataOutputStream = new DataOutputStream(outputStream);

                try {
                    dataOutputStream.write(response.body().bytes());

                } catch (NullPointerException e) {
                    Log.d(TAG, "Cannot write updated binary! " + Log.getStackTraceString(e));
                    throw new IOException(Log.getStackTraceString(e));

                } finally {
                    dataOutputStream.close();
                }



                if (downloadedCallback != null)
                    downloadedCallback.run();

            }
        });

    }
}
