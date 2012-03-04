package net.frubar.stfu;

/**
 * STFU
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */

import java.io.InputStream;
import java.security.Security;

import org.bouncycastle2.jce.provider.BouncyCastleProvider;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.TextView;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

public class STFU extends Activity implements Runnable, OnClickListener {	
	private static final String TAG = "STFU";
	
	// SSH
	private String user;
	private String host;	
	private JSch jsch = new JSch();	
	private Session session;
	private Channel channel;
	private static java.util.Properties config;

	// commands
	private String mute_cmd = "/usr/bin/pactl set-sink-mute 0 1";
	private String unmute_cmd = "/usr/bin/pactl set-sink-mute 0 0";
	private String increase_cmd = "/usr/bin/pactl set-sink-volume 0 +5%";
	private String decrease_cmd = "/usr/bin/pactl -- set-sink-volume 0 -5%";
	
	// buttons
	private View connect_btn;	
	private View disconnect_btn;	
	private View mute_btn;    	
	private View unmute_btn;
	private View increase_btn;
	private View decrease_btn;
	
	// ProgressDialog
	private ProgressDialog pd;
	
	// Views
	private TextView error_tv;
	private EditText host_txt;
	private EditText user_txt;
	
	static {
		// BouncyCastle for correct SSH Login
		Security.insertProviderAt(
				new BouncyCastleProvider(), 1);
		
		// Accept the fingerprint
		STFU.config = new java.util.Properties(); 
		STFU.config.put("StrictHostKeyChecking", "no");
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
				
		// Log.d(TAG, "onCreate()");
		setContentView(R.layout.main);
		
		// Log.d(TAG, "Starting the ProgressDialog");
		this.initProgressDialog();
		
		try {
			// Log.d(TAG, "Starting the Thread for loading the keys");
			Thread thread = new Thread(this);
			thread.start();
		}
		catch (Exception e) {
			// // Log.e(TAG, "Exception" , e);
		}		
		
		findViews();		
		setListeners();
		setHints();
	}

	/**
	 * find Views and Edits
	 */
    private void findViews() {
    	// Log.d(TAG, "find Views");
    	this.error_tv = (TextView) findViewById(R.id.error_tv);
    	this.host_txt = (EditText) findViewById(R.id.host_txt);
    	this.user_txt = (EditText) findViewById(R.id.user_txt);
    }	
	
	/**
	 * Set Hints
	 */
    private void setHints() {
    	// Log.d(TAG, "set Hints");
    	this.host = Prefs.getHostname(this);
    	this.host_txt.setHint(this.host);
    	this.user = Prefs.getUsername(this);
    	this.user_txt.setHint(this.user);    	
    }    
    
    /**
     * Set listeners for Buttons
     */
    private void setListeners() {
    	// Log.d(TAG, "setListeners");    	
    	// Set up click listeners for all the buttons
    	
    	// connect button
		this.connect_btn = findViewById(R.id.connect_btn);	
		connect_btn.setOnClickListener(this);
		
    	// disconnect button
		this.disconnect_btn = findViewById(R.id.disconnect_btn);
		disconnect_btn.setOnClickListener(this);
		
    	// mute button
		this.mute_btn = findViewById(R.id.mute_btn);
		mute_btn.setOnClickListener(this);

		// unmute button
		this.unmute_btn = findViewById(R.id.unmute_btn);
		unmute_btn.setOnClickListener(this);

		// increase button
		this.increase_btn = findViewById(R.id.increase_btn);
		increase_btn.setOnClickListener(this);
		
		// decrease button
		this.decrease_btn = findViewById(R.id.decrease_btn);
		decrease_btn.setOnClickListener(this);		
    }    
    
    /**
     * Connect to a host via SSH
     * @param host_txt
     * @param user_txt
     */
    private void startSSH(String host_txt, String user_txt) {
    	// Log.d(TAG, "startSSH()");
    	
    	try {    		
    		this.session = jsch.getSession(user_txt, host_txt, 22);  		    		
    		this.session.setConfig(STFU.config);
    		
    		// Log.d(TAG, "start connect()");
    		this.session.connect();
    		// Log.d(TAG, "connect()  done");    	
    	}
		catch (Exception e) {
			this.error_tv.setText("Error:\n"+e.getMessage());
			this.error_tv.setVisibility(View.VISIBLE);
			// Log.e(TAG, "Exception" , e);
			return;
		}
    	
    	showCmdButtons();
    	return;
    }
    
    /**
     * Send a Commando via SSH
     * @param cmd
     */
    private void sendCmd(String cmd) {
    	// Log.d(TAG, "SendCmd( "+cmd+" )");
		try {
	    	this.channel = this.session.openChannel("exec");
	    	((ChannelExec)this.channel).setCommand(cmd);
	    	
    		this.channel.setInputStream(null);
    		((ChannelExec)this.channel).setErrStream(System.err);
    		InputStream in = this.channel.getInputStream();
    		this.channel.connect();

    		byte[] tmp = new byte[1024];
    		while (true) {
    			while(in.available() > 0) {
    				int i = in.read(tmp, 0, 1024);
    				if (i < 0) break;
    				// Log.d(TAG, new String(tmp, 0, i));
    			}
    			if (this.channel.isClosed()) {
    				// Log.d(TAG, "exit-status: "+this.channel.getExitStatus());
    				break;
    			}
    			try{
    				Thread.sleep(1000);
    			}
    			catch (Exception ee) {}
    		}	    	
		}
		catch (Exception e) {
			// Log.e(TAG, "Exception" , e);
		}
    }

    /**
     * Disconnect the SSH-Session
     */
    private void stopSSH() {
    	// Log.d(TAG, "Stopping SSH....");
    	
    	if(this.session.isConnected()) {    	
	    	try {
	    		InputStream in = this.channel.getInputStream();
	    		byte[] tmp = new byte[1024];
	    		while (true) {
	    			while(in.available() > 0) {
	    				int i = in.read(tmp, 0, 1024);
	    				if (i < 0) break;
	    				// Log.d(TAG, new String(tmp, 0, i));
	    			}
	    			if (this.channel.isClosed()) {
	    				// Log.d(TAG, "exit-status: "+this.channel.getExitStatus());
	    				break;
	    			}
	    			try{
	    				Thread.sleep(1000);
	    			}
	    			catch (Exception ee) {}
	    		}	    	    		
	    		this.channel.disconnect();
	    		this.session.disconnect();
	    	}
			catch (Exception e) {
				// Log.e(TAG, "Exception" , e);
			}
    	}
    	// Log.d(TAG, "SSH connection closed");
    	hideCmdButtons();
    }
    
    /**
     * Show the Control buttons
     */
    private void showCmdButtons() {
    	this.mute_btn.setVisibility(View.VISIBLE);
    	this.unmute_btn.setVisibility(View.VISIBLE);
    	this.increase_btn.setVisibility(View.VISIBLE);
    	this.decrease_btn.setVisibility(View.VISIBLE);
    }
    
    /**
     * hide the Control buttons
     */
    private void hideCmdButtons() {
    	this.mute_btn.setVisibility(View.INVISIBLE);
    	this.unmute_btn.setVisibility(View.INVISIBLE);
    	this.increase_btn.setVisibility(View.INVISIBLE);
    	this.decrease_btn.setVisibility(View.INVISIBLE);
    }    
    
    /**
     * initialize the ProgressDialog
     */
	private void initProgressDialog() {
		this.pd = new ProgressDialog(this);
		this.pd.setCancelable(true);
		this.pd.setMessage("One moment please");
		this.pd.setIndeterminate(true);
		this.pd.show();
	}      
    
    // display the context menu
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		// Log.d(TAG, "onCreateOptionsMenu()");	
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu, menu);
		return true;
	}	
	
	// selected a menu item from the contect menu
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Log.d(TAG, "onOptionsItemSelected()");
		switch (item.getItemId()) {
			// by clicking on the settings item
			case R.id.menu_settings:
				// Log.d(TAG, "Starting Prefs");
				try {
					startActivity(new Intent(this, Prefs.class));
				}
				catch (Exception e) {
					// Log.e(TAG, "Exception" , e);
				}
				return true;
		}
		return false;
	}

	// handles the buttons clicked
	@Override
	public void onClick(View v) {
		// Log.d(TAG, "onClick");
		switch (v.getId()) {
			case R.id.connect_btn: {
				// start the ssh connection
				// Log.d(TAG, "Connection to: "+this.host);
				if (this.host.equals("")) {
					this.host = this.host_txt.getText().toString();
				}
				if (this.user.equals("")) {
					this.user = this.user_txt.getText().toString();
				}				
				
				startSSH(this.host, this.user);					
				break;
			}
			
			case R.id.disconnect_btn: {
				// stop the ssh connection
				stopSSH();				
				break;
			}
			
			case R.id.increase_btn: {
				// increase the volume
				sendCmd(this.increase_cmd);				
				break;
			}
			
			case R.id.mute_btn: {
				// mute the volume
				sendCmd(this.mute_cmd);		
				break;
			}			
			
			case R.id.unmute_btn: {
				// increase the volume
				sendCmd(this.unmute_cmd);
				break;
			}
			
			case R.id.decrease_btn: {
				// decrease the volume
				sendCmd(this.decrease_cmd);
				break;
			}			
		}
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
	}

	// config stuff
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
	    super.onConfigurationChanged(newConfig);
	}

	@Override
	public void run() {
		// load Keys for Pub Auth
		try {
			// Log.d(TAG, "trying addIdentity()");
			this.jsch.addIdentity("/sdcard/STFU/id_rsa");
		}
		catch (Exception e) {
            
            this.error_tv.setText(R.string.could_not_find_id_rsa);
            this.error_tv.setVisibility(View.VISIBLE);
			// Log.e(TAG, "Exception" , e);
		}
		handler.sendEmptyMessage(0);
	}
	
	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			// Log.d(TAG, "dismiss the progress dialog");
			pd.dismiss();
		}
	};
}
