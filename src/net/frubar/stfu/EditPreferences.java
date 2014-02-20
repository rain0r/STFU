/**
 * 
 */
package net.frubar.stfu;

import java.util.ArrayList;
import java.util.Iterator;

import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.util.Log;

/**
 * @author rainer
 * 
 */
public class EditPreferences extends PreferenceActivity {
	private static final String TAG = "EditPreferences";

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(TAG, "onCreate()");
		setPreferenceScreen(this.create_text_preference());
	}

	/**
	 * Create the Preferences (Option to add a new RemoteComputer and a list of
	 * all saved RemoteComputer)
	 * 
	 * @return PreferenceScreen
	 */
	private PreferenceScreen create_text_preference() {
		PreferenceScreen root = getPreferenceManager().createPreferenceScreen(
				this);

		PreferenceCategory saved_remote_computer_category = new PreferenceCategory(this);
		PreferenceCategory add_remote_category = new PreferenceCategory(this);
		root.addPreference(add_remote_category);
		root.addPreference(saved_remote_computer_category);

		// Pops up the form to add a new Remote Computer
		RemoteComputerPreference remote_computer_preference = new RemoteComputerPreference(
				this, null, null);
		add_remote_category.addPreference(remote_computer_preference);

		// Start of the List of saved Remote Computer
		saved_remote_computer_category.setTitle("Saved Remote Computer");
		add_remote_category.setTitle("Add Remote Computer");

		// Read the XML File for the saved Remote Computer
		ProfileXMLHandler profile_xml_handler = new ProfileXMLHandler(
				getApplicationContext());
		ArrayList<RemoteComputerData> remote_computer_data_list = profile_xml_handler
				.read_xml();
		Iterator<RemoteComputerData> it = remote_computer_data_list.iterator();
		while (it.hasNext()) {
			RemoteComputerData rcd = it.next();
			RemoteComputerPreference item = new RemoteComputerPreference(this, null, rcd);		
			item.setTitle(rcd.toString());
			
			saved_remote_computer_category.addPreference(item);
		}

		return root;
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		Log.d(TAG, "onConfigurationChanged()");
	}
}
