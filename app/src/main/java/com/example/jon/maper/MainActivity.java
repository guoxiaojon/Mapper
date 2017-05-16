package com.example.jon.maper;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.PersistableBundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.UiSettings;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.MyLocationStyle;
import com.amap.api.navi.model.NaviLatLng;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.geocoder.GeocodeQuery;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.RegeocodeResult;
import com.amap.api.services.route.BusRouteResult;
import com.amap.api.services.route.DriveRouteResult;
import com.amap.api.services.route.RidePath;
import com.amap.api.services.route.RideRouteResult;
import com.amap.api.services.route.RouteSearch;
import com.amap.api.services.route.WalkPath;
import com.amap.api.services.route.WalkRouteResult;
import com.example.jon.maper.overlay.RideRouteOverlay;
import com.example.jon.maper.overlay.WalkRouteOverlay;
import com.example.jon.maper.utils.AMapUtil;
import com.example.jon.maper.utils.CacheUtil;
import com.example.jon.maper.widget.Cache;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.view.View.GONE;

public class MainActivity extends AppCompatActivity implements AMapLocationListener, GeocodeSearch.OnGeocodeSearchListener {

    @BindView(R.id.mv_main)
    MapView mMapView;
    @BindView(R.id.toolbar)
    Toolbar mToolBar;
    @BindView(R.id.et_start)
    AutoCompleteTextView mStartET;
    @BindView(R.id.et_end)
    AutoCompleteTextView mEndET;
    @BindView(R.id.route_bus)
    ImageView mBus;
    @BindView(R.id.route_drive)
    ImageView mDrive;
    @BindView(R.id.route_walk)
    ImageView mWalk;
    ProgressDialog mProgDialog;
    @BindView(R.id.routemap_header)
    RelativeLayout mHeader;
    @BindView(R.id.bottom_layout)
    RelativeLayout mBottomLayout;
    @BindView(R.id.firstline)
    TextView mFirstLine;
    @BindView(R.id.ll_search_top)
    LinearLayout mLLSearch;
    @BindView(R.id.et_search)
    AutoCompleteTextView mETSearch;

    @BindView(R.id.tv_search)
    TextView mTVSearch;

    ArrayAdapter<String> mSTAdapter;
    ArrayAdapter<String> mETAdapter;
    ArrayAdapter<String> mSearchTAdapter;

    Cache<String> mSC;
    Cache<String> mEC;
    Cache<String> mSearchC;

    private final int ROUTE_TYPE_WALK = 3;
    private final int ROUTE_TYPE_BIKE = 4;

    AMap mAMap;
    UiSettings mUiSettings;
    Marker mMarker;

    AMapLocationClientOption mLocationOption;
    AMapLocationClient mLocationClient;

    LatLonPoint mCurrLocation;

    RouteSearch mRouteSearch;
    GeocodeSearch mGeocoderSearch;
    MyLocationStyle myLocationStyle;


    private LatLonPoint mStartPoint;
    private LatLonPoint mEndPoint;
    private LatLonPoint mMarkPoint;

    private String mSearchWord;

    private Handler mHandler = new Handler();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        setSupportActionBar(mToolBar);

        mMapView.onCreate(savedInstanceState);
        if (mMapView != null) {
            mAMap = mMapView.getMap();
            mUiSettings = mAMap.getUiSettings();
        }
        initLocation();
        initShowIndoor();
        initUISettings();
        initRouteSearch();
        initGeocodeSearch();
        initMarkClick();
        initAutoCompleteText();
        watchSearch();


    }

    private void initAutoCompleteText() {
        mSC = CacheUtil.getCache(Contrants.START_CACHE);
        mEC = CacheUtil.getCache(Contrants.END_CACHE);
        mSearchC = CacheUtil.getCache(Contrants.SEARCH_CACHE);

        mSTAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, mSC);
        mETAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, mEC);
        mSearchTAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, mSearchC);

        mStartET.setAdapter(mSTAdapter);
        mEndET.setAdapter(mETAdapter);
        mETSearch.setAdapter(mSearchTAdapter);
        Log.d("data", mSearchC.toString());


    }

    private void initMarkClick() {
        mAMap.setOnMarkerClickListener(new AMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                double lat = mMarkPoint.getLatitude();
                double lon = mMarkPoint.getLongitude();
                Log.d("data", lat + ":" + lon);
                PanoramaActivity.jumpToMe(mSearchWord, lat, lon, MainActivity.this);
                return true;
            }
        });
        mAMap.setOnInfoWindowClickListener(new AMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                double lat = mMarkPoint.getLatitude();
                double lon = mMarkPoint.getLongitude();
                PanoramaActivity.jumpToMe(mSearchWord, lat, lon, MainActivity.this);
                Log.d("data", lat + ":" + lon);
            }
        });
    }

    private void showMarker(LatLonPoint point) {
        dissmissProgressDialog();
        mMarkPoint = point;
        LatLng latLng = new LatLng(point.getLatitude(), point.getLongitude());
        //LatLng latLng = new LatLng(39.906901, 116.397972);
        if (mMarker != null) {
            mMarker.destroy();
        }
        mMarker = mAMap.addMarker(new MarkerOptions().position(latLng).title(mSearchWord).snippet("点击查看全景"));
        mMarker.showInfoWindow();
        mAMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(point.getLatitude(), point.getLongitude())));

    }

    private void initGeocodeSearch() {
        mGeocoderSearch = new GeocodeSearch(this);
        mGeocoderSearch.setOnGeocodeSearchListener(this);
    }

    private void initLocation() {
        mCurrLocation = new LatLonPoint(0, 0);
        myLocationStyle = new MyLocationStyle();//初始化定位蓝点样式类
        myLocationStyle.myLocationIcon(BitmapDescriptorFactory.fromResource(R.drawable.gps_point));


        myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATE);//定一次、且将视角移动到地图中心点，定位点依照设备方向旋转，并且会跟随设备移动。（1秒1次定位）如果不设置myLocationType，默认也会执行此种模式。
        //myLocationStyle.interval(2000); //设置连续定位模式下的定位间隔，只在连续定位模式下生效，单次定位模式下不会生效。单位为毫秒。
        myLocationStyle.radiusFillColor(getResources().getColor(R.color.colorRadiusFill));
        mAMap.setMyLocationStyle(myLocationStyle);//设置定位蓝点的Style
        //mAMap.getUiSettings().setMyLocationButtonEnabled(true);//设置默认定位按钮是否显示，非必需设置。
        mAMap.setMyLocationEnabled(true);// 设置为true表示启动显示定位蓝点，false表示隐藏定位蓝点并不进行定位，默认是false。
        mAMap.moveCamera(CameraUpdateFactory.zoomTo(18));

        //初始化定位
        mLocationClient = new AMapLocationClient(getApplicationContext());
        //初始化定位参数
        mLocationOption = new AMapLocationClientOption();
        //设置定位回调监听
        mLocationClient.setLocationListener(this);
        //设置为高精度定位模式
        mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        //设置定位间隔
        mLocationOption.setInterval(2000);
        //设置定位参数
        mLocationClient.setLocationOption(mLocationOption);
        mLocationClient.startLocation();//启动定位

    }

    private void initShowIndoor() {
        mAMap.showIndoorMap(true);
    }

    private void initUISettings() {

        mUiSettings.setCompassEnabled(true);//指南针
        mUiSettings.setMyLocationButtonEnabled(true); //显示默认的定位按钮

    }

    private void initRouteSearch() {
        mDrive = (ImageView) findViewById(R.id.route_drive);
        mBus = (ImageView) findViewById(R.id.route_bus);
        mWalk = (ImageView) findViewById(R.id.route_walk);
        mRouteSearch = new RouteSearch(this);
        mRouteSearch.setRouteSearchListener(new RouteSearch.OnRouteSearchListener() {
            @Override
            public void onBusRouteSearched(BusRouteResult busRouteResult, int i) {

            }

            @Override
            public void onDriveRouteSearched(DriveRouteResult driveRouteResult, int i) {

            }

            @Override
            public void onWalkRouteSearched(WalkRouteResult walkRouteResult, int i) {
                dissmissProgressDialog();
                final WalkPath walkPath = walkRouteResult.getPaths()
                        .get(0);
                mAMap.clear();
                Log.d("data", "+++++++++++++++++");
                WalkRouteOverlay walkRouteOverlay = new WalkRouteOverlay(
                        MainActivity.this, mAMap, walkPath,
                        walkRouteResult.getStartPos(),
                        walkRouteResult.getTargetPos());
                walkRouteOverlay.removeFromMap();
                walkRouteOverlay.addToMap();
                walkRouteOverlay.zoomToSpan();
                int dis = (int) walkPath.getDistance();
                int dur = (int) walkPath.getDuration();
                String des = AMapUtil.getFriendlyTime(dur) + "(" + AMapUtil.getFriendlyLength(dis) + ")";
                mFirstLine.setText(des);
                mBottomLayout.setVisibility(View.VISIBLE);


            }

            @Override
            public void onRideRouteSearched(RideRouteResult rideRouteResut, int i) {
                dissmissProgressDialog();
                final RidePath ridePath = rideRouteResut.getPaths()
                        .get(0);
                mAMap.clear();
                Log.d("data", "+++++++++++++++++");
                RideRouteOverlay rideRouteOverlay = new RideRouteOverlay(
                        MainActivity.this, mAMap, ridePath,
                        rideRouteResut.getStartPos(),
                        rideRouteResut.getTargetPos());
                rideRouteOverlay.removeFromMap();
                rideRouteOverlay.addToMap();
                rideRouteOverlay.zoomToSpan();
                int dis = (int) ridePath.getDistance();
                int dur = (int) ridePath.getDuration();
                String des = AMapUtil.getFriendlyTime(dur) + "(" + AMapUtil.getFriendlyLength(dis) + ")";
                mFirstLine.setText(des);
                mBottomLayout.setVisibility(View.VISIBLE);


            }
        });


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            if (mHeader.getVisibility() == View.VISIBLE) {
                mHeader.setVisibility(GONE);
                mBottomLayout.setVisibility(GONE);
                if (isSoftShowing()) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
                }
            } else {
                mHeader.setVisibility(View.VISIBLE);
                mLLSearch.setVisibility(GONE);
                //mBottomLayout.setVisibility(View.VISIBLE);
            }
            return true;
        } else if (id == R.id.action_search) {
            if (mLLSearch.getVisibility() == View.VISIBLE) {
                mLLSearch.setVisibility(GONE);
                if (mMarker != null) {
                    mMarker.destroy();
                }

            } else {
                mLLSearch.setVisibility(View.VISIBLE);
                mHeader.setVisibility(GONE);
                mBottomLayout.setVisibility(GONE);
            }
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
        mLocationClient.stopLocation();
        mLocationClient.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        mMapView.onSaveInstanceState(outState);
    }

    @Override
    protected void onStart() {
        super.onStart();

    }

    boolean first = true;

    @Override
    public void onLocationChanged(AMapLocation aMapLocation) {
        mCurrLocation.setLatitude(aMapLocation.getLatitude());
        mCurrLocation.setLongitude(aMapLocation.getLongitude());
        if (first) {
            first = false;
            mAMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(aMapLocation.getLatitude(), aMapLocation.getLongitude())));
            mAMap.moveCamera(CameraUpdateFactory.zoomTo(18));
            myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_SHOW);
            mAMap.setMyLocationStyle(myLocationStyle);
            mAMap.setMyLocationEnabled(true);
            Log.d("data", "(⊙v⊙)嗯？？？？？");
        } else {
            myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_SHOW);
            mAMap.setMyLocationStyle(myLocationStyle);
            mAMap.setMyLocationEnabled(true);
            mAMap.moveCamera(CameraUpdateFactory.newCameraPosition(mAMap.getCameraPosition()));
            Log.d("data", "LAT = " + aMapLocation.getLatitude() + " LON = " + aMapLocation.getLongitude());
        }

//        mAMap.setMyLocationStyle(myLocationStyle);
//        mAMap.setMyLocationEnabled(true);
//        mAMap.moveCamera(CameraUpdateFactory.newCameraPosition(mAMap.getCameraPosition()));

        //Log.d("data","currLocation : "+mCurrLocation.toString());
    }

    /**
     * 公交路线搜索
     */
    public void onBusClick(View view) {
//        searchRouteResult(ROUTE_TYPE_BUS, RouteSearch.BusDefault);
//        mDrive.setImageResource(R.drawable.route_drive_normal);
//        mBus.setImageResource(R.drawable.route_bus_select);
//        mWalk.setImageResource(R.drawable.route_walk_normal);
        Toast.makeText(this, "校内地图不支持", Toast.LENGTH_SHORT).show();


    }

    /**
     * 驾车路线搜索
     */
    public void onDriveClick(View view) {
//        searchRouteResult(ROUTE_TYPE_DRIVE, RouteSearch.DrivingDefault);
//        mDrive.setImageResource(R.drawable.route_drive_select);
//        mBus.setImageResource(R.drawable.route_bus_normal);
//        mWalk.setImageResource(R.drawable.route_walk_normal);
        Toast.makeText(this, "校内地图不支持", Toast.LENGTH_SHORT).show();

    }

    boolean isSearch = false;

    public void onSearchClick(View view) {
        String temp = mETSearch.getText().toString().trim();
        if (TextUtils.isEmpty(temp)) {
            Toast.makeText(this, "请输入地点", Toast.LENGTH_SHORT).show();
        } else {
            if (isSoftShowing()) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
            }

            if (!mSearchC.contains(temp)) {
                mSearchC.add(temp);
                mSearchTAdapter.notifyDataSetChanged();
                CacheUtil.saveCache(mSearchC, Contrants.SEARCH_CACHE);

            }

            showProgressDialog();
            GeocodeQuery query = new GeocodeQuery(temp, "028");
            mGeocoderSearch.getFromLocationNameAsyn(query);
            isSearch = true;
            mSearchWord = temp;
        }
    }

    /**
     * 步行路线搜索
     */
    public void onWalkClick(View view) {
        searchRouteResult(ROUTE_TYPE_WALK, RouteSearch.WalkDefault);
        mDrive.setImageResource(R.drawable.route_drive_normal);
        mBus.setImageResource(R.drawable.route_bus_normal);
        mWalk.setImageResource(R.drawable.route_walk_select);

        if (isSoftShowing()) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);


        }

    }

    public void gotoNavi(View view) {
        NaviLatLng start = new NaviLatLng(mStartPoint.getLatitude(), mStartPoint.getLongitude());
        NaviLatLng end = new NaviLatLng(mEndPoint.getLatitude(), mEndPoint.getLongitude());
        int mode = mRouteType == ROUTE_TYPE_BIKE ? NaviActivity.BIKE : NaviActivity.WALK;
        NaviActivity.jumpToMe(this, mode, start, end);
    }

    /**
     * 自行车路线搜索
     */
    public void onBikeClick(View view) {
        searchRouteResult(ROUTE_TYPE_BIKE, RouteSearch.RidingDefault);
        mDrive.setImageResource(R.drawable.route_drive_normal);
        mBus.setImageResource(R.drawable.route_bus_normal);
        mWalk.setImageResource(R.drawable.route_walk_normal);
        if (isSoftShowing()) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);


        }

    }

    private boolean isSoftShowing() {
        //获取当前屏幕内容的高度
        int screenHeight = getWindow().getDecorView().getHeight();
        //获取View可见区域的bottom
        Rect rect = new Rect();
        getWindow().getDecorView().getWindowVisibleDisplayFrame(rect);

        return screenHeight - rect.bottom != 0;
    }

    /**
     * 开始搜索路径规划方案
     */
    int mRouteType;

    public void searchRouteResult(int routeType, int mode) {
        mStartPoint = null;
        mEndPoint = null;
        mRouteType = routeType;
        String tempS = mStartET.getText().toString().trim();
        if (TextUtils.isEmpty(tempS)) {
            tempS = "我的位置";
        }

        List<String> arr = new ArrayList<>();
        if (tempS.equals("我的位置")) {
            mStartPoint = mCurrLocation;

        } else {
            arr.add(tempS);
            if (!mSC.contains(tempS)) {
                mSC.add(tempS);
                mSTAdapter.notifyDataSetChanged();
                CacheUtil.saveCache(mSC, Contrants.START_CACHE);
            }
        }

        String tempE = mEndET.getText().toString().trim();
        if (TextUtils.isEmpty(tempE)) {
            Toast.makeText(this, "终点未设置", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!mEC.contains(tempE)) {
            mEC.add(tempE);
            mETAdapter.notifyDataSetChanged();
            CacheUtil.saveCache(mEC, Contrants.END_CACHE);
        }

        Log.d("data", tempE);
        arr.add(tempE);
        //arr.add("电子科技大学清水河");
        showProgressDialog();
        geocodeQuery(arr);//查询终点的经纬度

    }

    /**
     * 显示进度框
     */
    private void showProgressDialog() {
        if (mProgDialog == null)
            mProgDialog = new ProgressDialog(this);
        mProgDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgDialog.setIndeterminate(false);
        mProgDialog.setCancelable(true);
        mProgDialog.setMessage("正在搜索");
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


    public void geocodeQuery(final List<String> arr) {


        if (arr.size() == 1) {
            GeocodeQuery query = new GeocodeQuery(arr.get(0), "028");
            mGeocoderSearch.getFromLocationNameAsyn(query);

        } else {

            GeocodeQuery query = new GeocodeQuery(arr.get(0), "028");
            mGeocoderSearch.getFromLocationNameAsyn(query);
            query = new GeocodeQuery(arr.get(1), "028");
            mGeocoderSearch.getFromLocationNameAsyn(query);
        }

    }

    @Override
    public void onRegeocodeSearched(RegeocodeResult regeocodeResult, int i) {

    }

    @Override
    public void onGeocodeSearched(GeocodeResult geocodeResult, int i) {

        if (isSearch) {
            isSearch = false;
            showMarker(geocodeResult.getGeocodeAddressList().get(0).getLatLonPoint());
            return;
        }
        //获取
        if (mStartPoint == null) {
            mStartPoint = geocodeResult.getGeocodeAddressList().get(0).getLatLonPoint();
            Log.d("data", mStartPoint.toString());
        } else {
            mEndPoint = geocodeResult.getGeocodeAddressList().get(0).getLatLonPoint();
            Log.d("data", mEndPoint.toString());
        }

        mHandler.post(new Runnable() {
            @Override
            public void run() {

                final RouteSearch.FromAndTo fromAndTo = new RouteSearch.FromAndTo(
                        mStartPoint, mEndPoint);

                if (mRouteType == ROUTE_TYPE_WALK) {// 步行路径规划
                    RouteSearch.WalkRouteQuery query = new RouteSearch.WalkRouteQuery(fromAndTo, RouteSearch.WALK_DEFAULT);
                    mRouteSearch.calculateWalkRouteAsyn(query);// 异步路径规划步行模式查询
                } else if (mRouteType == ROUTE_TYPE_BIKE) {//骑车路径规划
                    RouteSearch.RideRouteQuery query = new RouteSearch.RideRouteQuery(fromAndTo, RouteSearch.RIDING_DEFAULT);
                    mRouteSearch.calculateRideRouteAsyn(query);// 异步路径规划公交模式查询
                }

            }
        });

    }


    public void watchSearch() {
        mStartET.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId,
                                          KeyEvent keyEvent) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    //获取焦点
                    mWalk.setFocusable(true);
                    mWalk.setFocusableInTouchMode(true);
                    mWalk.requestFocus();
                    mWalk.requestFocusFromTouch();
                    onWalkClick(null);//默认使用步行路线规划
                    return true;
                }
                return false;
            }
        });
        mEndET.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    //获取焦点
                    mWalk.setFocusable(true);
                    mWalk.setFocusableInTouchMode(true);
                    mWalk.requestFocus();
                    mWalk.requestFocusFromTouch();
                    onWalkClick(null);//默认使用步行路线规划
                    return true;
                }
                return false;
            }
        });

        mETSearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                //Log.d("data",actionId+keyEvent.toString());
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    //获取焦点，防止软键盘二次弹出
                    mTVSearch.setFocusable(true);
                    mTVSearch.setFocusableInTouchMode(true);
                    mTVSearch.requestFocus();
                    mTVSearch.requestFocusFromTouch();
                    onSearchClick(null);
                    return true;
                }
                return false;
            }
        });
    }


}
