import java.io.*;
import java.net.Socket;
import java.text.*;
import java.util.*;
import java.util.concurrent.Semaphore;

public class ClientWorker implements Runnable {

	private Socket client;
	private static ArrayList<User> userList; 	//Create a new User arraylist
	private User newUser = new User(); 			//Create a new customer referencing the user corresponding to the current client.
	private String status = "known"; 			// Create a variable to represent the client is a known client.
	final static int MAX_USER = 100;			// Number of maximum users
	final static int MAX_MESS = 10;				// Number of maximum messages
	BufferedReader in = null;
	PrintWriter out = null;

	ClientWorker(Socket client, ArrayList<User> userList)
	{
		this.client = client;
		this.userList = userList;
	}

	public void run() {

		String line;

		try {

			in = new BufferedReader(new InputStreamReader(client.getInputStream()));
			out = new PrintWriter(client.getOutputStream(), true);

		} catch (IOException e) {

			System.out.println("in or out failed");
			System.exit(-1);

		}

		// Get the object USER in ArrayList UserList based on name of user
		newUser = userConnectionCheck(in, out, newUser, status);


		try {

			while ((line = in.readLine()) != null) {

				if (line.equals("1")) {

					checkAllKnownUsers(line, null); // check user list and print out all known users.


				} else if (line.equals("2")) {

					checkAllConnectedUsers(line, null); // check user list, find all connected users and print out them.


				} else if (line.equals("3")) {

					String name = in.readLine(); // read in the user's name needed to send message too.
					String message = in.readLine(); // read in the message that will send to the particular user.
					String time = getTime(); // get current time.
					String sendMessage = newUser.name + "\n" + time + "\n" + message;

					//                    check whether the person sending message to is a known user or not.
					//                    If he's unknown, add him to the user list.
					int index = checkAUserStatus(name);
					updateUser(index, name, sendMessage);


				} else if (line.equals("4")) {

					String message = in.readLine();
					checkAllConnectedUsers(line, message); // check user list, find all connected users and send message to all connected users.


				} else if (line.equals("5")) {

					//                    String time = getTime();
					String message = in.readLine(); // read in the message needs to be send.
					checkAllKnownUsers(line, message); // check user list, find all known users and send message to all known users.

					//                    checkAllKnownUsers(line, newUser.name + "\n" + time + "\n" + message);

				} else if (line.equals("6")) { // check current users messageList, if it's empty, print out "empty". if there are messages, print out all the messages

					if(newUser.messageList.size() == 0) {

						out.println("empty");

					} else {

						int i = 0;
						while (i < newUser.messageList.size()) {

							out.println(newUser.messageList.get(i));
							i++;

						}

					}

					System.out.println(getTime() + ", " + newUser.name + " gets messages.");

				} else if (line.equals("7")) { // change current user's connection status as disconnected and print out message on server side.

					newUser.setConnected(false);
					System.out.println(getTime() + ", " + newUser.name + " exits.");
					exitClient();
					break;

				} 

				out.println("stop"); // for each choice, after sending out all the messages, send a "stop" signal to client.

			}

			if (in.read() == -1) { // if client crashes, change his status as disconnected.
				
				newUser.connected = false;
				
			}
		} catch (IOException e) {
			System.out.println("Read failed");
			newUser.connected = false;
		}
	}

	// Check whether the name provide by the client is a valid name. First, check whether it is a user in user list. If it is, whether it is connected, 
	// if it is connected, then print out "invalid", otherwise print out "valid".
	// Second, if it is not in the user list and the user list is not full, add the user to the user list and print out "valid". Otherwise, print out "invalid".   
	
	public static synchronized User userConnectionCheck (BufferedReader in, PrintWriter out, User user, String status) {
		int index;
		String line = new String();

		while (true) {

			try {

				line = in.readLine();

			} catch (IOException e) {

				System.out.println("Read failed");
				System.exit(-1);

			}

			index = checkAUserStatus(line); // check whether the user is in the user list. If it is, get a nonnegative index value. If not, get -1 for index.
			if (index == -1) { // the user given is not in the user list

				boolean added = false;
				user.setName(line);
				added = addUser(user, out); // check whether the user can be added to user list
				if (added) { // the user is successfully added in user list.
					out.println("VALID");
					break;
				}



			} else { // user is in the user list

				if(!checkAUserConnection(index)) { // if the user is not connected, print out "VALID"

					out.println("VALID");
					user = userList.get(index);
					break;

				} else {

					out.println("INVALID");

				}

			}
		}

		System.out.println(getTime() + ", " + "Connection by " + user.status + " user " + line);
		user.setStatus(status);
		user.setConnected(true);
		return user;

	}

	//     get the current time
	public static String getTime(){
		DateFormat df = new SimpleDateFormat("MM/dd/yy, H:mm a");
		Date dateobj = new Date();
		String time = df.format(dateobj);
		return time;
	}

	//    check whether the userList is full, if not, add the user to it
	//    otherwise send a message back to client.
	public synchronized static boolean addUser(User currentUser, PrintWriter out) {

		boolean added = false;
		if (userList.size() < MAX_USER) {

			userList.add(currentUser);
			added = true;

		}else {

			out.println("FULL\nU");
		}

		return added;

	}

	//    check whether a user's messageList is full, if it is not full, add a message to it
	//    if it is full, send a message back to the client
	public synchronized static boolean storeMessage(User user, String message, PrintWriter out) {

		if (user.messageList.size() < MAX_MESS) {

			user.setMessage(message);
			return true;

		} else {

			return false;

		}

	}

	//    check userList to find the user with the same name provided and send the index of the user back.
	public static int checkAUserStatus(String name) {

		int index = -1;

		for(int i = 0; i < userList.size(); i++) {

			if(userList.get(i).name.equals(name)) {
				index = i;
				break;
			}

		}

		return index;

	}

	//    check one of the user from the userList to see whether it is connected and return the connection status back.
	public static boolean checkAUserConnection(int index) {

		boolean connection = false;

		if (userList.get(index).connected) {

			connection = true;

		}

		return connection;

	}


	//    Check userList to find all known users. Then if the client chose "1", just print out all the known user names
	//    If the client chose "5", save a message to all known users.
	public void checkAllKnownUsers (String choice, String message) {
		int i = 0;
		boolean status = true;
		while(i < userList.size()) {


			if(choice.equals("1")) {

				out.println(userList.get(i).name);

			} else if (choice.equals("5")){

				if (!userList.get(i).name.equals(newUser.name)) {

					String time = getTime();
					String sendMessage = newUser.name + "\n" + time + "\n" + message;
					boolean saved = storeMessage(userList.get(i), sendMessage, out);
					if(!saved) {
						out.println("FULL\nM\n"+userList.get(i).name);
						System.out.println(userList.get(i).name + "'s message is full and can't be stored.");
						status = false;

					}
				}
			}

			i++;
		}

		if(choice.equals("1")) {
			System.out.println(getTime() + ", " + newUser.name + " displays all known users.");
		} else if (choice.equals("5")){
			if (status){
				System.out.println(getTime() + ", " + newUser.name + " posts a message for all currently known users.");
				out.println("OK");	
			}	
		}
	}

	//    check userList to fined all connected users. Then if the client chose option "2", print out all connected user names
	//    If the client chose "4", add a message to all connected users.
	public void checkAllConnectedUsers(String choice, String message) {

		int i = 0;
		boolean status = true;
		while(i < userList.size()) {

			if(userList.get(i).connected) {

				if(choice.equals("2")) {

					out.println(userList.get(i).name);

				} else if (choice.equals("4")){

					if (!userList.get(i).name.equals(newUser.name)) {

						String time = getTime();
						String sendMessage = newUser.name + "\n" + time + "\n" + message;
						boolean saved = storeMessage(userList.get(i), sendMessage, out);
						if(!saved) {
							out.println("FULL\nM\n"+userList.get(i).name);
							System.out.println(userList.get(i).name + "'s message is full and can't be stored.");
							status = false;

						}

					}


				}

			}

			i++;

		}

		if(choice.equals("2")) {

			System.out.println(getTime() + ", " + newUser.name + " displays all connected users.");

		} else if (choice.equals("4")){
			if (status){
				System.out.println(getTime() + ", " + newUser.name + " posts a message for all currently connected users.");
				out.println("OK");	
			}

		}

	}

	//    Check whether it's a new user, if it is, add him to the user List, otherwise get the
	//    user from the user list.
	public void updateUser (int index, String name, String message) {

		User user = new User();
		if (index == -1) {

			boolean added;
			user.setName(name);
			user.setStatus(status);
			added = addUser(user, out);
			if (added) {
				saveOneUserMessage(user, message);         
			}
		} else {
			user = userList.get(index);
			saveOneUserMessage(user, message);

		}

	}
	
	// Save message to User's messageList
	public void saveOneUserMessage (User user, String sendMessage){

		boolean saved = storeMessage(user, sendMessage, out);

		if (saved) {
			out.println("OK");
			System.out.println(getTime() + ", " + newUser.name + " posts a message for " + user.name + ".");

		} else {
			out.println("FULL\nM\n"+user.name);

			System.out.println(getTime() + ", " + newUser.name + " failed to post a message for " + user.name + " because of user's message limit.");

		}
	}

	//    close the client socket.
	public void exitClient() {
		try
		{
			client.close();
		}
		catch (IOException e)
		{
			System.out.println("Close failed");
			System.exit(-1);
		}
	}

}
