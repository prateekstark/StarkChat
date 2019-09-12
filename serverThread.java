import java.io.*;
import java.net.*;
import java.util.*;

class TCPServer{
	public static ClientMap clientData = new ClientMap();
	public boolean isUserNameValid(String username){
		for(int i=0;i<username.length();i++){
			if(username.charAt(i) == ' ' || username.charAt(i) == '/' || username.charAt(i) == ':'){
				return false;
			}
		}
		return true;
	}

	public static void main(String argv[]) throws Exception{
		ServerSocket welcomeSocket = new ServerSocket(6789);
		while(true){
			Socket connectionSocket = welcomeSocket.accept();
			BufferedReader inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
			DataOutputStream outToClient = new DataOutputStream(connectionSocket.getOutputStream());
			SocketThread socketThread = new SocketThread(connectionSocket, inFromClient, outToClient);
			Thread thread = new Thread(socketThread);
			thread.start();
		}
	}
}

class SocketThread extends TCPServer implements Runnable{
	Socket connectionSocket;
	BufferedReader inFromClient;
	DataOutputStream outToClient;
	String username;

	SocketThread(Socket connectionSocket, BufferedReader inFromClient, DataOutputStream outToClient){
		this.connectionSocket = connectionSocket;
		this.inFromClient = inFromClient;
		this.outToClient = outToClient;
	}

	public void run(){
		String clientSentence;
		String capitalizedSentence;
		String clientMessage = "";
		String senderName = "";
		int index;
		boolean isMessageComplete = false;
		while(true){
			try{
				clientSentence = inFromClient.readLine();
				System.out.println(clientSentence);
				if(clientSentence.equals("")){
					clientSentence = inFromClient.readLine();
				}
				index = clientSentence.indexOf("REGISTER");
				if(index != -1){
					index = clientSentence.indexOf("TOSEND");
					if(index != -1){
						senderName = clientSentence.substring(index+7, clientSentence.length());
						this.username = senderName;
						boolean temp = clientData.registrationMap.containsKey(senderName);
						if(temp){
							String errString = "ERROR 102 Username already present";
							outToClient.writeBytes(errString + "\n");
							continue;
						}
						else{
							clientData.registrationMap.put(senderName, 1);
							clientData.inServerMap.put(senderName, inFromClient);
							String confirmationString = "REGISTERED TOSEND " + senderName;
							outToClient.writeBytes(confirmationString + "\n");
							continue;
						}
					}
					else{
						index = clientSentence.indexOf("TORECV");
						if(index != -1){
							senderName = clientSentence.substring(index+7, clientSentence.length());
							boolean temp = clientData.registrationMap.containsKey(senderName);
							if(temp){
								if(clientData.registrationMap.get(senderName) == 1){
									this.username = senderName;
									clientData.registrationMap.remove(senderName);
									clientData.dataMap.put(senderName, outToClient);
									clientData.registrationMap.put(senderName, 2);
									String confirmationString = "REGISTERED TORECV " + senderName;
									outToClient.writeBytes(confirmationString + "\n");
									continue;
								}	
							}
							
							else{
								String confirmationString = "FIRST GET TOSEND REGISTERED " + senderName;
								outToClient.writeBytes(confirmationString + "\n");
								continue;
							}
						}
					}
				}
				
				System.out.println(senderName + ": " + clientSentence);
				index = clientSentence.indexOf("SEND");
				// System.out.println(index);
				if(index != -1){
					String receiverName = clientSentence.substring(5,clientSentence.length());
					senderName = this.username;
					// System.out.println("hi");
					String contentLengthString = inFromClient.readLine();
					System.out.println(contentLengthString);
					int contentLength = Integer.parseInt(contentLengthString.substring(contentLengthString.indexOf(" ") + 1, contentLengthString.length()));
					System.out.println(contentLength);
					String a = inFromClient.readLine();
					String message = inFromClient.readLine() + "\n";
					System.out.println("Message is " + message);
					System.out.println("I was here");
					System.out.println(clientData.registrationMap.containsKey(receiverName));
					// for (Map.Entry<String, Integer> entry : clientData.registrationMap.entrySet()){
 				// 	   System.out.println(entry.getKey()+" : "+entry.getValue());
					// }
					// int registrationLevel = clientData.registrationMap.get(receiverName);
					// System.out.println("hello " + registrationLevel);
					if(clientData.registrationMap.get(receiverName) == 2){
						System.out.println("Receiver is present");
						String sender_info = "FORWARD " + senderName + "\n";
						DataOutputStream outPort = clientData.dataMap.get(receiverName);
						System.out.println("my mesage: " + message);
						System.out.println("I am here");
						outPort.writeBytes(sender_info + contentLengthString + "\n" + "\n" + message + "\n");
						String clientResponse = clientData.inServerMap.get(receiverName).readLine() + "\n";
						if(clientResponse.indexOf("RECEIVED") != -1){
							String confirmSenderString = "SENT" + receiverName + "\n";
							outToClient.writeBytes(confirmSenderString + "\n");
							continue;
						}
						else{
							System.out.println(clientResponse);
							clientData.dataMap.get(senderName).writeBytes(clientResponse + "\n");
							continue;
						}
					}
					else{
						String error_string = "ERROR 102 Unable to send\n";
						clientData.dataMap.get(senderName).writeBytes(error_string);
						continue;
					}
				}
					
			}
			catch(Exception e){
				try{
					connectionSocket.close();
				}
				catch(Exception ee){

				}
				break;
			}
		}
	}
}
