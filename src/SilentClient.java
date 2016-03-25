import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Random;

class SilentClient {
	private static byte[] oldData = new byte[1024];
	private static byte[] sendData = new byte[1024];
	private static byte[] receiveData = new byte[1024];
	private static InetAddress client_IP;
	private static byte[] MACAddress = {0x68,0x5d,0x43,0x06,0x56,0x35,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00};
	private static int port = 1234;
	
	private static void prepareForNext(){
		oldData = receiveData;
		sendData = new byte[1024];
		receiveData = new byte[1024];
	}
	
	private void sendAndReceive(){
		
	}
	public static void main(String args[]) throws Exception{
		Random r = new Random();
		Integer client_id = r.nextInt(999999999);	//transaction ID
		System.out.println("My transaction ID is: " + client_id);
		client_IP = InetAddress.getByName("10.43.117.207");	//FILL IN CORRECTLY IN ADVANCE
//		InetAddress IPAddress = InetAddress.getByName("10.33.14.246");	//DHCP TEST SERVER
		InetAddress IPAddress = InetAddress.getByName("localhost");
		DatagramSocket clientSocket = new DatagramSocket();
		

		Message question = new Message("DHCPDISCOVER");
		question.client(client_id, client_IP, MACAddress);
		sendData = question.get_data();
		DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
		clientSocket.send(sendPacket);
		System.out.println("SENT MESSAGE: " + question.type_of_message());
		
		DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length); //wait and receive answer
		clientSocket.receive(receivePacket);
		
		Message answer = new Message(receivePacket);
		System.out.println("RECEIVED MESSAGE: " + answer.type_of_message());

		if (answer.type_of_message() == "DHCPOFFER"){
			prepareForNext();
			question = new Message("DHCPREQUEST");
			question.client(client_id, client_IP, MACAddress);
			
			sendData = question.get_data();
		}
		else{
			System.out.println("Server answered, but not with an offer. Instead, what we got is: " + answer.type_of_message());
		}
		
		
		
		
		
		
		
		
		
		
		
		
		clientSocket.close();
		System.out.println("Socket closed!		--------------------END--------------------");
	}
}
