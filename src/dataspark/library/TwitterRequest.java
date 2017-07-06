package dataspark.library;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.ReceiverAdapter;

import processing.core.PApplet;
import processing.core.PImage;


/*
 * Class which sends a data request to the data handler and receives values from it.
 * It returns this values on demand from the processing sketch.
 * 
 */

public class TwitterRequest extends ReceiverAdapter {
	
	PApplet parent; // The processing sketch which calls this class.
	JChannel channel; // The channel to communicate with the data handler.
	int pValue; // The value obtained from the data handler to be used to modify sketch parameters.
	String query; // The query to make to the Twitter API.
	int maxValue = Integer.MAX_VALUE; // The maximum value that can be used in the animation.
	int minValue = 0; // The minimum value that can be used in the animation.
	int timeWindow; // The number of minutes of tweets we want to retrieve. (MAX = 10080 = 7 days).
	
	// Initialise a request with given query and time window.
	public TwitterRequest(PApplet parent, String query, int min) {
		this.parent = parent;
		if (parent != null)	{
			parent.registerMethod("dispose", this);
		}
		this.query = query;
		this.timeWindow = min;
		try {
			start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/*
	 * Creates the JGroups channel to communicate with the data handler.
	 */
	private void start() throws Exception {
		channel = new JChannel();
		channel.setReceiver(this);
		channel.connect("TwitterDataCluster");
		channel.setDiscardOwnMessages(true);
		Object[] array = {"twitter", query, new Integer(timeWindow).toString()};
		Message req = new Message(null, array);
		channel.send(req);
	}
	
	/*
	 * This function is triggered when a message is sent through the channel from the
	 * data handler. It updates the variables accordingly with the value received.
	 * 
	 * @param msg: The message object sent by the data handler.
	 */
	public void receive(Message msg){
		try {
			this.pValue = (int) msg.getObject();
		} catch (ClassCastException e){
			
		}
	}
	/*
	 * Closes the communication channel.
	 */
	private void closeChannel() {
		channel.close();
	}
	
	/*
	 * Function called by the processing sketch. Returns the last value received
	 * from the data handler.
	 */
	public int getValue(){
		if (pValue < maxValue) {
			if (pValue > minValue) {
				return pValue;
			} else {
				return minValue;
			}
		} else {
			return maxValue;
		}
	}
	
	/*
	 * Sets the upper limit for the value returned to the processing script.
	 * 
	 * @param v: The value to be set as maximum.
	 */
	public void setMaxValue(int v){
		this.maxValue = v;
	}
	/*
	 * Sets the lower limit for the value returned to the processing script.
	 * 
	 * @param v: The value to be set as minimum.
	 */
	public void setMinValue(int v){
		this.minValue = v;
	}
	
	/*
	 * Disconnects the class instance from the channel.
	 */
	public void dispose() {
		channel.disconnect();
	}
}
