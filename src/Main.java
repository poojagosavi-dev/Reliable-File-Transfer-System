
import java.util.Scanner;

public class Main {

public static void main(String[] args) throws Exception {
        Scanner sc = new Scanner(System.in);

        System.out.println("================================");
        System.out.println("   RELIABLE FILE TRANSFER SYSTEM");
        System.out.println("================================");

        System.out.println("1. Start Receiver");
        System.out.println("2. Start Sender");

        System.out.print("Enter your choice: ");

        int choice = sc.nextInt();
        sc.nextLine();

        if (choice == 1) {

            Receiver.startReceiver();

        } else if (choice == 2) {

            System.out.print("Enter file path: ");
            String filePath = sc.nextLine();

            System.out.print("Enter receiver IP address: ");
            String serverIP = sc.nextLine();

            Sender.sendFile(filePath, serverIP);

        } else {

            System.out.println("Invalid choice!");

        }

        sc.close();
    }
}
