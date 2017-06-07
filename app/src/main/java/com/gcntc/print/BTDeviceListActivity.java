package com.gcntc.print;

import java.util.Set;

import android.app.Activity;
import android.app.ProgressDialog;
import android.app.TabActivity;
import android.app.AlertDialog.Builder;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TabHost.TabSpec;

/**
 * @author Javen Wong 用于选择蓝牙设备的 列表
 */
public class BTDeviceListActivity extends TabActivity {

	public static BluetoothAdapter mBtAdapter;
	private ArrayAdapter<String> mPairedDevicesArrayAdapter;
	private ArrayAdapter<String> mNewDevicesArrayAdapter;

	public static boolean openBluetoothByMe = true ;

	private ProgressDialog pd;

	// Return Intent extra
	public static String EXTRA_DEVICE_ADDRESS = "device_address";

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

		setContentView(R.layout.bt_list_tab);

		TabHost th = getTabHost(); // 获取TabHost对象
		TabSpec ts = null;

		ts = th.newTabSpec("pairedBTTab"); // 为TabHost创建新选项卡 参数可为""
		ts.setIndicator("已配对蓝牙设备"); // 为选择卡设置文文
		ts.setContent(R.id.ListView01); // 设置该选项卡被选中后显示的视图组件
		th.addTab(ts);

		ts = th.newTabSpec("newBTTab");
		ts.setIndicator("新的蓝牙设备");
		ts.setContent(R.id.ListView02);
		th.addTab(ts);

		th.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
			public void onTabChanged(String tabId) {
				if (tabId.equals("newBTTab")) {
					Builder builder = new Builder(BTDeviceListActivity.this);
					builder.setTitle("询问");
					builder.setMessage("是否开始搜索新的蓝牙设备？");
					builder.setPositiveButton("是",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int which) {
									doDiscovery();
								}
							});
					builder.setNegativeButton("否", null);
					builder.show();
				}
			}
		});

		// Initialize array adapters. One for already paired devices and
		// one for newly discovered devices
		mPairedDevicesArrayAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1);
		mNewDevicesArrayAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1);

		// Find and set up the ListView for paired devices
		ListView pairedListView = (ListView) findViewById(R.id.ListView01);
		pairedListView.setAdapter(mPairedDevicesArrayAdapter);
		pairedListView.setOnItemClickListener(mDeviceClickListener);

		// Find and set up the ListView for newly discovered devices
		ListView newDevicesListView = (ListView) findViewById(R.id.ListView02);
		newDevicesListView.setAdapter(mNewDevicesArrayAdapter);
		newDevicesListView.setOnItemClickListener(mDeviceClickListener);

		// 注册广播接收器 以响应 找到新的蓝牙设备
		IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
		registerReceiver(mReceiver, filter);

		// 注册广播接收器 以响应 搜索新设备完成
		filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
		registerReceiver(mReceiver, filter);

		// 注册广播接收器 以响应 蓝牙开/关 状态
		filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
		registerReceiver(mReceiver, filter);

		// Get the local Bluetooth adapter
		mBtAdapter = BluetoothAdapter.getDefaultAdapter();

		if (mBtAdapter == null) {

			Toast.makeText(BTDeviceListActivity.this, "很遗憾，您的手机不支持蓝牙！",
					Toast.LENGTH_SHORT).show();

			finish();
		}

		if (!mBtAdapter.isEnabled()) {
			// 提示用户 开启蓝牙

			Builder builder = new Builder(BTDeviceListActivity.this);
			builder.setTitle("询问");
			builder.setMessage("蓝牙尚未开启，是否立即开启？");
			builder.setPositiveButton("是",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {

							// 提示正在开启蓝牙
							pd = new ProgressDialog(BTDeviceListActivity.this);
							pd.setTitle("请稍候");
							pd.setMessage("正在开启蓝牙……");
							pd.show();

							mBtAdapter.enable();

						}
					});
			builder.setNegativeButton("否",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							// 用户没有选择开启蓝牙
							Toast.makeText(BTDeviceListActivity.this,
									"蓝牙尚未开启，请开启蓝牙后重试！", Toast.LENGTH_SHORT)
									.show();

							finish();
						}
					});
			builder.show();
		} else {
			doGetBondedDevices();
		}
	}

	private void doGetBondedDevices() {
		// Get a set of currently paired devices
		Set<BluetoothDevice> pairedDevices = mBtAdapter.getBondedDevices();

		// If there are paired devices, add each one to the ArrayAdapter
		if (pairedDevices.size() > 0) {
			for (BluetoothDevice device : pairedDevices) {
				mPairedDevicesArrayAdapter.add(device.getName() + "\n"
						+ device.getAddress());
			}
		} else {
			mPairedDevicesArrayAdapter.add("没有已配对的蓝牙设备");
		}

		// mPairedDevicesArrayAdapter.notifyDataSetChanged();
	}

	/**
	 * Start device discover with the BluetoothAdapter
	 */
	private void doDiscovery() {

		mNewDevicesArrayAdapter.clear();

		pd = new ProgressDialog(this);
		pd.setTitle("请稍候");
		pd.setMessage("正在搜索新的蓝牙设备……");
		pd.setOnCancelListener(new DialogInterface.OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {
				mBtAdapter.cancelDiscovery();

				Toast.makeText(BTDeviceListActivity.this, "搜索过程被取消！",
						Toast.LENGTH_SHORT).show();
			}
		});
		pd.show();

		// If we're already discovering, stop it
		if (mBtAdapter.isDiscovering()) {
			mBtAdapter.cancelDiscovery();
		}

		// Request discover from BluetoothAdapter
		mBtAdapter.startDiscovery();
	}

	// The on-click listener for all devices in the ListViews
	private OnItemClickListener mDeviceClickListener = new OnItemClickListener() {
		public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
			// Cancel discovery because it's costly and we're about to connect
			mBtAdapter.cancelDiscovery();
		
			// Get the device MAC address, which is the last 17 chars in the
			// View
			String info = ((TextView) v).getText().toString();//20:13:08:12:32:16
			String address = info.substring(info.length() - 17);

			// Create the result Intent and include the MAC address
			Intent intent = new Intent();
			intent.putExtra(EXTRA_DEVICE_ADDRESS, address);

			// Set result and finish this Activity
			setResult(Activity.RESULT_OK, intent);
			finish();
		}
	};

	// The BroadcastReceiver that listens for discovered devices and
	// changes the title when discovery is finished
	private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();

			// When discovery finds a device
			if (BluetoothDevice.ACTION_FOUND.equals(action)) {
				// Get the BluetoothDevice object from the Intent
				BluetoothDevice device = intent
						.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				// If it's already paired, skip it, because it's been listed
				// already
				if (device.getName() !=null) {
					if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
						mNewDevicesArrayAdapter.add(device.getName() + "\n"
								+ device.getAddress());	
				}

				}
				// When discovery is finished, change the Activity title
			} else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED
					.equals(action)) {
				if (pd !=null) {
					pd.dismiss();
					pd = null;
				}

				if (mNewDevicesArrayAdapter.getCount() == 0) {
					Builder builder = new Builder(BTDeviceListActivity.this);
					builder.setTitle("提示");
					builder.setMessage("搜索完成，找到了 "
							+ mNewDevicesArrayAdapter.getCount() + " 个新设备！");
					builder.setPositiveButton("确定", null);
					builder.show();
				}
			} else if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
				if (pd != null) {

					if (intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1) == BluetoothAdapter.STATE_ON) {
						pd.dismiss();
						pd = null;

						// 用户 选择开启蓝牙
						openBluetoothByMe = true;

						// 获取已配对的蓝牙设备
						doGetBondedDevices();
					}
				}
			}
		}
	};
	
	@Override
	 public void onDestroy() {
		   //删除广播注册
		   unregisterReceiver(mReceiver);
		  super.onDestroy();
		 }
}