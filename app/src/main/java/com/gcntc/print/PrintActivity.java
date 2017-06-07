package com.gcntc.print;

import java.io.IOException;
import java.io.InputStream;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.gcntc.bluetooh.BluetoothAPI;

/**
 * @author Javen Wong 完成打印功能的Activity
 */
public class PrintActivity extends Activity {

	private EditText etMAC;
	private EditText toPrintText;
	private BluetoothAPI btapi= null;
	private Handler handler;

	private PrintActivity context = null;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.print_page);
		context = this;
		
		btapi=  new BluetoothAPI();
		
		etMAC = (EditText) findViewById(R.id.EditText01);
		toPrintText = (EditText) findViewById(R.id.EditText02);
		Button btnSelect = (Button) findViewById(R.id.Button01);
		Button btnTestPrint = (Button) findViewById(R.id.btn_print_visitor);
		Button bENA8 = (Button) findViewById(R.id.ENA8);
		Button bENA13 = (Button) findViewById(R.id.ENA13);
		Button bCODE39 = (Button) findViewById(R.id.CODE39);
		Button bCODE128 = (Button) findViewById(R.id.CODE128);
		Button btnCharacterPrint = (Button) findViewById(R.id.Button04);
		Button btnAsciiPrint = (Button) findViewById(R.id.Button05);
		Button btnCurvePrint = (Button) findViewById(R.id.Button06);


		/**
		 * 打开蓝牙打印机选择界面
		 */
		btnSelect.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(PrintActivity.this,
						BTDeviceListActivity.class);
				startActivityForResult(intent, 0);
			}
		});
		
		/**
		 * 连接打印机
		 */
		Button bconn = (Button) findViewById(R.id.btn_print_connect);
		bconn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {		
                String deviceid = etMAC.getText().toString();
                String pwd = "1234";
				
				int res = btapi.openPrinter(deviceid, pwd);
				if (res != 0) {
					Toast.makeText(PrintActivity.this, "打印机连接失败", Toast.LENGTH_SHORT).show();
				}
				else
				{
					Toast.makeText(PrintActivity.this, "打印机连接成功", Toast.LENGTH_SHORT).show();
				}
			}
		});

		/**
		 * 打印2维码
		 */
		btnTestPrint.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				printContent(4);
			}
		});

		/**
		 * 打印ENA8条形码
		 */
		bENA8.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				printContent(0);			
			}
		});
		
		/**
		 * 打印ENA13条形码
		 */
		bENA13.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				printContent(1);
			}
		});
		
		/**
		 * 打印CODE39条形码
		 */
		bCODE39.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				printContent(2);
			}
		});
		
		/**
		 * 打印CODE128条形码
		 */
		bCODE128.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				printContent(3);
			}
		});

		/**
		 * 打印图片
		 */
		btnCharacterPrint.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				printContent(5);
			}
		});
		
		/**
		 * 打印Ascii码
		 */
		btnAsciiPrint.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				printContent(6);
			}
		});
		
		/**
		 * 打印文本框输入内容
		 */
		btnCurvePrint.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				printContent(7);
			}
		});

		handler = new Handler() {
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case 0:
					Builder builder = new Builder(PrintActivity.this);
					builder.setTitle("提示");
					builder.setMessage("和蓝牙打印机通讯时发生错误！\n"
							+ msg.getData().getString("error"));
					builder.setPositiveButton("确定", null);
					builder.show();
					break;
				case 1:
					builder = new Builder(PrintActivity.this);
					builder.setTitle("提示");
					builder.setMessage("打印完成！");
					builder.setPositiveButton("确定", null);
					builder.show();
					break;
				}
			}
		};
	}

	/**
	 * 打印
	 * @param type 打印内容类型
	 */
	private void printContent(final int type) {
		if (etMAC.getText().toString().equals("")) {
			Toast.makeText(PrintActivity.this, "请选择一个蓝牙打印机先！",
					Toast.LENGTH_SHORT).show();
		} else {
			// 打印至蓝牙打印机
			final ProgressDialog pd = new ProgressDialog(PrintActivity.this);
			pd.setTitle("提示");
			pd.setMessage("正在打印，请稍候……");
			pd.show();

			/**
			 * 初始化打印机 ，带格式的数据打印完成后一定要设置回去否则以后打印的文字都会带次格式
			 */
			btapi.initPrinter();

			// //新开线程 打印
			new Thread() {
				public void run() {
					try {
						int rtn = 0;
						switch (type) {
						case 0: {		
							String code ="12345678";
							rtn = btapi.PrintOnedimCode(200,50,10,code,0);
						};
							break;
						case 1: {							
							String code ="12345678";
							rtn = btapi.PrintOnedimCode(200,50,10,code,1);
						}
							break;
						case 2: {							
							String code ="1234567";
							rtn = btapi.PrintOnedimCode(200,50,10,code,2);
						}
							break;
						case 3: {
							String code ="123456789012";
							rtn = btapi.PrintOnedimCode(200,50,10,code,3);
						}
							break;
						case 4: {
							rtn = btapi.PrintTwoCode("http://www.baidu.com");
						}
							break;
						case 5: {
							Bitmap bmplogo = null;
							AssetManager am = getResources().getAssets();
							try {
								InputStream is = am.open("logo.png");
								bmplogo = BitmapFactory.decodeStream(is);
								is.close();
							} catch (IOException e) {
								e.printStackTrace();
							}
							rtn = btapi.PrintImage(bmplogo);
						}
							break;
						case 6: {
							rtn = btapi.PrintByte(new byte[] { 0x0A, 0x20, 0x21,
									0x22, 0x23, 0x24, 0x25, 0x26, 0x27, 0x28,
									0x29, 0x2A, 0x2B, 0x2C, 0x2D, 0x2E, 0x2F,
									0x30, 0x31, 0x32, 0x33, 0x34, 0x35, 0x36,
									0x37, 0x38, 0x39, 0x3A, 0x3B, 0x3C, 0x3D,
									0x3E, 0x3F, 0x40, 0x41, 0x42, 0x43, 0x44,
									0x45, 0x46, 0x47, 0x48, 0x49, 0x4A, 0x4B,
									0x4C, 0x4D, 0x4E, 0x4F, 0x50, 0x51, 0x52,
									0x53, 0x54, 0x55, 0x56, 0x57, 0x58, 0x59,
									0x5A, 0x5B, 0x5C, 0x5D, 0x5E, 0x5F, 0x60,
									0x61, 0x62, 0x63, 0x64, 0x65, 0x66, 0x67,
									0x68, 0x69, 0x6A, 0x6B, 0x6C, 0x6D, 0x6E,
									0x6F, 0x70, 0x71, 0x72, 0x73, 0x74, 0x75,
									0x76, 0x77, 0x78, 0x79, 0x7A, 0x7B, 0x7C,
									0x7D, 0x7E, 0x7F, 0x0A, 0x0A });
						}
							break;
						case 7: {
							String temp="           丰城小雅居           " +
									    "番茄操蛋      1             38元" +
									    "辣椒炒肉      1             12元" +
									    "排骨汤        1              8元" +
									    "百威          10           100元"+
									    "百威          10           100元"+
									    "百威          10           100元"+
									    "百威          10           100元"+
									    "百威          10           100元"+
									    "百威          10           100元"+
									    "番茄操蛋      1             38元" +
									    "辣椒炒肉      1             12元" +
									"排骨汤        1              8元"+
									"番茄操蛋      1             38元" +
									"辣椒炒肉      1             12元" +
									"排骨汤        1              8元"+
									"番茄操蛋      1             38元" +
									"辣椒炒肉      1             12元" +
									"排骨汤        1              8元"+
									"-------------------------------"+
									"赠送金额：10元                  "+
									"应付金额：980元                 ";
//							rtn = btapi.PrintByte(toPrintText.getText().toString().getBytes("GBK"));
							rtn = btapi.PrintByte(temp.getBytes("GBK"));

						}
							break;
						default:
							break;
						}
						rtn = btapi.PrintLn();
						if(rtn == 0)
						{
						// 提示 打印完成
						handler.sendEmptyMessage(1);
						}
						else
						{
							Message msg =Message.obtain();
							msg.what = 0;
							msg.getData().putString("error", "" + rtn);
							handler.sendMessage(msg);
						}

					} catch (IOException e) {
						// 提示 打印时 出错
						Message msg =Message.obtain();
						msg.what = 0;
						msg.getData().putString("error", e.getMessage());
						handler.sendMessage(msg);

					} finally {
						pd.dismiss();

					}
				}
			}.start();
		}
	}


	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == 0 && resultCode == RESULT_OK) {
			/**
			 * 保存选择打印机地址
			 */
			etMAC.setText(data
					.getStringExtra(BTDeviceListActivity.EXTRA_DEVICE_ADDRESS));

	}
	}

	@Override
	protected void onDestroy() {

		btapi.closePrinter();
		super.onDestroy();
	}
}
