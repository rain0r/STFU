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

import java.io.File;
import java.io.InputStream;
import java.security.Security;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.bouncycastle2.jce.provider.BouncyCastleProvider;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

public class STFU extends Activity implements OnClickListener {
	private static final String TAG = "STFU";
	public static final String PREFS_FILE_NAME = "STFU";

	private JSch jsch = null;
	private Session session = null;
	private Channel channel = null;
	private static java.util.Properties config = null;
	private boolean keys_found = false;

	// commands
	private String mute_cmd = null;
	private String unmute_cmd = null;
	private String increase_cmd = null;
	private String decrease_cmd = null;
	// private String get_sink_cmd = "pacmd list-sinks | grep '* index'";

	// buttons
	private View connect_btn = null;
	private View disconnect_btn = null;
	private View mute_btn = null;
	private View unmute_btn = null;
	private View increase_btn = null;
	private View decrease_btn = null;
	private View no_saved_remote_computer_btn = null;

	public static RemoteComputerData connect_remote_computer_data = null;
	// private SpinnerActivity spinner_activity = null;
	private Spinner remote_computer_spinner = null;
	private boolean mute_status = false;

	static {
		// BouncyCastle for correct SSH Login
		Security.insertProviderAt(new BouncyCastleProvider(), 1);

		// Accept the fingerprint
		STFU.config = new java.util.Properties();
		STFU.config.put("StrictHostKeyChecking", "no");
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "onCreate()");
		super.onCreate(savedInstanceState);
		
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.main);

		this.create_stfu_folder();
		this.jsch = new JSch();
		this.find_views();
		this.setListeners();
		this.load_ssh_key();

		if (this.keys_found) {
			this.load_saved_remote_computer();
		}
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		Log.d(TAG, "onResume()");
		this.load_saved_remote_computer();
	}


	/**
	 * find Views and Edits
	 */
	private void find_views() {
		Log.d(TAG, "findViews()");
		this.connect_btn = (Button) findViewById(R.id.connect_btn);
		this.disconnect_btn = (Button) findViewById(R.id.disconnect_btn);
		this.mute_btn = (Button) findViewById(R.id.mute_btn);
		this.unmute_btn = (Button) findViewById(R.id.unmute_btn);
		this.increase_btn = (Button) findViewById(R.id.increase_btn);
		this.decrease_btn = (Button) findViewById(R.id.decrease_btn);
		this.no_saved_remote_computer_btn = (Button) findViewById(R.id.no_saved_remote_computer_btn);
	}

	/**
	 * Set listeners for Buttons
	 */
	private void setListeners() {
		Log.d(TAG, "setListeners()");
		this.connect_btn.setOnClickListener(this);
		this.disconnect_btn.setOnClickListener(this);
		this.mute_btn.setOnClickListener(this);
		this.unmute_btn.setOnClickListener(this);
		this.increase_btn.setOnClickListener(this);
		this.decrease_btn.setOnClickListener(this);
		this.no_saved_remote_computer_btn.setOnClickListener(this);
	}

	/**
	 * Connect to a host via SSH
	 */
	private void connect(RemoteComputerData rcd) {
		Log.d(TAG, "startSSH()");

		if (rcd != null) {
			String username = rcd.Username;
			String hostname = rcd.Hostname;
			int port = rcd.Port;
			int sink = rcd.SinkDevice;

			this.mute_cmd = "pactl set-sink-mute " + sink + " 1";
			this.unmute_cmd = "pactl set-sink-mute " + sink + " 0";
			this.increase_cmd = "pactl set-sink-volume " + sink + " +5%";
			this.decrease_cmd = "pactl -- set-sink-volume " + sink + " -5%";

			boolean auth_success = false;
			try {
				this.session = jsch.getSession(username, hostname, port);
				this.session.setConfig(STFU.config);
				this.session.connect();
				auth_success = true;
			} catch (Exception e) {
				if (e.getMessage().startsWith("Auth fail")) {
					// TODO use strings.xml
					String msg = "Auth failed";
					this.show_dialog(msg);
				}
				Log.e(TAG, e.getMessage());
			}

			if (auth_success) {
				this.hide_connect_btn();
				this.show_sound_btns();
			}
		}
	}

	/**
	 * Send a Commando via SSH
	 * 
	 * @param cmd
	 */
	private void send_cmd(String cmd) {
		Log.d(TAG, "send_cmd( " + cmd + " )");

		try {
			this.channel = this.session.openChannel("exec");
			((ChannelExec) this.channel).setCommand(cmd);

			this.channel.setInputStream(null);
			((ChannelExec) this.channel).setErrStream(System.err);
			InputStream in = this.channel.getInputStream();
			this.channel.connect();
			byte[] tmp = new byte[1024];
			while (true) {
				while (in.available() > 0) {
					int i = in.read(tmp, 0, 1024);
					if (i < 0) {
						break;
					}
					// Log.d(TAG, "foo: "+this.sink_device);
				}
				if (this.channel.isClosed()) {
					// Log.d(TAG, "exit-status: " +
					// this.channel.getExitStatus());
					break;
				}
			}
		} catch (Exception e) {
			Log.e(TAG, e.getMessage());
		}
	}

	/**
	 * Disconnect the SSH-Session
	 */
	private void disconnect() {
		Log.d(TAG, "stop_ssh()");

		try {
			this.channel.disconnect();
			this.session.disconnect();
		} catch (Exception e) {
			Log.e(TAG, "" + e.getMessage());
		}

		this.show_connect_btn();
		this.hide_sound_btns();
		Log.d(TAG, "" + this.increase_btn.getVisibility());
	}

	/**
	 * display the context menu
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		Log.d(TAG, "onCreateOptionsMenu()");
		super.onCreateOptionsMenu(menu);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu, menu);
		return true;
	}

	/**
	 * selected a menu item from the contect menu
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Log.d(TAG, "onOptionsItemSelected()");
		Log.d(TAG, "" + item.getItemId());

		switch (item.getItemId()) {
		case R.id.menu_settings:
			try {
				startActivity(new Intent(this, EditPreferences.class));
				// Intent pref_intent = new Intent(this, EditPreferences.class);
				// startActivityForResult(pref_intent, STFU.PREFS_UPDATED);
			} catch (Exception e) {
				Log.e(TAG, e.getMessage());
			}
			return true;
		}
		return false;
	}

	/**
	 * handle clicks
	 */
	@Override
	public void onClick(View v) {
		Log.d(TAG, "onClick()");

		switch (v.getId()) {
		case R.id.connect_btn: {
			// start the ssh connection
			Log.d(TAG, "connect_btn");
			Log.d(TAG, this.remote_computer_spinner.getSelectedItem()
					.toString());
			RemoteComputerData rcd = (RemoteComputerData) this.remote_computer_spinner
					.getSelectedItem();
			this.connect(rcd);
			break;
		}

		case R.id.disconnect_btn: {
			// stop the ssh connection
			this.disconnect();
			break;
		}

		case R.id.increase_btn: {
			// increase the volume
			this.send_cmd(this.increase_cmd);
			break;
		}
		case R.id.decrease_btn: {
			// decrease the volume
			send_cmd(this.decrease_cmd);
			break;
		}
		case R.id.mute_btn: {
			// mute the volume
			this.toggle_mute_btn();
			this.send_cmd(this.mute_cmd);
			break;
		}

		case R.id.unmute_btn: {
			// increase the volume
			this.toggle_mute_btn();
			this.send_cmd(this.unmute_cmd);
			break;
		}
		
		case R.id.no_saved_remote_computer_btn: {
			// start the 'Add RemoteComputer' formular
			Log.d(TAG, "clicked on 'Add RemoteComputer' via main.xml");
			try {
				startActivity(new Intent(this, EditPreferences.class));
			} catch (Exception e) {
				Log.e(TAG, e.getMessage());
			}
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
		Log.d(TAG, "onConfigurationChanged()");
	}

	/**
	 * load the ssh keys
	 */
	public void load_ssh_key() {
		Log.d(TAG, "load_ssh_key()");

		try {
			Log.d(TAG, "trying addIdentity()");
			this.jsch.addIdentity(Environment.getExternalStorageDirectory()
					.getPath() + "/STFU/id_rsa");
			this.keys_found = true;
		} catch (Exception e) {
			String e_msg = e.getMessage();
			Log.e(TAG, e_msg);
			if (e_msg.startsWith("java.io.FileNotFoundException")) {
				Log.d(TAG, "Display 'File Not Found'");
				// TODO use strings.xml for this
				String msg = "SSH-Keys not found! Copy them to "
						+ Environment.getExternalStorageDirectory().getPath()
						+ "/STFU/id_rsa to get this app working";
				msg += "\n\nFor help check http://hihn.org/stfu/";
				this.show_dialog(msg);
			}
		}
	}

	/**
	 * show a dialog box
	 * 
	 * @param msg
	 */
	private void show_dialog(String msg) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(msg).setCancelable(false)
				.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						// do things
					}
				});
		AlertDialog alert = builder.create();
		alert.show();
	}

	/**
	 * hide all the sound-control-buttons (after disconnecting)
	 */
	private void hide_sound_btns() {
		Log.d(TAG, "hide_sound_btns()");
		this.mute_btn.setVisibility(View.GONE);
		this.unmute_btn.setVisibility(View.GONE);
		this.increase_btn.setVisibility(View.GONE);
		this.decrease_btn.setVisibility(View.GONE);
	}

	/**
	 * show all the sound-contorl-buttons (after connecting)
	 */
	private void show_sound_btns() {
		Log.d(TAG, "show_sound_btns()");
		this.mute_btn.setVisibility(View.VISIBLE);
		this.increase_btn.setVisibility(View.VISIBLE);
		this.decrease_btn.setVisibility(View.VISIBLE);
	}

	/**
	 * show the connect button (after disconnecting) hide the disconnect button
	 */
	private void show_connect_btn() {
		Log.d(TAG, "show_connect_btn()");
		this.connect_btn.setVisibility(View.VISIBLE);
		this.disconnect_btn.setVisibility(View.GONE);
	}

	/**
	 * hide the connect button (after connecting) show the disconnect button
	 */
	private void hide_connect_btn() {
		Log.d(TAG, "hide_connect_btn()");
		this.connect_btn.setVisibility(View.GONE);
		this.disconnect_btn.setVisibility(View.VISIBLE);
	}

	/**
	 * toggle the mute / unmute button
	 */
	private void toggle_mute_btn() {
		Log.d(TAG, "toggle_mute_btn()");
		Log.d(TAG, "mute_status:" + this.mute_status);
		if (this.mute_status) {
			this.mute_status = false;
			this.mute_btn.setVisibility(View.VISIBLE);
			this.unmute_btn.setVisibility(View.GONE);
		} else {
			this.mute_status = true;
			this.mute_btn.setVisibility(View.GONE);
			this.unmute_btn.setVisibility(View.VISIBLE);
		}
	}

	/**
	 * load saved Remote Computer from the xml file
	 */
	private void load_saved_remote_computer() {
		// Display the saved Remote Computer

		List<RemoteComputerData> spinner_items = new ArrayList<RemoteComputerData>();
		ProfileXMLHandler profile_xml_handler = new ProfileXMLHandler(
				getApplicationContext());
		ArrayList<RemoteComputerData> remote_computer_data_list = profile_xml_handler
				.read_xml();

		this.remote_computer_spinner = (Spinner) findViewById(R.id.remote_computer_spinner);
		if (remote_computer_data_list.size() > 0) {
			Iterator<RemoteComputerData> it = remote_computer_data_list
					.iterator();
			while (it.hasNext()) {
				RemoteComputerData rcd = it.next();
				spinner_items.add(rcd);
			}

			ArrayAdapter<RemoteComputerData> remote_computer_adapter = new ArrayAdapter<RemoteComputerData>(
					this, android.R.layout.simple_spinner_item, spinner_items);
			// ArrayAdapter<String> ___remote_computer_adapter = new
			// ArrayAdapter<String>(this, android.R.layout.simple_spinner_item,
			// spinner_items);
			remote_computer_adapter
					.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

			remote_computer_spinner.setAdapter(remote_computer_adapter);

			this.show_connect_btn();
		} else {
			// Show the User that he hasnt added any RemoteComputer yet
			
			// The Text Message
			TextView error_tv = (TextView) findViewById(R.id.no_saved_remote_computer_tv);
			error_tv.setVisibility(View.VISIBLE);
			
			// The 'Add RemoteComputer' Button
			Button error_btn = (Button) findViewById(R.id.no_saved_remote_computer_btn);
			error_btn.setVisibility(View.VISIBLE);
			
			remote_computer_spinner.setVisibility(View.GONE);
		}
	}
	
	/**
	 * checks if the STFU Folder exists
	 * creates it, if not
	 */
	private void create_stfu_folder() {
		File f = new File(Environment.getExternalStorageDirectory() + "/STFU");
		if(!f.isDirectory()) {
			try {
				f.mkdir();
			}
			catch (Exception e) {
				Log.e(TAG, e.getMessage());
			}
		}
	}

}
