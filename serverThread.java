import java.io.*;
import java.net.*;







class TCPServer{

	boolean isUserNameValid(String username){
		for(int i=0;i<username.length();i++){
			if(username.charAt(i) == ' ' || username.charAt(i) == '/' || username.charAt(i) == ':'){
				return false;
			}
		}
		return true;
	}


	public static void main(String argv[]) throws Exception{
		//Initializing Welcome Socket with a given port
		ServerSocket welcomeSocket = new ServerSocket(6789);
		BufferedReader inFromClientArray[5] = new BufferedReader()[5];
		DataOutputStream outToClientArray[5] = new DataOutputStream()[5];
		String clientName[5] = new String[5];
		int numClients = 0;
		while(true){
			//Infinite loop until you stop the process
			Socket connectionSocket = welcomeSocket.accept();
			BufferedReader inFromClient[numClients] = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
			DataOutputStream outToClient[numClients] = new DataOutputStream(connectionSocket.getOutputStream());
			String askUserName = "Tell me your name?";
			outToClient.writeBytes(askUserName);
			String userName = inFromClient.readLine();
			if(isUserNameValid){
				clientName[numClients] = userName;
				numClients++;
			}
			SocketThread socketThread = new SocketThread(connectionSocket, inFromClient, outToClient, clientName);
			Thread thread = new Thread(socketThread);
			thread.start();
		}
	}
}

class SocketThread implements Runnable{
	String[] clientName;
	String clientSentence;
	String capitalizedSentence;
	Socket connectionSocket;
	BufferedReader[] inFromClient;
	DataOutputStream[] outToClient;

	SocketThread(Socket connectionSocket, BufferedReader[] inFromClient, DataOutputStream[] outToClient, String[] clientName){
		this.connectionSocket = connectionSocket;
		this.inFromClient = inFromClient;
		this.outToClient = outToClient;
		this.clientName = clientName;
	}

	int isUserPresent(String[] nameList, String key){
		for(int i=0;i<nameList.length;i++){
			if(nameList[i].equals(key)){
				return i;
			}
		}
		return -1;
	}

	public void run(){
		String clientMessage = "";
		String senderName = "";
		int index;
		boolean isMessageComplete = false;
		// String clientName[5];
		
		
		while(true){
			try{
				clientSentence = inFromClient.readLine();
				index = clientSentence.indexOf("USERNAME:");
				if(index != -1){
					senderName = clientSentence.substring(9,clientSentence.length());
				}
				index = clientSentence.indexOf("MESSAGE:");
				if(index != -1){
					clientMessage = clientSentence.substring(8,clientSentence.length());
					isMessageComplete = true;
				}
				if(isMessageComplete == true){
					System.out.println(senderName + ": " + clientMessage);
					capitalizedSentence = clientMessage.toUpperCase() + '\n';
					outToClient.writeBytes(capitalizedSentence);
					isMessageComplete = false;
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