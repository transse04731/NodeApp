package io.gigasource.appbuilt;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Utils {
    private Context context;
    private Process shellProcess;
    private Process mongodProcess;
    private ManagerProcess managerProcess = new ManagerProcess();
    private String assetName = "assets.zip";
    HashMap<String, Process> processMap = new HashMap<String, Process>();
    HashMap<String, Thread> threadMap = new HashMap<String, Thread>();

    public Utils(Context context) {
        this.context = context;
    }

    public void decompress() {
        Decompressor.unzip(this.context, assetName);
    }

    public void startBinaries(String appName) {
        startBinaries(appName, "", null);
    }

    public void startBinaries(String appName, String options) {
        startBinaries(appName, options, null);
    }

    public void startBinaries(String appName, String options, Runnable cb) {
        try {
            final List<String> environment = getEnv();
            environment.add("LD_LIBRARY_PATH=" + context.getApplicationInfo().dataDir + '/');
            Runtime.getRuntime().exec("chmod -R 777 " + context.getApplicationInfo().dataDir + '/' + appName);
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Process p = shellSpawn(context.getApplicationInfo().dataDir + '/' + appName + " " + options, environment.toArray(new String[environment.size()]));
            processMap.put(appName, p);
            threadMap.put(appName + "_log", new Thread(() -> {
                BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
                String line;
                boolean isStart = false;
                try {
                    while ((line = input.readLine()) != null) {
                        Log.d(appName, line);
                        if (!isStart && line.contains("listening")) {
                            isStart = true;
                            if (cb != null) {
                                try {
                                    Thread.sleep(500);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                cb.run();
                                try {
                                    Thread.sleep(2000);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                } catch (IOException e) {
                }
            }));

            threadMap.put(appName + "_err_log", new Thread(() -> {
                BufferedReader input2 = new BufferedReader(new InputStreamReader(p.getErrorStream()));
                String line;
                try {
                    while ((line = input2.readLine()) != null) {
                        Log.d(appName + "_err", line);
                    }
                } catch (IOException e) {
                }
            }));

            threadMap.get(appName + "_log").start();
            threadMap.get(appName + "_err_log").start();
        } catch (Exception e) {
        }
    }

//    public void startMongod() {
//        try {
//            if (this.mongodProcess == null) {
//                this.mongodProcess = Runtime.getRuntime().exec(context.getApplicationInfo().dataDir + "/mongod --dbpath=/sdcard/data --syncdelay 2 --setParameter diagnosticDataCollectionEnabled=true --nounixsocket --unixSocketPrefix=/sdcard/data");
//            }
//            Thread.sleep(500);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

    public void stopBinaries(String appName) {
        try {
            managerProcess.kill(new File(context.getApplicationInfo().dataDir + '/' + appName));
            threadMap.get(appName + "_log").interrupt();
            threadMap.get(appName + "_err_log").interrupt();
            processMap.get(appName).destroy();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private List<String> getEnv() {
        Map<String, String> map = System.getenv();
        List<String> environment = new ArrayList<>();
        for (String key : map.keySet()) {
            environment.add(key + "=" + map.get(key));
        }
        return environment;
    }

    private Process shellSpawn(String cmd, String[] env) {
        Process p = null;
        try {
            p = Runtime.getRuntime().exec(/*EnvUtils.isRooted() ? "su" : */"sh", env, new File(context.getApplicationInfo().dataDir));
            DataOutputStream dos = new DataOutputStream(p.getOutputStream());
            dos.writeBytes(String.format("%s\n", cmd));
            dos.flush();
            dos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return p;
    }
}
