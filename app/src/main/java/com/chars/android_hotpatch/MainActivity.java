package com.chars.android_hotpatch;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

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

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final TextView textViewResult = (TextView)findViewById(R.id.textViewResult);
        final Button  buttonReload = (Button)findViewById(R.id.buttonReload);

        final String className = "com.chars.testlib.TestLib";
        final String methods[] = {"getVersionString"};

        final Hotpatch hotpatch = new Hotpatch();
        try {
            hotpatch.loadLibrary("/sdcard/TestLib.jar", getApplicationContext());
            hotpatch.loadClass(className);
            hotpatch.loadMethods(className, methods);

            String result = (String)hotpatch.call(className, methods[0]);
            textViewResult.setText(result);

        } catch (Exception e) {
            Log.e("AndroidHotpatch", Log.getStackTraceString(e));
            textViewResult.setText(e.getMessage());
        }


        buttonReload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("AndroidHotpatch", "Reloading");

                try {
                    hotpatch.reload();
                    String result = (String)hotpatch.call(className, methods[0]);
                    textViewResult.setText(result);

                } catch (Exception e) {
                    Log.e("AndroidHotpatch", Log.getStackTraceString(e));
                    textViewResult.setText(e.getMessage());
                }
            }
        });
    }
}
