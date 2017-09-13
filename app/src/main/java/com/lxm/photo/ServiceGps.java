package com.lxm.photo;

import java.io.IOException;
import java.sql.Date;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.AndroidHttpTransport;
import org.xmlpull.v1.XmlPullParserException;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.BDNotifyListener;//�����õ�λ�����ѹ��ܣ���Ҫimport����
//���ʹ�õ���Χ�����ܣ���Ҫimport������
import com.baidu.location.BDGeofence;
import com.baidu.location.BDLocationStatusCodes;
import com.baidu.location.GeofenceClient;
import com.baidu.location.GeofenceClient.OnAddBDGeofencesResultListener;
import com.baidu.location.GeofenceClient.OnGeofenceTriggerListener;
import com.baidu.location.GeofenceClient.OnRemoveBDGeofencesResultListener;
import com.lxm.photo.MainActivity.MyLocationListener;

public class ServiceGps extends Service {
	private static final String TAG = "ServiceGps" ;
	public static final String ACTION = "com.lxm.photo.ServiceGps"; 
	public Boolean bStart = true;
	
	//��λ���õ��ı���
	public LocationClient mLocationClient = null;
	public BDLocationListener myListener = new MyLocationListener();
	private static final int UPDATE_TIME = 5000;
	private static Double mDLat = 0.0;	//��ǰ�ֻ��ľ���
	private static Double mDLon = 0.0;	//��ǰ�ֻ���γ��
	private static String mSAddrInfo  = "";	//��ǰ�ֻ��ĵ�ַ��Ϣ
	private static String m_sImei = "";	//�ֻ�IMEI��(��Ϊ��ǰ�û���)
	private static String ServiceLocation = "";
	
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void onCreate()
	{
		Log.v(TAG, "lxm ServiceGps onCreate");
		mLocationClient = new LocationClient(getApplicationContext());     //����LocationClient��
		mLocationClient.registerLocationListener( myListener );    //ע���������
		LocationClientOption option = new LocationClientOption();
		option.setOpenGps(true);        //�Ƿ��GPS
		option.setCoorType("bd09ll");       //���÷���ֵ���������͡�
        //option.setPriority(LocationClientOption.NetWorkFirst);  //���ö�λ���ȼ�
		option.setProdName("LocationDemo"); //���ò�Ʒ�����ơ�ǿ�ҽ�����ʹ���Զ���Ĳ�Ʒ�����ƣ����������Ժ�Ϊ���ṩ����Ч׼ȷ�Ķ�λ����
		option.setScanSpan(UPDATE_TIME);    //���ö�ʱ��λ��ʱ��������λ����
		option.setIsNeedAddress(true);
		mLocationClient.setLocOption(option);
		mLocationClient.start();
		super.onCreate(); 
	}
	

	@Override
	public void onStart(Intent intent, int startId)
	{	
		m_sImei = intent.getStringExtra("sImei");
		ServiceLocation = intent.getStringExtra("SL");
		//Log.v(TAG, ss);
		new Thread(new Runnable() {
    	    public void run() {
    	    	//String retValue = ftpUpload(mFTPServer,mFTPPort,mFTPUser,mFTPPassword,mFTPFileDir,mFilePath,mFileName);
    	    	while(bStart)
    	    	{
    	    		SoapObject rpc =new SoapObject("whservice", "SetJWD2");
    	    		rpc.addProperty("imei",m_sImei);
    	    		DecimalFormat decimalFormat = new DecimalFormat("#,##0.00000000");
    	    		if(decimalFormat.format(mDLat) == "0.00000000")
    	    			return;
    	    		rpc.addProperty("gps",decimalFormat.format(mDLat) + "," + decimalFormat.format(mDLon));
    	    		SimpleDateFormat formatter = new SimpleDateFormat("yyyy��MM��dd��    HH:mm:ss");
    	    		Date curDate = new Date(System.currentTimeMillis());//��ȡ��ǰʱ�� 
    	    		String sTime = formatter.format(curDate);
    	    		rpc.addProperty("datetime",sTime);
    	    		AndroidHttpTransport ht =new AndroidHttpTransport(ServiceLocation); 
    	    		ht.debug =true;
    	    		
    	    		SoapSerializationEnvelope envelope =new SoapSerializationEnvelope( 
    	    				SoapEnvelope.VER11);
    	    		
    	    		envelope.bodyOut = rpc; 
    	    		envelope.dotNet =true; 
    	    		envelope.setOutputSoapObject(rpc);
    	    		
    	    		try {
    	    			ht.call(null, envelope);
    	    		} catch (IOException e) {

    	    			e.printStackTrace();
    	    		} catch (XmlPullParserException e) {

    	    			e.printStackTrace();
    	    		}
    	    		
    	    		SoapObject result = (SoapObject) envelope.bodyIn;
    	    		String retVal = result.getProperty(0).toString();
    	    		Log.v(TAG, "gps: " + Double.toString(mDLat) + "--" + Double.toString(mDLon) + "--" + mSAddrInfo );
    	    		/*���͹㲥*/
    	    		Intent infoIntent = new Intent();
    	    		infoIntent.putExtra("info", Double.toString(mDLat) + "--" + Double.toString(mDLon) + "--" + mSAddrInfo);
    	    		infoIntent.setAction("ServiceGps");
    	    		sendBroadcast(infoIntent);
    	    		
    	    		try {
						Thread.sleep(5 * 60 * 1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
    	    	}
    	  }}).start();
		
        //super.onStart(intent, startId); 
	}
	
	@Override
	 public void onDestroy() {  
        Log.v(TAG, "lxm ServiceGps onDestroy");
        bStart = false;
        super.onDestroy();  
    }  
	
	public class MyLocationListener implements BDLocationListener {
		@Override
		public void onReceiveLocation(BDLocation location) {
			if (location == null)
		            return ;
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
			if (location.getLocType() == BDLocation.TypeGpsLocation){
				sb.append("\nspeed : ");
				sb.append(location.getSpeed());
				sb.append("\nsatellite : ");
				sb.append(location.getSatelliteNumber());
			} else if (location.getLocType() == BDLocation.TypeNetWorkLocation){
				sb.append("\naddr : ");
				sb.append(location.getAddrStr());
				mSAddrInfo = location.getAddrStr();
			}
		}
	}
}
