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
	private static String m_sFtpPort = "";	//ftp�˿�
	private static String m_sFtpUser = "";	//ftp�û�
	private static String m_sFtpPassword = "";	//ftp��¼����
	
	//WebService����
	private static final String NAMESPACE ="http://tempuri.org/"; 
	private static String ServiceURL = "";
	
	private int m_nThreadFlag = 1;	//�̵߳ȴ���־1���ȴ�ִ��
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_photo_show);
		
		TelephonyManager tm = (TelephonyManager) this.getSystemService(TELEPHONY_SERVICE);
		//m_sImei = tm.getDeviceId();
		m_sImagePath = Environment.getExternalStorageDirectory().toString() + "/MyImage/";
		ServiceURL = getResources().getString(R.string.ServiceURL);
		
		//��ȡFTP��Ϣ
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
			//�ȴ��߳�ִ����ϲż���ִ��
		}*/
		//ShowDlg(m_sFtpUrl + ":" + m_sFtpPort);
		//���SD�Ƿ����
		String sdStatus = Environment.getExternalStorageState();
		if (!sdStatus.equals(Environment.MEDIA_MOUNTED)) { // ���sd�Ƿ����  
            ShowDlg("�����SD������ʹ�ñ�����");
            return;  
        }  
		
		//ͼƬ������
		m_sImageName = m_sImei + new DateFormat().format("yyyyMMdd_hhmmss",Calendar.getInstance(Locale.CHINA)) + ".jpg";
		
		FileOutputStream b = null; 
		
		//����ǰ��Ƭѹ����ŵ�SD����
		//savePicToSdcard(m_bitmap,m_sImagePath,m_sImageName);
		//����Ƭ�ϴ�������Ϣ��ŵ����ݿ���
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
     * ���ָ���ļ���byte���� 
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
	
	//����ͨ��webservice�������ݿ�
	public void UploadImage(String filePath)
	{
		//ftpUpload(m_sFtpUrl,m_sFtpPort,m_sFtpUser,m_sFtpPassword,"",m_sImagePath,m_sImageName);
		
		//����webservice�����ݴ�ŵ����ݿ���
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
		
		// ��ȡ������Activity֮ǰ��Activity��Ӧ��Intent  
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
		
		//����webservice�����ݴ�ŵ����ݿ���
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
	 * ͨ��ftp�ϴ��ļ� 
	 * @param url ftp��������ַ �磺 192.168.1.110 
	 * @param port �˿��� �� 21 
	 * @param username  ��¼�� 
	 * @param password   ���� 
	 * @param remotePath  �ϵ�ftp�������Ĵ���·�� 
	 * @param fileNamePath  Ҫ�ϴ����ļ�·�� 
	 * @param fileName      Ҫ�ϴ����ļ��� 
	 * @return 
	 */  
	public String ftpUpload(String url, String port, String username,String password, String remotePath, String fileNamePath,String fileName) 
	{
		String retVal = "";
		// �����ͻ��� 
		FTPClient client = new FTPClient();
		 try {  
	            // ��ָ���˿ڣ���ʹ��Ĭ�϶˿�21  
	            client.connect(url, Integer.parseInt(port));  
	            // �û���¼  
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
	
	//��ͼƬ���浽ָ��Ŀ¼
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
	
	//������ʾ�Ի���
	public void ShowDlg(String sShow)
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("��ʾ");
		builder.setMessage(sShow);
		builder.setPositiveButton("ȷ��", null);
		AlertDialog alert = builder.create(); // create one
		alert.show();
	}
	
	public static Bitmap getBitmap(byte[] data){  
	      return BitmapFactory.decodeByteArray(data, 0, data.length);//���ֽ��������λͼ  
	}  
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.photo_show, menu);
		return true;
	}

}
