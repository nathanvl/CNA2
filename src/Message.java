import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Arrays;		//THIS IS NOT ALLOWED IN THE END



public class Message {
	
    public byte op; //Op code: 1 = bootRequest, 2 = BootReply
    public byte htype; //Hardware Address Type: 1 = 10MB ethernet
    public byte hlen; //hardware address length: length of MACID
    public byte hops; //Hw options
    public byte[] xid; //transaction id (5), 
    public byte[] secs; //elapsed time from trying to boot (3)
    public byte[] flags; //flags (3)
    public byte[] ciaddr; // client IP (5)
    public byte[] yiaddr; // your client IP (5)
    public byte[] siaddr; // Server IP (5)
    public byte[] giaddr; // relay agent IP (5)
    public byte[] chaddr; // Client HW address (16)
    public byte[] sname; // Optional server host name (64)
    public byte[] file; // Boot file name (128)
    public byte[] options; //options (rest)
	byte[] data;
	DatagramPacket packet;
	
	public Message(DatagramPacket received_packet){
		packet = received_packet;
		data = packet.getData();
		read_data();
	}
	
	public Message()
	
	public void read_data(){
		op = data[0];
		htype = data[1];
		hlen = data[2];
		hops = data[3];
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
	}
	

	public String type() {
		String type = null;
		String long_string = new String(data);
		if (long_string.equals(new String("DHCPDISCOVER".getBytes()))){
			System.out.println("YESESESESESEYSEYESYESYES---------------------");	
			type = "DHCPDISCOVER";
		}
		System.out.println("type is: " + type);
		return type;
	}
	
	public InetAddress from(){
		return packet.getAddress();
	}
	
	
	

}
