
import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Receiver {

    private static final int PORT = 5000;
    private static final int MAX_RETRIES = 3;

    public static String calculateChecksum(byte[] data)
            throws NoSuchAlgorithmException {

        MessageDigest md = MessageDigest.getInstance("SHA-256");

        byte[] hash = md.digest(data);

        StringBuilder hex = new StringBuilder();

        for (byte b : hash) {
            hex.append(String.format("%02x", b));
        }

        return hex.toString();
    }

    public static void startReceiver() {

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {

            System.out.println("Receiver is waiting for a connection...");
            System.out.println("Port: " + PORT);

            try (Socket socket = serverSocket.accept();
                 DataInputStream in = new DataInputStream(
                         socket.getInputStream());
                 DataOutputStream out = new DataOutputStream(
                         socket.getOutputStream())) {

                System.out.println("Sender connected!");

                String fileName = Paths.get(in.readUTF())
                        .getFileName().toString();

                long fileSize = in.readLong();
                int chunkSize = in.readInt();
                int totalChunks = in.readInt();

                if (fileSize < 0 || chunkSize < 1
                        || totalChunks < 0
                        || totalChunks != (fileSize + chunkSize - 1)
                        / chunkSize) {
                    throw new IOException("Invalid file information.");
                }

                Path folder = Paths.get("received_file");

                Files.createDirectories(folder);

                Path outputPath = folder.resolve(fileName);

                System.out.println("Receiving: " + fileName);

                long bytesWritten = 0;

                try (OutputStream fileOut =
                             Files.newOutputStream(outputPath)) {

                    for (int i = 0; i < totalChunks; i++) {

                        boolean valid = false;

                        for (int attempt = 1;
                             attempt <= MAX_RETRIES; attempt++) {

                            int length = in.readInt();

                            if (length < 1 || length > chunkSize) {
                                throw new IOException("Invalid chunk size.");
                            }

                            byte[] chunk = in.readNBytes(length);

                            if (chunk.length != length) {
                                throw new IOException("Incomplete chunk.");
                            }

                            String receivedChecksum = in.readUTF();

                            String actualChecksum =
                                    calculateChecksum(chunk);

                            int expectedLength = (int) Math.min(
                                    chunkSize, fileSize - bytesWritten);

                            valid = length == expectedLength
                                    && actualChecksum.equals(receivedChecksum);

                            out.writeBoolean(valid);
                            out.flush();

                            if (valid) {
                                fileOut.write(chunk);
                                bytesWritten += length;

                                System.out.println(
                                        "Chunk " + (i + 1)
                                        + " verified successfully."
                                );

                                break;
                            } else {
                                System.out.println(
                                        "Chunk " + (i + 1)
                                        + " corrupted. Waiting for retransmission."
                                );
                            }
                        }

                        if (!valid) {
                            throw new IOException(
                                    "Chunk verification failed."
                            );
                        }
                    }
                }

                if (bytesWritten != fileSize) {
                    throw new IOException("File size mismatch.");
                }

                out.writeUTF("SUCCESS");
                out.flush();

                System.out.println("File received successfully!");
                System.out.println("Saved at: " + outputPath.toAbsolutePath());

            }

        } catch (Exception e) {
            System.out.println("Receiver Error: " + e.getMessage());
        }
    }
}
