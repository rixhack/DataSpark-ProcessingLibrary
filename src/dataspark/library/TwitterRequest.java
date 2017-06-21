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
 * Class which emulates the behaviour of the TwitterRequest class from the backend 
 * processing library allowing the designer of a processing sketch to test different 
 * data values with a slider tool.
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
	
	SketchTemplate serverConn;
	// Initialise a request with given query and time window.
	public TwitterRequest(PApplet parent, String query, int min) {
		this.parent = parent;
		if (parent != null)	{
			parent.registerMethod("dispose", this);
		}
		this.query = query;
		this.timeWindow = min;
		try {
			if (parent.args != null){
				this.serverConn = new SketchTemplate(Integer.parseInt(parent.args[0]));
			} else {
				this.serverConn = new SketchTemplate(22637);
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		try {
			start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	// Start the communication and send the query to the data handler. Then wait for a message.
	private void start() throws Exception {
		channel = new JChannel();
		channel.setReceiver(this);
		channel.connect("TwitterDataCluster");
		channel.setDiscardOwnMessages(true);
		Object[] array = {"twitter", query, new Integer(timeWindow).toString()};
		Message req = new Message(null, array);
		channel.send(req);
	}
	
	// When a value is received from the data handler, store it in the pValue variable.
	public void receive(Message msg){
		try {
			this.pValue = (int) msg.getObject();
		} catch (ClassCastException e){
			
		}
	}
	
	private void closeChannel() {
		channel.close();
	}
	
	// When the processing sketch request the data value, return it if it is in the defined range.
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
	
	// Methods to set the maximum and minimum values.
	public void setMaxValue(int v){
		this.maxValue = v;
	}
	
	public void setMinValue(int v){
		this.minValue = v;
	}
	
	public void dispose() {
		channel.disconnect();
	}
	
	public void send(PImage img) {
		try {
			serverConn.sendFrame(img);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private class SketchTemplate {
		Socket echoSocket;
		OutputStream out;
		
		public SketchTemplate(int port) throws IOException {
			this.echoSocket = new Socket("localhost", port);
			this.out = echoSocket.getOutputStream();
		}
		
		void sendFrame(PImage img) throws IOException {
			byte[] buffer = new byte[img.width * img.height *3];
			
			for (int x = 0; x < img.width; x++){
				for (int y = 0; y < img.height; y++){
					int col = img.get(x, y);
					
					buffer[(y * img.width + x) * 3] = (byte) ((col >> 16) & 0xFF);
					buffer[(y * img.width + x) * 3 + 1] = (byte) ((col >> 8) & 0xFF);
	                buffer[(y * img.width + x) * 3 + 2] = (byte) (col & 0xFF);
				}
			}
			out.write(buffer);
			out.flush();
		}
	}
}
