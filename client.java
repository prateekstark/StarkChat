import java.io.*;
import java.net.*;
import java.util.*;

class TCPClient{
	public static BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
	public static boolean registerToSend(DataOutputStream outToServer, BufferedReader inFromServer, String username) throws Exception{
		String serverResponse;
		String registrationRequestToSend = "REGISTER TOSEND" + " " + username + "\n";
		outToServer.writeBytes(registrationRequestToSend + "\n");
		serverResponse = inFromServer.readLine();
		if(serverResponse.equals("")){
			serverResponse = inFromServer.readLine();
		}
		if(serverResponse.indexOf("REGISTERED") != -1){
			return true;
		}
		if(serverResponse.indexOf("ERROR") != -1){
			if(serverResponse.indexOf("100") != -1){
				System.out.println("Malformed Username" + "\n");
			}
			else if(serverResponse.indexOf("102") != -1){
				System.out.println("Username already present" + "\n");
			}
		}
		return false;
	}

	public static boolean registerToReceive(DataOutputStream outToServer, BufferedReader inFromServer, String username) throws Exception{
		String serverResponse;
		String registrationRequestToReceive = "REGISTER TORECV" + " " + username + "\n";
		outToServer.writeBytes(registrationRequestToReceive + "\n");
		serverResponse = inFromServer.readLine();
		if(serverResponse.equals("")){
			serverResponse = inFromServer.readLine();
		}
		if(serverResponse.indexOf("REGISTERED") != -1){
			return true;
		}
		if(serverResponse.indexOf("ERROR") != -1){
			if(serverResponse.indexOf("100") != -1){
				System.out.println("Malformed Username" + "\n");
			}
		}
		return false;
	}

	public static void main(String argv[]) throws Exception{
		Socket clientSocket1 = new Socket("localhost", 6789);
		Socket clientSocket2 = new Socket("localhost", 6789);
		DataOutputStream outToServer1 = new DataOutputStream(clientSocket1.getOutputStream());
		BufferedReader inFromServer1 = new BufferedReader(new InputStreamReader(clientSocket1.getInputStream()));
		DataOutputStream outToServer2 = new DataOutputStream(clientSocket2.getOutputStream());
		BufferedReader inFromServer2 = new BufferedReader(new InputStreamReader(clientSocket2.getInputStream()));
		System.out.println("Enter your username: ");
		String username = inFromUser.readLine();
		String sentence, modifiedSentence, message;
		boolean connectionStatus1 = registerToSend(outToServer1, inFromServer1, username);
		boolean connectionStatus2 = registerToReceive(outToServer2, inFromServer2, username);
		SocketThreadClient socketThread1 = new SocketThreadClient(clientSocket1, inFromServer1, outToServer1, 1);
		SocketThreadClient socketThread2 = new SocketThreadClient(clientSocket2, inFromServer2, outToServer2, 2);
		Thread thread1 = new Thread(socketThread1);
		Thread thread2 = new Thread(socketThread2);
		thread1.start();
		thread2.start();
	}
}



class SocketThreadClient extends TCPClient implements Runnable{
	Socket connectionSocket;
	BufferedReader inFromServer;
	DataOutputStream outToServer;
	int mode;
	
	SocketThreadClient(Socket connectionSocket, BufferedReader inFromServer, DataOutputStream outToServer, int mode){
		this.connectionSocket = connectionSocket;
		this.inFromServer = inFromServer;
		this.outToServer = outToServer;
		this.mode = mode;
	}

	public void senderNode() throws Exception{
		String sentence;
		String modifiedSentence;
		while(true){
			sentence = inFromUser.readLine();
			if(sentence.charAt(0) == '@'){
				int tempIndex = sentence.indexOf(' ');
				String toSend = sentence.substring(1,tempIndex);
				String message = sentence.substring(tempIndex+1, sentence.length());
				int messageLength = message.length();
				String toSendInfo = "SEND " + toSend + "\n";
				String contentLength = "Content-length: " + Integer.toString(messageLength)  + "\n";
				outToServer.writeBytes(toSendInfo + contentLength + "\n" + message + "\n\n");
			}
			else{
				System.out.println("Wrong input!");
				continue;
			}
			modifiedSentence = inFromServer.readLine();
			if(modifiedSentence.equals("")){
				modifiedSentence = inFromServer.readLine();
			}
			if(modifiedSentence.indexOf("SENT") != -1){
				continue;
			}
			else if(modifiedSentence.indexOf("ERROR") != -1){
				if(modifiedSentence.indexOf("103") != -1){
					System.out.println("Unable to send");

				}
				if(modifiedSentence.indexOf("104") != -1){
					System.out.println("Header Incomplete");
				}
			}
			else{
				System.out.println("UNKNOWN ERROR");
			}
		}
	}
	public void receiverNode() throws Exception{
		String sentence;
		String senderName;
		int index; 
		while(true){
			try{
				sentence = inFromServer.readLine();
				if(sentence.equals("")){
					sentence = inFromServer.readLine();
				}
				index = sentence.indexOf("FORWARD");
				if(index != -1){
					senderName = sentence.substring(sentence.indexOf(" ") + 1, sentence.length());
					String contentLengthString = inFromServer.readLine();
					int contentLength = Integer.parseInt(contentLengthString.substring(contentLengthString.indexOf(" ") + 1, contentLengthString.length()));
					String message = inFromServer.readLine();
					if(message.equals("")){
						message = inFromServer.readLine();
					}
					System.out.println(senderName +": " + message);
					String confirmationString = "RECEIVED " + senderName + "\n";
					outToServer.writeBytes(confirmationString+"\n");
					continue;
				}
			}
			catch(Exception e){
				String errorString = "ERROR 104 Header Incomplete" + "\n";
				outToServer.writeBytes(errorString);
				continue;
			}
		}
	}
	
	public void run(){
		if(mode==1){
			try{
				senderNode();	
			}
			catch(Exception e){
				System.out.println(e);
			}
		}
		else{
			try{
				receiverNode();	
			}
			catch(Exception e){
				System.out.println(e);
			}
			
		}
	}
}
