package com.example.jon.maper.widget;

import java.util.ArrayList;

/**
 * Created by jon on 2017/4/15.
 */

public class Cache<E> extends ArrayList<E> {
    private final int MAX_CACHE_SIZE;

    public Cache(int cacheSize){
        MAX_CACHE_SIZE = cacheSize;
    }


    @Override
    public boolean add(E e) {
        if(size() > MAX_CACHE_SIZE){
            super.add(e);
            remove(0);
        }
        return super.add(e);
    }
}
