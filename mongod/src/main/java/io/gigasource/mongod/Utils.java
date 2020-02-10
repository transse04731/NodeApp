package io.gigasource.mongod;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Utils {
    private Context context;
    private Process shellProcess;
    private Process mongodProcess;
    private String assetName = "mongod.zip";

    public Utils(Context context) {
        this.context = context;
    }

    public void decompress() {
        Decompressor.unzip(this.context, assetName);
    }

    public void startMongo() {
        try {
            Runtime.getRuntime().exec("chmod -R 777 " + context.getApplicationInfo().dataDir + "/mongod");
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            this.mongodProcess = Runtime.getRuntime().exec(context.getApplicationInfo().dataDir + "/mongod --dbpath=/sdcard/data --syncdelay 2 --setParameter diagnosticDataCollectionEnabled=true --nounixsocket --unixSocketPrefix=/sdcard/data");
            new Thread(() -> {
                BufferedReader input = new BufferedReader(new InputStreamReader(this.mongodProcess.getInputStream()));
                String line;
                try {
                    while ((line = input.readLine()) != null) {
                        Log.d("mongod", line);
                    }
                } catch (IOException e) {
                }
            }).start();

            new Thread(() -> {
                BufferedReader input2 = new BufferedReader(new InputStreamReader(this.mongodProcess.getErrorStream()));
                String line;
                try {
                    while ((line = input2.readLine()) != null) {
                        Log.d("mongod " + "_err", line);
                    }
                } catch (IOException e) {
                }
            }).start();
        } catch (Exception e) {
        }
    }

    public void stopMongod() {
        try {
            this.mongodProcess.destroy();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
