
import java.io.*;
import java.net.*;

public class Sender {

    private static final int PORT = 5000;
    private static final int CHUNK_SIZE = 4096;
    private static final int MAX_RETRIES = 3;

    public static void sendFile(String filePath, String serverIP)
            throws Exception {

        File file = new File(filePath);

        if (!file.exists() || !file.isFile()) {
            System.out.println("File not found!");
            return;
        }

        try (Socket socket = new Socket(serverIP, PORT);
             DataOutputStream out = new DataOutputStream(
                     socket.getOutputStream());
             DataInputStream in = new DataInputStream(
                     socket.getInputStream());
             FileInputStream fis = new FileInputStream(file)) {

            long fileSize = file.length();

            int totalChunks = (int) ((fileSize + CHUNK_SIZE - 1)
                    / CHUNK_SIZE);

            out.writeUTF(file.getName());
            out.writeLong(fileSize);
            out.writeInt(CHUNK_SIZE);
            out.writeInt(totalChunks);
            out.flush();

            System.out.println("Connected to receiver!");
            System.out.println("Sending: " + file.getName());

            byte[] buffer = new byte[CHUNK_SIZE];

            for (int i = 0; i < totalChunks; i++) {

                int bytesRead = fis.read(buffer);

                if (bytesRead == -1) {
                    throw new IOException("Unexpected end of file");
                }

                byte[] chunk = new byte[bytesRead];

                System.arraycopy(buffer, 0, chunk, 0, bytesRead);

                String checksum = Receiver.calculateChecksum(chunk);

                boolean acknowledged = false;

                for (int attempt = 1;
                     attempt <= MAX_RETRIES; attempt++) {

                    out.writeInt(bytesRead);
                    out.write(chunk);
                    out.writeUTF(checksum);
                    out.flush();

                    acknowledged = in.readBoolean();

                    if (acknowledged) {
                        System.out.println(
                                "Chunk " + (i + 1) + " transferred successfully."
                        );
                        break;
                    } else {
                        System.out.println(
                                "Chunk " + (i + 1)
                                + " failed. Retrying... Attempt "
                                + attempt
                        );
                    }
                }

                if (!acknowledged) {
                    throw new IOException(
                            "Transfer failed after maximum retries."
                    );
                }
            }

            String response = in.readUTF();

            System.out.println("Receiver response: " + response);
            System.out.println("File transfer completed!");

        } catch (Exception e) {
            System.out.println("Sender Error: " + e.getMessage());
        }
    }
}
