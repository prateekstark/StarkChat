import java.io.*;
import java.net.*;
import java.util.*;
class TCPClient{
	public static void main(String argv[]) throws Exception{
		BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
		Socket clientSocket = new Socket("localhost", 6789);
		DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
		BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
		// System.out.println("Enter your username: ");
		// String username = inFromUser.readLine();
		String sentence;
		String modifiedSentence;
		String message = "";
		String messageBody = "USERNAME:" + username + "\n" + "MESSAGE:";
		while(true){
			sentence = inFromUser.readLine();
			message = messageBody + sentence + "\n";
			outToServer.writeBytes(message);
			modifiedSentence = inFromServer.readLine();
			System.out.println(modifiedSentence);
		}
	}
}