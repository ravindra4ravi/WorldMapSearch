package com.tpg.worldmapsearch.ui;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.tpg.worldmapsearch.bean.Place;
import com.tpg.worldmapsearch.util.Constants;

public class PlaceDetailActivity  extends Activity implements OnClickListener{
	private ImageView imgProfile;
	private Button btnMap,btnDirection; 
	private Place place = null;
	private ProgressDialog pd;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	// TODO Auto-generated method stub
    	super.onCreate(savedInstanceState);
    	setContentView(R.layout.activity_place_details);
    	if(android.os.Build.VERSION.SDK_INT >= 11){
			getActionBar().setBackgroundDrawable(
					new ColorDrawable(Color.parseColor("#0377d3")));
		}
    	if(place == null)
    	    place = (Place) getIntent().getSerializableExtra(Constants.PLACE_DETAILS);
    	TextView txtTitle = (TextView)findViewById(R.id.txtTitle);
    	TextView txtAdd = (TextView)findViewById(R.id.txtAddress);
    	btnMap  = (Button)findViewById(R.id.btnmapview);
    	btnDirection  = (Button)findViewById(R.id.btnRoute);
    	btnMap.setOnClickListener(this);
    	btnDirection.setOnClickListener(this);
    	
    	txtTitle.setText(""+place.name);
    	txtAdd.setText(place.vicinity);
    	imgProfile=(ImageView) findViewById(R.id.imgPlaceHolder);
		new DownloadImagesTask().execute(place.icon);
		pd = new ProgressDialog(this);
    }
    @Override
    public void onBackPressed() {
    	// TODO Auto-generated method stub
    	pd.setMessage("Please wait..");
		pd.show();
		Intent intent =new Intent();
		intent.putExtra(Constants.PLACE_DETAILS, place);
		intent.putExtra(Constants.CLICK_ON_DIRECTION_BUTTON, false);
		setResult(RESULT_OK, intent);
		finish();
    	super.onBackPressed();
    }
    class DownloadImagesTask extends AsyncTask<String, Void, Bitmap> {

		@Override
		protected Bitmap doInBackground(String... urls) {
		    return download_Image(urls[0]);
		}

		@Override
		protected void onPostExecute(Bitmap result) {
			imgProfile.setImageBitmap(result);              // how do I pass a reference to mChart here ?
		}


		private Bitmap download_Image(String url) {
		    //---------------------------------------------------
		    Bitmap bm = null;
		    try {
		        URL aURL = new URL(url);
		        URLConnection conn = aURL.openConnection();
		        conn.connect();
		        InputStream is = conn.getInputStream();
		        BufferedInputStream bis = new BufferedInputStream(is);
		        bm = BitmapFactory.decodeStream(bis);
		        bis.close();
		        is.close();
		    } catch (IOException e) {
		        Log.e("Hub","Error getting the image from server : " + e.getMessage().toString());
		    } 
		    return bm;
		    //---------------------------------------------------
		}


		}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if(v == btnMap){
			
			finish();
		}else if(v == btnDirection){
			pd.setMessage("Please wait..");
			pd.show();
			Intent intent =new Intent();
			intent.putExtra(Constants.PLACE_DETAILS, place);
			intent.putExtra(Constants.CLICK_ON_DIRECTION_BUTTON, true);
			setResult(RESULT_OK, intent);
			finish();
		}
	}
@Override
protected void onPause() {
	// TODO Auto-generated method stub
	pd.cancel();
	super.onPause();
}
}
