package com.lxm.photo;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.content.res.Resources.NotFoundException;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.telephony.TelephonyManager;
import android.text.format.DateFormat;
import android.text.format.Time;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.JavascriptInterface;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.AndroidHttpTransport;
import org.xmlpull.v1.XmlPullParserException;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.sql.Date;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import it.sauronsoftware.ftp4j.FTPClient;
import it.sauronsoftware.ftp4j.FTPException;
import it.sauronsoftware.ftp4j.FTPIllegalReplyException;

//如果使用地理围栏功能，需要import如下类

public class MainActivity extends Activity {
    private ImageView imageView;
    private Button button;
    private TextView txtView;
    private WebView mWebView; //网页显示控件
    private final int CAMREA_RESQUSET = 1;
    private final int PHOTOSHOW_RESQUEST = 2;
    private Uri imageUri; //图片路径
    private String filename; //图片名称
    //系统相机照片的临时照片路径
    private String tempPicPath = Environment.getExternalStorageDirectory().toString() + "/MyImage/temp.jpg";

    //定位所用到的变量
    public LocationClient mLocationClient = null;
    public BDLocationListener myListener = new MyLocationListener();
    private static final int UPDATE_TIME = 5000;
    private static Double mDLat = 0.0;    //当前手机的经度
    private static Double mDLon = 0.0;    //当前手机的纬度
    private static String mSAddrInfo = "";    //当前手机的地址信息

    //上传文件的名称
    private static String mFileName = "";
    //要上传的本地文件路径(不包括文件名)
    private static String mFilePath = "";
    //FTP服务器的IP
    private static String mFTPServer = "192.168.0.100";
    //FTP服务器端口
    private static String mFTPPort = "8888";
    //FTP服务器登录名
    private static String mFTPUser = "ftpuser";
    //FTP服务器登录密码
    private static String mFTPPassword = "ftpuser";
    //FTP服务器要存放的目录
    private static String mFTPFileDir = "";


    private static final String NAMESPACE = "http://tempuri.org/";
    private static String m_sImei = "";    //手机IMEI码(改为当前用户名)
    // WebService地址
    private static String URL = "http://10.0.2.2/heihe/WebService/Station.asmx?wsdl";
    private static String ServiceURL = "";
    private static String ServiceLocation = "";
    private static final String METHOD_NAME = "Test";
    private static String SOAP_ACTION = "http://WebXml.com.cn/getWeatherbyCityName";
    private static String mTest = "";

    private Menu m_cCMenu = null;
    private int m_nThreadGetState = 1;    //获取当前状态线程等待标志1不等待执行
    private Boolean m_bRPartol = false;    //是否开始再次巡查
    private Boolean m_bSP = true;    //巡查按钮是否可用
    private Boolean m_bEP = false;    //结束巡查按钮是否可用
    private Boolean m_isThreadLocation = true;    //记录经纬度线程运行状态
    private Handler mHandler = new Handler(); //用于JS调用JAVA代码
    private long m_exitTime = 0;
    private int m_nNoteLocationInterval = 1;
    private Boolean m_bThreadLocateionGetState = true;    //获取记录经纬度间隔线程状态，true执行，false等待

    private MyReceiver m_receiver = null;        //获取广播数据类
    private int nTest = 1;
    //安卓消息队列
    private final Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    ShowDlg(mTest);
                    break;
                case 2:
                    ShowDlg("开始调用");
                    break;
                case 3:
                    ShowDlg("mTest");
            }
        }

        ;
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //imageView = (ImageView) this.findViewById(R.id.imageView1);
        //txtView = (TextView) this.findViewById(R.id.textView1);

        TelephonyManager tm = (TelephonyManager) this.getSystemService(TELEPHONY_SERVICE);
        /*String sImei = tm.getDeviceId();

		m_sImei = sImei;*/

        //WebView设置
        mWebView = (WebView) findViewById(R.id.webView1);
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setSavePassword(false);
        mWebView.getSettings().setSaveFormData(false);
        //mWebView.getSettings().setPluginState(WebSettings.PluginState.ON);
        // 开启 DOM storage API 功能  
        mWebView.getSettings().setDomStorageEnabled(true);
        mWebView.setWebViewClient(new WebViewClientDemo());
        mWebView.setWebChromeClient(new HellowebViewClient());

        ServiceURL = getResources().getString(R.string.ServiceURL);
        ServiceLocation = getResources().getString(R.string.ServiceLocation);

        mWebView.addJavascriptInterface(new Object() {
            @JavascriptInterface
            public void PostUserOnAndroid(final String ssTest) {
                mHandler.post(new Runnable() {
                    public void run() {
                        m_sImei = ssTest;
                        Intent ServiceGpsIntent = new Intent(ServiceGps.ACTION);
                        ServiceGpsIntent.putExtra("sImei", m_sImei);
                        ServiceGpsIntent.putExtra("SL", ServiceLocation);
                        startService(ServiceGpsIntent);
                        //ShowDlg(m_sImei);
                        //onClickMenuPhoto();
                    }
                });
            }

            @JavascriptInterface
            public void ExitOnAndroid() {
                mHandler.post(new Runnable() {
                    public void run() {
                        m_isThreadLocation = false;
                        finish();
                        System.exit(0);
                        //ShowDlg(m_sImei);
                        //onClickMenuPhoto();
                    }
                });
            }

            @JavascriptInterface
            public void clickOnAndroid(final String ssTest) {
                mHandler.post(new Runnable() {
                    public void run() {
                        //ShowDlg(ssTest);
                        onClickMenuPhoto();
                    }
                });
            }
        }, "demo");

        //mWebView.loadUrl(getResources().getString(R.string.app_url) + "?imei=" + sImei); 
        mWebView.loadUrl(getResources().getString(R.string.app_url));

        //ShowDlg(ServiceURL);

        //LogWrite("liuxinming");

        mLocationClient = new LocationClient(getApplicationContext());     //声明LocationClient类
        mLocationClient.registerLocationListener(myListener);    //注册监听函数
        LocationClientOption option = new LocationClientOption();
        option.setOpenGps(true);        //是否打开GPS
        option.setCoorType("bd09ll");       //设置返回值的坐标类型。
        //option.setPriority(LocationClientOption.NetWorkFirst);  //设置定位优先级
        option.setProdName("LocationDemo"); //设置产品线名称。强烈建议您使用自定义的产品线名称，方便我们以后为您提供更高效准确的定位服务。
        option.setScanSpan(UPDATE_TIME);    //设置定时定位的时间间隔。单位毫秒
        option.setIsNeedAddress(true);
        mLocationClient.setLocOption(option);
        mLocationClient.start();

        new Thread(new Runnable() {
            public void run() {
                m_bThreadLocateionGetState = false;
                GetNoteLocationInterval();
            }
        }).start();

        try {
            UpdateTest();
        } catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            //LogWrite(e.toString());
            e.printStackTrace();
        } catch (NotFoundException e) {
            // TODO Auto-generated catch block
            //LogWrite(e.toString());
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            //LogWrite(e.toString());
            e.printStackTrace();
        }

        //注册广播接收器
        m_receiver = new MyReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction("ServiceGps");
        this.registerReceiver(m_receiver, filter);
    }

    public void LogWrite(String info) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss ");
        Date curDate = new Date(System.currentTimeMillis());//获取当前时间
        String strDate = formatter.format(curDate);
        info = strDate + ":" + info;

        File file = new File("lxm.txt");
        if (file.exists()) {
            FileOutputStream phone_outStream1 = null;
            try {
                phone_outStream1 = openFileOutput("lxm.txt", Context.MODE_APPEND);
            } catch (FileNotFoundException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            } //追加模式继续写
            try {
                phone_outStream1.write(info.getBytes());
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } else {
            FileOutputStream phone_outStream = null;
            try {
                phone_outStream = this.openFileOutput("lxm.txt", Context.MODE_PRIVATE);
            } catch (FileNotFoundException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
            try {
                phone_outStream.write(info.getBytes());
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }


    }

    public void UpdateTest() throws MalformedURLException, NotFoundException, IOException {
        final UpdateManager manager = new UpdateManager(this);
        manager.checkUpdate();
        /*new Thread(new Runnable() {
            public void run() {
    	    	// 检查软件更新
    			try {
					manager.checkUpdate();
				} catch (MalformedURLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (NotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
    	    }
    	  }).start();*/

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // TODO Auto-generated method stub
        if (keyCode == KeyEvent.KEYCODE_BACK
                && event.getAction() == KeyEvent.ACTION_DOWN) {

            if ((System.currentTimeMillis() - m_exitTime) > 2000) {

                Toast.makeText(getApplicationContext(), "再按一次退出程序",
                        Toast.LENGTH_SHORT).show();
                m_exitTime = System.currentTimeMillis();
            } else {
                m_isThreadLocation = false;
                stopService(new Intent(ServiceGps.ACTION));
                unregisterReceiver(m_receiver);
                //删除COOKIES
                CookieSyncManager.createInstance(this);
                CookieManager.getInstance().removeAllCookie();
                finish();
                System.exit(0);
            }
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    /*创建menu*/
    public boolean onCreateOptionsMenu(Menu menu) {
        //为menu添加内容
        m_cCMenu = menu;
        m_nThreadGetState = m_nThreadGetState + 1;
        new Thread(new Runnable() {
            public void run() {
                //String retValue = ftpUpload(mFTPServer,mFTPPort,mFTPUser,mFTPPassword,mFTPFileDir,mFilePath,mFileName);
                CallCreateMenu();
            }
        }).start();
        while (m_nThreadGetState != 1) {
            //等待线程执行完毕才继续执行
        }
        return true;
    }

    public boolean onPrepareOptionsMenu(Menu menu) {
        //更新menu的内容
        m_cCMenu = menu;
        m_nThreadGetState = m_nThreadGetState + 1;
        new Thread(new Runnable() {
            public void run() {
                //String retValue = ftpUpload(mFTPServer,mFTPPort,mFTPUser,mFTPPassword,mFTPFileDir,mFilePath,mFileName);
                CallCreateMenu();
            }
        }).start();
        while (m_nThreadGetState != 1) {
            //等待线程执行完毕才继续执行
        }
        return true;
    }

    //获取记录当前经纬度的时间间隔
    public void GetNoteLocationInterval() {
        SoapObject rpc = new SoapObject("whservice", "SetTimeIntv");

        AndroidHttpTransport ht = new AndroidHttpTransport(ServiceLocation);
        ht.debug = true;

        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(
                SoapEnvelope.VER11);

        envelope.bodyOut = rpc;
        envelope.dotNet = true;
        envelope.setOutputSoapObject(rpc);

        try {
            ht.call(null, envelope);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            //attaget.what = 3;
            //mTest = e.toString();
            //handler.sendMessage(attaget);
            e.printStackTrace();
        } catch (XmlPullParserException e) {
            // TODO Auto-generated catch block
            //attaget.what = 3;
            //mTest = e.toString();
            //handler.sendMessage(attaget);
            e.printStackTrace();
        }

        SoapObject result = (SoapObject) envelope.bodyIn;
        String retVal = result.getProperty(0).toString();

        m_nNoteLocationInterval = Integer.valueOf(retVal);
        //ShowDlg(retVal);

        m_bThreadLocateionGetState = true;
    }

    //记录当前经纬度到数据库
    public void NoteLocation() {

        SoapObject rpc = new SoapObject("whservice", "SetJWD");
        rpc.addProperty("imei", m_sImei);
        DecimalFormat decimalFormat = new DecimalFormat("#,##0.00000000");
        if (decimalFormat.format(mDLat) == "0.00000000")
            return;
        rpc.addProperty("gps", decimalFormat.format(mDLat) + "," + decimalFormat.format(mDLon));

        AndroidHttpTransport ht = new AndroidHttpTransport(ServiceLocation);
        ht.debug = true;

        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(
                SoapEnvelope.VER11);

        envelope.bodyOut = rpc;
        envelope.dotNet = true;
        envelope.setOutputSoapObject(rpc);

        try {
            ht.call(null, envelope);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            //attaget.what = 3;
            //mTest = e.toString();
            //handler.sendMessage(attaget);
            e.printStackTrace();
        } catch (XmlPullParserException e) {
            // TODO Auto-generated catch block
            //attaget.what = 3;
            //mTest = e.toString();
            //handler.sendMessage(attaget);
            e.printStackTrace();
        }

        SoapObject result = (SoapObject) envelope.bodyIn;
        String retVal = result.getProperty(0).toString();

        //ShowDlg(retVal);
    }

    public void CallCreateMenu() {
        SoapObject rpc = new SoapObject(NAMESPACE, "GetLoginDataTable_State");
        rpc.addProperty("PDACode", m_sImei);

        AndroidHttpTransport ht = new AndroidHttpTransport(ServiceURL);
        ht.debug = true;

        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(
                SoapEnvelope.VER11);

        envelope.bodyOut = rpc;
        envelope.dotNet = true;
        envelope.setOutputSoapObject(rpc);

        try {
            ht.call(null, envelope);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            //attaget.what = 3;
            //mTest = e.toString();
            //handler.sendMessage(attaget);
            e.printStackTrace();
        } catch (XmlPullParserException e) {
            // TODO Auto-generated catch block
            //attaget.what = 3;
            //mTest = e.toString();
            //handler.sendMessage(attaget);
            e.printStackTrace();
        }

        SoapObject result = (SoapObject) envelope.bodyIn;
        String retVal = result.getProperty(0).toString();
        m_nThreadGetState = m_nThreadGetState - 1;
        if (Integer.parseInt(retVal) != 3) {
            m_cCMenu.clear();
            MenuItem itemStart;
            MenuItem itemStop;
            if (m_bRPartol == true) {
                itemStart = m_cCMenu.add(0, 0, 0, R.string.rPartol);
            } else {
                itemStart = m_cCMenu.add(0, 0, 0, R.string.startPartol);
            }
            m_cCMenu.add(0, 1, 1, R.string.MenuPhoto);
            itemStop = m_cCMenu.add(0, 2, 2, R.string.stopPartol);

            itemStart.setEnabled(m_bSP);
            itemStop.setEnabled(m_bEP);
        } else {
            m_cCMenu.clear();
        }

    }

    /*处理menu的事件 */
    public boolean onOptionsItemSelected(MenuItem item) { //得到当前选中的MenuItem的ID,
        int item_id = item.getItemId();
        switch (item_id) {
            case 0:
                onClickStartPartol();    //开始巡查
                break;
            case 1:
                onClickMenuPhoto();    //拍照上传
                break;
            case 2:
                onClickStopPartol();    //结束巡查
                break;
        }
        return true;
    }

    //结束巡查
    public void onClickStopPartol() {
        //菜单可用控制
    	/*MenuItem itemStart = m_cCMenu.getItem(0);
    	MenuItem itemStop = m_cCMenu.getItem(2);
    	itemStart.setEnabled(true);
    	itemStop.setEnabled(false);*/
        m_bSP = true;
        m_bEP = false;

        //刷新当前页面
        mWebView.reload();

        new Thread(new Runnable() {
            public void run() {
                //String retValue = ftpUpload(mFTPServer,mFTPPort,mFTPUser,mFTPPassword,mFTPFileDir,mFilePath,mFileName);
                CallStopPartol();
            }
        }).start();
    }

    //开启巡查
    public void onClickStartPartol() {
        //菜单可用控制
    	/*MenuItem itemStart = m_cCMenu.getItem(0);
    	MenuItem itemStop = m_cCMenu.getItem(2);
    	itemStart.setEnabled(false);
    	itemStop.setEnabled(true);*/
        m_bSP = false;
        m_bEP = true;

        //刷新当前页面
        mWebView.reload();

        new Thread(new Runnable() {
            public void run() {
                //String retValue = ftpUpload(mFTPServer,mFTPPort,mFTPUser,mFTPPassword,mFTPFileDir,mFilePath,mFileName);
                CallStartPartol();
            }
        }).start();
    }

    //点击拍照上传菜单
    public void onClickMenuPhoto() {
        {
            // TODO Auto-generated method stub
            File file = new File(Environment.getExternalStorageDirectory().toString() + "/MyImage/");
            if (!file.exists()) {
                boolean bR = file.mkdirs();// 创建文件夹
            }
            File outputImage = new File(tempPicPath);
            //删除系统相机留下的临时照片
            if (outputImage.exists()) {
                outputImage.delete();
            }

            try {
                if (outputImage.exists()) {
                    outputImage.delete();
                }
                outputImage.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }

            //将File对象转换为Uri并启动照相程序
            imageUri = Uri.fromFile(outputImage);
            Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
            intent.addCategory("android.intent.category.DEFAULT");
            intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri); //指定图片输出地址
            startActivityForResult(intent, CAMREA_RESQUSET);

        }
    }

    //弹出提示对话框
    public void ShowDlg(String sShow) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("提示");
        builder.setMessage(sShow);
        builder.setPositiveButton("确定", null);
        AlertDialog alert = builder.create(); // create one
        alert.show();
    }

    @Override
    public void onConfigurationChanged(Configuration config) {
        super.onConfigurationChanged(config);
    }

    //将图片保存到指定目录
    public String savePicToSdcard(Bitmap bitmap, String path,
                                  String fileName) {
        String filePath = "";
        if (bitmap == null) {
            return filePath;
        } else {

            filePath = path + fileName;
            File destFile = new File(filePath);
            FileOutputStream os = null;
            try {
                os = new java.io.FileOutputStream(destFile);
                bitmap.compress(CompressFormat.JPEG, 90, os);
                os.flush();
                os.close();
            } catch (IOException e) {
                filePath = "";
            }
        }
        //ShowDlg(filePath);
        return filePath;
    }

    public static byte[] getBytes(Bitmap bitmap) {
        //实例化字节数组输出流
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 0, baos);//压缩位图
        return baos.toByteArray();//创建分配字节数组
    }

    //使用意图的回传值，判断照片是否已拍摄完毕
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMREA_RESQUSET && resultCode == RESULT_OK) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inTempStorage = new byte[1024 * 1024 * 2];
            options.inSampleSize = 2;
            Bitmap bitmap = BitmapFactory.decodeFile(tempPicPath,
                    options);

            File tF = new File(tempPicPath);
            //删除系统相机留下的临时照片
            if (tF.exists()) {
                //tF.delete();
                //图片的名称
                mFileName = new DateFormat().format("yyyyMMdd_hhmmss", Calendar.getInstance(Locale.CHINA)) + ".jpg";
                mFilePath = Environment.getExternalStorageDirectory().toString() + "/MyImage/";
                String fileFullPath = savePicToSdcard(bitmap, mFilePath, mFileName);

            }

            //跳转新的ACTIVITY
            Intent it = new Intent();
            it.putExtra("bitmap", tempPicPath);
            it.putExtra("gps", mDLat + "," + mDLon);
            it.putExtra("imei", m_sImei);
            it.setClass(MainActivity.this, PhotoShow.class);
            //startActivity(it);
            startActivityForResult(it, PHOTOSHOW_RESQUEST);
        } else if (requestCode == PHOTOSHOW_RESQUEST && resultCode == RESULT_OK) {
            String sTemp = data.getStringExtra("picPath");
            mWebView.loadUrl("javascript: phonecallback('" + sTemp + "')");
        }
    }

    public class MyLocationListener implements BDLocationListener {
        @Override
        public void onReceiveLocation(BDLocation location) {
            if (location == null)
                return;
            StringBuffer sb = new StringBuffer(256);
            sb.append("time : ");
            sb.append(location.getTime());
            sb.append("\nerror code : ");
            sb.append(location.getLocType());
            sb.append("\nlatitude : ");
            sb.append(location.getLatitude());
            mDLat = location.getLatitude();
            sb.append("\nlontitude : ");
            mDLon = location.getLongitude();
            sb.append(location.getLongitude());
            sb.append("\nradius : ");
            sb.append(location.getRadius());
            sb.append("\n省份 : ");
            sb.append(location.getProvince());
            sb.append("\n城市 : ");
            sb.append(location.getCity());
            sb.append("\n区县 : ");
            sb.append(location.getDistrict());
            if (location.getLocType() == BDLocation.TypeGpsLocation) {
                sb.append("\nspeed : ");
                sb.append(location.getSpeed());
                sb.append("\nsatellite : ");
                sb.append(location.getSatelliteNumber());
            } else if (location.getLocType() == BDLocation.TypeNetWorkLocation) {
                sb.append("\naddr : ");
                sb.append(location.getAddrStr());
                mSAddrInfo = location.getAddrStr();
            }
        }
    }

    public String CallStopPartol() {
        SoapObject rpc = new SoapObject(NAMESPACE, "PartolEnd");
        rpc.addProperty("PadCode", m_sImei);

        AndroidHttpTransport ht = new AndroidHttpTransport(ServiceURL);
        ht.debug = true;

        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(
                SoapEnvelope.VER11);

        envelope.bodyOut = rpc;
        envelope.dotNet = true;
        envelope.setOutputSoapObject(rpc);

        try {
            ht.call(null, envelope);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            //attaget.what = 3;
            //mTest = e.toString();
            //handler.sendMessage(attaget);
            e.printStackTrace();
        } catch (XmlPullParserException e) {
            // TODO Auto-generated catch block
            //attaget.what = 3;
            //mTest = e.toString();
            //handler.sendMessage(attaget);
            e.printStackTrace();
        }

        SoapObject result = (SoapObject) envelope.bodyIn;
        mTest = result.getProperty(0).toString();

        return mTest;
    }

    public String CallStartPartol() {
        SoapObject rpc;
        if (m_bRPartol == true) {
            rpc = new SoapObject(NAMESPACE, "Add_PartolRecord");
            rpc.addProperty("PDACode", m_sImei);
        } else {
            rpc = new SoapObject(NAMESPACE, "PartolStart");
            rpc.addProperty("PadCode", m_sImei);
            m_bRPartol = true;
        }

        AndroidHttpTransport ht = new AndroidHttpTransport(ServiceURL);
        ht.debug = true;

        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(
                SoapEnvelope.VER11);

        envelope.bodyOut = rpc;
        envelope.dotNet = true;
        envelope.setOutputSoapObject(rpc);

        try {
            ht.call(null, envelope);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            //attaget.what = 3;
            //mTest = e.toString();
            //handler.sendMessage(attaget);
            e.printStackTrace();
        } catch (XmlPullParserException e) {
            // TODO Auto-generated catch block
            //attaget.what = 3;
            //mTest = e.toString();
            //handler.sendMessage(attaget);
            e.printStackTrace();
        }

        SoapObject result = (SoapObject) envelope.bodyIn;
        mTest = result.getProperty(0).toString();
        return mTest;
    }

    //调用WebService
    public String CallWebService() {
        SoapObject rpc = new SoapObject(NAMESPACE, METHOD_NAME);
        DecimalFormat decimalFormat = new DecimalFormat("#,##0.00000000");
        rpc.addProperty("s1", mSAddrInfo);
        rpc.addProperty("s2", decimalFormat.format(mDLat));
        rpc.addProperty("s3", decimalFormat.format(mDLon));

        AndroidHttpTransport ht = new AndroidHttpTransport(URL);
        ht.debug = true;

        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(
                SoapEnvelope.VER11);

        envelope.bodyOut = rpc;
        envelope.dotNet = true;
        envelope.setOutputSoapObject(rpc);

        //Message attaget = Message.obtain();
        //attaget.what = 2;
        //handler.sendMessage(attaget);
        try {
            ht.call(null, envelope);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            //attaget.what = 3;
            //mTest = e.toString();
            //handler.sendMessage(attaget);
            e.printStackTrace();
        } catch (XmlPullParserException e) {
            // TODO Auto-generated catch block
            //attaget.what = 3;
            //mTest = e.toString();
            //handler.sendMessage(attaget);
            e.printStackTrace();
        }

        SoapObject result = (SoapObject) envelope.bodyIn;
        mTest = result.getProperty(0).toString();

        //attaget.what = 1;
        //handler.sendMessage(attaget);
        //ShowDlg("刚开始");

        // 获取返回的结果
        //String result = object.getProperty(0).toString();

        return "";
    }

    /**
     * 通过ftp上传文件
     *
     * @param url          ftp服务器地址 如： 192.168.1.110
     * @param port         端口如 ： 21
     * @param username     登录名
     * @param password     密码
     * @param remotePath   上到ftp服务器的磁盘路径
     * @param fileNamePath 要上传的文件路径
     * @param fileName     要上传的文件名
     * @return
     */
    public String ftpUpload(String url, String port, String username, String password, String remotePath, String fileNamePath, String fileName) {
        String retVal = "";
        // 创建客户端
        FTPClient client = new FTPClient();
        try {
            // 不指定端口，则使用默认端口21
            client.connect(url, Integer.parseInt(port));
            // 用户登录
            client.login(username, password);
            File tempFile = new File(fileNamePath + fileName);
            client.upload(tempFile);
            retVal = "1";
        } catch (Exception e) {
            e.printStackTrace();
            retVal = "0";
        }

        try {
            client.disconnect(true);
        } catch (IllegalStateException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (FTPIllegalReplyException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (FTPException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return retVal;
    }

    public class WebViewClientDemo extends WebViewClient {
        @Override
        // 在WebView中而不是默认浏览器中显示页面
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }

        @Override
        public void onPageStarted(WebView view,
                                  String url,
                                  Bitmap favicon) {
            //ShowDlg("开始加载");
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            //ShowDlg("加载完成");
        }

        @Override
        public void onReceivedError(WebView view, int errorCode,
                                    String description, String failingUrl) {
            String data = "当前网络不通，请检查是否有网络信号！";
            view.loadUrl("javascript:document.body.innerHTML=\"" + data + "\"");
        }
    }

    /**
     * 获取广播数据
     *
     * @author jiqinlin
     */
    public class MyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String ss = intent.getStringExtra("info");
            Time time = new Time();
            time.setToNow();
            String str_time2 = time.format("%Y-%m-%d %H:%M:%S");
            //txtView.setText(ss + ":" + Integer.toString(nTest) + ":" + str_time2);
        }
    }

    private class HellowebViewClient extends WebChromeClient {
        @Override
        public boolean onJsAlert(WebView view, String url, String message,
                                 final JsResult result) {
            AlertDialog.Builder b2 = new AlertDialog.Builder(view.getContext())
                    .setTitle("提示：").setMessage(message)
                    .setPositiveButton("ok",
                            new AlertDialog.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog,
                                                    int which) {
                                    result.confirm();
                                }
                            });
            b2.setCancelable(false);
            b2.create();
            b2.show();
            return true;
        }
    }
}
