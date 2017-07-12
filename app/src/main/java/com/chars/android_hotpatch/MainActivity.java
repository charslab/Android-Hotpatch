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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    protected TextView textViewResult;

    //Broadcast for updating text in the MainActivity's view, after Hotpatch update
    protected final BroadcastReceiver updateTextReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String text = intent.getStringExtra("version");
            textViewResult.setText(text);
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        registerReceiver(updateTextReceiver, new IntentFilter("update-textview"));

        textViewResult = (TextView)findViewById(R.id.textViewResult);
        final Button  buttonReload = (Button)findViewById(R.id.buttonReload);

        final String className = "com.chars.testlib.TestLib";
        final String methods[] = {"getVersionString"};

        final Hotpatch hotpatch = new Hotpatch();
        final String hotpatchPath = getFilesDir() + "/TestLib.jar";

        try {
            //github.com's ssl-certificate public key, hashed with sha256.
            //You can obtain a public key hash running by running:
            // ./get_key_sha256.sh github.com

            hotpatch.addSecureDomain("https://github.com", "sha256/pL1+qb9HTMRZJmuC/bB/ZI9d302BYrrqiVuRyW+DGrU=");
            hotpatch.downloadHotpatch("https://github.com/charslab/Android-Hotpatch/raw/master/TestLib/testlib_v1.0.jar",
                    hotpatchPath,
                    new Hotpatch.Callback() {
                        @Override
                        public void run() {
                            try {
                                hotpatch.loadLibrary(hotpatchPath, getApplicationContext());
                                hotpatch.loadClass(className);
                                hotpatch.loadMethods(className, methods);

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

        } catch (Exception e) {
            Log.e("AndroidHotpatch", Log.getStackTraceString(e));
        }

        buttonReload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("AndroidHotpatch", "Reloading (fetching new patch from remote)");

                try {
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

                } catch (Exception e) {
                    Log.d("AndroidHotpatch", Log.getStackTraceString(e));
                    Log.e("AndroidHotpatch", Log.getStackTraceString(e));
                }

            }
        });
    }
}
