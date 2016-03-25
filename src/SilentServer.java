import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;

class SilentServer{
	public static void main(String args[]) throws Exception{
		DatagramSocket serverSocket = new DatagramSocket(1234);
		Boolean stop = false;
		while(!stop){
			byte[] receiveData = new byte[1024];
			byte[] sendData = new byte[1024];//Wait for packet and receive it.
			
			DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
			serverSocket.receive(receivePacket);
			Message question = new Message(receivePacket);
			System.out.println("RECEIVED A MESSAGE: " + question.type_of_message());
			
			InetAddress IPAddress = receivePacket.getAddress();
			int port = receivePacket.getPort();
			Message answer = answer(question);
			sendData = answer.get_data();
			
			DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
			serverSocket.send(sendPacket);
			System.out.println("SENT AN ANSWER: " + answer.type_of_message());
		}
		serverSocket.close();
	
	}
	/**
	 * Returns the answer to be sent upon receiving a certain question.
	 * 
	 * @param question
	 * @return answer
	 */
	private static Message answer(Message question) {
		String type = question.type_of_message();
		Message answer = null;
		if (type=="DHCPDISCOVER"){
			answer = new Message("DHCPOFFER");
			byte[] IP_address = lend_IP(question.getTransactionID());
			answer.offerIP(IP_address);
			answer.leaseTime(500);
		}
		else if(type == "DHCPREQUEST"){
			
		}
		
		else if(type == "DHCPRELEASE"){
			
		}
		else{
			System.out.println("We don't serve servers here.");
		}
		answer.server(question.getMacAddress(), question.get_raw_transaction_id());
		
		
		return answer;
		
	}
	
	private static byte[][] available_IPs = {{(byte) 0xC0, (byte) 0xA8, 0x01, 0x61}, {(byte) 0xC0, (byte) 0xA8, 0x01, 0x62},
			{(byte) 0xC0, (byte) 0xA8, 0x01, 0x63}, {(byte) 0xC0, (byte) 0xA8, 0x01, 0x64}};
	
	private static int last_lent = 3;
	
	private static byte[] lend_IP(String transactionID) {
		if (last_lent < 3){
			last_lent += 1;
		}
		else{
			last_lent = 0;
		}
		System.out.println("Offering IP: "+toHex(available_IPs[last_lent]));
		return available_IPs[last_lent];
	}
	
	private static final char[] HEX_DIGITS = "0123456789abcdef".toCharArray();
	/**
	 * Convert a bytearray into a string in hexadecimals.
	 * @param bytes
	 * @return
	 */
	public static String toHex(byte[] bytes){
        char[] c = new char[bytes.length*2];
        int index = 0;
        for (byte b : bytes)
        {
            c[index++] = HEX_DIGITS[(b >> 4) & 0xf];
            c[index++] = HEX_DIGITS[b & 0xf];
        }
        return new String(c);
    }
}