package com.lxm.photo;

import com.lxm.photo.R;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;

public class SubmitPhoto extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_submit_photo);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.submit_photo, menu);
		return true;
	}

}
