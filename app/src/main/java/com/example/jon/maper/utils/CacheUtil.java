package com.example.jon.maper.utils;

import android.text.TextUtils;

import com.example.jon.maper.Contrants;
import com.example.jon.maper.widget.Cache;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * Created by jon on 2017/4/15.
 */

public class CacheUtil {
    public static void saveCache(Cache<String> cahce, String fileName){
        if(cahce == null){
            return;
        }
        File dir = new File(Contrants.CACHE_PATH);
        if(!dir.exists()){
            dir.mkdirs();
        }
        File cacheFile = new File(dir,fileName);
        if(!cacheFile.exists()){
            try {
                cacheFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        ObjectOutputStream out = null;
        try {
            out = new ObjectOutputStream(new FileOutputStream(cacheFile));
            out.writeObject(cahce);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static Cache<String> getCache(String fileName){
        Cache<String> cache = new Cache<>(10);
        if(TextUtils.isEmpty(fileName)){
            return cache;
        }
        File dir = new File(Contrants.CACHE_PATH);
        File cacheFile = new File(dir,fileName);
        if(!cacheFile.exists()){
            return cache;
        }

        ObjectInputStream in = null;
        try {
            in = new ObjectInputStream(new FileInputStream(cacheFile));
            cache = (Cache<String>) in.readObject();
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            try {
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return  cache;
    }
}
