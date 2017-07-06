package dataspark.library;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import processing.core.PApplet;
import processing.core.PImage;

public class FrameSender {
	Socket echoSocket;
	OutputStream out;
	
	public FrameSender(PApplet parent) {
		parent.frameRate(25);
		int port = 22637;
		if (parent.args != null) {
			port = Integer.parseInt(parent.args[0]);
		}
		try {
			this.echoSocket = new Socket("localhost", port);
			this.out = echoSocket.getOutputStream();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	public void send(PImage img) {
		byte[] buffer = new byte[img.width * img.height *3];
		
		for (int x = 0; x < img.width; x++){
			for (int y = 0; y < img.height; y++){
				int col = img.get(x, y);
				
				buffer[(y * img.width + x) * 3] = (byte) (col & 0xFF);
				buffer[(y * img.width + x) * 3 + 1] = (byte) ((col >> 8) & 0xFF);
				buffer[(y * img.width + x) * 3 + 2] = (byte) ((col >> 16) & 0xFF);
                
			}
		}
		try {
			out.write(buffer);
			out.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
