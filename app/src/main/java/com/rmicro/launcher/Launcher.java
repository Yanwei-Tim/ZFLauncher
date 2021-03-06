package com.rmicro.launcher;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.graphics.Color;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.rmicro.launcher.utils.IntentUtils;
import com.rmicro.launcher.utils.PreferanceUtils;
import com.rmicro.launcher.utils.AUtils;
import com.rmicro.launcher.adapter.MyVPAdapter;
import com.rmicro.launcher.custom.Constant;
import com.rmicro.launcher.custom.WeatherBean;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class Launcher extends AppCompatActivity implements View.OnClickListener {

    private Context mContext = Launcher.this;
    private Activity mActivity = Launcher.this;
    private static final int REFRESH_WATER = 10086;
    private static final int REFRESH_LIGHT = 10087;

    //第二页全部图标
    private RelativeLayout qqLayout, wechatLayout;
    private TextView fmSize;
    private static SeekBar soundSeekBar, lightSeekBar;

    private LinearLayout navigationPoint;

    private boolean isLongClick = false;
    private float y1;
    private float x1;
    private int normal = 0;
    private TelephonyManager mTelephoneManager;
    private AudioManager mAudioManager;
    private long drivTimeLong;
    private MyHandler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        requestWindowFeature(Window.FEATURE_NO_TITLE);
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
//                WindowManager.LayoutParams.FLAG_FULLSCREEN);
//        getWindow().setStatusBarColor(Color.TRANSPARENT);
        setContentView(R.layout.activity_main);
        mHandler = new MyHandler(this);
        initData();
        initView();
        doOther();
    }

    //更换默认字体重写
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    protected void initView() {
        List<View> viewData = new ArrayList<>();
        viewData.clear();
        ViewPager viewPagerMain = (ViewPager)findViewById(R.id.viewPagerMain);
        navigationPoint = (LinearLayout)findViewById(R.id.navigationPoint);
        View pageFirst = LayoutInflater.from(mContext).inflate(R.layout.pager_first, null, false);
        View pagerSecond = LayoutInflater.from(mContext).inflate(R.layout.pager_second, null, false);
        viewData.add(pageFirst);
        viewData.add(pagerSecond);
        //初始化控件
        initPagerFirst(pageFirst);
        initPagerSecond(pagerSecond);
        setPointFocors(0);
        MyVPAdapter myVPAdapter = new MyVPAdapter(viewData);
        viewPagerMain.setAdapter(myVPAdapter);
        viewPagerMain.setOffscreenPageLimit(4);//缓存4页
        viewPagerMain.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                setPointFocors(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
    }

    public void startSetWallpaper() {
        final Intent pickWallpaper = new Intent(Intent.ACTION_SET_WALLPAPER);
        Intent chooser = Intent.createChooser(pickWallpaper, "Set WallPaper");
        startActivityForResult(chooser, 10);
    }
    protected void initData() {
        mTelephoneManager = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
        mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
    }


    protected void doOther() {
        registerBroadcast();//注册广播
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        PreferanceUtils.saveDataBoolean(mContext, "isFullWind", false);
        unregisterReceiver(recordReceiver);
        unregisterReceiver(mNetWorkState);
        unregisterReceiver(otherReceiver);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.zhifayi:
                if (IntentUtils.haveAPP(mContext, Constant.PACKAGE_RMICRO_RECODER)) {
                    IntentUtils.startAPP(mActivity, Constant.PACKAGE_RMICRO_RECODER);
                }
                break;
            case R.id.ptt:
                if (IntentUtils.haveAPP(mContext, Constant.PACKAGE_RMICRO_VOIP)) {
                    IntentUtils.startAPP(mActivity, Constant.PACKAGE_RMICRO_VOIP);
                }
                break;
            case R.id.filemanager:
                if (IntentUtils.haveAPP(mContext, Constant.PACKAGE_RMICRO_FILE_MANAGE)) {
                    IntentUtils.startAPP(mActivity, Constant.PACKAGE_RMICRO_FILE_MANAGE);
                }
                break;
            case R.id.settings:
                if (IntentUtils.haveAPP(mContext, Constant.PACKAGE_RMICRO_SETTINGS)) {
                    IntentUtils.startAPP(mActivity, Constant.PACKAGE_RMICRO_SETTINGS);
                }
                break;
            case R.id.cards:
                if (IntentUtils.haveAPP(mContext, Constant.PACKAGE_RMICRO_CARDS_REC)) {
                    IntentUtils.startAPP(mActivity, Constant.PACKAGE_RMICRO_CARDS_REC);
                }
                break;
            case R.id.police_do:
                if (IntentUtils.haveAPP(mContext, Constant.PACKAGE_RMICRO_POLICEINFO)) {
                    IntentUtils.startAPP(mActivity, Constant.PACKAGE_RMICRO_POLICEINFO);
                }
                break;
            case R.id.phone:
                if (IntentUtils.haveAPP(mContext, Constant.PACKAGE_RMICRO_PHONE)) {
                    IntentUtils.startAPP(mActivity, Constant.PACKAGE_RMICRO_PHONE);
                }
                break;
            case R.id.apps:
                Intent allAPP = new Intent(mContext, AllAppList.class);
                mContext.startActivity(allAPP);
                break;
            default:
                break;
        }
    }

    private void initPagerFirst(View view) {
        view.findViewById(R.id.zhifayi).setOnClickListener(this);
        view.findViewById(R.id.ptt).setOnClickListener(this);
        view.findViewById(R.id.filemanager).setOnClickListener(this);
        view.findViewById(R.id.settings).setOnClickListener(this);
        newPoint();
    }

    private void initPagerSecond(View view) {
        view.findViewById(R.id.cards).setOnClickListener(this);
        view.findViewById(R.id.police_do).setOnClickListener(this);
        view.findViewById(R.id.phone).setOnClickListener(this);
        view.findViewById(R.id.apps).setOnClickListener(this);
        newPoint();
    }
    private void registerBroadcast() {
        IntentFilter filterRecord = new IntentFilter();
        filterRecord.addAction(Constant.RECORDER_START);
        filterRecord.addAction(Constant.RECORDER_STOP);
        mContext.registerReceiver(recordReceiver, filterRecord);

        //网络状态改变广播
        IntentFilter filterNetWork = new IntentFilter();
        filterNetWork.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(mNetWorkState, filterNetWork);

        //其他广播
        IntentFilter filterOther = new IntentFilter();
        filterOther.addAction(Constant.ETC_BROADCAST);
        filterOther.addAction(Constant.REMOVE_NAVIGATIONBAR);
        filterOther.addAction(Constant.AMPA_BROAD);
        filterOther.addAction(Constant.VOLUME_CHANGED_ACTION);
        mContext.registerReceiver(otherReceiver, filterOther);

        //控制屏幕亮度
        mContext.getContentResolver().registerContentObserver(
                Settings.System.getUriFor(Settings.System.SCREEN_BRIGHTNESS),
                true, brightnessMode);

    }

    static class MyHandler extends Handler {

        WeakReference<Launcher> mWeak;

        MyHandler(Launcher activity) {
            mWeak = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Launcher mMian = mWeak.get();
            if (mMian == null)
                return;
            switch (msg.what) {
                case REFRESH_WATER:
                    WeatherBean bean = (WeatherBean) msg.obj;
                    break;
                case REFRESH_LIGHT:
                    int mBrightProgress = Settings.System.getInt(
                            mMian.getContentResolver(),
                            Settings.System.SCREEN_BRIGHTNESS, 255);
                    lightSeekBar.setMax(255);
                    lightSeekBar.setProgress(mBrightProgress);
                    break;
                default:
                    break;
            }

        }
    }

//
//    private Handler mHandler = new Handler() {
//        @Override
//        public void handleMessage(Message msg) {
//            super.handleMessage(msg);
//            switch (msg.what) {
//                case REFRESH_WATHER:
//                    WeatherBean bean = (WeatherBean) msg.obj;
//                    LogUtils.e(TAG, bean.getmWeather() + "  " + bean.getmTempratureSize() + "  " + bean.getmWeatherPic());
//                    break;
//                default:
//                    break;
//            }
//        }
//    };



    private void switchRecord(boolean status) {
        if (status) {
            mContext.sendBroadcast(new Intent(Constant.RECORDER_CONTROL).putExtra("msg", "start"));
        } else {
            mContext.sendBroadcast(new Intent(Constant.RECORDER_CONTROL).putExtra("msg", "stop"));
        }
    }

    private void newPoint() {
        ImageView pointView = new ImageView(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        params.rightMargin = 3;
        params.leftMargin = 3;
        params.gravity = Gravity.CENTER_VERTICAL;
        pointView.setLayoutParams(params);
        pointView.setBackgroundResource(R.drawable.pointbg);
        navigationPoint.addView(pointView);
    }

    private void setPointFocors(int pos) {
        for (int i = 0; i < navigationPoint.getChildCount(); i++) {
            if (i == pos) {
                navigationPoint.getChildAt(i).setSelected(true);
            } else {
                navigationPoint.getChildAt(i).setSelected(false);
            }
        }
    }

    private void refreshRecordingView(boolean isRecording) {
        mHandler.removeCallbacks(mTimeRunnable);
        drivTimeLong = 0;
    }

    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                y1 = event.getY();
            }
            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (Math.abs(y1 - event.getY()) > 50 && isLongClick) {
                    if (IntentUtils.haveAPP(mContext, Constant.PACKAGE_SET_ANDROID)) {
                        AUtils.makeToast(mContext, mContext.getString(R.string.androidSetting));
                        IntentUtils.startAPP(mActivity, Constant.PACKAGE_SET_ANDROID);
                    }
                    isLongClick = false;
                }
            }
            return false;
        }
    };

    private View.OnTouchListener mLightTouch = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                x1 = event.getX();
                normal = lightSeekBar.getProgress();
                if (x1 <= 50) {
                    if (normal <= 0)
                        return true;
                    normal -= 20;
                } else if (x1 >= 140) {
                    if (normal >= 255)
                        return true;
                    normal += 20;
                }
                mContext.sendBroadcast(new Intent(
                        Constant.SYSTEM_BRIGHTNESS_CHANGE_ACTION));
                Settings.System.putInt(mContext.getContentResolver(),
                        Settings.System.SCREEN_BRIGHTNESS, normal);
                normal = Settings.System.getInt(mContext.getContentResolver(),
                        Settings.System.SCREEN_BRIGHTNESS, -1);
                WindowManager.LayoutParams wl = getWindow().getAttributes();
                float tmpFloat = (float) normal / 255;
                if (tmpFloat > 0 && tmpFloat <= 1) {
                    wl.screenBrightness = tmpFloat;
                }
                getWindow().setAttributes(wl);
                lightSeekBar.setProgress(normal);
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            return true;
        }
    };

    private View.OnTouchListener mSoundTouch = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                x1 = event.getX();
                normal = soundSeekBar.getProgress();
                if (x1 <= 50) {
                    if (normal <= 0)
                        return true;
                    normal -= 1;
                } else if (x1 >= 140) {
                    if (normal >= 255)
                        return true;
                    normal += 1;
                }
                AsyncTask.execute(new Runnable() {
                    @Override
                    public void run() {
                        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
                                normal, 0);
                    }
                });
                Intent VolumnChangeAction = new Intent(Constant.SYSTEM_VOLUMN_CHANGE_ACTION);
                mContext.sendBroadcast(VolumnChangeAction);
                soundSeekBar.setProgress(normal);
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            return true;
        }
    };
    private View.OnLongClickListener mLongClick = new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View v) {
            isLongClick = true;
            return true;
        }
    };

    private void takePhoto() {
        Intent makePic = new Intent(Constant.NOTIFACATION_MSG_TO_RECODER);
        makePic.putExtra("msg", "getphoto");
        mContext.sendBroadcast(makePic);
    }

    private Runnable mTimeRunnable = new Runnable() {
        @Override
        public void run() {
            drivTimeLong++;
            int hour = (int) (drivTimeLong / 60 / 60) % 60;
            int minute = (int) (drivTimeLong / 60) % 60;
            int second = (int) (drivTimeLong % 60);

            mHandler.postDelayed(mTimeRunnable, 1000);
        }
    };

    private BroadcastReceiver mNetWorkState = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            switch (action) {
                case ConnectivityManager.CONNECTIVITY_ACTION:
                    break;
                case Intent.ACTION_TIME_TICK:
                    break;
                default:
                    break;
            }

        }
    };

    private BroadcastReceiver otherReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            switch (action) {
                case Constant.ETC_BROADCAST:
                    break;
                case Constant.REMOVE_NAVIGATIONBAR:
                    boolean isWindFullData = intent.getBooleanExtra("remove", false);
                    PreferanceUtils.saveDataBoolean(mContext, "isFullWind", isWindFullData);
                    break;
                case Constant.AMPA_BROAD:
                    String lessDis = String.format("%.2f",
                            (float) intent.getIntExtra("ROUTE_REMAIN_DIS", 0) / 1000);
                    break;
                case Constant.VOLUME_CHANGED_ACTION:
                    int currVolume = mAudioManager
                            .getStreamVolume(AudioManager.STREAM_MUSIC);
                    soundSeekBar.setProgress(currVolume);
                    break;
                default:
                    break;
            }
        }
    };

    private ContentObserver brightnessMode = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            mHandler.sendEmptyMessageDelayed(REFRESH_LIGHT, 1000);
        }
    };

    private BroadcastReceiver recordReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            switch (action) {
                case Constant.RECORDER_START:
                    refreshRecordingView(true);
                    break;
                case Constant.RECORDER_STOP:
                    refreshRecordingView(false);
                    break;
                case Constant.CAPTURE_DONE:
                    break;
                default:
                    break;

            }

        }
    };

}
