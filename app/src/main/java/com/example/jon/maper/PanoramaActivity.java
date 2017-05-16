package com.example.jon.maper;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;
import android.widget.Toast;

import com.baidu.lbsapi.BMapManager;
import com.baidu.lbsapi.panoramaview.PanoramaView;
import com.baidu.lbsapi.panoramaview.PanoramaViewListener;
import com.baidu.lbsapi.tools.CoordinateConverter;
import com.baidu.lbsapi.tools.Point;
import com.baidu.mapapi.search.geocode.GeoCoder;

/**
 * Created by jon on 2017/4/9.
 */

public class PanoramaActivity extends AppCompatActivity {

    PanoramaView mPanoView;
    ProgressDialog mProgDialog;

    double mLon;
    double mLat;
    String mQuery;

    GeoCoder mCoder;
    public static final String QUERY = "query";
    public static final String LAT = "lat";
    public static final String LON = "lon";

    public static void jumpToMe(String q, double lat,double lon,Context context){
        Intent intent = new Intent(context,PanoramaActivity.class);
        intent.putExtra(QUERY,q);
        intent.putExtra(LAT,lat);
        intent.putExtra(LON,lon);
        context.startActivity(intent);

    }
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        initBMapManager();
        setContentView(R.layout.panorama_main);
        Intent intent = getIntent();
        mQuery = intent.getStringExtra(QUERY);
        mLat = intent.getDoubleExtra(LAT,0);
        mLon = intent.getDoubleExtra(LON,0);

        mPanoView = (PanoramaView)findViewById(R.id.panorama);
       // mPanoView.setPanorama("0100220000130817164838355J5");

        mPanoView.setPanoramaViewListener(new PanoramaViewListener() {
            @Override
            public void onDescriptionLoadEnd(String s) {

            }

            @Override
            public void onLoadPanoramaBegin() {

                mPanoView.post(new Runnable() {
                    @Override
                    public void run() {
                        showProgressDialog();
                    }
                });
            }

            @Override
            public void onLoadPanoramaEnd(String s) {

                mPanoView.post(new Runnable() {
                    @Override
                    public void run() {
                        dissmissProgressDialog();
                    }
                });
            }

            @Override
            public void onLoadPanoramaError(String s) {
                mPanoView.post(new Runnable() {
                    @Override
                    public void run() {
                        dissmissProgressDialog();
                        Toast.makeText(PanoramaActivity.this,"加载出错",Toast.LENGTH_SHORT).show();
                    }
                });

            }

            @Override
            public void onMessage(String s, int i) {

            }

            @Override
            public void onCustomMarkerClick(String s) {

            }
        });
        //103.938555,30.757407
//        mCoder = GeoCoder.newInstance();
//        mCoder.setOnGetGeoCodeResultListener(new OnGetGeoCoderResultListener() {
//            @Override
//            public void onGetGeoCodeResult(GeoCodeResult geoCodeResult) {
//                if (geoCodeResult == null || geoCodeResult.error != SearchResult.ERRORNO.NO_ERROR) {
//                    Toast.makeText(PanoramaActivity.this, "抱歉，未能找到结果", Toast.LENGTH_LONG)
//                            .show();
//                    return;
//                }
//
//                mLon = geoCodeResult.getLocation().longitude;
//                mLat = geoCodeResult.getLocation().latitude;
//                Log.d("data","lat:"+mLat+"mLon:"+mLon);
                //103.939177,30.756033
//                if(true){
//                    mLon = 103.939177;
//                    mLat = 30.756033;
//                }
//
//                mPanoView.setPanorama(mLon,mLat);
//
//            }
//
//            @Override
//            public void onGetReverseGeoCodeResult(ReverseGeoCodeResult reverseGeoCodeResult) {
//
//            }
//        });
//        Log.d("data",mQuery);
//        mCoder.geocode(new GeoCodeOption().city("成都").address(mQuery));

        //"lng":104.10761345914301,"lat":30.68194977226741
        //mPanoView.setPanorama();

        Point source = new Point(mLon,mLat);
        Point taget = CoordinateConverter.converter(CoordinateConverter.COOR_TYPE.COOR_TYPE_GCJ02, source);
        mPanoView.setPanorama(taget.x,taget.y);



    }

    private void initBMapManager() {
        App app = (App) this.getApplication();
        if (app.mBMapManager == null) {
            app.mBMapManager = new BMapManager(app);
            app.mBMapManager.init(new App.MyGeneralListener());
        }
    }

    private void showProgressDialog() {
        if (mProgDialog == null)
            mProgDialog = new ProgressDialog(this);
        mProgDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgDialog.setIndeterminate(false);
        mProgDialog.setCancelable(true);
        mProgDialog.setMessage("正在加载");
        mProgDialog.show();
    }

    /**
     * 隐藏进度框
     */
    private void dissmissProgressDialog() {
        if (mProgDialog != null) {
            mProgDialog.dismiss();
        }
    }
    @Override
    protected void onPause() {
        super.onPause();
        mPanoView.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mPanoView.onResume();
    }

    @Override
    protected void onDestroy() {
        mPanoView.destroy();
        super.onDestroy();
    }
}
