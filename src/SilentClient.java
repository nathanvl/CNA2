import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;

class SilentClient {
	public static void main(String args[]) throws Exception{

//		InetAddress IPAddress = InetAddress.getByName("10.33.14.246");	//DHCP TEST SERVER
		InetAddress IPAddress = InetAddress.getByName("localhost");
		Integer i = 0;
		String sentence = "";
		DatagramSocket clientSocket = new DatagramSocket();
		Boolean stop = false;
		while((i < 6) && !stop){
			
			
			byte[] sendData = new byte[1024];
			byte[] receiveData = new byte[1024];
			sentence = i.toString();
			System.out.println(sentence);
			i += 1;
			
			if(i==3){
				sendData = "DHCPDISCOVER".getBytes();
			}
			else{
			sendData = sentence.getBytes();
			}
//			DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, 1234);	//DHCP TEST SERVER
			DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, 4456);
			clientSocket.send(sendPacket);
			System.out.println("SENT: " + new String(sendPacket.getData()));
			System.out.println("and data is: "+ sendData);
			if (new String (sendData).equals(new String("DHCPDISCOVER".getBytes()))){
				System.out.println("This should work");
			}
			else{
				System.out.println("not working");
			}
			DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length); //wait and receive answer
			clientSocket.receive(receivePacket);
			String answer = new String(receivePacket.getData());
			System.out.println("RECEIVED: " + answer);
			
		}
		clientSocket.close();
		System.out.println("Socket closed bitch!");
	}
}
