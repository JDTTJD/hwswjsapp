package com.lxm.photo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

import com.lxm.photo.R;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources.NotFoundException;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

public class UpdateManager 
{
	/* ������ */
	private static final int DOWNLOAD = 1;
	/* ���ؽ��� */
	private static final int DOWNLOAD_FINISH = 2;
	/* ���������XML��Ϣ */
	HashMap<String, String> mHashMap;
	/* ���ر���·�� */
	private String mSavePath;
	/* ��¼���������� */
	private int progress;
	/* �Ƿ�ȡ������ */
	private boolean cancelUpdate = false;

	private Context mContext;
	/* ���½����� */
	private ProgressBar mProgress;
	private Dialog mDownloadDialog;
	
	private boolean m_bThreadGetVision = false;	//��ȡ�汾��Ϣ�̣߳�true����ִ�У�false�ȴ��߳�ִ���������ִ��
	private static InputStream m_inStream = null;	//�ֻ�IMEI��(��Ϊ��ǰ�û���)
	
	private Handler mHandler = new Handler()
	{
		public void handleMessage(Message msg)
		{
			switch (msg.what)
			{
			// ��������
			case DOWNLOAD:
				// ���ý�����λ��
				mProgress.setProgress(progress);
				break;
			case DOWNLOAD_FINISH:
				// ��װ�ļ�
				installApk();
				break;
			default:
				break;
			}
		};
	};

	public UpdateManager(Context context)
	{
		//ShowDlg("1");
		this.mContext = context;
	}

	/**
	 * ����������
	 * @throws IOException 
	 * @throws NotFoundException 
	 * @throws MalformedURLException 
	 */
	public void checkUpdate() throws MalformedURLException, NotFoundException, IOException
	{
		if (isUpdate())
		{
			// ��ʾ��ʾ�Ի���
			showNoticeDialog();
		} else
		{
			//�Ѿ������°汾��ʾ
			//Toast.makeText(mContext, R.string.soft_update_no, Toast.LENGTH_LONG).show();
		}
	}
	
	/**
	 * ����URL�õ�������
	 *
	 * @param urlStr
	 * @return
	 * @throws MalformedURLException
	 * @throws IOException
	 */
	public InputStream getInputStreamFromUrl(String urlStr)
	        throws MalformedURLException, IOException {
	    URL sUrl = new URL(urlStr);
	    HttpURLConnection urlConn = (HttpURLConnection) sUrl.openConnection();
	    InputStream inputStream = urlConn.getInputStream();
	    m_bThreadGetVision = false;
	    return inputStream;
	}
	
	//������ʾ�Ի���
		public void ShowDlg(String sShow)
		{
			AlertDialog.Builder builder = new AlertDialog.Builder(this.mContext);
			builder.setTitle("��ʾ");
			builder.setMessage(sShow);
			builder.setPositiveButton("ȷ��", null);
			AlertDialog alert = builder.create(); // create one
			alert.show();
		}	
		
	/**
	 * �������Ƿ��и��°汾
	 * 
	 * @return
	 * @throws IOException 
	 * @throws MalformedURLException 
	 */
	private boolean isUpdate() throws MalformedURLException, IOException
	{
		// ��ȡ��ǰ����汾
		int versionCode = getVersionCode(mContext);
		
		// ��version.xml�ŵ������ϣ�Ȼ���ȡ�ļ���Ϣ
		//InputStream inStream = ParseXmlService.class.getClassLoader().getResourceAsStream("http://192.168.1.98/heihe/version.xml");
		//InputStream inStream = getInputStreamFromUrl("http://127.0.0.1/heihe/version.xml");
		m_bThreadGetVision = true;
		new Thread(new Runnable() {
    	    public void run() {
    	    	try {
    	    		m_inStream = getInputStreamFromUrl(mContext.getResources().getString(R.string.soft_update_url));
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
    	  }).start();
		
		while(m_bThreadGetVision)
		{
			//�ȴ��߳�ִ����������ִ��
		}
		
		m_bThreadGetVision = true;
		new Thread(new Runnable() {
    	    public void run() {
    	    	// ����XML�ļ��� ����XML�ļ��Ƚ�С�����ʹ��DOM��ʽ���н���
    			ParseXmlService service = new ParseXmlService();
    			try
    			{
    				mHashMap = service.parseXml(m_inStream);
    				m_bThreadGetVision = false;
    			} catch (Exception e)
    			{
    				e.printStackTrace();
    			}
    	    }
    	  }).start();
		
		while(m_bThreadGetVision)
		{
			//�ȴ��߳�ִ����������ִ��
		}
		
		/*ParseXmlService service = new ParseXmlService();
		try
		{
			mHashMap = service.parseXml(m_inStream);
		} catch (Exception e)
		{
			e.printStackTrace();
		}*/
		
		if (null != mHashMap)
		{
			int serviceCode = Integer.valueOf(mHashMap.get("version"));
			// �汾�ж�
			if (serviceCode > versionCode)
			{
				return true;
			}
		}
		return false;
	}
	
	
	/**
	 * ��ȡ����汾��
	 * 
	 * @param context
	 * @return
	 */
	private int getVersionCode(Context context)
	{
		int versionCode = 0;
		try
		{
			// ��ȡ����汾�ţ���ӦAndroidManifest.xml��android:versionCode
			versionCode = context.getPackageManager().getPackageInfo("com.lxm.photo", 0).versionCode;
		} catch (NameNotFoundException e)
		{
			e.printStackTrace();
		}
		return versionCode;
	}

	/**
	 * ��ʾ������¶Ի���
	 */
	private void showNoticeDialog()
	{
		// ����Ի���
		AlertDialog.Builder builder = new Builder(mContext);
		builder.setTitle(R.string.soft_update_title);
		builder.setMessage(R.string.soft_update_info);
		// ����
		builder.setPositiveButton(R.string.soft_update_updatebtn, new OnClickListener()
		{
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				dialog.dismiss();
				// ��ʾ���ضԻ���
				showDownloadDialog();
			}
		});
		// �Ժ����
		builder.setNegativeButton(R.string.soft_update_later, new OnClickListener()
		{
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				dialog.dismiss();
			}
		});
		Dialog noticeDialog = builder.create();
		noticeDialog.show();
	}

	/**
	 * ��ʾ������ضԻ���
	 */
	private void showDownloadDialog()
	{
		// ����������ضԻ���
		AlertDialog.Builder builder = new Builder(mContext);
		builder.setTitle(R.string.soft_updating);
		// �����ضԻ������ӽ�����
		final LayoutInflater inflater = LayoutInflater.from(mContext);
		View v = inflater.inflate(R.layout.softupdate_progress, null);
		mProgress = (ProgressBar) v.findViewById(R.id.update_progress);
		builder.setView(v);
		// ȡ������
		builder.setNegativeButton(R.string.soft_update_cancel, new OnClickListener()
		{
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				dialog.dismiss();
				// ����ȡ��״̬
				cancelUpdate = true;
			}
		});
		mDownloadDialog = builder.create();
		mDownloadDialog.show();
		// �����ļ�
		downloadApk();
	}

	/**
	 * ����apk�ļ�
	 */
	private void downloadApk()
	{
		// �������߳��������
		new downloadApkThread().start();
	}

	/**
	 * �����ļ��߳�
	 * 
	 * @author coolszy
	 *@date 2012-4-26
	 *@blog http://blog.92coding.com
	 */
	private class downloadApkThread extends Thread
	{
		@Override
		public void run()
		{
			try
			{
				// �ж�SD���Ƿ���ڣ������Ƿ���ж�дȨ��
				if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
				{
					// ��ô洢����·��
					String sdpath = Environment.getExternalStorageDirectory() + "/";
					mSavePath = sdpath + "download";
					URL url = new URL(mHashMap.get("url"));
					// ��������
					HttpURLConnection conn = (HttpURLConnection) url.openConnection();
					conn.connect();
					// ��ȡ�ļ���С
					int length = conn.getContentLength();
					// ����������
					InputStream is = conn.getInputStream();

					File file = new File(mSavePath);
					// �ж��ļ�Ŀ¼�Ƿ����
					if (!file.exists())
					{
						file.mkdir();
					}
					File apkFile = new File(mSavePath, mHashMap.get("name"));
					FileOutputStream fos = new FileOutputStream(apkFile);
					int count = 0;
					// ����
					byte buf[] = new byte[1024];
					// д�뵽�ļ���
					do
					{
						int numread = is.read(buf);
						count += numread;
						// ���������λ��
						progress = (int) (((float) count / length) * 100);
						// ���½���
						mHandler.sendEmptyMessage(DOWNLOAD);
						if (numread <= 0)
						{
							// �������
							mHandler.sendEmptyMessage(DOWNLOAD_FINISH);
							break;
						}
						// д���ļ�
						fos.write(buf, 0, numread);
					} while (!cancelUpdate);// ���ȡ����ֹͣ����.
					fos.close();
					is.close();
				}
			} catch (MalformedURLException e)
			{
				e.printStackTrace();
			} catch (IOException e)
			{
				e.printStackTrace();
			}
			// ȡ�����ضԻ�����ʾ
			mDownloadDialog.dismiss();
		}
	};

	/**
	 * ��װAPK�ļ�
	 */
	private void installApk()
	{
		File apkfile = new File(mSavePath, mHashMap.get("name"));
		if (!apkfile.exists())
		{
			return;
		}
		// ͨ��Intent��װAPK�ļ�
		Intent i = new Intent(Intent.ACTION_VIEW);
		i.setDataAndType(Uri.parse("file://" + apkfile.toString()), "application/vnd.android.package-archive");
		mContext.startActivity(i);
	}
}
