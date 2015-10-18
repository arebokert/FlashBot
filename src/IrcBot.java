package flashbot;

import org.pircbotx.*;
import org.pircbotx.output.OutputIRC;
import org.pircbotx.output.OutputRaw;

public class IrcBot implements Runnable {

    private PircBotX myBot;
    private OutputIRC execute;
    private OutputRaw executeRaw;
    private String ident;
    private String nickName;
    private String secondNick;
    private String realName;
    private String userName;
    private String password;
    private String version;
    private String IP;
    private int port;
    private String[] channels;

    public IrcBot(String ident, String nickName, String realName, String password, String version, String IP, int port, String[] channels) {
        this.ident = ident;
        this.nickName = nickName;
        this.realName = realName;
        this.password = password;
        this.version = version;
        this.IP = IP;
        this.port = port;
        this.channels = channels;
        this.myBot = new PircBotX(buildConf());
        this.execute = new OutputIRC(myBot);
        this.executeRaw = new OutputRaw(myBot);
    }

    @Override
    public void run() {
        connect();
    }

    public Configuration buildConf() {
        Configuration.Builder build = new Configuration.Builder();
        build.setAutoReconnect(false);
        build.setAutoNickChange(true);
        build.setName(this.nickName);
        build.setRealName(this.realName);
        build.setNickservDelayJoin(false);
        build.addServer(this.IP, this.port);
        build.setVersion(this.version);
        //build.setListenerManager(this.listenManager);
        build.setLogin(this.ident);
        build.setSocketFactory(new UtilSSLSocketFactory().trustAllCertificates());
        build.setNickservPassword(this.password);
        for (String channel1 : channels) {
            String[] channel = channel1.split(",");
            if (channel1.split(",").length > 1) {
                build.addAutoJoinChannel(channel[0], channel[1]);
            } else {
                build.addAutoJoinChannel(channel[0]);
            }
        }
        //build.getListenerManager().addListener((Listener) new IRCListener());
        return build.buildConfiguration();
    }

    public void connect() {
        try {
            this.myBot.startBot();
        } catch (Exception e) {
            Loghandler.writeLog("error", e);
        }
    }

    public void disconnect() {
        try {
            this.execute.quitServer("Uppdaterar min hjärna, kommer åter snart...");
        } catch (Exception e) {
            Loghandler.writeLog("error", e);
        }
    }

    public void executeRawCommand(String command) {
        this.executeRaw.rawLineNow(command);
    }

    public boolean isConnected() {
        return this.myBot.isConnected();
    }

    public void registerNick(String nickservName, String firstTimeRegister, boolean verify) {
        if (!verify) {
            this.execute.message(nickservName, firstTimeRegister);
        } else {
            this.execute.message(nickservName, firstTimeRegister);
        }
    }

    public void authenticate(String nickservName, String customAuthCommand) {
        this.execute.message(nickservName, customAuthCommand);
    }
    
    public void message(String target, String message){
        this.execute.message(target, message);
    }
    
    public void action(String target, String action){
        this.execute.action(target, action);
    }
    
    public void op(String username, String channel){
        this.executeRawCommand("mode " + channel + " +o " + username);
    }
    
    public void deop(String username, String channel){
        this.executeRawCommand("mode " + channel + " -o " + username);
    }
    
    public void voice(String username, String channel){
        this.executeRawCommand("mode " + channel + " +v " + username);
    }
    
    public void devoice(String username, String channel){
        this.executeRawCommand("mode " + channel + " -v " + username);
    }
    
    public void joinChannel(String channel, String key) {
        if(key == null){
            this.execute.joinChannel(channel);
        } else {
            this.execute.joinChannel(channel, key);
        }
    }
    
    public void addListener(Interaction in){
        myBot.getConfiguration().getListenerManager().addListener(new IRCListener(in));
    }

}
