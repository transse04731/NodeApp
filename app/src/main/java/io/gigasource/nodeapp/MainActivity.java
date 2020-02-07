package io.gigasource.nodeapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import io.gigasource.appbuilt.Utils;

public class MainActivity extends AppCompatActivity {
    public Utils utils = new Utils(this);
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        this.utils.createShellProcess();
        if (askForPermission()) {
            this.utils.decompress();
//            this.utils.startMongod();
            this.utils.startBinaries("app_built");
        }
    }

    private boolean askForPermission() {
        int permission = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_EXTERNAL_STORAGE: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                    this.utils.decompress();
//                    this.utils.startMongod();
//                    this.utils.startNodeApp(() -> runOnUiThread(() -> mainWebView.loadUrl("http://localhost:8888")));
                }
                return;
            }
        }
    }

    @Override
    public void onBackPressed() {
        this.utils.stopBinaries("app_built");
        super.onBackPressed();
    }
}
