package com.chars.android_hotpatch;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

/**
 * Created by chars on 04/04/17.
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
