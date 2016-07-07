package com.example.zero.androidskeleton.ui;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import com.example.zero.androidskeleton.GlobalObjects;
import com.example.zero.androidskeleton.R;
import com.example.zero.androidskeleton.bt.BlueLockProtocol;
import com.example.zero.androidskeleton.bt.BtLeDevice;
import com.example.zero.androidskeleton.bt.BtLeService;
import com.example.zero.androidskeleton.log.Log;
import com.example.zero.androidskeleton.sort.CharacterParser;
import com.example.zero.androidskeleton.sort.PinyinComparator;
import com.example.zero.androidskeleton.sort.SideBar;
import com.example.zero.androidskeleton.sort.SideBar.OnTouchingLetterChangedListener;
import com.example.zero.androidskeleton.sort.SortAdapter;
import com.example.zero.androidskeleton.state.impl.UnlockSM;
import com.example.zero.androidskeleton.storage.BtDeviceStorage;
import com.example.zero.androidskeleton.storage.Settings;
import com.example.zero.androidskeleton.utils.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SelectDeviceActivity extends BaseActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "SelectDeviceActivity";

    private Context mContext;
    private ListView mSortListView;
    private SideBar mSortSideBar;
    private TextView mSortDialog;
    private SortAdapter mSortAdapter;
    /**
     * 汉字转换成拼音的类
     */
    private CharacterParser characterParser;
    private List<BtLeDevice> sourceDateList = new ArrayList<>();
    /**
     * 根据拼音来排列ListView里面的数据类
     */
    private PinyinComparator pinyinComparator;
    private DrawerLayout mDrawer;

    private UnlockSM unlockSM = null;


    private class MyScanListener implements BtLeService.ScanListener {

        @Override
        public void onDeviceFound(final BtLeDevice dev) {
            Log.i(TAG, "onDeviceFound: " + dev.getAddress());
            sourceDateList.add(dev);
            mSortAdapter = new SortAdapter(mContext, R.layout.select_list_item_device, sourceDateList);
            mSortListView.setAdapter(mSortAdapter);

            //if (true) {
            //    return;
            //}

            if (Settings.INSTANCE.getUnlockMode() != Settings.UNLOCK_MODE_AUTO) {
                return;
            }
            final BtDeviceStorage.DeviceInfo info = BtDeviceStorage.INSTANCE.get(dev.getAddress());
            if (info == null) {
                Log.i(TAG, "device: " + dev.getAddress() + " not saved");
                return;
            }
            dev.connectGatt(getApplicationContext());
            dev.addDeviceListener(new BtLeDevice.DeviceListener() {
                @Override
                public void onDeviceStateChanged(BtLeDevice.State state) {
                    if (state == BtLeDevice.State.DISCONNECTING) {
                        dev.removeDeviceListener(this);
                        return;
                    }
                    if (state != BtLeDevice.State.READY) {
                        return;
                    }
                    final BluetoothGattCharacteristic char1 = dev.getCharacteristic(0xfff1);
                    dev.writeCharacteristic(char1, BlueLockProtocol.unlock(info.getPassword()), new BtLeDevice.ResultListener<Boolean>() {
                        @Override
                        public void onResult(Boolean result) {
                            if (result) {
                                dev.writeCharacteristic(char1, BlueLockProtocol.passPhone(Utils.getPhoneNum(getApplicationContext())), null);
                            }
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
                    if (result == BlueLockProtocol.RESULT_PASSWORD_WRONG
                        || result == BlueLockProtocol.RESULT_PASSWORD_CORRECT) {
                        dev.disconnectGatt();
                    }
                }
            });
        }

        @Override
        public void onScanChange(boolean isScanning) {
            if (!isScanning) {
                autoUnlock = false;
            }
            invalidateOptionsMenu();
        }
    }

    private final MyScanListener mScanListener = new MyScanListener();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;

        setContentView(R.layout.activity_select_device_main);

        android.support.v7.widget.Toolbar toolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setTitle("扫描设备");
            setSupportActionBar(toolbar);
        }

        mDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        assert mDrawer != null;
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, mDrawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        View headView = navigationView.getHeaderView(0);
        ImageView imageView = (ImageView) headView.findViewById(R.id.backImg);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDrawer.closeDrawer(GravityCompat.START);
            }
        });

        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Utils.makeToast(this, "该手机不支持低功耗蓝牙");
            finish();
        }

        if (BtLeService.INSTANCE.getAdapter() == null || BtLeService.INSTANCE.getScanner() == null) {
            Utils.makeToast(this, "蓝牙功能不支持或者开关未打开");
            finish();
        }

//        setupUiComp();

        initViews();

        // unlockSM = new UnlockSM(this, mD)
    }

    private void initViews() {
        //实例化汉字转拼音类
        characterParser = CharacterParser.getInstance();

        pinyinComparator = new PinyinComparator();

        mSortSideBar = (SideBar) findViewById(R.id.sidrbar);
        mSortDialog = (TextView) findViewById(R.id.dialog);
        mSortSideBar.setTextView(mSortDialog);

        //设置右侧触摸监听
        mSortSideBar.setOnTouchingLetterChangedListener(new OnTouchingLetterChangedListener() {

            @Override
            public void onTouchingLetterChanged(String s) {
                //该字母首次出现的位置
                int position = mSortAdapter.getPositionForSection(s.charAt(0));
                if(position != -1){
                    mSortListView.setSelection(position);
                }

            }
        });

        mSortListView = (ListView) findViewById(R.id.device_list_view);
        mSortListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //这里要利用adapter.getItem(position)来获取当前position所对应的对象
                stopScan();

                // get selected info
                final BtLeDevice device = mSortAdapter.getItem(position);
                Log.i(TAG, "device: " + device.getName() + ", " + device.getAddress());

                Bundle bundle = new Bundle();
                bundle.putString("addr", device.getAddress());

                Intent intent = new Intent(SelectDeviceActivity.this, ShowDeviceActivity.class);
                // Intent intent = new Intent(SelectDeviceActivity.this, ModifyPasswordActivity.class);
                intent.putExtras(bundle);

                startActivity(intent);
            }
        });

        // 根据a-z进行排序源数据
        Collections.sort(sourceDateList, pinyinComparator);
        mSortAdapter = new SortAdapter(mContext, R.layout.select_list_item_device, sourceDateList);
        mSortListView.setAdapter(mSortAdapter);

    }

    private void startScan() {
        BtLeService.INSTANCE.stopScan();
        sourceDateList.clear();
        mSortAdapter = new SortAdapter(mContext, R.layout.select_list_item_device, sourceDateList);
        mSortListView.setAdapter(mSortAdapter);
        BtLeService.INSTANCE.startScan();
        invalidateOptionsMenu();
    }

    private void stopScan() {
        BtLeService.INSTANCE.stopScan();
        invalidateOptionsMenu();
    }

    private boolean autoUnlock = false;
    @Override
    protected void onResume() {
        super.onResume();
        mSortAdapter.clear();
        BtLeService.INSTANCE.addScanListener(mScanListener);
        if (Settings.INSTANCE.getUnlockMode() == Settings.UNLOCK_MODE_AUTO) {
            autoUnlock = true;
            startScan();
        }
    }

    @Override
    protected void onPause() {
        BtLeService.INSTANCE.removeScanListener(mScanListener);
        stopScan();
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.select_device_menu, menu);
        // if (mScanner.isScanning()) {
        if (BtLeService.INSTANCE.isScanning()) {
            menu.findItem(R.id.menu_scan).setVisible(false);
            menu.findItem(R.id.menu_stop).setVisible(true);
            menu.findItem(R.id.menu_refresh).setActionView(
                R.layout.actionbar_indeterminate_progress);
        } else {
            menu.findItem(R.id.menu_scan).setVisible(true);
            menu.findItem(R.id.menu_stop).setVisible(false);
            menu.findItem(R.id.menu_refresh).setActionView(null);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
            case R.id.menu_scan:
                startScan();
                break;
            case R.id.menu_stop:
                stopScan();
                break;
            default:
                break;
        }
        return true;
    }
    
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.nav_sort_by_signal:
                mSortSideBar.setVisibility(View.INVISIBLE);
                break;
            case R.id.nav_sort_by_name:
                mSortSideBar.setVisibility(View.VISIBLE);
                break;
            case R.id.nav_sort_by_frequency:
                break;
            case R.id.nav_quit:
                clearAndFinish();
                break;
            case R.id.nav_unlock_mode: {
                Intent intent = new Intent(this, ModeSettingActivity.class);
                startActivity(intent);
                break;
            }
            case R.id.nav_visitor_mode: {
                Intent intent = new Intent(this, VisitorActivity.class);
                startActivity(intent);
                break;
            }
            default:
                break;
        }

        mDrawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void clearAndFinish() {
        BtLeService.INSTANCE.clearDevices();
        sourceDateList.clear();
        mSortAdapter = new SortAdapter(mContext, R.layout.select_list_item_device, sourceDateList);
        mSortListView.setAdapter(mSortAdapter);
        BtLeService.INSTANCE.stopScan();
        finish();
    }
}
