package flashbot;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Random;
import java.util.TimeZone;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.pircbotx.Channel;
import org.pircbotx.User;

public class Interaction {

    private static IrcBot myBot;
    //private static RSSFeedParser RSSParser;

    private static Thread t = null;
    //private static Thread tRSS = null;

    private String channels[];
    private String ident;
    private String nickName;
    private String realName;
    private String password;
    private String version;
    private String IP;
    private int port;
    private String nickservName;
    private String customAuthCommand;
    private String proxyIP;
    private int proxyPort = 0;
    //private String firstTimeRegister = constants.firstTimeRegister;
    //private String verificationString = constants.verificationString;
    public Hashtable<String, String> helpCommands = new Hashtable<String, String>();
    public Random randomGenerator = new Random();
    private Datamanager dm = new Datamanager();
    //private static final String RSS = constants.RSS;
    //private static ArrayList<String> opers = functions.readOpers();
    //private static ArrayList<String> voiced = functions.readVoiced();

    public Interaction() {
        populateHashTable();
        readConfig();
    }

    private void readConfig() {
        ident = dm.readPropertyFromFile("ident", "config.conf", false)[0];
        nickName = dm.readPropertyFromFile("nickName", "config.conf", false)[0];
        realName = dm.readPropertyFromFile("realName", "config.conf", false)[0];
        password = dm.readPropertyFromFile("password", "config.conf", false)[0];
        version = dm.readPropertyFromFile("version", "config.conf", false)[0];
        IP = dm.readPropertyFromFile("IP", "config.conf", false)[0];
        port = Integer.parseInt(dm.readPropertyFromFile("port", "config.conf", false)[0]);
        nickservName = dm.readPropertyFromFile("nickservName", "config.conf", false)[0];
        customAuthCommand = dm.readPropertyFromFile("customAuthCommand", "config.conf", false)[0];
        channels = dm.readPropertyFromFile("channels", "config.conf", false)[0].split(",");
        proxyIP = dm.readPropertyFromFile("proxyIP", "config.conf", false)[0];
        proxyPort = Integer.parseInt(dm.readPropertyFromFile("proxyPort", "config.conf", false)[0]);
    }

    private void populateHashTable() {
        helpCommands.put(",hjälp", "Detta kommando används för att få hjälp med andra kommandon. Användning: ,hjälp <,kommando>");
        helpCommands.put(",sågs", "Detta kommando används för att se när en användare senast sågs. Användning: ,sågs <användare> - alternativt - ,sågs <användare> meddelande");
        helpCommands.put(",alias", "Lägg till ett alias för ett meddelande som botten ska skriva ut. Användning: ,alias <läggtill|tabort> <aliasnamn> (<meddelande>)");
        helpCommands.put("!", "Skriv ut ett sparat alias. Användning: !<alias>");
        helpCommands.put(",ignorera", "(OP) Säg till FlashBot att ignorera kommandon utförda av en användare. Användning: ,ignorera <användare>");
        helpCommands.put(",lyssnapå", "(OP) Säg till FlashBot att inte ignorera kommandon utförda av en användare. Användning: ,lyssnapå <användare>");
        helpCommands.put(",poweruser", "(OP) Lägg till användare som poweruser. Användning: ,poweruser <användare>");
        helpCommands.put(",normaluser", "(OP) Ta bort användare som poweruser. Användning: ,normaluser <användare>");
    }

    public void connect() {
        try {
            if (t == null) {
                myBot = new IrcBot(ident, nickName, realName, password, version, IP, port, channels);
                myBot.addListener(this);
                t = new Thread(myBot);
                t.start();
            } else if (!t.isAlive()) {
                t = new Thread(myBot);
                t.start();
            } else {
                Loghandler.writeLog("A new connection could not be made as the old thread is still alive.", "general", getClass().getName());
            }
        } catch (Exception e) {
            Loghandler.writeLog("Could not start an IRC connection", e);
        }
    }

    public void authenticate() {
        myBot.authenticate(nickservName, customAuthCommand);
        Loghandler.writeLog("Server auth command sent", "general", getClass().getName());
    }

    public void execute(String command) {
        try {
            myBot.executeRawCommand(command);
        } catch (Exception e) {
            Loghandler.writeLog("Could not execute command: ", e);
        }
    }

    /*
     public void registerNick(boolean verify) {
     if (!verify) {
     myBot.registerNick(nickservName, firstTimeRegister, false);
     Loghandler.writeLog("Server register command sent.", "general", getClass().getName());
     } else {
     myBot.registerNick(nickservName, verificationString, true);
     Loghandler.writeLog("Server verification command sent.", "general", getClass().getName());
     }
     }
     */
    public void joinChannel() {
        for (int i = 0; i < channels.length; i++) {
            String channel[] = channels[i].split(";");
            if (channels[i].split(";").length > 1) {
                myBot.joinChannel(channel[0], channel[1]);
            } else {
                myBot.joinChannel(channel[0], null);
            }
        }
        Loghandler.writeLog("Channel join commands sent", "general", getClass().getName());
        //launchRSS();
    }

    public void disconnect() {
        try {
            if (myBot.isConnected()) {
                myBot.disconnect();
                Loghandler.writeLog("Server disconnect command sent.", "general", getClass().getName());
                while (t.isAlive());
                Loghandler.writeLog("Bot thread terminated.\n", "general", getClass().getName());
            } else {
                Loghandler.writeLog("Server already disconnected.", "general", getClass().getName());
            }
        } catch (Exception e) {
            Loghandler.writeLog("Could not disconnect from server properly", e);
        }
    }

    public void message(String target, String message) {
        try {
            myBot.message(target, message);
            Loghandler.writeLog("Server message commands sent.", "general", getClass().getName());
        } catch (Exception e) {
            Loghandler.writeLog("Could not submit IRC message: " + message, e);
        }
    }

    private void action(String target, String action) {
        try {
            myBot.action(target, action);
            Loghandler.writeLog("Server action commands sent.", "general", getClass().getName());
        } catch (Exception e) {
            Loghandler.writeLog("Could not submit IRc action: " + action, e);
        }
    }

    public void checkSeen(String user, String channel, boolean withMessage) {
        try {
            String[] userGet = dm.readUserSeen(user, channel);
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            long diff = 0;
            long diffSeconds = 0;
            long diffMinutes = 0;
            long diffHours = 0;
            long diffDays = 0;
            Date dateComp;
            if (!userGet[1].equals("Never")) {
                if (userGet.length < 3) {
                    dateComp = sdf.parse(userGet[0]);
                } else {
                    dateComp = sdf.parse(userGet[1]);
                }
                Date date = sdf.parse(getTime());
                diff = date.getTime() - dateComp.getTime();
                diffSeconds = diff / 1000 % 60;
                diffMinutes = diff / (60 * 1000) % 60;
                diffHours = diff / (60 * 60 * 1000) % 24;
                diffDays = diff / (24 * 60 * 60 * 1000);
                if (withMessage) {
                    if (userGet.length > 2 && userGet[0] != null) {
                        if (Boolean.parseBoolean(userGet[0])) {
                            message(channel, "Användaren " + user + " syntes senast till för "
                                    + diffDays + "D " + diffHours + "H " + diffMinutes + "M " + diffSeconds + "S"
                                    + " sedan. Hans/hennes senaste meddelande löd: " + userGet[2]);
                        } else {
                            message(channel, "Användaren " + user + " syntes senast till för "
                                    + diffDays + "D " + diffHours + "H " + diffMinutes + "M " + diffSeconds + "S"
                                    + " sedan (Oregistrerat nick!). Hans/hennes senaste meddelande löd: " + userGet[2]);
                        }
                    } else {
                        message(channel, "Användaren " + user + " syntes senast till för "
                                + diffDays + "D " + diffHours + "H " + diffMinutes + "M " + diffSeconds + "S"
                                + " sedan (Eventuellt oregistrerat nick!). Hans/hennes senaste meddelande löd: " + userGet[2]);
                    }
                } else {
                    if (userGet.length > 2 && userGet[0] != null) {
                        if (Boolean.parseBoolean(userGet[0])) {
                            message(channel, "Användaren " + user + " syntes senast till för "
                                    + diffDays + "D " + diffHours + "H " + diffMinutes + "M " + diffSeconds + "S"
                                    + " sedan.");
                        } else {
                            message(channel, "Användaren " + user + " syntes senast till för "
                                    + diffDays + "D " + diffHours + "H " + diffMinutes + "M " + diffSeconds + "S"
                                    + " sedan (Oregistrerat nick!).");
                        }
                    } else {
                        message(channel, "Användaren " + user + " syntes senast till för "
                                + diffDays + "D " + diffHours + "H " + diffMinutes + "M " + diffSeconds + "S"
                                + " sedan (Eventuellt oregistrerat nick!).");
                    }
                }
            } else if (userGet[1].equals("Never")) {
                message(channel, "Användaren " + user + " har aldrig tidigare synts till.");
            }
        } catch (Exception e) {
            Loghandler.writeLog("Could not check seen on user " + user, e);
        }
    }

    private void addAlias(User user, Channel channel, String aliasName, String message) {
        String nickString = "[ " + user.getNick() + " ] ";
        try {
            String[] aliasGet = dm.readAlias(aliasName, channel.getName());
            if (aliasGet != null) {
                if (aliasGet[0].equals(user.getNick()) || (dm.isPU(user.getNick(), channel.getName()) || channel.getOps().contains(user))) {
                    dm.saveAlias(aliasName, user.getNick(), message);
                    message(channel.getName(), nickString + "Alias " + aliasName + " ersattes.");
                    Loghandler.writeLog("Alias was replaced: " + aliasName, "general", getClass().getName());
                } else {
                    message(channel.getName(), nickString + "Denna alias tillhör inte dig.");
                    Loghandler.writeLog("Alias was not edited(wrong user): " + aliasName, "general", getClass().getName());
                }
            } else {
                dm.saveAlias(aliasName, user.getNick(), message);
                message(channel.getName(), nickString + "Alias " + aliasName + " lades till.");
                Loghandler.writeLog("Alias was added: " + aliasName, "general", getClass().getName());
            }
        } catch (Exception e) {
            Loghandler.writeLog("Could not add alias " + aliasName, e);
        }
    }

    private void removeAlias(User user, Channel channel, String aliasName) {
        String nickString = "[ " + user.getNick() + " ] ";
        try {
            String[] aliasGet = dm.readAlias(aliasName, channel.getName());
            if (aliasGet != null) {
                if (aliasGet[0].equals(user.getNick()) || (dm.isPU(user.getNick(), channel.getName()) || channel.getOps().contains(user))) {
                    dm.saveAlias(aliasName, user.getNick(), " ");
                    message(channel.getName(), nickString + "Alias " + aliasName + " togs bort.");
                    Loghandler.writeLog("Alias was removed: " + aliasName, "general", getClass().getName());
                } else {
                    message(channel.getName(), nickString + "Denna alias tillhör inte dig.");
                    Loghandler.writeLog("Alias was not removed(wrong user): " + aliasName, "general", getClass().getName());
                }
            } else {
                message(channel.getName(), nickString + "Alias " + aliasName + " hittades inte.");
                Loghandler.writeLog("Alias was not removed(not found): " + aliasName, "general", getClass().getName());
            }
        } catch (Exception e) {
            Loghandler.writeLog("Could not remove alias " + aliasName, e);
        }
    }

    private void printAlias(User user, Channel channel, String aliasName) {
        /*
         String regex = "(https?|ftp|file):\\/\\/[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]";
         String nickString = "[ " + user.getNick() + " ] ";
         */
        try {
            String[] aliasGet = dm.readAlias(aliasName, channel.getName());
            if (aliasGet == null) {
                //message(channel.getName(), nickString + "Alias was not found: " + aliasName);
                Loghandler.writeLog("Alias was not found: " + aliasName, "general", getClass().getName());
            } else {
                if (aliasGet[1].substring(0, 3).equals("/me")) {
                    action(channel.getName(), aliasGet[1].replace("/me ", ""));
                } else {
                    message(channel.getName(), aliasGet[1]);
                }
                /*
                 if (functions.returnRegex(regex, message) != null) {
                 functions.parseURL(channel, message);
                 }
                 */
                Loghandler.writeLog("Alias was found and broadcasted: " + aliasName, "general", getClass().getName());
            }
        } catch (Exception e) {
            Loghandler.writeLog("Could not print alias " + aliasName, e);
        }
    }

    private void setIgnore(User op, Channel channel, String subject, boolean ignore) {
        String nickString = "[ " + op.getNick() + " ] ";
        try {
            boolean ignored = dm.isIgnored(subject, channel);
            if (ignored && ignore) {
                message(channel.getName(), nickString + "Användare " + subject + " ignoreras redan.");
                Loghandler.writeLog("User is not being ignored(already ignored): " + subject, "general", getClass().getName());
            } else if (!ignored && ignore) {
                dm.setIgnoreUserState(subject, channel.getName(), true);
                message(channel.getName(), nickString + "Användare " + subject + " ignoreras.");
                Loghandler.writeLog("User is ignored: " + subject, "general", getClass().getName());
            } else if (ignored && !ignore) {
                dm.setIgnoreUserState(subject, channel.getName(), false);
                message(channel.getName(), nickString + "Användare " + subject + " ignoreras ej längre.");
                Loghandler.writeLog("User is no longer ignored: " + subject, "general", getClass().getName());
            } else if (!ignore || !ignored) {
                message(channel.getName(), nickString + "Användare " + subject + " är inte ignorerad.");
                Loghandler.writeLog("User is not being ignored(not ignored): " + subject, "general", getClass().getName());
            }
        } catch (Exception e) {
            Loghandler.writeLog("Could not set ignore state on user " + subject, e);
        }
    }

    private void setPU(User op, Channel channel, String subject, boolean pu) {
        String nickString = "[ " + op.getNick() + " ] ";
        try {
            boolean isPU = dm.isPU(subject, channel.getName());
            if (isPU && pu) {
                message(channel.getName(), nickString + "Användare " + subject + " är poweruser redan.");
                Loghandler.writeLog("User is not being made poweruser(already poweruser): " + subject, "general", getClass().getName());
            } else if (!isPU && pu) {
                dm.setPUUSerState(subject, channel.getName(), true);
                message(channel.getName(), nickString + "Användare " + subject + " är nu poweruser.");
                Loghandler.writeLog("User is being made poweruser: " + subject, "general", getClass().getName());
            } else if (isPU && !pu) {
                dm.setPUUSerState(subject, channel.getName(), false);
                message(channel.getName(), nickString + "Användare " + subject + " är ej poweruser längre.");
                Loghandler.writeLog("User is no longer poweruser: " + subject, "general", getClass().getName());
            } else if (!pu || !isPU) {
                message(channel.getName(), nickString + "Användare " + subject + " är inte poweruser.");
                Loghandler.writeLog("User is not being made normal user(not poweruser): " + subject, "general", getClass().getName());
            }
        } catch (Exception e) {
            Loghandler.writeLog("Could not set user " + subject + " as poweruser.", e);
        }
    }

    private ArrayList<String> convertHashtable(Hashtable<String, String> table) {
        ArrayList<String> returnArray = new ArrayList<>();
        Enumeration<String> enumKey = table.keys();
        while (enumKey.hasMoreElements()) {
            String key = enumKey.nextElement();
            String val = table.get(key);
            returnArray.add(key + '"' + val + '"');
        }
        return returnArray;
    }

    public void parseURL(Channel channel, String URL) {
        try {

            Document finaldoc = Jsoup.connect(URL)
                    .userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; sv-SE; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
                    .header("Accept-Language", "sv-SE")
                    .timeout(7000)
                    .referrer("http://www.google.se")
                    .get();

            setProxy(false);

            String titleText = finaldoc.title();
            String description = getMetaTag(finaldoc, "description");

            if ((description == null || description == "") && !titleText.equals("")) {
                Loghandler.writeLog("URL title found, parsing URL.", "general", getClass().getName());
                if (titleText.length() > 400) {
                    titleText = titleText.substring(0, 400) + "...";
                }

                message(channel.getName(), "" + titleText);
                Loghandler.writeLog("URL description not found on URL, pasting without it.", "general", getClass().getName());
            } else if (!titleText.equals("")) {
                Loghandler.writeLog("URL title found, parsing URL.", "general", getClass().getName());
                if (description.length() > 90) {
                    description = description.substring(0, 90) + "...";
                }
                if (titleText.length() > 310) {
                    titleText = titleText.substring(0, 310) + "...";
                }

                message(channel.getName(), "" + titleText + " | " + description);
                Loghandler.writeLog("URL description found on URL, pasting with it.", "general", getClass().getName());
            }
        } catch (Exception e) {
            Loghandler.writeLog("Could not parse url", e);
        }
    }

    private String readUrl(String urlString) throws Exception {
        BufferedReader reader = null;
        try {
            URL url = new URL(urlString);
            URLConnection hc = url.openConnection();
            hc.setRequestProperty("User-Agent", "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.4; en-US; rv:1.9.2.2) Gecko/20100316 Firefox/3.6.2");
            reader = new BufferedReader(new InputStreamReader(hc.getInputStream()));
            StringBuilder buffer = new StringBuilder();
            int read;
            char[] chars = new char[1024];
            while ((read = reader.read(chars)) != -1) {
                buffer.append(chars, 0, read);
            }
            return buffer.toString();
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
    }

    private static String getMetaTag(Document document, String attr) {
        Elements elements = document.select("meta[name=" + attr + "]");
        for (Element element : elements) {
            final String s = element.attr("content");
            if (s != null) {
                return s;
            }
        }
        elements = document.select("meta[property=" + attr + "]");
        for (Element element : elements) {
            final String s = element.attr("content");
            if (s != null) {
                return s;
            }
        }
        return null;
    }

    private String getTime() {
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        calendar.setTimeZone(TimeZone.getTimeZone("Europe/Stockholm"));
        return dateFormat.format(calendar.getTime());
    }

    private void setProxy(boolean bol) {
        if (proxyIP != null && proxyPort != 0) {
            if (bol) {
                System.setProperty("http.proxyHost", proxyIP);
                System.setProperty("http.proxyPort", String.valueOf(proxyPort));
                System.setProperty("https.proxyHost", proxyIP);
                System.setProperty("https.proxyPort", String.valueOf(proxyPort));
            } else {
                System.setProperty("https.proxyHost", "");
                System.setProperty("https.proxyPort", "");
                System.setProperty("http.proxyHost", "");
                System.setProperty("http.proxyPort", "");
            }
        }
    }

    public boolean getContentType(String URL) {
        try {
            Jsoup.connect(URL)
                    .userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
                    .referrer("http://www.google.com")
                    .timeout(7000)
                    .execute();
            return (true);
        } catch (Exception e) {
            Loghandler.writeLog("Could not connect to URL, trying proxy.", "general", getClass().getName());
            try {
                setProxy(true);
                Jsoup.connect(URL)
                        .userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
                        .referrer("http://www.google.se")
                        .timeout(7000)
                        .execute();
                Loghandler.writeLog("Connection to URL through proxy succeeded.", "general", getClass().getName());
                return true;
            } catch (Exception b) {
                setProxy(false);
                return false;
            }
        }
    }

    private String anyItem(ArrayList<String> arraylist) {
        int index = randomGenerator.nextInt(arraylist.size() - 1);
        String item = arraylist.get(index);
        return item;
    }

    public void checkChatMessage(User user, Channel channel, String message) {
        readChatMessage(user, channel, message);
    }

    public void checkIgnoreUserState(User user, Channel channel, String message) {
        try {
            if (!dm.isIgnored(user.getNick(), channel)) {
                checkChatMessage(user, channel, message);
            } else {
                message(user.getNick(), "Du är satt som ignorerad av FlashBot."
                        + " Du får detta meddelande för att du försökte triggra ett bott-kommando fastän du är ignorerad."
                        + " Bete dig och fråga en OP snällt så kanske du blir av med ignoreringen.");
            }
        } catch (Exception e) {
            Loghandler.writeLog("Could not check ignore state of user " + user.getNick(), e);
        }
    }

    public void checkPrivateChatMessage(User user, String message) {
        readPrivateChatMessage(user, message);
    }

    public void readPrivateChatMessage(User user, String message) {
        String nickString = "[ " + user.getNick() + " ] ";
        message(user.getNick(), nickString + "För frågor angående denna bot, kontakta ErkkiErkki.");
        Loghandler.writeLog(user.getNick() + " called bot in private message: " + message, "general", getClass().getName());
    }

    public void readChatMessage(User user, Channel channel, String message) {
        String split[] = message.split(" ");
        String nickString = "[ " + user.getNick() + " ] ";
        if (split[0].toLowerCase().equals(",hjälp")) {
            if (split.length == 1) {
                message(channel.getName(), nickString + "Detta kommando används för att få hjälp med andra kommandon. Användning: ,hjälp ,kommando");
                String commandString = "Följande kommandon är tillgängliga:";
                Enumeration<String> enumKey = helpCommands.keys();
                while (enumKey.hasMoreElements()) {
                    String key = enumKey.nextElement();
                    commandString = commandString + " " + key;
                }
                message(channel.getName(), nickString + commandString);
            } else if (split.length > 2) {
                message(channel.getName(), nickString + "Felaktigt syntax. Användning: ,hjälp ,kommando");
            } else if (helpCommands.containsKey(split[1])) {
                message(channel.getName(), nickString + helpCommands.get(split[1]));
            } else {
                message(channel.getName(), nickString + "Okänt kommando '" + split[1] + "'");
            }
        } else if (split[0].toLowerCase().equals(",alias")) {
            if (split.length < 4) {
                if (split.length < 2) {
                    message(channel.getName(), nickString + "Felaktigt syntax. Var vänlig använd ,hjälp ,alias");
                } else {
                    if (split[1].toLowerCase().equals("tabort")) {
                        if (user.isVerified()) {
                            removeAlias(user, channel, split[2].replace("!", ""));
                        } else {
                            message(channel.getName(), nickString + "Du måste vara inloggad med nickserv och ägare av detta alias(eller poweruser) för att kunna utföra detta kommando.");
                        }
                    } else {
                        message(channel.getName(), nickString + "Felaktigt syntax. Var vänlig använd ,hjälp ,alias");
                    }
                }
            } else {
                if (split[1].toLowerCase().equals("läggtill")) {
                    if (user.isVerified()) {
                        String tempMessage = "";
                        for (int i = 3; i < split.length; i++) {
                            tempMessage = tempMessage + split[i] + " ";
                        }
                        addAlias(user, channel, split[2].replace("!", ""), tempMessage);
                    } else {
                        message(channel.getName(), nickString + "Du måste vara inloggad med nickserv och ägare av detta alias(eller poweruser) för att kunna utföra detta kommando.");
                    }
                } else {
                    message(channel.getName(), nickString + "Felaktigt syntax. Var vänlig använd ,hjälp ,alias");
                }
            }
        } else if (split[0].toLowerCase().startsWith(",ignorera")) {
            if (split.length != 2) {
                message(channel.getName(), nickString + "Felaktigt syntax. Var vänlig använd ,hjälp ,ignorera");
            } else {
                if ((dm.isPU(user.getNick(), channel.getName()) || channel.getOps().contains(user)) && user.isVerified()) {
                    setIgnore(user, channel, message.replace(",ignorera ", ""), true);
                } else {
                    message(channel.getName(), nickString + "Du måste vara inloggad med nickserv och poweruser för att utföra detta kommando.");
                }
            }
        } else if (split[0].toLowerCase().startsWith(",lyssnapå")) {
            if (split.length != 2) {
                message(channel.getName(), nickString + "Felaktigt syntax. Var vänlig använd ,hjälp ,lyssnapå");
            } else {
                if ((dm.isPU(user.getNick(), channel.getName()) || channel.getOps().contains(user)) && user.isVerified()) {
                    setIgnore(user, channel, message.replace(",lyssnapå ", ""), false);
                } else {
                    message(channel.getName(), nickString + "Du måste vara inloggad med nickserv och poweruser för att utföra detta kommando.");
                }
            }
        } else if (split[0].toLowerCase().startsWith(",poweruser")) {
            if (split.length != 2) {
                message(channel.getName(), nickString + "Felaktigt syntax. Var vänlig använd ,hjälp ,poweruser");
            } else {
                if (channel.getOps().contains(user) && user.isVerified()) {
                    setPU(user, channel, message.replace(",poweruser ", ""), true);
                } else {
                    message(channel.getName(), nickString + "Du måste vara inloggad med nickserv och OP för att utföra detta kommando.");
                }
            }
        } else if (split[0].toLowerCase().startsWith(",normaluser")) {
            if (split.length != 2) {
                message(channel.getName(), nickString + "Felaktigt syntax. Var vänlig använd ,hjälp ,normaluser");
            } else {
                if (channel.getOps().contains(user) && user.isVerified()) {
                    setPU(user, channel, message.replace(",normaluser ", ""), false);
                } else {
                    message(channel.getName(), nickString + "Du måste vara inloggad med nickserv och OP för att utföra detta kommando.");
                }
            }
        } else if (split[0].toLowerCase().startsWith("!")) {
            if (split.length > 1) {
                message(channel.getName(), nickString + "Felaktigt syntax. Var vänlig använd ,hjälp !");
            } else {
                printAlias(user, channel, split[0].replace("!", ""));
            }
        } else {
            message(channel.getName(), nickString + "Okänt kommando '" + split[0] + "'");
        }

        Loghandler.writeLog(user.getNick() + " got their command processed in message: " + message + " from channel: " + channel.getName(), "general", getClass().getName());
    }

    /*
     public static boolean isOP(ImmutableSortedSet OPlist, User user) {
     try {
     logger.logger.writeLog("Checking if " + user.getNick() + "is op.", "general", class
     );
     if (OPlist.contains(user)) {
     logger.logger.writeLog(user.getNick() + " is already op.", "general", class);
     return true;
     } else {
     logger.logger.writeLog(user.getNick() + " is not op.", "general", class);
     return false;
     }
     } catch (Exception e) {
     logger.logger.writeLog("error", e
     );

     return (false);
     }
     }
     */
    /*
     public static void op(String username, String channel) {
     try {
     String nickString = "[ " + username + " ] ";
     myBot.op(username, channel);
     message(channel, nickString + " was granted OP status.");
     if (!opers.contains(username.toLowerCase() + "," + channel.toLowerCase())) {
     opers.add(username.toLowerCase() + "," + channel.toLowerCase());
     functions.writeOpers(opers);
     }
     logger.logger.writeLog("OP command sent" + " to channel: " + channel, "general", class);
     } catch (Exception e) {
     logger.logger.writeLog("error", e);
     }
     }

     public static void voice(String username, String channel) {
     try {
     String nickString = "[ " + username + " ] ";
     myBot.voice(username, channel);
     message(channel, nickString + " was granted voice status.");
     if (!voiced.contains(username.toLowerCase() + "," + channel.toLowerCase())) {
     voiced.add(username.toLowerCase() + "," + channel.toLowerCase());
     functions.writeVoiced(voiced);
     }
     logger.logger.writeLog("voice command sent" + " to channel: " + channel, "general", class);
     } catch (Exception e) {
     logger.logger.writeLog("error", e);
     }
     }

     public static void deop(String username, String channel) {
     try {
     myBot.deop(username, channel);
     if (opers.contains(username.toLowerCase() + "," + channel.toLowerCase())) {
     opers.remove(username.toLowerCase() + "," + channel.toLowerCase());
     functions.writeOpers(opers);
     }
     logger.logger.writeLog("deOP command sent" + " to channel: " + channel, "general", class);
     } catch (Exception e) {
     logger.logger.writeLog("error", e);
     }
     }

     public static void devoice(String username, String channel) {
     try {
     myBot.devoice(username, channel);
     if (voiced.contains(username.toLowerCase() + "," + channel.toLowerCase())) {
     voiced.remove(username.toLowerCase() + "," + channel.toLowerCase());
     functions.writeVoiced(voiced);
     }
     logger.logger.writeLog("devoice command sent" + " to channel: " + channel, "general", class);
     } catch (Exception e) {
     logger.logger.writeLog("error", e);
     }
     }

     public static void checkOpers(User user, String channel) {
     try {
     String nickString = "[ " + user.getNick() + " ] ";
     for (int i = 0; i < opers.size(); i++) {
     if (opers.get(i).toLowerCase().equals(user.getNick().toLowerCase() + "," + channel.toLowerCase())) {
     if (user.isVerified()) {
     myBot.op(user.getNick(), channel);
     message(channel, nickString + "was granted OP status.");
     logger.logger.writeLog("OP command sent" + " to channel: " + channel, "general", class);
     }
     }
     }
     } catch (Exception e) {
     logger.logger.writeLog("error", e);
     }
     }

     public static void checkVoiced(User user, String channel) {
     try {
     String nickString = "[ " + user.getNick() + " ] ";
     for (int i = 0; i < voiced.size(); i++) {
     if (voiced.get(i).toLowerCase().equals(user.getNick().toLowerCase() + "," + channel.toLowerCase())) {
     if (user.isVerified()) {
     myBot.voice(user.getNick(), channel);
     message(channel, nickString + "was granted voice status.");
     logger.logger.writeLog("voice command sent" + " to channel: " + channel, "general", class);
     }
     }
     }
     } catch (Exception e) {
     logger.logger.writeLog("error", e);
     }
     }

     public static boolean isOP(User user, String channel) {
     boolean isOP = false;
     try {
     for (int i = 0; i < opers.size(); i++) {
     if (opers.get(i).toLowerCase().equals(user.getNick().toLowerCase() + "," + channel.toLowerCase())) {
     if (user.isVerified()) {
     isOP = true;
     break;
     } else {
     break;
     }
     } else {
     isOP = false;
     }
     }
     } catch (Exception e) {
     logger.logger.writeLog("error", e);
     }
     return isOP;
     }
     */
    /*
     public static void launchRSS() {
     try {
     if (tRSS == null) {
     RSSParser = new RSSFeedParser(RSS);
     tRSS = new Thread(RSSParser);
     tRSS.start();
     } else if (tRSS.isAlive()) {
     RSSParser.stopRSS();
     tRSS = new Thread(RSSParser);
     tRSS.start();
     }
     logger.logger.writeLog("RSS-thread launched.", "general", class);
     } catch (Exception e) {
     logger.logger.writeLog("error", e);
     }
     }
     */
    /*
     public static ArrayList<String> readVoiced() {
     return readFile("voiced.txt");
     }

     public static void writeVoiced(ArrayList<String> opersList) {
     writeFile("voiced.txt", opersList);
     }

     public static ArrayList<String> readOpers() {
     return readFile("opers.txt");
     }

     public static void writeOpers(ArrayList<String> opersList) {
     writeFile("opers.txt", opersList);
     }
     */
}
