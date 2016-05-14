package com.example.zero.androidskeleton.ui;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ToggleButton;
import com.example.zero.androidskeleton.R;
import com.example.zero.androidskeleton.bt.BtLeDevice;
import com.example.zero.androidskeleton.bt.BtLeService;
import com.example.zero.androidskeleton.log.Log;
import com.example.zero.androidskeleton.utils.Utils;

public class SelectDeviceActivity extends BaseActivity {

    private static final String TAG = "SelectDeviceActivity";

    private SimpleArrayAdapter mListViewAdapter;

    private void log(final String msg) {
        Log.i(TAG, msg + '\n');
    }

    private class MyScanListener implements BtLeService.ScanListener {

        @Override
        public void onDeviceFound(BtLeDevice dev) {
            mListViewAdapter.add(dev);
        }

        @Override
        public void onScanChange(boolean isScanning) {
            invalidateOptionsMenu();
        }
    }

    private final MyScanListener mScanListener = new MyScanListener();

    private void checkAllMyPermission() {
        final String[] permission_list = {
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.ACCESS_COARSE_LOCATION,
        };
        for (String permission: permission_list) {
            checkMyPermission(permission);
        }
    }

    private void checkMyPermission(String permission) {
        int result = ContextCompat.checkSelfPermission(getApplicationContext(), permission);
        if (result == PackageManager.PERMISSION_GRANTED) {
            return;
        }

        ActivityCompat.requestPermissions(this, new String[] { permission }, 0);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        checkAllMyPermission();

        mListViewAdapter = new SimpleArrayAdapter(this, R.layout.select_list_item_device);

        setContentView(R.layout.activity_select_device);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("Select Device");
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Utils.makeToast(this, "该手机不支持低功耗蓝牙");
            finish();
        }

        if (BtLeService.INSTANCE.getAdapter() == null || BtLeService.INSTANCE.getScanner() == null) {
            Utils.makeToast(this, "蓝牙功能不支持或者开关未打开");
            finish();
        }

        setupUiComp();

    }

    private void startScan() {
        mListViewAdapter.clear();
        BtLeService.INSTANCE.startScan();
        invalidateOptionsMenu();
    }

    private void stopScan() {
        BtLeService.INSTANCE.stopScan();
        invalidateOptionsMenu();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mListViewAdapter.clear();
        BtLeService.INSTANCE.addScanListener(mScanListener);
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

    private ToggleButton mAutoButton;
    private void setupUiComp() {
        ListView device_list_view = (ListView) findViewById(R.id.device_list_view);
        assert device_list_view != null;
        device_list_view.setAdapter(mListViewAdapter);
        device_list_view.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                BtLeService.INSTANCE.stopScan();

                // get selected info
                final BtLeDevice device = mListViewAdapter.getItem(position);
                log("device: " + device.getName() + ", " + device.getAddress());

                Bundle bundle = new Bundle();
                bundle.putString("addr", device.getAddress());

                Intent intent = new Intent(SelectDeviceActivity.this, ShowDeviceActivity.class);
                // Intent intent = new Intent(SelectDeviceActivity.this, ModifyPasswordActivity.class);
                intent.putExtras(bundle);

                startActivity(intent);
            }
        });
    }
}
