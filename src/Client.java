import java.io.*;
import java.net.*;

class Client {
	public static void main(String args[]) throws Exception{
		
		
		
//		InetAddress IPAddress = InetAddress.getByName("10.33.14.246");	//DHCP TEST SERVER
		InetAddress IPAddress = InetAddress.getByName("192.168.0.131");
//		InetAddress IPAddress = InetAddress.getByName("localhost");
		String sentence = "";
		while(sentence != "STOP"){
			DatagramSocket clientSocket = new DatagramSocket();
			System.out.println("Send to server:");
			BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
			
			byte[] sendData = new byte[1024];
			byte[] receiveData = new byte[1024];
			
			sentence = inFromUser.readLine();	//read from user input, and then send it.
			System.out.println("Sending...");
			sendData = sentence.getBytes();
//			DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, 1234);	//DHCP TEST SERVER
			DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, 4456);
			clientSocket.send(sendPacket);
			
			DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length); //wait and receive answer
			clientSocket.receive(receivePacket);
			String answer = new String(receivePacket.getData());
			System.out.println("RECEIVED ANSWER: '" + answer + "'"+ " FROM " + IPAddress.getHostAddress());
			System.out.println("ON PORT: "+receivePacket.getPort());
			System.out.println("---------------");
			
			
		clientSocket.close();
		}
	}
}
