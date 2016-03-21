import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;

class SilentServer{
	public static void main(String args[]) throws Exception{
		DatagramSocket serverSocket = new DatagramSocket(4456);
		Message message;
		System.out.println("TEST");
		System.out.println("TEST".getBytes());
		System.out.println("TEST".getBytes());
		System.out.println("END TEST");
		byte[] a = "TEST".getBytes();
		byte[] b = "TEST".getBytes();
		
		if (a.equals(b)){
			System.out.println("succes1");
		}
		if (a == b){
			System.out.println("succes2");
		}
		if (new String(a).equals(new String(b))){
			System.out.println("succes3");
		}
		if (a.equals(b)){
			System.out.println("succes1");
		}
		
		while(true){
			byte[] receiveData = new byte[1024];
			byte[] sendData = new byte[1024];//Wait for packet and receive it.
			DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
			serverSocket.receive(receivePacket);
			if (new String (receivePacket.getData()).equals(new String( "DHCPDISCOVER".getBytes()))){
				System.out.println("This should work");
			}
			else{
				System.out.println("not working");
			}
			message = new Message(receivePacket);
			System.out.println("RECEIVED: " + new String(receivePacket.getData()));
			System.out.println(receivePacket.getData());
			InetAddress IPAddress = receivePacket.getAddress();

			int port = receivePacket.getPort();
			sendData = answer(message.type());
			
			DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
			serverSocket.send(sendPacket);
			System.out.println("SENT: " + new String(sendPacket.getData()));
		}
		
	}

	private static byte[] answer(String type) {
		String answer;
		answer = "what?";
		if(type == "DHCPDISCOVER"){
			answer = "ACK";
		}
		return answer.getBytes();
	}
}