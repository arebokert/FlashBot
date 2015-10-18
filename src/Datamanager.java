package flashbot;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Properties;
import java.util.TimeZone;
import org.pircbotx.Channel;

public class Datamanager {

    public Datamanager() {

    }

    private String getTime() {
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        calendar.setTimeZone(TimeZone.getTimeZone("Europe/Stockholm"));
        return dateFormat.format(calendar.getTime());
    }

    public String[] readPropertyFromFile(String key, String fileString, boolean split) {
        Properties prop = new Properties();
        InputStream input = null;
        try {
            if (new File(fileString).exists()) {
                input = new FileInputStream(fileString);
                prop.load(input);
                String tempString = prop.getProperty(key);
                if (tempString != null) {
                    String[] tempArray = null;
                    if (split) {
                        tempArray = tempString.split(";;,;;");
                    } else {
                        tempArray = new String[]{tempString};
                    }
                    return tempArray;
                } else {
                    return null;
                }
            } else {
                return null;
            }
        } catch (Exception e) {
            Loghandler.writeLog("Could not read property: " + key + " from file: " + fileString, e);
            return null;
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (Exception e) {
                    Loghandler.writeLog("Could not close reatproperty output", e);
                }
            }
        }
    }

    private void writeProperty(String key, String setting, String fileString) {
        Properties prop = new Properties();
        OutputStream output = null;
        InputStream input = null;
        try {
            if (new File(fileString).exists()) {
                input = new FileInputStream(fileString);
                prop.load(input);
                input.close();
            }
            output = new FileOutputStream(fileString);
            prop.setProperty(key, setting);
            prop.store(output, null);
        } catch (Exception e) {
            Loghandler.writeLog("Could not write property: " + key + " - " + setting + " to file: " + fileString, e);
        } finally {
            if (output != null) {
                try {
                    output.close();
                } catch (Exception e) {
                    Loghandler.writeLog("Could not close writeproperty output", e);
                }
            }

        }
    }

    public void saveSeenUser(String user, String channel, String message, boolean identified) {
        String date = getTime();
        String fileString = "seen" + channel + ".txt";
        String[] userGet = readUserSeen(user, channel);
        try {
            if (identified) {
                writeProperty(user.toLowerCase(), true + ";;,;;" + date + ";;,;;" + message, fileString);
            } else {
                if (userGet.length == 2) {
                    writeProperty(user.toLowerCase(), false + ";;,;;" + date + ";;,;;" + message, fileString);
                } else if (!Boolean.parseBoolean(userGet[0])) {
                    writeProperty(user.toLowerCase(), false + ";;,;;" + date + ";;,;;" + message, fileString);
                }
            }
        } catch (Exception e) {
            Loghandler.writeLog("Could not save user " + user + " as seen", e);
        }
    }

    public void saveAlias(String alias, String user, String message) {
        String fileString = "alias.txt";
        try {
            writeProperty(alias.toLowerCase(), user + ";;,;;" + message, fileString);
        } catch (Exception e) {
            Loghandler.writeLog("Could not save alias " + alias, e);
        }
    }

    public String[] readPU(String user, String channel) {
        String fileString = "poweruser" + channel + ".txt";
        try {
            if (new File(fileString).exists()) {
                String[] userGet = readPropertyFromFile(user, fileString, false);
                if (userGet == null) {
                    return null;
                } else {
                    return userGet;
                }
            } else {
                return null;
            }
        } catch (Exception e) {
            Loghandler.writeLog("Could not read poweruser " + user + " as poweruser or not", e);
            return null;
        }
    }

    public String[] readAlias(String alias, String channel) {
        String fileString = "alias.txt";
        try {
            if (new File(fileString).exists()) {
                String[] userGet = readPropertyFromFile(alias.toLowerCase(), fileString, true);
                if (userGet == null) {
                    return null;
                } else {
                    if (userGet[1].equals(" ")) {
                        return null;
                    } else {
                        return userGet;
                    }
                }
            } else {
                return null;
            }
        } catch (Exception e) {
            Loghandler.writeLog("Could not read alias " + alias + " in channel " + channel, e);
            return null;
        }
    }

    public String[] readUserSeen(String user, String channel) {
        String fileString = "seen" + channel + ".txt";
        try {
            if (new File(fileString).exists()) {
                String userGet[] = readPropertyFromFile(user.toLowerCase(), fileString, true);
                if (userGet == null) {
                    String[] temp = new String[3];
                    temp[1] = "Never";
                    temp[2] = " ";
                    return temp;
                } else {
                    return userGet;
                }
            } else {
                String[] temp = new String[3];
                temp[1] = "Never";
                temp[2] = " ";
                return temp;
            }
        } catch (Exception e) {
            Loghandler.writeLog("Could not read user " + user + " as seen or not", e);
            return null;
        }
    }

    public void setPUUSerState(String user, String channel, boolean poweruser) {
        String fileString = "poweruser" + channel + ".txt";
        try {
            if (poweruser) {
                writeProperty(user, "true", fileString);
            } else {
                writeProperty(user, "false", fileString);
            }
        } catch (Exception e) {
            Loghandler.writeLog("Could not set user " + user + " as poweruser", e);
        }
    }

    public void setIgnoreUserState(String user, String channel, boolean ignored) {
        String fileString = "ignored" + channel + ".txt";
        try {
            if (ignored) {
                writeProperty(user, "true", fileString);
            } else {
                writeProperty(user, "false", fileString);
            }
        } catch (Exception e) {
            Loghandler.writeLog("Could not set user " + user + " as ignored", e);
        }
    }

    public boolean isPU(String user, String channel) {
        String[] userGet = readPU(user, channel);
        if (userGet == null) {
            return false;
        } else {
            return Boolean.parseBoolean(userGet[0]);
        }
    }

    public boolean isIgnored(String user, Channel channel) {
        String fileString = "ignored" + channel.getName() + ".txt";
        try {
            String ignored[] = readPropertyFromFile(user, fileString, false);
            if (ignored != null) {
                return ignored[0].equals("true");
            } else {
                return false;
            }
        } catch (Exception e) {
            Loghandler.writeLog("Could not check if user " + user + " was ignored", e);
            return false;
        }
    }
}
