package flashbot;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Calendar;
import javax.swing.JOptionPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Loghandler {

    public static void displayError(String error) {
        JOptionPane.showMessageDialog(null, error, "An error has occurred!", JOptionPane.ERROR_MESSAGE);
    }

    public static void writeLog(String line, String logType, String curClass) {
        Logger log = LoggerFactory.getLogger(curClass);
        Calendar date = Calendar.getInstance();
        try {
            Writer toFile = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("log.txt", true)));
            switch (logType) {
                case "general":
                    log.info("General: " + line);
                    toFile.append(date.getTime().toString() + " General: " + line + "\n");
                    break;
                case "usercommand":
                    log.error("Usercommand: " + line);
                    toFile.append(date.getTime().toString() + " Usercommand: " + line + "\n");
                    break;
                case "other":
                    log.debug("Other: " + line);
                    toFile.append(date.getTime().toString() + " Other: " + line + "\n");
                    break;
            }
            toFile.close();
        } catch (Exception ex) {
            displayError(ex.toString());
        }
    }

    public static void writeLog(String line, Exception e) {
        Logger log = LoggerFactory.getLogger(e.getClass());
        Calendar date = Calendar.getInstance();
        try {
            Writer toFile = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("log.txt", true)));
            log.error("Error: " + line + " Trace: " + formatError(e));
            toFile.append(date.getTime().toString() + " Error: " + line + " Trace: " + formatError(e)  + "\n");
            toFile.close();
        } catch (Exception ex) {
            displayError(ex.toString());
        }
    }

    private static String formatError(Exception e) {
        String error = e.toString()
                + " | In class: "
                + e.getStackTrace()[1].getClassName()
                + " | In method: "
                + e.getStackTrace()[1].getMethodName()
                + " | On line: "
                + e.getStackTrace()[1].getLineNumber();
        return error;
    }

}
