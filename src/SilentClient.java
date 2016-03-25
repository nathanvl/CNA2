import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Random;

/**
 * A class to start a UDP client with certain DHCP functions implemented. To start
 * the client, the main method needs to be run.
 * @author Nathan Van Laere
 *
 */
class SilentClient {
	
	/**
	 * Some self-explanatory constants.
	 */
	private static byte[] sendData = new byte[1024];
	private static byte[] receiveData = new byte[1024];
	private static InetAddress client_IP;
	private static byte[] MACAddress = {0x68,0x5d,0x43,0x06,0x56,0x35,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00};
	private static int port = 1234;
	
	/**
	 * Resets the datas so they can be refilled.
	 */
	private static void prepareForNext(){
		sendData = new byte[1024];
		receiveData = new byte[1024];
	}
	
	/**
	 * One giant method with if/else logic to construct a client.
	 * 
	 * @throws Exception because otherwise InetAddress behaves like a baby.
	 */
	public static void main(String args[]) throws Exception{
		Random r = new Random();
		Integer client_id = r.nextInt(999999999);	//transaction ID
		System.out.println("My transaction ID is: " + client_id);
		client_IP = InetAddress.getByName("10.43.117.207");	//FILL IN CORRECTLY IN ADVANCE
//		InetAddress IPAddress = InetAddress.getByName("10.33.14.246");	//DHCP TEST SERVER
		
		InetAddress IPAddress = InetAddress.getByName("localhost");
		DatagramSocket clientSocket = new DatagramSocket();
		
		System.out.println("				--------------------START--------------------");
		/**
		 * Send a DHCPDISCOVER message.
		 */
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
		
		/**
		 * If an offer is received, send a DHCPREQUEST.
		 */
		if (answer.type_of_message() == "DHCPOFFER"){
			prepareForNext();
			question = new Message("DHCPREQUEST");
			question.client(client_id, client_IP, MACAddress);
			question.setServerIP(answer.get_option54());
			question.request(answer.offered());
			sendData = question.get_data();
			
			sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
			clientSocket.send(sendPacket);
			System.out.println("SENT MESSAGE: " + question.type_of_message());
			
			receivePacket = new DatagramPacket(receiveData, receiveData.length); //wait and receive answer
			clientSocket.receive(receivePacket);
			
			answer = new Message(receivePacket);
			System.out.println("RECEIVED MESSAGE: " + answer.type_of_message());
			/**
			 * If an ACK is received, send a RELEASE.
			 */
			if (answer.type_of_message() == "DHCPACK"){
				System.out.println("Got an IP address, we're happy.");
				System.out.println("Now let's get rid of it by sending a DHCPRELEASE.");
				prepareForNext();
				question = new Message("DHCPRELEASE");
				question.client(client_id, client_IP, MACAddress);
				sendData = question.get_data();
				
				sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
				clientSocket.send(sendPacket);
				System.out.println("SENT MESSAGE: " + question.type_of_message());
			}
			/**
			 * If a DHCPNAK is received, tell the user something is wrong.
			 */
			else if (answer.type_of_message() == "DHCPNAK"){
				System.out.println("This means the server does not agree with our request. Try again later.");
			}
			else{
				System.out.println("Server answered, but not with an offer." +
						" Instead, what we got is: " + answer.type_of_message());
			}
		}
		else{
			System.out.println("Server answered, but not with an offer." +
					" Instead, what we got is: " + answer.type_of_message());
		}

		clientSocket.close();
		System.out.println("Socket closed!");
		System.out.println("				--------------------END--------------------");
	}
}
