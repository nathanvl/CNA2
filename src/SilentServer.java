import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * A class to start a UDP server with certain DHCP functions implemented. To start
 * the server, the main method needs to be run.
 * @author Nathan Van Laere
 *
 */
class SilentServer{
	/**
	 * A boolean to store whether the current IP/transationID combination can be
	 * acknowledged. If not, this boolean needs to be set "true".
	 */
	private static boolean NAK = false;
	
	public static void main(String args[]) throws Exception{
		printStatus();
		DatagramSocket serverSocket = new DatagramSocket(1234);
		Boolean stop = false;
		while(!stop){
			byte[] receiveData = new byte[1024];
			byte[] sendData = new byte[1024];//Wait for packet and receive it.
			
			DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
			serverSocket.receive(receivePacket);
			Message question = new Message(receivePacket);
			System.out.println("RECEIVED: " + question.type_of_message());
			printStatus();
			InetAddress IPAddress = receivePacket.getAddress();
			int port = receivePacket.getPort();
			Message answer = answer(question);
			if (!(question.type_of_message() == "DHCPRELEASE")){
				sendData = answer.get_data();
				DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
				serverSocket.send(sendPacket);
				System.out.println("SENT: " + answer.type_of_message());
				printStatus();
			}
		}
		serverSocket.close();
	}
	
	/**
	 * Returns the answer to be sent upon receiving a certain question.
	 * 
	 * @param question is the question in a Message object.
	 * @return answer the answer, also in a Message object.
	 */
	private static Message answer(Message question) throws Exception{
		InetAddress server_IP = InetAddress.getByName("localhost");
		String type = question.type_of_message();
		Message answer = null;
		if (type=="DHCPDISCOVER"){
			answer = new Message("DHCPOFFER");
			byte[] IP_address = lend_IP(question.getTransactionID(), question.getMacAddress());
			answer.offerIP(IP_address);
			answer.setServerIP(server_IP.getAddress());
			answer.leaseTime(500);
		}
		else if(type == "DHCPREQUEST"){
			boolean allocate_address = block_IP(question.getTransactionID(), question.getMacAddress());
			if (!allocate_address){
				NAK = true;
			}
			if (!NAK){
				answer = new Message("DHCPACK");
				byte[] IP_address = question.get_option50();
				answer.offerIP(IP_address);
				answer.setServerIP(server_IP.getAddress());
				answer.leaseTime(500);
			}
			else{
				answer = new Message("DHCPNAK");
				byte[] IP_address = question.get_option50();
				answer.offerIP(IP_address);
				answer.setServerIP(server_IP.getAddress());
				answer.leaseTime(500);
				NAK = false;
			}
		}
		
		else if(type == "DHCPRELEASE"){
			release_IP(question.getTransactionID());
		}
		else{
			System.out.println("We don't serve servers here.");
		}
		
		if (type != "DHCPRELEASE"){
			answer.server(question.getMacAddress(), question.get_raw_transaction_id());
		}

		return answer;
		
	}
	
	/**
	 * Some arrays to store the IPs to hand out, assigned MAC addresses and transaction IDs and
	 * the current state of the IPs (taken or not).
	 */
	private static byte[][] mac_addresses = {null, null, null, null};
	private static String[] transaction_ids = {null, null, null, null};
	private static boolean[] taken = {false, false, false, false};
	private static byte[][] available_IPs = {{(byte) 0xC0, (byte) 0xA8, 0x01, 0x61}, {(byte) 0xC0, (byte) 0xA8, 0x01, 0x62},
			{(byte) 0xC0, (byte) 0xA8, 0x01, 0x63}, {(byte) 0xC0, (byte) 0xA8, 0x01, 0x64}};
	
	/**
	 * A function that prints the current state of the IP list, more precisely:
	 * which ones are currently leased and to which MAC Address.
	 */
	private static void printStatus(){
		boolean used = false;
		int i = 0;
		while(i<4){
			if (taken[i]){
				used = true;
				System.out.println(toHex(available_IPs[i]) + " is leased to: " + toHex(mac_addresses[i]));
			}
			i += 1;
		}
		if(!used){
			System.out.println("No addresses are leased yet.");
		}
		
	}
	
	/**
	 * This function is called after DHCPRELEASE is received from a client.
	 * It changes the arrays in such a way that the transaction ID is not anymore
	 * linked to the IP addresses. 
	 * 
	 * @param transaction_id the transaction ID of the binding that needs to be removed.
	 */
	private static void release_IP(String transaction_id){
		int i = 0;
		while(i < 4){
			if(transaction_ids[i].equals(transaction_id)){
				taken[i] = false;
				mac_addresses[i] = null;
				transaction_ids[i] = null;
				return;
			}
			
			i += 1;
		}
		System.out.println("No IP leased with that transaction ID.");
	}
	
	/**
	 * This function is the opposite of release_IP, and links a transactionID and
	 * MACaddress with a certain IP. First it checks whether there are IPs available,
	 * and when it has chosen one, it returns that one to the caller. It is important
	 * to note, however, that taken[i] is not set to true, because that only happens
	 * when an ACK is sent. The links are set, so that it can be checked whether this client
	 * has asked for this IP address yet.
	 * 
	 * @param transaction_id a string with the transaction ID.
	 * @param MACAddress the MACAddress in byte[] form.
	 * 
	 * @return the leased IP address.
	 */
	private static byte[] lend_IP(String transaction_id, byte[] MACAddress) {
		int i = 0;
		while(i<4){
			if (!taken[i]){
				System.out.println("Offering IP: " + toHex(available_IPs[i]));
				transaction_ids[i] = transaction_id;
				mac_addresses[i] = MACAddress;
				return available_IPs[i];
			}
			i += 1;
		}
		System.out.println("All IP addresses are taken!");
		NAK = true;
		return available_IPs[0];
	}
	
	/**
	 * This function actually links the IP and transaction ID together. It does this
	 * by blocking an IP, so that other clients can not get it assigned. 
	 * 
	 * @param transactionID
	 * @param MACAddress
	 * 
	 * @return true if the IP was succesfully blocked, false if that was not possible.
	 * Possible reasons for a false return are that the transaction ID was not linked
	 * before to an IP.
	 */
	private static boolean block_IP(String transactionID, byte[] MACAddress) {
		int i = 0;
		while(i<4){
			if (transaction_ids[i].equals(transactionID)){
				taken[i] = true;
				mac_addresses[i] = MACAddress;
				return true;
			}
			i += 1;
		}
		
		System.out.println("No IP address allocated to such transactionID.");
		return false;
	}
	
	/**
	 * Convert a bytearray into a string in hexadecimals.
	 * 
	 * @param bytes The bytearray that is to be converted.
	 * @return the String, without 0x in front.
	 */
	private static final char[] HEX_DIGITS = "0123456789abcdef".toCharArray();
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