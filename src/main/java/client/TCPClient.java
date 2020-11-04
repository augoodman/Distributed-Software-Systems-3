package client;

import java.net.*;
import java.io.*;

public class TCPClient {
	public static void main (String args[]) {
		// arguments supply dimension and hostname
		Socket s = null;
		int status = 1;
		boolean newGameFlag = true;
		String response;
		//1) fetch client params (host, port, dimensions)
        if (args.length != 3) {
          System.out.println("Wrong number of arguments:\ngradle runClient --args=\"host port dimension\"");
          System.exit(0);
        }
        String host = args[0];
        int dimension = Integer.parseInt(args[2]);
        int port = 9099; // default port
        try {
            port = Integer.parseInt(args[1]);
        } catch (NumberFormatException nfe) {
            System.out.println("port must be integer");
            System.exit(2);
        }
        //2) create a socket using tcp
		try{
			s = new Socket(host, port);
			DataInputStream in = new DataInputStream( s.getInputStream());
			//4) establish connection to server
			DataOutputStream out =new DataOutputStream( s.getOutputStream());
			while(status == 1) {
				//5) send data
				if(newGameFlag)
					out.writeUTF("start");        // UTF is a string encoding
				else{
					out.writeUTF(ClientGui.getResponse());        // UTF is a string encoding
				}
				//6) receive dimension
				String data = in.readUTF();        // read a line of data from the stream
				System.out.println("Received: " + data);
			}
		} catch (UnknownHostException e) {
			System.out.println("Socket:"+e.getMessage());
		} catch (EOFException e) {
			System.out.println("EOF:"+e.getMessage());
		} catch (IOException e) { 
			System.out.println("readline:"+e.getMessage());
		} finally {
			if(s!=null) 
				try {
					//8) close the socket
					s.close();
				} catch (IOException e) {
					System.out.println("close:"+e.getMessage());
				}
			}
	}
}
