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
 */

import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.util.Log;

public class Prefs extends PreferenceActivity {
	private static final String TAG = "Prefs";
	
   // Option names and default values	
   private static final String OPT_HOSTNAME = "hostname";
   private static final String OPT_HOSTNAME_DEF = "";
   
   private static final String OPT_USERNAME = "username";
   private static final String OPT_USERNAME_DEF = "";
  
   
   @Override
   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      // Log.d(TAG, "onCreate()-Prefs");     
      addPreferencesFromResource(R.xml.settings);
   }

   // Hostname  
   public static String getHostname(Context context) {
	   return PreferenceManager.getDefaultSharedPreferences(context)
	   	.getString(OPT_HOSTNAME, OPT_HOSTNAME_DEF);
   }
   
   // Username
   public static String getUsername(Context context) {
	   return PreferenceManager.getDefaultSharedPreferences(context)
	   	.getString(OPT_USERNAME, OPT_USERNAME_DEF);
   }

   
   @Override
   public void onConfigurationChanged(Configuration newConfig) {
	   super.onConfigurationChanged(newConfig);
	   // Log.d(TAG, "onConfigurationChanged()");   
   }   
}