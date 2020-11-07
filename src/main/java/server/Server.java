package server;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class Server {
	static int dimension;
	static boolean haveDimension = false;
	static List<String[]> questions = new ArrayList<String[]>();

	public static void main (String args[]) {

		//create question bank
		String[] question0 = {"Question: What is the first president's last name?", "Washington"};
		String[] question1 = {"Question: 13 * 3 =", "39"};
		String[] question2 = {"Question: What is the capital of California?", "Sacramento"};
		String[] question3 = {"Question: Who is the instructor of SER 321 (last name)?", "Mehlhase"};
		String[] question4 = {"Question: What is the best school ever? (3 letters)", "ASU"};
		String[] question5 = {"Question: Define is Pi? (round nearest hundredth)", "3.14"};
		String[] question6 = {"Question: What letter is missing in the phrase: 'Softwar_ _ngin__r'?", "E"};
		String[] question7 = {"Question: Which type of animal barks?", "Dog"};
		String[] question8 = {"Question: Number of letters in alphabet?", "26"};
		String[] question9 = {"Question: How many hours of sleep should you get each night?", "8"};
		String[] question10 = {"Question: How many states are there?", "50"};
		String[] question11 = {"Question: Was 2020 a terrible year (yes or no)?", "Yes"};
		String[] question12 = {"Question: Was this assignment too long and complex (yes or no)?", "Yes"};
		String[] question13 = {"Question: True or False: Megadeth is awesome! (hint: true)", "True"};
		String[] question14 = {"Question: The sky is which color?", "Blue"};
		String[] question15 = {"Question: Enter the word 'answer'?", "Answer"};
		questions.add(question0);
		questions.add(question1);
		questions.add(question2);
		questions.add(question3);
		questions.add(question4);
		questions.add(question5);
		questions.add(question6);
		questions.add(question7);
		questions.add(question8);
		questions.add(question9);
		questions.add(question10);
		questions.add(question11);
		questions.add(question12);
		questions.add(question13);
		questions.add(question14);
		questions.add(question15);
		ServerSocket listenSocket = null;
		//1) fetch server params
		if (args.length != 1) {
			System.out.println("Wrong number of arguments:\ngradle runServer --args=\"8888 9\"");
			System.exit(0);
		}
		int portNo = 9099; // default port
		int delay = 9; // default port
		try {
			portNo = Integer.parseInt(args[0]);
			delay = 9;
		} catch (NumberFormatException nfe) {
			System.out.println("port must be integer");
			System.exit(2);
		}
		try {
			//2) create a socket using TCP
			listenSocket = new ServerSocket(portNo);
			while(true) {
				//5) mark socket so it listens for connections
				//6) blocking wait
				Socket clientSocket = listenSocket.accept();
				new Connection(clientSocket, delay);
			}
		} catch(IOException e) {
			System.out.println("Listen socket:"+e.getMessage());
		} finally {
			if (listenSocket != null && listenSocket.isClosed()) {
				try {
					listenSocket.close();
				} catch (Throwable t) {
					System.out.println("Problem closing ServerSocket " + t.getMessage());
				}
			}
		}
	}
}

class Connection extends Thread {
	DataInputStream in;
	DataOutputStream out;
	Socket clientSocket;
	long __msDelay;
	BufferedImage image;

	public Connection (Socket aClientSocket, long msDelay) {
		try {
			clientSocket = aClientSocket;
			//7)handle the connection
			in = new DataInputStream(clientSocket.getInputStream());
			out = new DataOutputStream(clientSocket.getOutputStream());
			__msDelay = msDelay;
			this.start();
		} catch(IOException e) {
			System.out.println("Connection:"+e.getMessage());
		}
	}
	public void run(){
		try {
			String data = in.readUTF();	 // read a line of data from the stream
			if((data.equals("2") || data.equals("3") || data.equals("4")) && !Server.haveDimension){
				Server.haveDimension = true;
				Server.dimension = Integer.parseInt(data);
				System.out.println("Dimension: " + data);
				ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
				ByteBuffer byteBuffer = ByteBuffer.allocate(4);
				File inputFile;
				String solution;
				if(Server.dimension == 2) {
					inputFile = new File("src/main/java/server/img/Holy-Water.jpg");
					image = ImageIO.read(inputFile);
					ImageIO.write(image, "jpg", byteArrayOutputStream);
					byte[] size = byteBuffer.putInt(byteArrayOutputStream.size()).array();
					out.write(size);
					out.write(byteArrayOutputStream.toByteArray());
					out.flush();
					solution = "Holy Water";
					out.writeUTF(solution);
				}
				else if(Server.dimension == 3){
					inputFile = new File("src/main/java/server/img/Bucket-List.jpg");
					image = ImageIO.read(inputFile);
					ImageIO.write(image, "jpg", byteArrayOutputStream);
					byte[] size = byteBuffer.putInt(byteArrayOutputStream.size()).array();
					out.write(size);
					out.write(byteArrayOutputStream.toByteArray());
					out.flush();
					solution = "Bucket List";
					out.writeUTF(solution);
				}
				else {
					inputFile = new File("src/main/java/server/img/I-Believe-In-You.jpg");
					image = ImageIO.read(inputFile);
					ImageIO.write(image, "jpg", byteArrayOutputStream);
					byte[] size = byteBuffer.putInt(byteArrayOutputStream.size()).array();
					out.write(size);
					out.write(byteArrayOutputStream.toByteArray());
					out.flush();
					solution = "I Believe In You";
					out.writeUTF(solution);
				}
				String[] qa = new String[2];
				while(!Server.questions.isEmpty()) {
					qa = Server.questions.remove((int) (Math.random() * (Server.questions.size())));
					out.writeUTF(qa[0]);
					out.writeUTF(qa[1]);
				}
				clientSocket.close();
			}
			else {
				System.out.println("Read " + data);
				Thread.sleep(__msDelay);
				out.writeUTF(data);
			}
		} catch (EOFException e) {
			System.out.println("EOF:"+e.getMessage());
		} catch(IOException e) {
			System.out.println("readline:"+e.getMessage());
		} catch (Throwable t) {
			System.out.println("Caught some other ugliness " + t.getMessage());
		}
		finally {
			try {
				//8) close the connection
				clientSocket.close();
			} catch (IOException e) {
				/*close failed*/
			}
		}
	}
}
