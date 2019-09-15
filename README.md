# StarkChat

StarkChat is a chat application that allows a user to do encrypted chat with one another, which cannot be decrypted by the chat server.

Users can direct messages to other users using an @prefix, and the server needs to forward these messages to the intended recipient. The message itself would be encrypted between any pair of users so that the server cannot read the messages, the server can only infer that communication is happening between the given pair of users.



## Installation


```bash
javac *.java #Compilation
```


```bash
java TCPServer #For running the server
```


```bash
java TCPClient #For running the client
```

## Usage

To send a message write @username [Message Content]
The messages you receive are in the form of #senderName [Message Content]

## Contributing
Pull requests are welcome. For major changes, please open an issue first to discuss what you would like to change.

Please make sure to update tests as appropriate.
