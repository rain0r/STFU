/**
 * 
 */
package net.frubar.stfu;

import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Parcelable;
import android.preference.ListPreference;
import android.util.Log;

/**
 * @author rainer
 * 
 */
public class RemoteComputerListPreference extends ListPreference {
	private static final String TAG = "RemoteComputerListPreference";
	private String id = null;

	/**
	 * @param context
	 */
	public RemoteComputerListPreference(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		Log.d(TAG, "RemoteComputerListPreference()");
	}

	@Override
	public int findIndexOfValue(String value) {
		// TODO Auto-generated method stub
		Log.d(TAG, "findIndexOfValue()");
		Log.d(TAG, "value: " + value);
		this.id = value;
		return super.findIndexOfValue(value);
	}

	@Override
	public CharSequence[] getEntries() {
		// TODO Auto-generated method stub
		Log.d(TAG, "getEntries()");
		return super.getEntries();
	}

	@Override
	public CharSequence getEntry() {
		// TODO Auto-generated method stub
		Log.d(TAG, "getEntry()");
		return super.getEntry();
	}

	@Override
	public CharSequence[] getEntryValues() {
		// TODO Auto-generated method stub
		Log.d(TAG, "getEntryValues()");
		return super.getEntryValues();
	}

	@Override
	public String getValue() {
		// TODO Auto-generated method stub
		Log.d(TAG, "getValue()");
		return super.getValue();
	}

	@Override
	protected void onDialogClosed(boolean positiveResult) {
		// TODO Auto-generated method stub
		super.onDialogClosed(positiveResult);
		Log.d(TAG, "onDialogClosed()");
		Log.d(TAG, "positiveResult:" + positiveResult);
		if (positiveResult) {
			ProfileXMLHandler profile_handler = new ProfileXMLHandler(
					getContext());
			profile_handler.delete_profile(this.id);
		}
	}

	@Override
	protected Object onGetDefaultValue(TypedArray a, int index) {
		// TODO Auto-generated method stub
		Log.d(TAG, "onGetDefaultValue()");
		return super.onGetDefaultValue(a, index);
	}

	@Override
	protected void onPrepareDialogBuilder(Builder builder) {
		// TODO Auto-generated method stub
		super.onPrepareDialogBuilder(builder);
		Log.d(TAG, "onPrepareDialogBuilder()");
	}

	@Override
	protected void onRestoreInstanceState(Parcelable state) {
		// TODO Auto-generated method stub
		super.onRestoreInstanceState(state);
		Log.d(TAG, "onRestoreInstanceState()");
	}

	@Override
	protected Parcelable onSaveInstanceState() {
		// TODO Auto-generated method stub
		Log.d(TAG, "onSaveInstanceState()");
		return super.onSaveInstanceState();
	}

	@Override
	protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
		// TODO Auto-generated method stub
		super.onSetInitialValue(restoreValue, defaultValue);
		Log.d(TAG, "onSetInitialValue()");
	}

}
