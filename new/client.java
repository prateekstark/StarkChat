import java.io.*;
import java.net.*;
import java.util.*;


class TCPClient{
	public static boolean registerToServer(DataOutputStream outToServer, BufferedReader inFromServer, String username) throws Exception{
		int registrationLevel = 0;
		String serverResponse;
		if(registrationLevel == 0){
			String registrationRequestToSend = "REGISTER"
		}
	}
	public static void main(String argv[]) throws Exception{
		BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
		Socket clientSocket1 = new Socket("localhost", 6789);
		Socket clientSocket2 = new Socket("localhost", 6789);
		DataOutputStream outToServer1 = new DataOutputStream(clientSocket1.getOutputStream());
		BufferedReader inFromServer1 = new BufferedReader(new InputStreamReader(clientSocket1.getInputStream()));
		DataOutputStream outToServer2 = new DataOutputStream(clientSocket1.getOutputStream());
		BufferedReader inFromServer2 = new BufferedReader(new InputStreamReader(clientSocket1.getInputStream()));
		
		System.out.println("Enter your username: ");
		String username = inFromUser.readLine();
		String sentence, modifiedSentence, message;
		boolean connectionStatus1 = registerToServer(outToServer1, inFromServer1, username);
		boolean connectionStatus2 = registerToServer(outToServer2, inFromServer2, username);

		outToServer1 = new DataOutputStream(clientSocket1.getOutputStream());
		inFromServer1 = new BufferedReader(new InputStreamReader(clientSocket1.getInputStream()));
		outToServer2 = new DataOutputStream(clientSocket1.getOutputStream());
		inFromServer2 = new BufferedReader(new InputStreamReader(clientSocket1.getInputStream()));
		while(connectionStatus1 && connectionStatus2){
			sentence = inFromUser.readLine();
			outToServer2.writeBytes(sentence + "\n");
			modifiedSentence = inFromServer1.readLine();
			System.out.println(modifiedSentence);
		}
		System.out.println("Connection Closed");
	}	


}
