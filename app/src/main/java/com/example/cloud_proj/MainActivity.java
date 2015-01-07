package com.example.cloud_proj;

import java.io.*;
import java.net.*;
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
import android.os.Handler ;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

public class MainActivity extends Activity {
	
	WifiManager wifi;
	WifiReceiver wifiReceiver ;
	TextView test ;
	TextView classroom  ;
    Handler receivefromserver ;
	 
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
			Log.d("ap mac ==  " , wifi_list.get(0).BSSID ) ; // show detail
			Log.d("size ==  " , String.valueOf(wifi_list.size())) ;
			test = (TextView) findViewById(R.id.testing) ;
			
			for(int i = 0 ; i < wifi_list.size() ; i++ ){
				test.setText("");
				test.append(wifi_list.get(i).BSSID);
				test.append(String.valueOf(wifi_list.get(i).level));
				test.append("\n") ;
			}
			
			 // send data to server here
			
		    MyClientTask myClientTask = new MyClientTask(
		    	       "140.113.179.18", 5566 , wifi_list.get(0).BSSID + "\n" );
		    myClientTask.execute();
			setContentView(R.layout.activity_main) ;
            classroom = (TextView) findViewById(R.id.classroom) ;
            receivefromserver = new Handler(){
                @Override
                public void handleMessage(Message msg) {
                    super.handleMessage(msg);
                    if( msg.what == 1 ){
                        classroom.setText((String)msg.obj) ;
                    }else{
                        Log.d("handler" , "unmatch") ;
                    }
                }
            } ;
			unregisterReceiver(this);
		}

	}
	
	public class MyClientTask extends AsyncTask<Void, Void, Void> {
		  
		String dstAddress;
		String send_msg ;
		int dstPort;

		MyClientTask(String addr, int port , String msg){
			dstAddress = addr;
			dstPort = port;
            send_msg = msg ;
		}

		@Override
		protected Void doInBackground(Void... arg0) {
            Socket socket = new Socket();
            InetSocketAddress isa = new InetSocketAddress(dstAddress, dstPort);

            Log.d("in the thread" , " start thread +param " + send_msg ) ;
			try {
                socket.connect(isa, 10000);

                if (socket.isConnected()) Log.i("Chat", "Socket Connected");
                BufferedOutputStream out = new BufferedOutputStream(socket.getOutputStream());
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));


                String recv_msg = new String() ;
                String output = "wifistatus" ; // header


                //sending message
                out.write(output.concat(send_msg).getBytes() , 0, send_msg.length()+10);
                out.flush();
                Log.d("send" , output.concat(send_msg) + "=====had been send!") ;

                //receiving message
                recv_msg = in.readLine() ;
                Log.d("recv" , recv_msg + "===had been read!") ;

                // pass message to handler
                Message toHandle  = new Message() ;
                toHandle.what = 1 ;
                toHandle.obj = recv_msg ;

			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
			    e.printStackTrace();
			} catch (IOException e) {
			    // TODO Auto-generated catch block
			    e.printStackTrace();
			}finally{
			    if(socket != null){
				    try {
                        Log.d("finnaly" , "socket close") ;
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

