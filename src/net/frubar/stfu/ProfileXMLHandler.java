/**
 * 
 */
package net.frubar.stfu;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Iterator;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;
import org.xmlpull.v1.XmlSerializer;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.util.Xml;

/**
 * Handles all the Interaction with the XML file ("profiles.xml") that contains
 * all the RemoteComputer-Profiles
 * 
 * Basically, this class checks if the file exists, creates it if it doesn't,
 * adds profiles to the file and reads them out
 * 
 */
public class ProfileXMLHandler {
	private static final String TAG = "ProfileXMLHandler";
	private Context c = null;
	private File xml_file = null;

	/**
	 * 
	 * @param c
	 */
	public ProfileXMLHandler(Context c) {
		Log.d(TAG, "ProfileXMLHandler()");

		this.c = c;
		this.xml_file = new File(this.c.getFilesDir(), "profiles.xml");
		Log.d(TAG, "file.exists: " + this.xml_file.exists());

		if (!this.xml_file.exists()) {
			this.create_xml_file(xml_file);
		}
	}

	/**
	 * 
	 * @param xml_file
	 */
	private void create_xml_file(File xml_file) {
		Log.d(TAG, "create_xml_file()");

		try {
			XmlSerializer xmlSerializer = Xml.newSerializer();
			StringWriter writer = new StringWriter();
			xmlSerializer.setOutput(writer);
			xml_file.createNewFile();

			xmlSerializer.startDocument("UTF-8", true);

			FileOutputStream fileos = new FileOutputStream(xml_file, true);

			xmlSerializer.startTag(null, "RemoteComputerList");
			xmlSerializer.endTag(null, "RemoteComputerList");

			xmlSerializer.endDocument();
			xmlSerializer.flush();
			String dataWrite = writer.toString();
			fileos.write(dataWrite.getBytes());
			fileos.close();

		} catch (Exception e) {
			Log.e(TAG, e.getMessage());
		}
	}

	/**
	 * 
	 * @param username
	 * @param hostname
	 * @param port
	 * @param sink_device
	 */
	public void add_profile(String username, String hostname, String port,
			String sink_device) {
		Log.d(TAG, "add_profile()");
		Log.d(TAG, "Username: " + username + "Hostname: " + hostname + "Port: "
				+ port + " sink_device: " + sink_device);

		try {
			String filepath = this.xml_file.getAbsolutePath();
			File file = this.xml_file;
			DocumentBuilderFactory docFactory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
			Document doc = docBuilder.parse(file);

			// Get the root element
			Node RemoteComputerList = doc.getFirstChild();

			Element RemoteComputerData = doc
					.createElement("RemoteComputerData");
			int id = this.get_id();
			this.increase_id();
			RemoteComputerData.setAttribute("id", "" + id);
			Element Username = doc.createElement("Username");
			Username.appendChild(doc.createTextNode(username));
			Element Hostname = doc.createElement("Hostname");
			Hostname.appendChild(doc.createTextNode(hostname));
			Element Port = doc.createElement("Port");
			Port.appendChild(doc.createTextNode(port));
			Element SinkDevice = doc.createElement("SinkDevice");
			SinkDevice.appendChild(doc.createTextNode(sink_device));

			RemoteComputerData.appendChild(Username);
			RemoteComputerData.appendChild(Hostname);
			RemoteComputerData.appendChild(Port);
			RemoteComputerData.appendChild(SinkDevice);

			RemoteComputerList.appendChild(RemoteComputerData);

			TransformerFactory transformerFactory = TransformerFactory
					.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(new File(filepath));
			transformer.transform(source, result);

		} catch (Exception e) {
			Log.e(TAG, e.getMessage());
		}
	}

	/**
	 * @return
	 * 
	 */
	public ArrayList<RemoteComputerData> read_xml() {
		Log.d(TAG, "read_xml()");
		// File xml_file = new File(this.c.getFilesDir(), "profiles.xml");
		File xml_file = this.xml_file;
		ArrayList<RemoteComputerData> remote_computer_data = null;

		if (xml_file.exists()) {
			XmlPullParserFactory pullParserFactory;
			try {
				pullParserFactory = XmlPullParserFactory.newInstance();
				XmlPullParser parser = pullParserFactory.newPullParser();

				// InputStream in_s =
				// getApplicationContext().getAssets().open("profiles.xml");
				InputStream in_s = this.c.getApplicationContext()
						.openFileInput("profiles.xml");

				parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES,
						false);
				parser.setInput(in_s, null);

				remote_computer_data = this.parse_xml(parser);
			} catch (Exception e) {
				Log.e(TAG, e.getMessage());
			}
		}
		return remote_computer_data;
	}

	/**
	 * 
	 * @param parser
	 * @return
	 * @throws XmlPullParserException
	 * @throws IOException
	 */
	private ArrayList<RemoteComputerData> parse_xml(XmlPullParser parser)
			throws XmlPullParserException, IOException {
		Log.d(TAG, "parse_xml()");

		ArrayList<RemoteComputerData> remote_computer_data_list = null;
		int eventType = parser.getEventType();
		RemoteComputerData remote_computer_data = null;

		while (eventType != XmlPullParser.END_DOCUMENT) {
			String name = null;
			switch (eventType) {
			case XmlPullParser.START_DOCUMENT:
				remote_computer_data_list = new ArrayList<RemoteComputerData>();
				break;
			case XmlPullParser.START_TAG:
				name = parser.getName();
				Log.d(TAG, "name: " + name);
				if (name.equals("RemoteComputerData")) {
					Log.d(TAG, "attrvalue: " + parser.getAttributeValue(0));
					int id = Integer.valueOf(parser.getAttributeValue(0));
					remote_computer_data = new RemoteComputerData();
					remote_computer_data.id = id;
				}
				if (remote_computer_data != null) {
					if (name.equals("Username")) {
						remote_computer_data.Username = parser.nextText();
					} else if (name.equals("Hostname")) {
						remote_computer_data.Hostname = parser.nextText();
					} else if (name.equals("Port")) {
						remote_computer_data.Port = Integer.valueOf(parser
								.nextText());
					} else if (name.equals("SinkDevice")) {
						remote_computer_data.SinkDevice = Integer
								.valueOf(parser.nextText());
					}
				}
				break;
			case XmlPullParser.END_TAG:
				name = parser.getName();
				if (name.equalsIgnoreCase("RemoteComputerData")
						&& remote_computer_data != null) {
					remote_computer_data_list.add(remote_computer_data);
				}
			}
			eventType = parser.next();
		}

		this.log_profiles(remote_computer_data_list);
		return remote_computer_data_list;
	}

	/**
	 * 
	 * @param remote_computer_data_list
	 */
	private void log_profiles(
			ArrayList<RemoteComputerData> remote_computer_data_list) {
		Log.d(TAG, "log_profiles()");

		Iterator<RemoteComputerData> it = remote_computer_data_list.iterator();
		while (it.hasNext()) {
			RemoteComputerData _rcp = it.next();
			Log.d(TAG, "Username: " + _rcp.Username + ", Hostname: "
					+ _rcp.Hostname + ", Port: " + _rcp.Port);
		}
	}

	/**
	 * 
	 * @return id
	 */
	private int get_id() {
		SharedPreferences settings = this.c.getSharedPreferences(
				STFU.PREFS_FILE_NAME, 0);
		int id = settings.getInt("id", 1);

		return id;
	}

	/**
	 * increase the id
	 */
	private void increase_id() {
		int id = this.get_id();
		id = id + 1;
		SharedPreferences settings = this.c.getSharedPreferences(
				STFU.PREFS_FILE_NAME, 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putInt("id", id);
		editor.commit();
	}

	/**
	 * 
	 * @param id
	 */
	public void delete_profile(String id) {
		Log.d(TAG, "delete_profile()");
		Log.d(TAG, "id: " + id);

		try {
			String filepath = this.xml_file.getAbsolutePath();
			DocumentBuilderFactory docFactory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
			Document doc = docBuilder.parse(this.xml_file);
			Element e = doc.getElementById(id);
			e.getParentNode().removeChild(e);
			TransformerFactory transformerFactory = TransformerFactory
					.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(new File(filepath));
			transformer.transform(source, result);

			// Restart the Settings
			Context c_ = this.c;
			Activity activity = (Activity) c_;
			activity.finish();
			activity.startActivity(new Intent(activity, EditPreferences.class));
		} catch (Exception e) {
			Log.e(TAG, "" + e.getMessage());
		}
	}
}
