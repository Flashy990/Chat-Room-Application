package server;

public class ServerMain {
    public static void main(String[] args) {
        int port = 8000;
        ChatServer server = new ChatServer(port);
        server.start();
    }
}