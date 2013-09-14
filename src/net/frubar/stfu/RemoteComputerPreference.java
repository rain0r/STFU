/**
 * 
 */
package net.frubar.stfu;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

/**
 * @author rainer
 * 
 */
public class RemoteComputerPreference extends DialogPreference {
	private static final String TAG = "RemoteComputerPreference";
	private RemoteComputer remote_computer = null;
	private Context c = null;

	/**
	 * @param context
	 * @param attrs
	 */
	public RemoteComputerPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		Log.d(TAG, "RemoteComputerPreference()");
		this.c = context;
		this.setTitle("New Remote Computer");
	}

	protected View onCreateDialogView() {
		this.remote_computer = new RemoteComputer(getContext());

		return (this.remote_computer);
	}

	@Override
	protected void onDialogClosed(boolean positiveResult) {
		super.onDialogClosed(positiveResult);
		Log.d(TAG, "onDialogClosed()");

		if (positiveResult) {
			// save
			String username = this.remote_computer.get_username().toString();
			String hostname = this.remote_computer.get_hostname().toString();
			String port = this.remote_computer.get_port().toString();
			String sink_device = this.remote_computer.get_sink_device()
					.toString();

			ProfileXMLHandler profile_editor = new ProfileXMLHandler(this.c);
			profile_editor.add_profile(username, hostname, port, sink_device);

			// Restart the Settings
			Context c_ = this.c;
			Activity activity = (Activity) c_;
			activity.finish();
			activity.startActivity(new Intent(activity, EditPreferences.class));
		}
	}
}
