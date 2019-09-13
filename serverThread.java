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
		String clientResponse;
		boolean isMessageComplete = false;
		while(true){
			if(clientData.inServerMap.containsValue(inFromClient)){
				break;
			}
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
									clientData.inServerMap.put(senderName, inFromClient);
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
				
				index = clientSentence.indexOf("SEND");
				if(index != -1){
					String receiverName = clientSentence.substring(5,clientSentence.length());
					senderName = this.username;
					String contentLengthString = inFromClient.readLine();
					int contentLength = Integer.parseInt(contentLengthString.substring(contentLengthString.indexOf(" ") + 1, contentLengthString.length()));
					String a = inFromClient.readLine();
					String message = inFromClient.readLine() + "\n";
					if(clientData.registrationMap.get(receiverName) == 2){
						String sender_info = "FORWARD " + senderName + "\n";
						DataOutputStream outPort = clientData.dataMap.get(receiverName);
						BufferedReader inPort = clientData.inServerMap.get(receiverName);
						outPort.writeBytes(sender_info + contentLengthString + "\n" + "\n" + message + "\n");
						clientResponse = inPort.readLine();
						if(clientResponse.equals("")){
							clientResponse = inPort.readLine();
						}
						if(clientResponse.indexOf("RECEIVED") != -1){
							String confirmSenderString = "SENT" + receiverName + "\n";
							outToClient.writeBytes(confirmSenderString + "\n");
							continue;
						}
						else{
							outToClient.writeBytes(clientResponse +"\n" + "\n");
							continue;
						}
					}
					else{
						String error_string = "ERROR 102 Unable to send\n";
						outToClient.writeBytes(error_string);
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