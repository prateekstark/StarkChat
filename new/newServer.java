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

	SocketThread(Socket connectionSocket, BufferedReader inFromClient, DataOutputStream outToClient){
		this.connectionSocket = connectionSocket;
		this.inFromClient = inFromClient;
		this.outToClient = outToClient;
	}

	int isUserPresent(String nameList[], String key){
		if(numClients == 0){
			return -1;
		}
		for(int i=0;i<nameList.length;i++){
			if(nameList[i].equals(key)){
				return i;
			}
		}
		return -1;
	}

	public void run(){
		String clientSentence;
		String capitalizedSentence;
		String clientMessage = "";
		String senderName = "";
		int index;
		int registrationLevel = 0;
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
						senderName = clientSentence.substring(index+6, clientSentence.length());
						int temp = isUserPresent(connectedClients, senderName);
						if(temp != -1){
							if(/*registrationArray[temp] == 1 || */clientData.registrationMap.get(senderName) == 1){
								String confirmationString = "REGISTERED TOSEND " + senderName;
								System.out.println(confirmationString);
								outToClient.writeBytes(confirmationString + "\n");
								continue;	
							}
							else{
								String errString = "ERROR 102 Username already present";
								outToClient.writeBytes(errString + "\n");
								continue;
							}
						}
						else{
							// connectedClients[numClients] = senderName;
							// int t = clientData.registrationMap.get(senderName);
							// clientData.registrationMap.remove(senderName);
							// clientData.registrationMap.put(senderName, t++);
							// numClients++;
							String confirmationString = "REGISTERED TOSEND " + senderName;
							System.out.println(confirmationString);
							outToClient.writeBytes(confirmationString + "\n");
							continue;
						}
					}
					else{
						index = clientSentence.indexOf("TORECV");
						if(index != -1){
							senderName = clientSentence.substring(index+6, clientSentence.length());
							int temp = isUserPresent(connectedClients, senderName);
							if(temp != -1 && clientData.registrationMap.get(senderName) == 1){
								clientData.dataMap.remove(senderName);
								clientData.registrationMap.remove(senderName);
								clientData.dataMap.put(senderName, outToClient);
								clientData.registrationMap.put(senderName, 2);
								String confirmationString = "REGISTERED TORECV " + senderName;
								outToClient.writeBytes(confirmationString + "\n");
							}
							else{
								clientData.registrationMap.put(senderName, 1);
								clientData.dataMap.put(senderName, outToClient);
								String confirmationString = "REGISTERED TORECV " + senderName;
								outToClient.writeBytes(confirmationString + "\n");
								continue;					
							}	
						}
					}
				}


				
				System.out.println(senderName + ": " + clientSentence);
				capitalizedSentence = clientSentence.toUpperCase();
			
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