package controllers.util;

import play.Play;

import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by Strelchenko Vadym on 29.05.15.
 */
public class Utils {
    public static final String DEFAULT_RESOURCE_FOLDER = "conf/resources/";
    public static final String DEFAULT_STATS_CSV_FILE = "dataset.csv";

    public static void eraseFileContent() throws IOException {
        new FileWriter(Play.application().getFile(Utils.DEFAULT_RESOURCE_FOLDER + Utils.DEFAULT_STATS_CSV_FILE)).close();
    }

    public static void writeDataToFile(String data) throws IOException {
        FileWriter cswWriter = new FileWriter(Play.application().getFile(Utils.DEFAULT_RESOURCE_FOLDER + Utils.DEFAULT_STATS_CSV_FILE));
        cswWriter.write(data);
        cswWriter.close();
    }
}
