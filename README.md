# StarkChat

StarkChat is a chat application that allows a user to do encrypted chat with one another, which cannot be decrypted by the chat server.

Users can direct messages to other users using an @prefix, and the server needs to forward these messages to the intended recipient. The message itself would be encrypted between any pair of users so that the server cannot read the messages, the server can only infer that communication is happening between the given pair of users.



## Installation


```bash
bash compile.sh  #Compilation
```

```bash
java TCPServer #For running the server
```

```bash
java TCPClient [username] [SERVER_ADDRESS]    #For running the client
```

## Extensions
For offline users, we can add a database in the server, which stores the messages sent by users to offline client.

If the user exits by pressing CTRL + C, we can handle this by placing a timer in the serverThreads. This would mean that if the server does not hear from the client for let's say 3 minutes, the server deregisters the client from other hashmaps, stop its thread and store it in the offline map. This way we can also deal with offline users. Later when the client registers again.

Also, we can make another hashmap in the server, which stores the offline users. In this case, they are registered but offline.


## Usage

To send a message write @username [Message Content]
The messages you receive are in the form of #senderName [Message Content]

## Contributing
Pull requests are welcome. For major changes, please open an issue first to discuss what you would like to change.

Please make sure to update tests as appropriate.
