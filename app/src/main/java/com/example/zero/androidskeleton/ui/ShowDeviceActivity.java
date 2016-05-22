package com.example.zero.androidskeleton.ui;

import android.app.Service;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.TextView;
import com.example.zero.androidskeleton.GlobalObjects;
import com.example.zero.androidskeleton.R;
import com.example.zero.androidskeleton.bt.BlueLockProtocol;
import com.example.zero.androidskeleton.bt.BtLeDevice;
import com.example.zero.androidskeleton.bt.BtLeService;
import com.example.zero.androidskeleton.component.PasswordEdit;
import com.example.zero.androidskeleton.log.Log;
import com.example.zero.androidskeleton.state.impl.UnlockSM;
import com.example.zero.androidskeleton.storage.BtDeviceStorage;
import com.example.zero.androidskeleton.storage.Settings;
import com.example.zero.androidskeleton.ui.anim.Rotate3dAnimation;
import com.example.zero.androidskeleton.utils.Utils;

import java.util.TimerTask;


/**
 *
 */
public class ShowDeviceActivity extends BaseActivity implements NavigationView.OnNavigationItemSelectedListener, BtLeDevice.DeviceListener, SensorEventListener {
    private static final String TAG = "ShowDeviceActivity";

    private BtLeDevice mDevice = null;

    private SensorManager mSensorManager = null;
    private Vibrator mVibrator = null;

    private PasswordEdit mPasswordEdit;
    private ImageView mUnlockImg;
    private ImageView mUnlockHintImg;
    private TextView mUnlockHint;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_show_device_main);

        android.support.v7.widget.Toolbar toolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.activity_show_device_toolbar);
        setSupportActionBar(toolbar);

        final DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout_show);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        View headView = navigationView.getHeaderView(0);
        ImageView imageView = (ImageView) headView.findViewById(R.id.backImg);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawer.closeDrawer(GravityCompat.START);
            }
        });

        Intent intent = getIntent();
        if (intent == null || intent.getExtras() == null) {
            finish();
            return;
        }
        Bundle bundle = intent.getExtras();

        mDevice = BtLeService.INSTANCE.getDevice(bundle.getString("addr"));
        if (mDevice == null) {
            Utils.makeToast(this, "no device supplied");
            finish();
            return;
        }

        ActionBar actionBar = getSupportActionBar();
        Log.e(TAG, "action bar: " + actionBar);
        if (actionBar != null) {
            actionBar.setTitle(mDevice.getName());
        }

        setupUiComp();

        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mVibrator = (Vibrator) getSystemService(Service.VIBRATOR_SERVICE);

        unlockSM = new UnlockSM(this, mDevice);

        mDevice.addDeviceListener(this);
    }

    private void setupUiComp() {
        mPasswordEdit = (PasswordEdit) findViewById(R.id.password_edit);
        assert mPasswordEdit != null;

        mUnlockImg = (ImageView) findViewById(R.id.icon_mode_img);
        assert mUnlockImg != null;

        mUnlockHintImg = (ImageView) findViewById(R.id.icon_hint_img);
        assert mUnlockHintImg != null;

        mUnlockHint = (TextView) findViewById(R.id.result_hint);
        assert mUnlockHint != null;

        //mPasswordEdit.addTextChangedListener(new TextWatcher() {
        //    @Override
        //    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        //    }
        //
        //    @Override
        //    public void onTextChanged(CharSequence s, int start, int before, int count) {
        //    }
        //
        //    @Override
        //    public void afterTextChanged(Editable s) {
        //        if (GlobalObjects.unlockMode != GlobalObjects.UNLOCK_MODE_AUTO) {
        //            return;
        //        }
        //
        //        String password = mPasswordEdit.getText().toString();
        //        if (password.length() != 6) {
        //            return;
        //        }
        //
        //        // cancel previous task
        //        // unlockSM.handle(EVENT_CANCEL, -1, null);
        //
        //        // try this one
        //        unlockSM.handle(EVENT_UNLOCK, -1, password);
        //    }
        //});
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.show_device_menu, menu);

        final BtLeDevice.State state;
        if (mDevice == null) {
            state = BtLeDevice.State.DISCONNECTED;
        } else {
            state = mDevice.getState();
        }

        // TODO: 暂时使用连接按钮来表示状态
        MenuItem item = menu.findItem(R.id.menu_connect);
        item.setVisible(true);
        item.setEnabled(false);
        item.setTitle(state.toString());
        //switch (state) {
        //    case DISCONNECTED:
        //        menu.findItem(R.id.menu_connect).setVisible(true);
        //        menu.findItem(R.id.menu_disconnect).setVisible(false);
        //        menu.findItem(R.id.menu_refresh).setActionView(null);
        //        break;
        //    case READY:
        //        menu.findItem(R.id.menu_connect).setVisible(false);
        //        menu.findItem(R.id.menu_disconnect).setVisible(true);
        //        menu.findItem(R.id.menu_refresh).setActionView(null);
        //        break;
        //    case CONNECTING:
        //    case CONNECTED:
        //    case DISCOVERING_SERVICE:
        //        menu.findItem(R.id.menu_connect).setVisible(false);
        //        menu.findItem(R.id.menu_disconnect).setVisible(true);
        //        menu.findItem(R.id.menu_refresh).setActionView(
        //                R.layout.actionbar_indeterminate_progress);
        //        break;
        //    case DISCONNECTING:
        //        menu.findItem(R.id.menu_connect).setVisible(true);
        //        menu.findItem(R.id.menu_connect).setEnabled(false);
        //        menu.findItem(R.id.menu_disconnect).setVisible(false);
        //        menu.findItem(R.id.menu_refresh).setActionView(
        //                R.layout.actionbar_indeterminate_progress);
        //        break;
        //    default:
        //        break;
        //}
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
            case R.id.menu_connect:
                mDevice.connectGatt(getApplicationContext());
                invalidateOptionsMenu();
                break;
            case R.id.menu_disconnect:
                mDevice.disconnectGatt();
                invalidateOptionsMenu();
                break;
            default:
                break;
        }
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();

        Log.i(TAG, "onResume");

        mSensorManager.registerListener(
            this,
            mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
            SensorManager.SENSOR_DELAY_NORMAL);

        BtDeviceStorage.DeviceInfo info = BtDeviceStorage.INSTANCE.get(mDevice.getAddress());
        if (info != null) {
            mPasswordEdit.setText(info.getPassword());
        }

        mUnlockImg.setImageResource(getUnlockImage());

        mUnlockHintImg.setVisibility(View.INVISIBLE);

        // 根据不同的模式决定界面如何显示
        switch (Settings.INSTANCE.getUnlockMode()) {
            case Settings.UNLOCK_MODE_AUTO:
            case Settings.UNLOCK_MODE_MANUNAL: {
                mUnlockHintImg.setVisibility(View.VISIBLE);
                mUnlockImg.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String password = mPasswordEdit.getText().toString();
                        if (password.length() != 6) {
                            Utils.makeToast(getApplicationContext(), "password incorrect");
                            return;
                        }
                        // disable touch first
                        mUnlockImg.setOnClickListener(null);
                        unlock(password);
                    }
                });
                break;
            }
            case Settings.UNLOCK_MODE_SHAKE: {
                mUnlockImg.setOnClickListener(null);
                break;
            }
            default:
                break;
        }
    }

    private static int getUnlockImage() {
        switch (Settings.INSTANCE.getUnlockMode()) {
            case Settings.UNLOCK_MODE_MANUNAL:
            case Settings.UNLOCK_MODE_AUTO:
                return R.drawable.icon_manualmode_lock;
            case Settings.UNLOCK_MODE_SHAKE:
                return R.drawable.icon_rockmode_lock;
            default:
                return R.drawable.icon_manualmode_lock;
        }
    }

    private static int getUnlockImageSuccess() {
        switch (Settings.INSTANCE.getUnlockMode()) {
            case Settings.UNLOCK_MODE_MANUNAL:
            case Settings.UNLOCK_MODE_AUTO:
            case Settings.UNLOCK_MODE_SHAKE:
                return R.drawable.icon_manualmode_succeed;
            default:
                return R.drawable.icon_manualmode_succeed;
        }
    }

    @Override
    protected void onPause() {
        mSensorManager.unregisterListener(this);
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if (mDevice != null) {
            mDevice.removeDeviceListener(this);
            mDevice.disconnectGatt();
        }
        super.onDestroy();
    }

    private void unlock(String password) {
        // FIXME: delete this line
        Log.d(TAG, "unlock: " + password);
        unlockSM.handle(UnlockSM.EVENT_UNLOCK, -1, password);
    }

    private UnlockSM unlockSM;

    private class AutoUnlockTask extends Thread {
        private boolean isStop;
        @Override
        public void run() {
            // TODO
        }
    }

    private final Thread autoUnlockTask = new Thread(new AutoUnlockTask());

    @Override
    public void onDeviceStateChanged(final BtLeDevice.State state) {
        Log.e(TAG, "new state: " + state);

        unlockSM.handle(UnlockSM.EVENT_DEV_STATE_CHANGED, -1, state);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                invalidateOptionsMenu();
            }
        });
    }

    @Override
    public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        byte[] value = characteristic.getValue();
        if (value == null || value.length <= 0) {
            // ignore
            return;
        }

        final byte result = characteristic.getValue()[0];
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Utils.makeToast(getApplicationContext(), BlueLockProtocol.getCodeDesc(result));
                if (result == BlueLockProtocol.RESULT_PASSWORD_CORRECT) {
                    // 保存密码
                    String password = mPasswordEdit.getText().toString();
                    BtDeviceStorage.DeviceInfo info = BtDeviceStorage.INSTANCE.get(mDevice.getAddress());
                    if (info == null) {
                        info = new BtDeviceStorage.DeviceInfo(mDevice.getName(), mDevice.getAddress());
                    }
                    if (!password.equals(info.getPassword())) {
                        info.setPassword(password);
                        if (BtDeviceStorage.INSTANCE.put(info)) {
                            Log.i(TAG, "password saved");
                        } else {
                            Log.e(TAG, "failed to save password");
                        }
                    }

                    playAnime(true);
                    delayClearAndFinish();
                } else if (result == BlueLockProtocol.RESULT_PASSWORD_WRONG){
                    playAnime(false);
                    delayClearAndFinish();
                }
            }
        });

    }

    private void delayClearAndFinish() {
        GlobalObjects.timer.schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        clearAndFinish();
                    }
                });
            }
        }, 1000);
    }

    private static final long ANIME_INTERVAL = 100;
    private void playAnime(boolean result) {
        mUnlockImg.setImageResource(getUnlockImage());
        if (result) {
            Rotate3dAnimation anime1 = new Rotate3dAnimation(
                0, 90, mUnlockImg.getWidth() / 2.0f, mUnlockImg.getHeight() / 2.0f, 310.0f, true);
            anime1.setDuration(ANIME_INTERVAL);
            anime1.setFillAfter(true);
            anime1.setInterpolator(new AccelerateInterpolator());
            anime1.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    mUnlockImg.setImageResource(getUnlockImageSuccess());
                    Rotate3dAnimation anime2 = new Rotate3dAnimation(
                        90, 0, mUnlockImg.getWidth() / 2.0f, mUnlockImg.getHeight() / 2.0f, 310.0f, false);
                    anime2.setDuration(ANIME_INTERVAL);
                    anime2.setFillAfter(true);
                    anime2.setInterpolator(new AccelerateInterpolator());
                    anime2.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {
                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            mUnlockHint.setText("解锁成功");
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {
                        }
                    });
                    mUnlockImg.startAnimation(anime2);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }
            });
            mUnlockImg.startAnimation(anime1);
        } else {
            mUnlockHint.setText("解锁失败");
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (Settings.INSTANCE.getUnlockMode() != Settings.UNLOCK_MODE_SHAKE) {
            return;
        }

        int sensorType = event.sensor.getType();
        float[] values = event.values;
        if (sensorType == Sensor.TYPE_ACCELEROMETER) {
            if ((Math.abs(values[0]) > 17 || Math.abs(values[1]) > 17 || Math.abs(values[2]) > 17)) {
                Log.d("sensor x ", "============ values[0] = " + values[0]);
                Log.d("sensor y ", "============ values[1] = " + values[1]);
                Log.d("sensor z ", "============ values[2] = " + values[2]);
                mVibrator.vibrate(500);

                String password = mPasswordEdit.getText().toString();
                if (password.length() == 6) {
                    unlock(password);
                } else {
                    Utils.makeToast(getApplicationContext(), "bad password");
                }
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        switch (id) {
            case R.id.nav_show_password: {
                Bundle bundle = new Bundle();
                bundle.putString("addr", mDevice.getAddress());

                Intent intent = new Intent(this, ModifyPasswordActivity.class);
                intent.putExtras(bundle);
                startActivity(intent);
                break;
            }
            //case R.id.nav_show_mode: {
            //    Intent intent = new Intent(this, ModeSettingActivity.class);
            //    startActivity(intent);
            //    break;
            //}
            case R.id.nav_about:
                break;
            case R.id.nav_quit:
                clearAndFinish();
                break;
            default:
                break;
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout_show);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void clearAndFinish() {
        if (mDevice != null) {
            mDevice.removeDeviceListener(this);
            mDevice.disconnectGatt();
        }
        finish();

    }
}
