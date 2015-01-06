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
				test.clearComposingText();
				test.append(wifi_list.get(i).BSSID);
				test.append(String.valueOf(wifi_list.get(i).level));
				test.append("\n") ;
			}
			
			 // send data to server here
			
		    MyClientTask myClientTask = new MyClientTask(
		    	       "140.113.179.18",5577 , wifi_list.get(0).BSSID );
		    myClientTask.execute();
			setContentView(R.layout.activity_main) ;
            classroom = (TextView) findViewById(R.id.classroom) ;
            receivefromserver = new Handler(){
                @Override
                public void handleMessage(Message msg) {
                    super.handleMessage(msg);
                    if( msg.what == 1 ){
                        classroom.setText(msg.obj.toString()) ;
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
		String response = "";
		  
		MyClientTask(String addr, int port , String msg){
			dstAddress = addr;
			dstPort = port;
            send_msg = msg ;
		}

		@Override
		protected Void doInBackground(Void... arg0) {
            Socket socket = new Socket();
            InetSocketAddress isa = new InetSocketAddress(dstAddress, dstPort);
			Log.d("in the thread" , " start thread") ;
			try {
                socket.connect(isa, 10000);

                if (socket.isConnected()) Log.i("Chat", "Socket Connected");
                BufferedOutputStream out = new BufferedOutputStream(socket.getOutputStream());
//			    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(1024);
			    byte[] buffer = new byte[1024];
                String output = "wifistatus" ; // header
			    
			    int bytesRead;
			    InputStream inputStream = socket.getInputStream();

                //sending message
                out.write(output.concat(send_msg).getBytes() , 0, send_msg.length()+10);
                Log.e("send" , output + "=====had been read!") ;
//                byteArrayOutputStream.write(output.concat(send_msg).getBytes() , 0, send_msg.length()) ;

                //receiving message

                if((bytesRead = inputStream.read(buffer)) != -1 ) Log.e("recv" , "error") ;
                Log.e("recv" , String.valueOf(bytesRead) + " had been read!") ;

                // pass message to handler
                Message toHandle  = new Message() ;
                toHandle.what = 1 ;
                toHandle.obj = buffer.toString() ;
//                toHandle.setData(new Bundle().se );

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

