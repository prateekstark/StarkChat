import java.util.*;
import java.net.*;
import java.io.*;
public class ClientMap{
	HashMap<String, DataOutputStream> dataMap = new HashMap<>();
	HashMap<String, BufferedReader> inServerMap = new HashMap<>();
	HashMap<String, Integer> registrationMap = new HashMap<>();
	HashMap<String, String> publicKeyMap = new HashMap<>();
	HashMap<String, Integer> encryptionMap = new HashMap<>();
}


class ServerClientData{
	DataOutputStream outToClient;
	BufferedReader inFromClient;
	String publicKey;
	int registrationLevel;
}