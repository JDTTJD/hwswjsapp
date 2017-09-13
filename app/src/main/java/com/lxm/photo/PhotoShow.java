package com.lxm.photo;

import it.sauronsoftware.ftp4j.FTPClient;
import it.sauronsoftware.ftp4j.FTPException;
import it.sauronsoftware.ftp4j.FTPIllegalReplyException;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.Locale;

import org.kobjects.base64.Base64;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.AndroidHttpTransport;
import org.xmlpull.v1.XmlPullParserException;

import com.lxm.photo.R;

import android.os.Bundle;
import android.os.Environment;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.CompressFormat;
import android.telephony.TelephonyManager;
import android.text.format.DateFormat;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;

public class PhotoShow extends Activity {
	
	private ImageView imageView;
	private EditText m_cEditText;
	private static String m_sImei = "";
	private Bitmap m_bitmap = null;
	private String m_sImagePath = "";
	private String m_sImageName = "";
	private String m_sGps = "";
	
	private static String m_sFtpUrl = "";	//ftpIP
	private static String m_sFtpPort = "";	//ftp端口
	private static String m_sFtpUser = "";	//ftp用户
	private static String m_sFtpPassword = "";	//ftp登录密码
	
	//WebService访问
	private static final String NAMESPACE ="http://tempuri.org/"; 
	private static String ServiceURL = "";
	
	private int m_nThreadFlag = 1;	//线程等待标志1不等待执行
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_photo_show);
		
		TelephonyManager tm = (TelephonyManager) this.getSystemService(TELEPHONY_SERVICE);
		//m_sImei = tm.getDeviceId();
		m_sImagePath = Environment.getExternalStorageDirectory().toString() + "/MyImage/";
		ServiceURL = getResources().getString(R.string.ServiceURL);
		
		//获取FTP信息
		/*m_nThreadFlag = 2;
		new Thread(new Runnable() {
    	    public void run() {
    	    	//String retValue = ftpUpload(mFTPServer,mFTPPort,mFTPUser,mFTPPassword,mFTPFileDir,mFilePath,mFileName);
    	    	GetFtpInfo();
    	    }
    	  }).start();*/
		
		imageView = (ImageView) this.findViewById(R.id.imageView1);
		m_cEditText = (EditText) this.findViewById(R.id.txtDesc);
		
		Intent it = getIntent();
		String ss = it.getStringExtra("bitmap");
		m_sGps = it.getStringExtra("gps");
		m_sImei = it.getStringExtra("imei");
		//ShowDlg(ss);
		
		BitmapFactory.Options options = new BitmapFactory.Options();
        options.inTempStorage = new byte[1024 * 1024 * 2];
        options.inSampleSize = 2;
        m_bitmap = BitmapFactory.decodeFile(ss,
                options);
		
		imageView.setImageBitmap(m_bitmap);
	}
	
	public void GetFtpInfo()
	{
		SoapObject rpc =new SoapObject(NAMESPACE, "PicturePath");
		//rpc.addProperty("PadCode",m_sImei);
		
		AndroidHttpTransport ht =new AndroidHttpTransport(ServiceURL); 
		ht.debug =true;
		
		SoapSerializationEnvelope envelope =new SoapSerializationEnvelope( 
				SoapEnvelope.VER11);
		
		envelope.bodyOut = rpc; 
		envelope.dotNet =true; 
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
		String sTemp = result.getProperty(0).toString();
		String[] sArray=sTemp.split("\\|\\|");
		m_sFtpUrl = sArray[0];
		m_sFtpPort = sArray[1];
		m_sFtpUser = sArray[2];
		m_sFtpPassword = sArray[3];
		//m_nThreadFlag = 1;
	}
	
	public void CancleBtnClick(View view)
	{
		finish();
	}
	
	public void OkBtnClick(View view)
	{
		/*while(m_nThreadFlag != 1)
		{
			//等待线程执行完毕才继续执行
		}*/
		//ShowDlg(m_sFtpUrl + ":" + m_sFtpPort);
		//检查SD是否可用
		String sdStatus = Environment.getExternalStorageState();
		if (!sdStatus.equals(Environment.MEDIA_MOUNTED)) { // 检测sd是否可用  
            ShowDlg("请插入SD卡后再使用本功能");
            return;  
        }  
		
		//图片的名称
		m_sImageName = m_sImei + new DateFormat().format("yyyyMMdd_hhmmss",Calendar.getInstance(Locale.CHINA)) + ".jpg";
		
		FileOutputStream b = null; 
		
		//将当前照片压缩存放到SD卡中
		//savePicToSdcard(m_bitmap,m_sImagePath,m_sImageName);
		//将照片上传并将信息存放到数据库中
		//m_nThreadFlag = 3;
		m_nThreadFlag = 0;
		new Thread(new Runnable() {
	    public void run() {
	    	savePicToSdcard(m_bitmap,m_sImagePath,m_sImageName);
	    	//UploadImage();
	    	//String retValue = ftpUpload(mFTPServer,mFTPPort,mFTPUser,mFTPPassword,mFTPFileDir,mFilePath,mFileName);
	    	//CallWebService();
	    }
	  }).start();
		
		while(m_nThreadFlag != 1)
		{
			
		}
		finish();
	}
	
	/** 
     * 获得指定文件的byte数组 
     */  
    public byte[] getBytes(String filePath){  
        byte[] buffer = null;  
        try {  
            File file = new File(filePath);  
            FileInputStream fis = new FileInputStream(file);  
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] b = new byte[1000];  
            int n;  
            while ((n = fis.read(b)) != -1) {  
                bos.write(b, 0, n);  
            }  
            fis.close();  
            bos.close();  
            buffer = bos.toByteArray();  
        } catch (FileNotFoundException e) {  
            e.printStackTrace();  
        } catch (IOException e) {  
            e.printStackTrace();  
        }  
        return buffer;  
    }
	
	//将流通过webservice传入数据库
	public void UploadImage(String filePath)
	{
		//ftpUpload(m_sFtpUrl,m_sFtpPort,m_sFtpUser,m_sFtpPassword,"",m_sImagePath,m_sImageName);
		
		//调用webservice将数据存放到数据库中
		//SoapObject rpc =new SoapObject(NAMESPACE, "PartolRecord");
		SoapObject rpc =new SoapObject("whservice", "UpLoadIMG");
		rpc.addProperty("imei",m_sImei);
		rpc.addProperty("gps",m_sGps);
		String uploadBuffer = new String(Base64.encode(getBytes(filePath))); 
		rpc.addProperty("base64String",uploadBuffer);
		rpc.addProperty("msg",m_cEditText.getText().toString());
		
		AndroidHttpTransport ht =new AndroidHttpTransport(ServiceURL); 
		ht.debug =true;
		
		SoapSerializationEnvelope envelope =new SoapSerializationEnvelope( 
				SoapEnvelope.VER11);
		
		envelope.bodyOut = rpc; 
		envelope.dotNet =true; 
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
		String sTemp = result.getProperty(0).toString();
		//MainActivity.mWebView.loadUrl("javascript: phonecallback(" + sTemp + ")");
		
		// 获取启动该Activity之前的Activity对应的Intent  
        Intent intent=this.getIntent();
        intent.putExtra("picPath", sTemp);
        this.setResult(RESULT_OK,intent);
        
		m_nThreadFlag = 1;
		//ShowDlg(sTemp);
		//m_nThreadFlag = m_nThreadFlag - 1;
	}
	
	public void UploadImage()
	{
		ftpUpload(m_sFtpUrl,m_sFtpPort,m_sFtpUser,m_sFtpPassword,"",m_sImagePath,m_sImageName);
		
		//调用webservice将数据存放到数据库中
		SoapObject rpc =new SoapObject(NAMESPACE, "PartolRecord");
		rpc.addProperty("GPS",m_sGps);
		rpc.addProperty("PDACode",m_sImei);
		rpc.addProperty("PictureName",m_sImageName);
		rpc.addProperty("Contents",m_cEditText.getText().toString());
		
		AndroidHttpTransport ht =new AndroidHttpTransport(ServiceURL); 
		ht.debug =true;
		
		SoapSerializationEnvelope envelope =new SoapSerializationEnvelope( 
				SoapEnvelope.VER11);
		
		envelope.bodyOut = rpc; 
		envelope.dotNet =true; 
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
		String sTemp = result.getProperty(0).toString();
		
		//m_nThreadFlag = m_nThreadFlag - 1;
	}
	
	/** 
	 * 通过ftp上传文件 
	 * @param url ftp服务器地址 如： 192.168.1.110 
	 * @param port 端口如 ： 21 
	 * @param username  登录名 
	 * @param password   密码 
	 * @param remotePath  上到ftp服务器的磁盘路径 
	 * @param fileNamePath  要上传的文件路径 
	 * @param fileName      要上传的文件名 
	 * @return 
	 */  
	public String ftpUpload(String url, String port, String username,String password, String remotePath, String fileNamePath,String fileName) 
	{
		String retVal = "";
		// 创建客户端 
		FTPClient client = new FTPClient();
		 try {  
	            // 不指定端口，则使用默认端口21  
	            client.connect(url, Integer.parseInt(port));  
	            // 用户登录  
	            client.login(username, password);  
	            File tempFile = new File(fileNamePath+fileName);
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
		//m_nThreadFlag = m_nThreadFlag - 1;
		return retVal;  
	}
	
	//将图片保存到指定目录
	public String savePicToSdcard(Bitmap bitmap, String path,  
            String fileName) {  
        String filePath = "";  
        if (bitmap == null) {  
            return filePath;  
        } else {  
  
            filePath=path+ fileName;  
            File destFile = new File(filePath);  
            FileOutputStream os = null;  
            try {  
                os = new java.io.FileOutputStream(destFile);  
                bitmap.compress(CompressFormat.JPEG, 10, os);
                
                UploadImage(filePath);
                
                os.flush(); 
                os.close();  
            } catch (IOException e) {
                filePath = "";  
            }  
        }  
        //ShowDlg(filePath);
        return filePath;  
    }    
	
	//弹出提示对话框
	public void ShowDlg(String sShow)
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("提示");
		builder.setMessage(sShow);
		builder.setPositiveButton("确定", null);
		AlertDialog alert = builder.create(); // create one
		alert.show();
	}
	
	public static Bitmap getBitmap(byte[] data){  
	      return BitmapFactory.decodeByteArray(data, 0, data.length);//从字节数组解码位图  
	}  
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.photo_show, menu);
		return true;
	}

}
