// By Linh Hoang, University of Texas at Dallas
// April, 2017

import java.io.*;
import java.util.Scanner;
import java.net.*;

public class SocketClient
{
	Socket socket 					= null;
	static PrintWriter out 			= null;
	static BufferedReader in 		= null;
	static Scanner sc 				= new Scanner(System.in);
	final static int MAX_LENGTH 	= 80;
	static String recipient_name;		
	public void communicate()
	{
		
		// Login State
		String name= "";
		System.out.print("Enter your name : ");		// Enter the name 
		while(true){			
			name 			= sc.nextLine();		// Get User's name
			sendServer(name);						// Send user name to Server to check
			String status 	= readServer();			// Receive status from Server
			
			// Check Validity of the User's name
			if (status.equals("VALID")){
				break;
			}else if (status.equals("INVALID")){
				System.out.print("User name is connected, enter a new name: ");		// Existing user, enter again
			}else if (status.equals("FULL")){
				System.out.print("Number of user reached limit, please login with an existing name: "); 
			}else{
				break;
			}
		}
		// ---------------------------------------------------------------------------------------------------------
		
		// Menu Choice
		String choice ="";		
		outerloop:
		while(true){
			displayChoice();			// Display menu
			innerloop:
			while(true){
				choice = sc.nextLine();	// Take the user's choice			
				switch (choice){
				case "1":
					System.out.println("Known Users:");
					sendData(choice);
					displayInfo(choice);
					break innerloop;
				case "2":
					System.out.println("Connected Users:");
					sendData(choice);
					displayInfo(choice);
					break innerloop;
				case "3":
					sendData(choice);
					displayInfo(choice);
					break innerloop;
				case "4":
					sendData(choice);
					displayInfo(choice);
					break innerloop;	
				case "5":
					sendData(choice);
					displayInfo(choice);
					break innerloop;
				case "6":
					System.out.println("Your Messages:");
					sendData(choice);
					displayInfo(choice);
					break innerloop;
				case "7":
					sendData(choice);
					break outerloop;
				default:
					System.out.print("Invalid choice (only 1~7), Enter Your Choice: ");
				}	
			}
		}
		// -------------------------------------------------------------------------------
		sc.close();
	}
	
	// Check STT of message and user limit
	public boolean readStatus(){
		String st = readServer();
		if (st.equals("FULL")){
			return false;
		}else{
			return true;
		}
	}
	
	// Read a single message from Server
	public String readServer(){
		try {
			String st = in.readLine();
//			System.out.println("Incomming data: "+st);
			return (st);
		} catch (NumberFormatException | IOException e) {
			return null;
		}	

	}
	
	// Send a single message to Server
	public static void sendServer(String stt){
//		System.out.println("Out data: "+stt);
		out.println(stt);
	}
	
	// Read multiple lines from Server and print out formatted string
	public void displayInfo(String choice){
		String message;	
		int i = 0;
		int n = Integer.parseInt(choice);
		
		while(true){
			message = readServer();
			if (message.equals("stop"))
				break;
			else{
				if (n == 1 || n == 2){
					System.out.println("\t"+(i+1)+". "+message);								// Print out Users
				}else if (n == 6){
					if (message.equals("empty")){
						System.out.println("Empty Inbox");
						readServer();
						break;
					}else{
						String name 	= message;
						String date 	= readServer();
						String inbox	= readServer();
						System.out.println("\t"+(i+1)+". From "+name+", "+date+", "+inbox);		// Print out Messages
						
					}				
				}else{	// 3,4,5
					if (message.equals("OK")){
						status_OK(choice);					
					}else if (message.equals("FULL")){
						status_FULL(choice);
					}
				}				
			}
				
			i++;
		}	
	}
	
	// Handle status OK
	public void status_OK(String choice){
		if (choice.equals("3"))
			System.out.println("Message posted to "+recipient_name);
		if (choice.equals("4"))
			System.out.println("Message posted to all connected users");
		if (choice.equals("5"))
			System.out.println("Message posted to all known users");
	}
	
	// Handle status FULL
	public void status_FULL(String choice){
		String type = readServer();
		
		if (type.equals("M")){
			String user_name = readServer();
			System.out.println("Message posted to SOME users");
			System.out.println("Delivery to "+user_name+" fail because user's inbox is FULL");
		}
			
		if (type.equals("U")){
			System.out.println("Delivery to fail because number of user reaches limit");
		}
	}

	// Send information to Server
	public void sendData(String choice){
		String name 	= "";
		String message 	= "";
		int n = Integer.parseInt(choice);
		
		if ( n != 1 && n != 2 && n != 6 && n != 7){
			if (n==3){
				System.out.print("Enter recipient's name: ");
				name = sc.nextLine()+"\n";
				recipient_name = name;
			}
			System.out.print("Enter a message: ");
			String st ="";
			while(true){
				st = sc.nextLine();				
				if (st.length() <= MAX_LENGTH){
					break;
				}else{
					System.out.print("Your message is too long, enter a message again: ");
				}
			}
			message = choice+"\n"+name+st;
			sendServer(message);
			
		}else{
			sendServer(choice);
		}
	}
	
	
	public void displayChoice(){
		System.out.print("\n\n1. Display the names of all known users.\n"
				+ "2. Display the names of all currently connected users.\n"
				+ "3. Send a text message to a particular user.\n"
				+ "4. Send a text message to all currently connected users.\n"
				+ "5. Send a text message to all known users.\n"
				+ "6. Get my messages.\n"
				+ "7. Exit.\n"
				+ "Enter your choice: ");
	}
	public void listenSocket(String host, int port)
	{
		//Create socket connection
		try
		{
			socket = new Socket(host, port);
			out = new PrintWriter(socket.getOutputStream(), true);
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		} 
		catch (UnknownHostException e) 
		{
			System.out.println("Unknown host");
			System.exit(1);
		} 
		catch (IOException e) 
		{
			System.out.println("No I/O");
			System.exit(1);
		}
		
        String hostname = "unknown";
        try {

            InetAddress address;
            address = InetAddress.getLocalHost();
            hostname = address.getHostName();

        } catch(UnknownHostException ex) {

            System.out.println("Hostname can not be resolved");
            ex.printStackTrace();

        }
        System.out.println("Connecting to " + hostname + " : " + port + "\n");		
	}

	public static void main(String[] args)
	{
		if (args.length != 2)
		{
			System.out.println("Usage:  client hostname port");
			System.exit(1);
		}

		SocketClient client = new SocketClient();

		String host = args[0];
		int port = Integer.valueOf(args[1]);
		client.listenSocket(host, port);
		client.communicate();
	}
}
