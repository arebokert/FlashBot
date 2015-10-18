package flashbot;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.JoinEvent;

import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.events.PartEvent;
import org.pircbotx.hooks.events.PrivateMessageEvent;

public class IRCListener extends ListenerAdapter {

    private String nickName;
    private Interaction in;
    private Datamanager dm = new Datamanager();
    
    public IRCListener(Interaction in){
        this.in = in;
        nickName = dm.readPropertyFromFile("nickName", "config.conf", false)[0];
    }
    
    @Override
    public void onMessage(MessageEvent evt) {
        //String regex = "(?:(?:http|https):\\/\\/)?([-a-zA-Z0-9.]{2,256}\\.[a-z]{2,4})\\b(?:\\/[-a-zA-Z0-9@:%_\\+.~#?&//=]*)?";
        String regex = "(https?|ftp|file):\\/\\/[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]";
        String regexMessage = returnRegex(regex, evt.getMessage());
        try {
            if (evt.getMessage().startsWith(",") || evt.getMessage().startsWith("!")) {
                Loghandler.writeLog("Message with familiar syntax detected: " + evt.getMessage(), "general", getClass().getName());
                in.checkIgnoreUserState(evt.getUser(), evt.getChannel(), evt.getMessage());
            } else if (regexMessage != null && !dm.isIgnored(evt.getUser().getNick(), evt.getChannel()) && !evt.getMessage().contains("(spoiler)")) {

                Loghandler.writeLog("URL detected: " + evt.getMessage(), "general", getClass().getName());
                Loghandler.writeLog("Checking contenttype of URL: " + regexMessage, "general", getClass().getName());
                /*
                 if (!regexMessage.toLowerCase().matches("^\\w+://.*")) {
                 regexMessage = "http://" + regexMessage;
                 }
                 */
                if (in.getContentType(regexMessage)) {
                    Loghandler.writeLog("Correct contenttype found in message: " + regexMessage, "general", getClass().getName());
                    in.parseURL(evt.getChannel(), regexMessage);
                } else {
                    Loghandler.writeLog("Wrong contenttype of URL: " + regexMessage, "general", getClass().getName());
                }
            } /*else if (!evt.getUser().getNick().equals(nickName)) {
                if(evt.getUser().isVerified()){
                    functions.saveSeenUser(evt.getUser().getNick(), evt.getChannel().getName(), evt.getMessage(), true);
                } else {
                    functions.saveSeenUser(evt.getUser().getNick(), evt.getChannel().getName(), evt.getMessage(), false);
                }
            }*/

        } catch (Exception e) {
            Loghandler.writeLog("Could not parse message", e);
        }
    }

    @Override
    public void onPrivateMessage(PrivateMessageEvent evt) {
        try {
            in.checkPrivateChatMessage(evt.getUser(), evt.getMessage());
            Loghandler.writeLog("Private message with familiar syntax detected: " + evt.getMessage(), "general", getClass().getName());
        } catch (Exception e) {
            Loghandler.writeLog("Could not parse private message", e);
        }
    }

    /*
     @Override
     public void onJoin(JoinEvent evt) {
     try {
     logger.writeLog("Joinevent triggered by: " + evt.getUser().getNick(), "general", IRCListener.class);
     //functions.checkJoin(evt.getUser(), evt.getChannel());
     } catch (Exception e) {
     logger.writeLog(functions.formatError(e), "error", getClass());
     }
     }
     */
    @Override
    public void onPart(PartEvent evt) {
        try {
            Loghandler.writeLog("Partevent triggered by: " + evt.getUser().getNick(), "general", getClass().getName());
            /*
            if (!evt.getUser().getNick().equals(nickName)) {
                logger.writeLog("Saving seen user on part.", "general", IRCListener.class);
                functions.saveSeenUser(evt.getUser().getNick(), evt.getChannel().getName(), evt.getReason());
            }
            */
            //functions.checkJoin(evt.getUser(), evt.getChannel());
        } catch (Exception e) {
            Loghandler.writeLog("Could not parse onPart message", e);
        }
    }

    @Override
    public void onJoin(JoinEvent evt) {
        try {
            Loghandler.writeLog("Joinevent triggered by: " + evt.getUser().getNick(), "general", getClass().getName());
            /*
            if (!evt.getUser().getNick().equals(nickName)) {
                logger.writeLog("Saving seen user on join.", "general", IRCListener.class);
                functions.saveSeenUser(evt.getUser().getNick(), evt.getChannel().getName(), "Joined");
            }
            */
            //functions.checkJoin(evt.getUser(), evt.getChannel());
        } catch (Exception e) {
            Loghandler.writeLog("Could not parse onJoin message", e);
        }
    }

    private String returnRegex(String regex, String messsage) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(messsage);
        if (matcher.find()) {
            return matcher.group(0);
        } else {
            return null;
        }
    }
}
