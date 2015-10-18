package flashbot;

import java.util.Scanner;

public class Console {

    private final Interaction in;

    public Console(Interaction in) {
        this.in = in;
        getCommand();
    }

    private void getCommand() {
        Scanner scan = new Scanner(System.in);
        Loghandler.writeLog("Press any key to start bot.", "general", getClass().getName());
        Loghandler.writeLog("When you want to exit, type exit and press enter.", "general", getClass().getName());
        scan.nextLine();
        in.connect();
        while (1 == 1) {
            String command = scan.nextLine();
            if (command.toLowerCase().equals("disconnect")) {
                in.disconnect();
            } else if (command.toLowerCase().equals("connect")) {
                in.connect();
            } else if (command.toLowerCase().equals("exit")) {
                in.disconnect();
                System.exit(0);
            } else if (command.toLowerCase().startsWith("message ")) {
                String exec[] = command.split(" ");
                String temp = "";
                for (int i = 2; i < exec.length; i++) {
                    temp = temp + exec[i] + " ";
                }
                in.message(exec[1], temp);
            /*} else if (command.toLowerCase().equals("register")) {
                in.registerNick(false);
            } else if (command.toLowerCase().equals("verify")) {
                in.registerNick(true);*/
            } else if (command.toLowerCase().equals("join")) {
                in.joinChannel();
            } else if (command.toLowerCase().startsWith("command ")) {
                in.execute(command.replace("command ", ""));
            } else if (command.toLowerCase().contains("authenticate")) {
                in.authenticate();
            } else {
                Loghandler.writeLog("Command: '" + command + "' does not exist.", "general", getClass().getName());
            }
        }
    }
}
