package io.gigasource.mongod;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class Decompressor {
    public static void unzip(Context context, String fileFromAssets) {
        try {
            InputStream in = context.getAssets().open(fileFromAssets);
            ZipInputStream zin = new ZipInputStream(in);
            ZipEntry ze = null;
            while ((ze = zin.getNextEntry()) != null) {
                if (ze.getName().endsWith("/")) {
                    new File(context.getApplicationInfo().dataDir + '/' + ze.getName()).mkdir();
                    continue;
                }
                Log.v("Decompress", "Unzipping " + ze.getName());
                FileOutputStream fout = new FileOutputStream(context.getApplicationInfo().dataDir + '/' + ze.getName());
                byte[] buffer = new byte[4096];
                int length = 0;
                while ((length = zin.read(buffer)) > 0) {
                    fout.write(buffer, 0, length);
                }
                fout.close();
                zin.closeEntry();
            }
            zin.close();
        } catch (Exception e) {
            Log.e("Decompress", "unzip", e);
        } finally {
            Log.v("Decompress", "Finished");
        }
    }
}
