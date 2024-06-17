package clients;

import clients.menu.ClientMenu;

public class ClientMain {
    public static void main(String[] args) {
        ClientSetup client = new ClientSetup("127.0.0.1", 3838);
        ClientMenu clientMenu = new ClientMenu(client.getClientHandler());
        clientMenu.run();
    }
}
