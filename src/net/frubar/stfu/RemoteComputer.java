/**
 * 
 */
package net.frubar.stfu;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.EditText;
import android.widget.RelativeLayout;

/**
 * @author rainer
 * 
 */
public class RemoteComputer extends RelativeLayout {
	private static final String TAG = "RemoteComputer";
	private EditText username = null;
	private EditText port = null;
	private EditText hostname = null;
	private EditText sink_device = null;

	/**
	 * @param context
	 */
	public RemoteComputer(Context context) {
		super(context);
		Log.d(TAG, "RemoteComputer()");
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

		set_username((EditText) findViewById(R.id.user_txt));
		set_hostname((EditText) findViewById(R.id.host_txt));
		set_port((EditText) findViewById(R.id.port_txt));
		set_sink_device((EditText) findViewById(R.id.sink_device_txt));
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
}
