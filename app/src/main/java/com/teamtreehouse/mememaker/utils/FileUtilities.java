package com.teamtreehouse.mememaker.utils;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.widget.Toast;

import com.teamtreehouse.mememaker.R;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by Evan Anger on 7/28/14.
 */
public class FileUtilities {

    public static void saveAssetImage(Context context, String assetName) {
        // 1.Create a file handle so we have somewhere that we can write to;
        //File fileDirectory = context.getFilesDir();
        //Use this file directory to create a file handle to write to
        File fileToWrite = new File(getFileDirectory(context), assetName);
        //The variable fileToWrite will give us a file instead the path of FileDirectory, but named
        //assetName which when written to, will write data to it in internal storage
        // 2.Get access to our asset files so we can read them;
        AssetManager assetManager = context.getAssets();
        //Create a file stream for input and output
        try {
            InputStream in = assetManager.open(assetName);
            FileOutputStream out = new FileOutputStream(fileToWrite);
            //Copy the contents from input stream into output stream
            copyFile(in, out);
            in.close();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static File getFileDirectory(Context context){
        //Using SharedPreferences to pick which location to use
        String storageType = StorageType.INTERNAL;
        //Get directory from internal storage
        if(storageType.equals(StorageType.INTERNAL)){
            return context.getFilesDir();
        }else {
            if(isExternalStorageAvailable()){
                if(storageType.equals(StorageType.PRIVATE_EXTERNAL)){
                    return context.getExternalFilesDir(null);
                }
                else{
                    return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
                }
            }else {
                Toast.makeText(context, context.getString(R.string.external_storage_unavailable_message),
                        Toast.LENGTH_LONG).show();
                return context.getFilesDir();
            }
        }
    }

    public static boolean isExternalStorageAvailable(){
        String state = Environment.getExternalStorageState();
        return state.equals(Environment.MEDIA_MOUNTED);
    }

    private static void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while((read = in.read(buffer)) != -1){
            out.write(buffer, 0, read);
        }
    }

    public static File[] listFiles(Context context){
        return getFileDirectory(context).listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                return file.getAbsolutePath().contains(".jpg");
            }
        });
    }


    public static Uri saveImageForSharing(Context context, Bitmap bitmap,  String assetName) {
        File fileToWrite = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                assetName);

        try {
            FileOutputStream outputStream = new FileOutputStream(fileToWrite);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            outputStream.flush();
            outputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            return Uri.fromFile(fileToWrite);
        }
    }


    public static void saveImage(Context context, Bitmap bitmap, String name) {
        File fileDirectory = getFileDirectory(context);
        File fileToWrite = new File(fileDirectory, name);

        try {
            FileOutputStream outputStream = new FileOutputStream(fileToWrite);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            outputStream.flush();
            outputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
