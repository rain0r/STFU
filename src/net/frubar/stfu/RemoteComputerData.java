/**
 * 
 */
package net.frubar.stfu;

/**
 * @author rainer
 * 
 */
public class RemoteComputerData {
	public String Hostname = null;
	public String Username = null;
	public int Port = 22;
	public int SinkDevice = 0;
	public int id;

	public String toString() {
		return this.Username + "@" + this.Hostname + ":" + this.Port;
	}
	
	public String get_port() {
		return String.valueOf(this.Port);
	}
	
	public String get_sink_device() {
		return String.valueOf(this.SinkDevice);
	}
	
	public String get_id() {
		return String.valueOf(this.id);
	}
}
