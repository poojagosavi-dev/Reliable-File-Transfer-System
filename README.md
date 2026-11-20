# Reliable File Transfer System

## About the Project
Reliable File Transfer System is a Java-based application that transfers files between computers using TCP socket programming.

It uses checksum verification and acknowledgment mechanisms to verify received data and support retransmission when a chunk fails verification.

## Features
- File transfer using TCP sockets
- Chunk-based file transmission
- SHA-256 checksum verification
- ACK/NACK acknowledgment mechanism
- Retransmission of failed chunks
- Automatic saving of received files

## Technologies Used
- Java
- TCP Socket Programming
- SHA-256
- VS Code

## Project Structure
```text
ReliableFileTransferSystem/
├── src/
│   ├── Main.java
│   ├── Sender.java
│   └── Receiver.java
├── received_file/
├── .gitignore
└── README.md
```

## How to Run

### 1. Compile the project
```bash
javac src/Main.java src/Sender.java src/Receiver.java
```

### 2. Start the Receiver
```bash
java -cp src Main
```
Enter `1` to start the receiver.

### 3. Start the Sender
Open another terminal and execute:
```bash
java -cp src Main
```
Enter `2` to start the sender.

Provide the file path and receiver IP address.

For testing on the same computer, use:
```text
127.0.0.1
```

## Output
The received file is saved inside the `received_file` directory.

## Project Status
Basic file transfer and checksum verification implementation completed. Further improvements and testing are ongoing.

## Author
Pooja Gosavi