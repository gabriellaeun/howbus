package com.example.howbus;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Context;
import android.content.Intent;
import android.location.*;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.text.*;
import android.widget.*;
import android.appwidget.*;
import android.printservice.*;
import android.Manifest.permission;
import com.pedro.library.AutoPermissions;
import com.pedro.library.AutoPermissionsListener;

public class MainActivity extends AppCompatActivity implements AutoPermissionsListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        AutoPermissions.Companion.loadAllPermissions(this, 101);

        Button mapbutton = (Button) findViewById(R.id.mapbutton);
        mapbutton.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                    Intent intent = new Intent(MainActivity.this, Map.class);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[],
                                           int[] grantresults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantresults);
        AutoPermissions.Companion.parsePermissions(this, requestCode, permissions, this);
    }

    @Override
    public void onDenied(int requestCode, String[] permissions) {
        Toast.makeText(this, "승인이 거부됨.", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onGranted(int requestCode, String[] permissions) {
        Toast.makeText(this, "승인이 허가됨." , Toast.LENGTH_LONG).show();
    }
}