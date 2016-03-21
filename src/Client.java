import java.io.BufferedReader;	//THIS IS NOT ALLOWED IN THE END PROGRAM
import java.io.InputStreamReader;	//THIS IS NOT ALLOWED IN THE END PROGRAM
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;

class Client {
	public static void main(String args[]) throws Exception{

//		InetAddress IPAddress = InetAddress.getByName("10.33.14.246");	//DHCP TEST SERVER
//		InetAddress IPAddress = InetAddress.getByName("192.168.0.131");
//		InetAddress IPAddress = InetAddress.getByName("192.168.1.3");
//		InetAddress IPAddress = InetAddress.getByName("192.168.1.5");
//		InetAddress IPAddress = InetAddress.getByName("109.131.176.147");
		InetAddress IPAddress = InetAddress.getByName("localhost");
		
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
			System.out.println("packet sent!");
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
