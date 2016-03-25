import java.net.DatagramPacket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.Arrays;


/**
 * A class for DHCP messages. These messages consist only of data that is used in the DHCP protocol.
 * This class can be used for both creating and analyzing messages to be sent or that are received.
 * 
 * @author Nathan Van Laere
 *
 */
public class Message {
	
	/** Chunks of data that are required to do DHCP protocol operations.**/
	private byte[] op = new byte[1]; //Op code: 1 = bootRequest, 2 = BootReply
    private byte[] htype = {1}; //Hardware Address Type: 1 = 10MB ethernet
    private byte[] hlen = {6}; //hardware address length: length of MACID
    private byte[] hops = new byte[1]; //Hw options
    private byte[] xid = new byte[4]; //transaction id (5), 
    private byte[] secs = new byte[2]; //elapsed time from trying to boot (3)
    private byte[] flags = new byte[2]; //flags (3)
    private byte[] ciaddr = new byte[4]; // client IP (5)
    private byte[] yiaddr = new byte[4]; // your client IP (5)
    private byte[] siaddr = new byte[4]; // Server IP (5)
    private byte[] giaddr = new byte[4]; // relay agent IP (5)
    private byte[] chaddr = new byte[16]; // Client HW address (16)
    private byte[] sname = new byte[64]; // Optional server host name (64)
    private byte[] file = new byte[128]; // Boot file name (128)
    private byte[] magic_cookie = {0x63,  (byte) 0x82, 0x53, 0x63};
    private byte[] options; //options (rest)
    private 
    
    /** The array where the data is kept together.**/
	byte[] data;
	
	private DatagramPacket packet;
	
	/**
	 * Create a message from a received DatagramPacket, so operations on it are possible.
	 * 
	 * @param received_packet
	 */
	public Message(DatagramPacket received_packet){
		packet = received_packet;
		data = packet.getData();
		read_data();
	}
	
	/**
	 * Returns the data in the message, after concatenating all the elements.
	 * The function also prints all the data in Hex.
	 * 
	 * @return 
	 */
	public byte[] get_data(){
		concatenate_message();
		return data;
	}
	
	public void hexdump(){
		System.out.println("Contents of " + type_of_message() + ": ");
		System.out.println(toHex(data));
		System.out.println("---------");
	}

	/**
	 * Reads out the data into the protocols' chunks.
	 */
	private void read_data(){
		op = Arrays.copyOfRange(data, 0, 1);
		htype = Arrays.copyOfRange(data, 1, 2);
		hlen = Arrays.copyOfRange(data, 2, 3);
		hops = Arrays.copyOfRange(data, 3, 4);
		xid = Arrays.copyOfRange(data, 4, 8);
		secs = Arrays.copyOfRange(data, 8, 10);
		flags = Arrays.copyOfRange(data, 10, 12);
		ciaddr = Arrays.copyOfRange(data, 12, 16);
		yiaddr = Arrays.copyOfRange(data, 16, 20);
		siaddr = Arrays.copyOfRange(data, 20, 24);
		giaddr = Arrays.copyOfRange(data, 24, 28);
		chaddr = Arrays.copyOfRange(data, 28, 44);
		sname = Arrays.copyOfRange(data, 44, 108);
		file = Arrays.copyOfRange(data, 108, 236);
		magic_cookie = Arrays.copyOfRange(data, 236,240);
		options = Arrays.copyOfRange(data, 240, data.length);
	}
	

	/**
	 * Create a Message based on the type of message you want to send.
	 * 
	 * @param type
	 */
	public Message(String type){
		if (type == "DHCPDISCOVER"){	//REQUEST
			set_op("Client");
			flags = new byte[] {(byte) 0x80,0};
			set_option53(1);
			
		}
		if (type == "DHCPOFFER"){		//REPLY
			set_op("Server");
			set_option53(2);
		}
		if (type == "DHCPREQUEST"){		//REQUEST
			set_op("Client");
			set_option53(3);
		}
		if (type == "DHCPACK"){			//REPLY
			set_op("Server");
			set_option53(5);
		}
		if (type == "DHCPNAK"){			//REPLY
			set_op("Server");
			set_option53(6);
		}
		if (type == "DHCPRELEASE"){		//REQUEST
			set_op("Client");
			set_option53(7);
		}
		
	}
	
	/**
	 * Sets option 53 to an int. This option stores what kind of DHCP message is sent.
	 * @param type_int
	 */
	private void set_option53(int type_int) {
		byte[] option_53 = {53, 1, (byte) type_int};
		options = option_53;
	}
	
	/**
	 * Concatenates all the message segments into one big data byte array.
	 */
	private void concatenate_message() {
		byte[] message = op;
		message = concatenate_two(message,htype);
		message = concatenate_two(message,hlen);
		message = concatenate_two(message,hops);
		message = concatenate_two(message,xid);
		message = concatenate_two(message,secs);
		message = concatenate_two(message,flags);
		message = concatenate_two(message,ciaddr);
		message = concatenate_two(message,yiaddr);
		message = concatenate_two(message,siaddr);
		message = concatenate_two(message,giaddr);
		message = concatenate_two(message,chaddr);
		message = concatenate_two(message,sname);
		message = concatenate_two(message,file);
		message = concatenate_two(message,magic_cookie);
		message = concatenate_two(message,options);
		data = message;
	}
	
	/**
	 * Concatenates two byte arrays and returns the result.
	 */
	private byte[] concatenate_two(byte[] a, byte[] b){
		byte[] c = new byte[a.length + b.length];
		System.arraycopy(a, 0, c, 0, a.length);
		System.arraycopy(b, 0, c, a.length, b.length);
		return c;
	}

	/**
	 * This function returns whether the message was sent from a server or client.
	 * 
	 * @return a String "Client" or "Server", else null.
	 * 
	 */
	public String get_optype(){
		if (toHex(op).equals("01")){
			return "Client";
		}
		else if(toHex(op).equals("02")){
			return "Server";
		}
		else{
			return null;
		}
	}

	/**
	 * This function sets the opcode of the message to the reply/request or server/client option.
	 * 
	 * @param type is a string containing "Server" or "Client"
	 */
	private void set_op(String type){
		if (type == "Server"){
			op = new byte[] {2};
		}
		else if (type == "Client"){
			op = new byte[] {1};
		}
		
		else throw new IllegalArgumentException();
	}
	
	/**
	 * Returns the value of option 53, which defines the type of DHCP message.
	 */
	private int get_option53(){
		for (int index = 0; index < options.length; index++){
			if ( ((options[index] & 0xff) == 53) && ((options[index+1] & 0xff) == 1)){
				return options[index + 2] & 0xff;
			}
		}
		System.out.println("Option 53 not found!");
		return 0;
	}
	
	/**
	 * Returns the leasetime that is given after an OFFER or ACK.
	 * 
	 * @return the bytearray with the leasetime if there is an option 51, otherwise null.
	 */
	public byte[] get_leasetime(){
		for (int index = 0; index < options.length; index++){
			if ( ((options[index] & 0xff) == 51) && ((options[index+1] & 0xff) == 4)){
				System.out.println("Time for lease: "+toHex(Arrays.copyOfRange(options, index+2, index+6)));
				return Arrays.copyOfRange(options, index+2, index+6);
			}
		}
		System.out.println("Option 51 not found!");
		return null;
	}
	
	
	/**
	 * Returns what kind of message the client has sent.
	 * 
	 * @return a string in upper case, containing the kind of DHCP message.
	 */
	public String type_of_message() {
		int type_int = get_option53();
		if (type_int == 1){
			return "DHCPDISCOVER";
		}
		if (type_int == 2){
			return "DHCPOFFER";
		}
		if (type_int == 3){
			return "DHCPREQUEST";
		}
		if (type_int == 5){
			return "DHCPACK";
		}
		if (type_int == 6){
			return "DHCPNAK";
		}
		if (type_int == 7){
			return "DHCPRELEASE";
		}
		else{
			return null;
		}
	}

	/**
	 * Returns the address where the message is sent from.
	 * 
	 * @return the InetAddress.
	 */
	public InetAddress from(){
		return packet.getAddress();
	}
	
	/**
	 * Convert a bytearray into a string in hexadecimals.
	 * @param bytes
	 * @return
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
	
	/**
	 * Returns the xid field in a Hex value.
	 */
	public String getTransactionID(){
		return toHex(xid);
	}
	
	/**
	 * Returns the xid field byte.
	 */
	public byte[] get_raw_transaction_id(){
		return xid;
	}
	
	/**
	 * Prepares a message if it needs to be sent by a client.
	 * In that case, the xid, ciaddr and chaddr are set to the appropriate values.
	 * Therefore the transaction ID (client_id),
	 * client IP and MAC address of the client need to be given.
	 * 
	 * @param client_id
	 * @param client_IP
	 * @param MACAddress
	 */
	public void client(Integer client_id, InetAddress client_IP, byte[] MACAddress) {
		xid = int_to_byte_array(client_id, xid.length);
		ciaddr = client_IP.getAddress();
		chaddr = MACAddress;
		secs = new byte[] {0,0};
	}
	
	/**
	 * This function prepares a message that needs to be sent by a server.
	 * It sets the xid and chaddr fields using the given parameters.
	 * 
	 * @param MAC
	 * @param transaction_id
	 */
	public void server(byte[] MAC, byte[] transaction_id){
		setTransactionID(transaction_id);
		setMAC(MAC);
	}
	
	/**
	 * This function returns the chaddr field.
	 */
	public byte[] getMacAddress(){
		return chaddr;
	}
	
	/**
	 * This function sets the chaddr field to the given byte array.
	 * @param MACAddress
	 */
	private void setMAC(byte[] MACAddress){
		chaddr = MACAddress;
	}
	
	/**
	 * This function converts an int to a byte array with the given length.
	 * 
	 * @param a
	 * @param length
	 * 
	 * @return the integer.
	 */
	public byte[] int_to_byte_array(Integer a, Integer length){
		return ByteBuffer.allocate(length).putInt(a).array();
	}

	/**
	 * This function sets the xid to the given byte array.
	 * @param transaction_id
	 */
	public void setTransactionID(byte[] transaction_id) {
		xid = transaction_id;
		
	}
	
	/**
	 * This function sets the yiaddr field to the given IP address,
	 * in byte[] format.
	 * 
	 * @param IP_address
	 */
	public void offerIP(byte[] IP_address) {
		yiaddr = IP_address;
	}
	
	/**
	 * This method sets option 51 (leasetime) to the given integer.
	 * The integer denotes the amount of seconds the IP will be valid
	 * for.
	 * 
	 * @param time
	 */
	public void leaseTime(int time) {
		set_option51(int_to_byte_array(time, 4));
	}
	
	/**
	 * This function sets option 54 to the given address.
	 * Option 54 stores the server IP to which a client
	 * should answer.
	 * 
	 * @param server_address
	 */
	public void setServerIP(byte[] server_address){
		byte[] option_54 = {54, 4};
		option_54 = concatenate_two(option_54, server_address);
		options = concatenate_two(options, option_54);
	}
	
	/**
	 * This function returns the address stored in option 54.
	 * 
	 * @return This server address is returned in the form of a byte array,
	 * or null if option 54 is not found.
	 */
	public byte[] get_option54(){
		for (int index = 0; index < options.length; index++){
			if ( ((options[index] & 0xff) == 54) && ((options[index+1] & 0xff) == 4)){
				return Arrays.copyOfRange(options, index+2, index+6);
			}
		}
		System.out.println("Option 54 not found!");
		return null;
	}
	
	/**
	 * This function sets option 51 to the given byte array.
	 * Option 51 keeps strack of the leasetime of an IP address.
	 * 
	 * @param time
	 */
	private void set_option51(byte[] time) {
		byte[] option_51 = {51, 4};
		option_51 = concatenate_two(option_51,time);
		options = concatenate_two(options, option_51);
	}
	
	/**
	 * This function returns the yiaddr field as a byte array, in which an offered
	 * IP address is stored.
	 * 
	 * @return
	 */
	public byte[] offered(){
		return yiaddr;
	}
	
	/**
	 * This function returns the yiaddr field as a string, in which an offered
	 * IP address is stored.
	 * 
	 * @return
	 */
	public String offeredString(){
		return toHex(yiaddr);
	}
	
	/**
	 * This function stores a given IP address in option 50, so that it
	 * can be used in a DHCPREQUEST.
	 * 
	 * @param IP_address
	 */
	public void request(byte[] IP_address){
		byte[] option_50 = {50, 4};
		option_50 = concatenate_two(option_50,IP_address);
		options = concatenate_two(options, option_50);
	}
	
	/**
	 * This method returns the address stored in option 50.
	 * This address is a requested address by a client.
	 * Therefore this function should typically be called by
	 * a server after receiving a DHCPREQUEST.
	 * 
	 * @return
	 */
	public byte[] get_option50(){
		for (int index = 0; index < options.length; index++){
			if ( ((options[index] & 0xff) == 50) && ((options[index+1] & 0xff) == 4)){
				return Arrays.copyOfRange(options, index+2, index+6);
			}
		}
		System.out.println("Option 50 not found!");
		return null;
	}
}
