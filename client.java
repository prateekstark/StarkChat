import java.io.*;
import java.net.*;
import java.util.*;
import java.security.MessageDigest;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import javax.crypto.Cipher;

class TCPClient{
	public static Cryptography security = new Cryptography();
	public static BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
	public static byte[] clientPublicKey;
	public static byte[] clientPrivateKey;
	public static MessageDigest md;
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

	public static boolean registerToReceive(DataOutputStream outToServer, BufferedReader inFromServer, String username, String publicKey) throws Exception{
		String serverResponse;
		String registrationRequestToReceive = "REGISTER TORECV" + " " + username + "\n";
		outToServer.writeBytes(registrationRequestToReceive + publicKey + "\n" + "\n");
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
		KeyPair generateKeyPair = security.generateKeyPair();
		try{
			md = MessageDigest.getInstance("SHA-256");
		}
		catch(Exception e){
			System.out.println(e);
		}
		clientPrivateKey = generateKeyPair.getPrivate().getEncoded();
		clientPublicKey = generateKeyPair.getPublic().getEncoded();
		String base64PublicKey = Base64.getEncoder().encodeToString(clientPublicKey);
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
		boolean connectionStatus2 = registerToReceive(outToServer2, inFromServer2, username, base64PublicKey);
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
		String encryptedMessage;
		String modifiedSentence;
		byte[] shaBytes;
		String messageSignature;
		while(true){
			try{
				sentence = inFromUser.readLine();
				while(sentence.equals("")){
					sentence = inFromUser.readLine();
				}
			}
			catch(Exception e){
				System.out.println("Wrong Input");
				continue;
			}
			if(sentence.equals("UNREGISTER")){
				String unregisterString = "UNREGISTER" + "\n";
				outToServer.writeBytes(unregisterString + "\n");
				String confirmationString = inFromServer.readLine();
				if(confirmationString.equals("")){
					confirmationString = inFromServer.readLine();
				}
				if(confirmationString.indexOf("DONE") != -1){
					System.out.println("You are unregistered!");
					break;
				}
				continue;
			}
			if(sentence.charAt(0) == '@'){
				if(sentence.length() >= 3){
					int tempIndex = sentence.indexOf(" ");
					if(tempIndex == -1){
						System.out.println("Wrong Input!");
						continue;
					}
					String toSend = sentence.substring(1,tempIndex);
					if(tempIndex == sentence.length()){
						System.out.println("Wrong Input!");
						continue;
					}
					String message = sentence.substring(tempIndex+1, sentence.length());
					int messageLength = message.length();
					
					String fetchKey = "FETCHKEY "+ toSend + "\n";
					outToServer.writeBytes(fetchKey + "\n");
					String publicKeyInfo = inFromServer.readLine();
					if(publicKeyInfo.equals("")){
						publicKeyInfo = inFromServer.readLine();
					}
					if(publicKeyInfo.indexOf("KEY") != -1){
						String tempKey = publicKeyInfo.substring(4, publicKeyInfo.length());
						byte[] receiverPublicKey = Base64.getDecoder().decode(tempKey);
						byte[] encryptedMessageByte = security.encrypt(receiverPublicKey, message.getBytes());
						encryptedMessage = Base64.getEncoder().encodeToString(encryptedMessageByte);
						// shaBytes = md.digest(Base64.getDecoder().decode(encryptedMessage));
						messageSignature = Base64.getEncoder().encodeToString(security.encryptWithPrivateKey(clientPrivateKey, md.digest(encryptedMessageByte)));
						String toSendInfo = "SEND " + toSend + "\n";
						String contentLength = "Content-length: " + Integer.toString(messageLength)  + "\n";
						outToServer.writeBytes(toSendInfo + contentLength + "\n" + encryptedMessage + "\n" + messageSignature + "\n\n");
					}
					else{
						System.out.println(publicKeyInfo);
						continue;

					}
				}
				else{
					System.out.println("Wrong input");
					continue;
				}
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
				if(modifiedSentence.indexOf("102") != -1){
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
		String decryptedMessage;
		String decryptSignature;
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
					String messageSignature = inFromServer.readLine();
					if(messageSignature.equals("")){
						messageSignature = inFromServer.readLine();
					}
					String fetchPublicKey = "FETCHKEY " + senderName;
					outToServer.writeBytes(fetchPublicKey + "\n\n");
					String signatureKey = inFromServer.readLine();
					if(signatureKey.equals("")){
						signatureKey = inFromServer.readLine();
					}
					String keyString = signatureKey.substring(4, signatureKey.length());
				
					if(signatureKey.indexOf("KEY") != -1){
						byte[] senderPublicKey = Base64.getDecoder().decode(keyString);
						decryptedMessage = new String(security.decrypt(clientPrivateKey, Base64.getDecoder().decode(message)));
						decryptSignature = new String(security.decryptWithPublicKey(senderPublicKey, Base64.getDecoder().decode(messageSignature)));
						byte[] decryptSignatureByte = security.decryptWithPublicKey(senderPublicKey, Base64.getDecoder().decode(messageSignature));
						if(Base64.getEncoder().encodeToString(md.digest(Base64.getDecoder().decode(message))).equals(Base64.getEncoder().encodeToString(decryptSignatureByte))){
							System.out.println("#" + senderName +": " + decryptedMessage);
							String confirmationString = "RECEIVED " + senderName + "\n";
							outToServer.writeBytes(confirmationString+"\n");
							continue;	
						}
						String errorString = "ERROR 105 Wrong message" + "\n";
						outToServer.writeBytes(errorString + "\n");
						continue;
					}	
				}
			}
			catch(Exception e){
				System.out.println(e);
				String errorString = "ERROR 104 Header Incomplete" + "\n";
				outToServer.writeBytes(errorString + "\n");
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
