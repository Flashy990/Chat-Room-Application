package client;

public class ClientMain {
    public static void main(String[] args) {
        String serverAddress = "localhost";
        int port = 8000;
        String username = "DefaultUser";

        if (args.length >= 3) {
            serverAddress = args[0];
            port = Integer.parseInt(args[1]);
            username = args[2];
        }

        else {
            System.out.println("Using default values: serverAddress=localhost, port=8000, username=SideC");
        }

        ChatClient client = new ChatClient(serverAddress, port, username);
        client.start();
    }
}