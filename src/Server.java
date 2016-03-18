import java.io.*;
import java.net.*;

class Server{
	public static void main(String args[]) throws Exception{
		DatagramSocket serverSocket = new DatagramSocket(4456);
		System.out.println("Socket opened");
		
		while(true){
			byte[] receiveData = new byte[1024];
			byte[] sendData = new byte[1024];
			
			System.out.println("Waiting for a packet...");	//Wait for packet and receive it.
			DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
			serverSocket.receive(receivePacket);
			System.out.println("Packet received!");
			
			InetAddress IPAddress = receivePacket.getAddress();
			String receivedData = new String(receivePacket.getData());
			
			int port = receivePacket.getPort();
			System.out.println("RECEIVED: '" + receivedData + "'" + " FROM " + IPAddress.getHostAddress());
			System.out.println("ON PORT: " + port);
			String answer = new String("Hello, server talking. I just received this from you: "+ receivedData);
			sendData = answer.getBytes();
			DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
			serverSocket.send(sendPacket);
			System.out.println("ANSWERED");
			System.out.println("---------------");
		}
		
	}
}