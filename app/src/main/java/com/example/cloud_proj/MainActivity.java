package com.example.cloud_proj;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.List;

import android.R.integer;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

public class MainActivity extends Activity {
	
	 WifiManager wifi;
	 WifiReceiver wifiReceiver ;
	 TextView test ;
	 TextView classroom  ;
	 
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.splash_sreen);
		wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		
	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		
	    wifiReceiver = new WifiReceiver();  
	    registerReceiver(wifiReceiver, new IntentFilter( WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));  
		wifi.startScan() ;
		
		
	}
	
	
	public class WifiReceiver extends BroadcastReceiver{

		@Override
		public void onReceive(Context c, Intent intent) {
			// TODO Auto-generated method stub
			List list ;
			integer count ;
			Log.d("scan " , "complete" ) ;
			List<ScanResult> wifi_list =  wifi.getScanResults() ;
			Log.d("SSID ==  " , wifi_list.get(0).BSSID ) ; // show detail
			Log.d("size ==  " , String.valueOf(wifi_list.size())) ;
			test = (TextView) findViewById(R.id.testing) ;
			
			for(int i = 0 ; i < wifi_list.size() ; i++ ){
				test.clearComposingText();
				test.append(wifi_list.get(i).BSSID);
				test.append(String.valueOf(wifi_list.get(i).level));
				test.append("\n") ;
			}
			
			 // send data to server here
			
		    MyClientTask myClientTask = new MyClientTask(
		    	       "140.113.179.18",5566 , wifi_list.get(0).BSSID );
		    myClientTask.execute();
			setContentView(R.layout.activity_main) ;
			unregisterReceiver(this);
			
		}

	}
	
	public class MyClientTask extends AsyncTask<Void, Void, Void> {
		  
		String dstAddress;
		String send_msg ;
		int dstPort;
		String response = "";
		  
		MyClientTask(String addr, int port , String msg){
			dstAddress = addr;
			dstPort = port;
			send_msg = msg ;
		}

		@Override
		protected Void doInBackground(Void... arg0) {
		   
			Socket socket = null;
			   
			try {
				socket = new Socket(dstAddress, dstPort);
			    
			    ByteArrayOutputStream byteArrayOutputStream = 
			                  new ByteArrayOutputStream(1024);
			    byte[] buffer = new byte[1024];
			    
			    int bytesRead;
			    InputStream inputStream = socket.getInputStream();

			    classroom = (TextView) findViewById(R.id.classroom) ;
			    
			    
			    /*
			     * notice:
			     * inputStream.read() will block if no data return
			     */
/*			    
			    while ((bytesRead = inputStream.read(buffer)) != -1){
			    	byteArrayOutputStream.write(buffer, 0, bytesRead);
			        response += byteArrayOutputStream.toString("UTF-8");
			    }
*/	
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
			    e.printStackTrace();
			    response = "UnknownHostException: " + e.toString();
			} catch (IOException e) {
			    // TODO Auto-generated catch block
			    e.printStackTrace();
			    response = "IOException: " + e.toString();
			}finally{
			    if(socket != null){
				    try {
				    	socket.close();
				    } catch (IOException e) {
				      // TODO Auto-generated catch block
				      e.printStackTrace();
				    }
			    }
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
//			textResponse.setText(response);
			super.onPostExecute(result);
		}
		  
	}
	
	
}

