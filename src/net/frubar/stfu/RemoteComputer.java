/**
 * 
 */
package net.frubar.stfu;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;

/**
 * @author rainer
 * 
 */
public class RemoteComputer extends RelativeLayout implements OnClickListener {
	private static final String TAG = "RemoteComputer";
	private EditText username = null;
	private EditText port = null;
	private EditText hostname = null;
	private EditText sink_device = null;
	private Button delete_remote_computer_btn = null;
	private RemoteComputerData rcd = null;
	private Context c = null;

	/**
	 * @param context
	 */
	public RemoteComputer(Context context, RemoteComputerData rcp) {
		super(context);
		Log.d(TAG, "RemoteComputer()");
		this.rcd = rcp;
		this.c = context;
		this.init_form(null);
	}

	/**
	 * initialize the form
	 * 
	 * @param attrs
	 */
	private void init_form(AttributeSet attrs) {
		Log.d(TAG, "init_form()");
		((Activity) getContext()).getLayoutInflater().inflate(R.layout.form,
				this, true);

		this.username = (EditText) findViewById(R.id.user_txt);
		this.set_username(this.username);

		this.hostname = (EditText) findViewById(R.id.host_txt);
		this.set_hostname(this.hostname);

		this.port = (EditText) findViewById(R.id.port_txt);
		this.set_port(this.port);

		this.sink_device = (EditText) findViewById(R.id.sink_device_txt);
		this.set_sink_device(this.sink_device);

		this.delete_remote_computer_btn = (Button) findViewById(R.id.delete_remote_computer_btn);
		this.delete_remote_computer_btn.setOnClickListener(this);
		this.set_delete_remote_computer_btn(this.delete_remote_computer_btn);

		if (this.rcd != null) {
			this.delete_remote_computer_btn.setVisibility(View.VISIBLE);

			this.username.setText(this.rcd.Username);
			this.hostname.setText(this.rcd.Hostname);
			this.port.setText(this.rcd.get_port());
			this.sink_device.setText(this.rcd.get_sink_device());
		}
	}

	/**
	 * @return the username
	 */
	public String get_username() {
		return username.getText().toString();
	}

	/**
	 * @param username
	 *            the username to set
	 */
	public void set_username(EditText username) {
		this.username = username;
	}

	/**
	 * @return the port
	 */
	public String get_port() {
		return port.getText().toString();
	}

	/**
	 * @param port
	 *            the port to set
	 */
	public void set_port(EditText port) {
		this.port = port;
	}

	/**
	 * @return the hostname
	 */
	public String get_hostname() {
		return hostname.getText().toString();
	}

	/**
	 * @param hostname
	 *            the hostname to set
	 */
	public void set_hostname(EditText hostname) {
		this.hostname = hostname;
	}

	/**
	 * @return the sink_device
	 */
	public String get_sink_device() {
		return sink_device.getText().toString();
	}

	/**
	 * @param sink_device
	 *            the sink_device to set
	 */
	public void set_sink_device(EditText sink_device) {
		this.sink_device = sink_device;
	}

	/**
	 * @return the delete_remote_computer_btn
	 */
	public Button get_delete_remote_computer_btn() {
		return delete_remote_computer_btn;
	}

	/**
	 * @param delete_remote_computer_btn
	 *            the delete_remote_computer_btn to set
	 */
	public void set_delete_remote_computer_btn(Button delete_remote_computer_btn) {
		this.delete_remote_computer_btn = delete_remote_computer_btn;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.View.OnClickListener#onClick(android.view.View)
	 */
	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.delete_remote_computer_btn) {
			ProfileXMLHandler profile_editor = new ProfileXMLHandler(this.c);

			if (this.rcd != null) {
				// delete the old profile
				try {
					profile_editor.delete_profile(String.valueOf(this.rcd.id));
				} catch (Exception e) {
					Log.e(TAG, e.getMessage());
				}
			}

		}
	}
}