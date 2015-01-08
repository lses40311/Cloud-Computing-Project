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
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;

public class MainActivity extends Activity {
	
	WifiManager wifi;
	WifiReceiver wifiReceiver ;
	TextView score ;
	TextView classroom  ;
    TextView teacher ;
    TextView course ;
    TextView chatroom ;
    EditText send_msg ;
    Button refresh , send ;
    RatingBar bar ;
    Handler receivefromserver ;
    Socket socket ;
    BufferedOutputStream out ;
    BufferedReader in ;

	 
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.splash_sreen);
		wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);

        socket = new Socket();
        connect_thread connect_toserver = new connect_thread( "140.113.179.18" , 5566 , socket );
        connect_toserver.execute() ;

	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();

	    wifiReceiver = new WifiReceiver();  
	    registerReceiver(wifiReceiver, new IntentFilter( WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));

		wifi.startScan() ;
	}

    @Override
    protected void onResume() {
        super.onResume();
        /*******************
         * what = 1 : course update
         * what = 2 : send WS
         * what = 3 : refresh score
         * what = 4 : refresh chatroom // not used
         *
         *
        *******************/

        receivefromserver = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if( msg.what == 1 ){
                    String[] x = ((String)msg.obj).split(",");
                    classroom.setText(x[0]) ;
                    course.setText(x[1]);
                    teacher.setText(x[2]);

                }else if(msg.what == 2){
                    try{
                        // connect and send data to server here
                        out = new BufferedOutputStream(socket.getOutputStream());


                        //sending message WIFI STATUS
                        out.write(("WS"+((List<ScanResult>)msg.obj).get(0).BSSID+"\n").getBytes() , 0, ((List<ScanResult>)msg.obj).get(0).BSSID.length()+3);
                        out.flush();
                        Log.d("send" , "WS"+((List<ScanResult>)msg.obj).get(0).BSSID+"\n" + "=====had been send!") ;

                        //handle recv msg
//                        MyClientTask myClientTask = new MyClientTask( in , 1 );
//                        myClientTask.execute();

                    }catch (UnknownHostException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    //get into next screen
                    setContentView(R.layout.activity_main) ;
                    classroom = (TextView) findViewById(R.id.classroom) ;
                    teacher = (TextView) findViewById(R.id.teacher) ;
                    course = (TextView) findViewById(R.id.crouse) ;
                    refresh = (Button) findViewById(R.id.button) ;
                    score = (TextView) findViewById(R.id.score) ;
                    bar = (RatingBar) findViewById(R.id.ratingBar) ;
                    send_msg = (EditText) findViewById(R.id.sendmsg) ;
                    chatroom = (TextView) findViewById(R.id.chatroom) ;
                    send = (Button) findViewById(R.id.send_btn) ;

                    send.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
//                            MyClientTask chat = new MyClientTask( in , 4 );
//                            chat.execute();
                            try {
                                String x = send_msg.getText().toString() ;
                                send_msg.setText("");
                                out.write(("CH"+ x + "\n").getBytes());
                                out.flush();
                                Log.d("send", x + "===had been send!");
                            } catch (IOException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                        }
                    });

                    bar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
                        @Override
                        public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
//                            MyClientTask rate = new MyClientTask( in , 3 );
//                            rate.execute();
                            try {
                                out.write(("RT"+ String.valueOf(rating) + "\n").getBytes(), 0, 6);
                                out.flush();
                                Log.d("send", String.valueOf(rating) + "===had been send!");
                            } catch (IOException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                        }
                    });
                    refresh.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            try {
                                out.write("UR\n".getBytes(), 0, 3);
                                out.flush();
//                                MyClientTask UR_recv = new MyClientTask(in, 3);
//                                UR_recv.execute();
                                Log.d("send", "UR===had been send!");
                            } catch (IOException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }

                        }
                    });
                }else if(msg.what == 3){
                    score.setText((String) msg.obj) ;
                }else if(msg.what == 4){
                    chatroom.append((String) msg.obj + "\n") ;
                }else{
                    Log.d("handler" , "unmatch") ;
                }
            }
        } ;
    }

    public class WifiReceiver extends BroadcastReceiver{

		@Override
		public void onReceive(Context c, Intent intent) {
			// TODO Auto-generated method stub

			List<ScanResult> wifi_list =  wifi.getScanResults() ;
			Log.d("ap mac ==  " , wifi_list.get(0).BSSID ) ; // show detail
			Log.d("size ==  " , String.valueOf(wifi_list.size())) ;

            // pass list to inResume
            Message toHandle  = new Message() ;
            toHandle.what = 2 ;
            toHandle.obj = wifi_list ;
            toHandle.setTarget(receivefromserver);
            toHandle.sendToTarget();

			unregisterReceiver(this);
		}
	}

    @Override
    protected void onStop() {
        super.onStop();
        try {
            socket.close();
        }catch (IOException e) {
        // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public class MyClientTask extends AsyncTask<Void, Void, Void> {
		int w ;
        BufferedReader in ;
		MyClientTask(BufferedReader input_socket , int what ){
            in = input_socket ;
            w = what ;
		}

		@Override
		protected Void doInBackground(Void... arg0) {
            Log.d("in the thread" , " start thread") ;
			try {
                String recv_msg = new String() ;

                //receiving message
                recv_msg = in.readLine() ;
                Log.d("recv" , recv_msg + "===had been recv!") ;

                // pass message to handler
                Message toHandle  = new Message() ;
                toHandle.what = w ;
                toHandle.obj = recv_msg ;
                toHandle.setTarget(receivefromserver);
                toHandle.sendToTarget();

			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
			    e.printStackTrace();
			} catch (IOException e) {
			    // TODO Auto-generated catch block
			    e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
//			textResponse.setText(response);
			super.onPostExecute(result);
		}
		  
	}

    public class connect_thread extends AsyncTask<Void, Void, Void> {
        String s_addr ;
        int s_port ;
        Socket socket ;
        connect_thread(String addr , int port , Socket input_socket ){
            s_addr = addr ;
            s_port = port ;
            socket = input_socket ;
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            Log.d("in the thread" , " connecting thread") ;
            InetSocketAddress isa = new InetSocketAddress(s_addr, s_port) ;
            try {
                socket.connect(isa, 10000);
                if (socket.isConnected()) Log.i("Chat", "Socket Connected");
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            } catch (UnknownHostException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            while(true){
                try {
                    String recv_msg = new String() ;

                    //receiving message
                    recv_msg = in.readLine() ;
                    Log.d("recv" , recv_msg + "===had been recv!") ;

                    // pass message to handler
                    Message toHandle  = new Message() ;
                    toHandle.what = Integer.valueOf(recv_msg.substring(0,1)) ;
                    toHandle.obj = recv_msg.substring(1) ;
                    toHandle.setTarget(receivefromserver);
                    toHandle.sendToTarget();

                } catch (UnknownHostException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
    }

/*
    public class connect_thread extends AsyncTask<Void, Void, Void> {
        String s_addr ;
        int s_port ;
        connect_thread(String addr , int port ){
            s_addr = addr ;
            s_port = port ;
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            Log.d("in the thread" , " connecting thread") ;
            InetSocketAddress isa = new InetSocketAddress(s_addr, s_port) ;
            try {
                socket.connect(isa, 10000);
                if (socket.isConnected()) Log.i("Chat", "Socket Connected");
            } catch (UnknownHostException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return null;
        }
    }*/

}

