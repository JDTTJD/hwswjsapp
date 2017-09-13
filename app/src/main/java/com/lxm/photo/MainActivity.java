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

//���ʹ�õ���Χ�����ܣ���Ҫimport������

public class MainActivity extends Activity {
    private ImageView imageView;
    private Button button;
    private TextView txtView;
    private WebView mWebView; //��ҳ��ʾ�ؼ�
    private final int CAMREA_RESQUSET = 1;
    private final int PHOTOSHOW_RESQUEST = 2;
    private Uri imageUri; //ͼƬ·��
    private String filename; //ͼƬ����
    //ϵͳ�����Ƭ����ʱ��Ƭ·��
    private String tempPicPath = Environment.getExternalStorageDirectory().toString() + "/MyImage/temp.jpg";

    //��λ���õ��ı���
    public LocationClient mLocationClient = null;
    public BDLocationListener myListener = new MyLocationListener();
    private static final int UPDATE_TIME = 5000;
    private static Double mDLat = 0.0;    //��ǰ�ֻ��ľ���
    private static Double mDLon = 0.0;    //��ǰ�ֻ���γ��
    private static String mSAddrInfo = "";    //��ǰ�ֻ��ĵ�ַ��Ϣ

    //�ϴ��ļ�������
    private static String mFileName = "";
    //Ҫ�ϴ��ı����ļ�·��(�������ļ���)
    private static String mFilePath = "";
    //FTP��������IP
    private static String mFTPServer = "192.168.0.100";
    //FTP�������˿�
    private static String mFTPPort = "8888";
    //FTP��������¼��
    private static String mFTPUser = "ftpuser";
    //FTP��������¼����
    private static String mFTPPassword = "ftpuser";
    //FTP������Ҫ��ŵ�Ŀ¼
    private static String mFTPFileDir = "";


    private static final String NAMESPACE = "http://tempuri.org/";
    private static String m_sImei = "";    //�ֻ�IMEI��(��Ϊ��ǰ�û���)
    // WebService��ַ
    private static String URL = "http://10.0.2.2/heihe/WebService/Station.asmx?wsdl";
    private static String ServiceURL = "";
    private static String ServiceLocation = "";
    private static final String METHOD_NAME = "Test";
    private static String SOAP_ACTION = "http://WebXml.com.cn/getWeatherbyCityName";
    private static String mTest = "";

    private Menu m_cCMenu = null;
    private int m_nThreadGetState = 1;    //��ȡ��ǰ״̬�̵߳ȴ���־1���ȴ�ִ��
    private Boolean m_bRPartol = false;    //�Ƿ�ʼ�ٴ�Ѳ��
    private Boolean m_bSP = true;    //Ѳ�鰴ť�Ƿ����
    private Boolean m_bEP = false;    //����Ѳ�鰴ť�Ƿ����
    private Boolean m_isThreadLocation = true;    //��¼��γ���߳�����״̬
    private Handler mHandler = new Handler(); //����JS����JAVA����
    private long m_exitTime = 0;
    private int m_nNoteLocationInterval = 1;
    private Boolean m_bThreadLocateionGetState = true;    //��ȡ��¼��γ�ȼ���߳�״̬��trueִ�У�false�ȴ�

    private MyReceiver m_receiver = null;        //��ȡ�㲥������
    private int nTest = 1;
    //��׿��Ϣ����
    private final Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    ShowDlg(mTest);
                    break;
                case 2:
                    ShowDlg("��ʼ����");
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

        //WebView����
        mWebView = (WebView) findViewById(R.id.webView1);
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setSavePassword(false);
        mWebView.getSettings().setSaveFormData(false);
        //mWebView.getSettings().setPluginState(WebSettings.PluginState.ON);
        // ���� DOM storage API ����  
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

        mLocationClient = new LocationClient(getApplicationContext());     //����LocationClient��
        mLocationClient.registerLocationListener(myListener);    //ע���������
        LocationClientOption option = new LocationClientOption();
        option.setOpenGps(true);        //�Ƿ��GPS
        option.setCoorType("bd09ll");       //���÷���ֵ���������͡�
        //option.setPriority(LocationClientOption.NetWorkFirst);  //���ö�λ���ȼ�
        option.setProdName("LocationDemo"); //���ò�Ʒ�����ơ�ǿ�ҽ�����ʹ���Զ���Ĳ�Ʒ�����ƣ����������Ժ�Ϊ���ṩ����Ч׼ȷ�Ķ�λ����
        option.setScanSpan(UPDATE_TIME);    //���ö�ʱ��λ��ʱ��������λ����
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

        //ע��㲥������
        m_receiver = new MyReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction("ServiceGps");
        this.registerReceiver(m_receiver, filter);
    }

    public void LogWrite(String info) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy��MM��dd�� HH:mm:ss ");
        Date curDate = new Date(System.currentTimeMillis());//��ȡ��ǰʱ��
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
            } //׷��ģʽ����д
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
    	    	// ����������
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

                Toast.makeText(getApplicationContext(), "�ٰ�һ���˳�����",
                        Toast.LENGTH_SHORT).show();
                m_exitTime = System.currentTimeMillis();
            } else {
                m_isThreadLocation = false;
                stopService(new Intent(ServiceGps.ACTION));
                unregisterReceiver(m_receiver);
                //ɾ��COOKIES
                CookieSyncManager.createInstance(this);
                CookieManager.getInstance().removeAllCookie();
                finish();
                System.exit(0);
            }
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    /*����menu*/
    public boolean onCreateOptionsMenu(Menu menu) {
        //Ϊmenu�������
        m_cCMenu = menu;
        m_nThreadGetState = m_nThreadGetState + 1;
        new Thread(new Runnable() {
            public void run() {
                //String retValue = ftpUpload(mFTPServer,mFTPPort,mFTPUser,mFTPPassword,mFTPFileDir,mFilePath,mFileName);
                CallCreateMenu();
            }
        }).start();
        while (m_nThreadGetState != 1) {
            //�ȴ��߳�ִ����ϲż���ִ��
        }
        return true;
    }

    public boolean onPrepareOptionsMenu(Menu menu) {
        //����menu������
        m_cCMenu = menu;
        m_nThreadGetState = m_nThreadGetState + 1;
        new Thread(new Runnable() {
            public void run() {
                //String retValue = ftpUpload(mFTPServer,mFTPPort,mFTPUser,mFTPPassword,mFTPFileDir,mFilePath,mFileName);
                CallCreateMenu();
            }
        }).start();
        while (m_nThreadGetState != 1) {
            //�ȴ��߳�ִ����ϲż���ִ��
        }
        return true;
    }

    //��ȡ��¼��ǰ��γ�ȵ�ʱ����
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

    //��¼��ǰ��γ�ȵ����ݿ�
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

    /*����menu���¼� */
    public boolean onOptionsItemSelected(MenuItem item) { //�õ���ǰѡ�е�MenuItem��ID,
        int item_id = item.getItemId();
        switch (item_id) {
            case 0:
                onClickStartPartol();    //��ʼѲ��
                break;
            case 1:
                onClickMenuPhoto();    //�����ϴ�
                break;
            case 2:
                onClickStopPartol();    //����Ѳ��
                break;
        }
        return true;
    }

    //����Ѳ��
    public void onClickStopPartol() {
        //�˵����ÿ���
    	/*MenuItem itemStart = m_cCMenu.getItem(0);
    	MenuItem itemStop = m_cCMenu.getItem(2);
    	itemStart.setEnabled(true);
    	itemStop.setEnabled(false);*/
        m_bSP = true;
        m_bEP = false;

        //ˢ�µ�ǰҳ��
        mWebView.reload();

        new Thread(new Runnable() {
            public void run() {
                //String retValue = ftpUpload(mFTPServer,mFTPPort,mFTPUser,mFTPPassword,mFTPFileDir,mFilePath,mFileName);
                CallStopPartol();
            }
        }).start();
    }

    //����Ѳ��
    public void onClickStartPartol() {
        //�˵����ÿ���
    	/*MenuItem itemStart = m_cCMenu.getItem(0);
    	MenuItem itemStop = m_cCMenu.getItem(2);
    	itemStart.setEnabled(false);
    	itemStop.setEnabled(true);*/
        m_bSP = false;
        m_bEP = true;

        //ˢ�µ�ǰҳ��
        mWebView.reload();

        new Thread(new Runnable() {
            public void run() {
                //String retValue = ftpUpload(mFTPServer,mFTPPort,mFTPUser,mFTPPassword,mFTPFileDir,mFilePath,mFileName);
                CallStartPartol();
            }
        }).start();
    }

    //��������ϴ��˵�
    public void onClickMenuPhoto() {
        {
            // TODO Auto-generated method stub
            File file = new File(Environment.getExternalStorageDirectory().toString() + "/MyImage/");
            if (!file.exists()) {
                boolean bR = file.mkdirs();// �����ļ���
            }
            File outputImage = new File(tempPicPath);
            //ɾ��ϵͳ������µ���ʱ��Ƭ
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

            //��File����ת��ΪUri�������������
            imageUri = Uri.fromFile(outputImage);
            Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
            intent.addCategory("android.intent.category.DEFAULT");
            intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri); //ָ��ͼƬ�����ַ
            startActivityForResult(intent, CAMREA_RESQUSET);

        }
    }

    //������ʾ�Ի���
    public void ShowDlg(String sShow) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("��ʾ");
        builder.setMessage(sShow);
        builder.setPositiveButton("ȷ��", null);
        AlertDialog alert = builder.create(); // create one
        alert.show();
    }

    @Override
    public void onConfigurationChanged(Configuration config) {
        super.onConfigurationChanged(config);
    }

    //��ͼƬ���浽ָ��Ŀ¼
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
        //ʵ�����ֽ����������
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 0, baos);//ѹ��λͼ
        return baos.toByteArray();//���������ֽ�����
    }

    //ʹ����ͼ�Ļش�ֵ���ж���Ƭ�Ƿ����������
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
            //ɾ��ϵͳ������µ���ʱ��Ƭ
            if (tF.exists()) {
                //tF.delete();
                //ͼƬ������
                mFileName = new DateFormat().format("yyyyMMdd_hhmmss", Calendar.getInstance(Locale.CHINA)) + ".jpg";
                mFilePath = Environment.getExternalStorageDirectory().toString() + "/MyImage/";
                String fileFullPath = savePicToSdcard(bitmap, mFilePath, mFileName);

            }

            //��ת�µ�ACTIVITY
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
            sb.append("\nʡ�� : ");
            sb.append(location.getProvince());
            sb.append("\n���� : ");
            sb.append(location.getCity());
            sb.append("\n���� : ");
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

    //����WebService
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
        //ShowDlg("�տ�ʼ");

        // ��ȡ���صĽ��
        //String result = object.getProperty(0).toString();

        return "";
    }

    /**
     * ͨ��ftp�ϴ��ļ�
     *
     * @param url          ftp��������ַ �磺 192.168.1.110
     * @param port         �˿��� �� 21
     * @param username     ��¼��
     * @param password     ����
     * @param remotePath   �ϵ�ftp�������Ĵ���·��
     * @param fileNamePath Ҫ�ϴ����ļ�·��
     * @param fileName     Ҫ�ϴ����ļ���
     * @return
     */
    public String ftpUpload(String url, String port, String username, String password, String remotePath, String fileNamePath, String fileName) {
        String retVal = "";
        // �����ͻ���
        FTPClient client = new FTPClient();
        try {
            // ��ָ���˿ڣ���ʹ��Ĭ�϶˿�21
            client.connect(url, Integer.parseInt(port));
            // �û���¼
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
        // ��WebView�ж�����Ĭ�����������ʾҳ��
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }

        @Override
        public void onPageStarted(WebView view,
                                  String url,
                                  Bitmap favicon) {
            //ShowDlg("��ʼ����");
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            //ShowDlg("�������");
        }

        @Override
        public void onReceivedError(WebView view, int errorCode,
                                    String description, String failingUrl) {
            String data = "��ǰ���粻ͨ�������Ƿ��������źţ�";
            view.loadUrl("javascript:document.body.innerHTML=\"" + data + "\"");
        }
    }

    /**
     * ��ȡ�㲥����
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
                    .setTitle("��ʾ��").setMessage(message)
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
