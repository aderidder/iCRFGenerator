package icrfgenerator.utils;

import icrfgenerator.settings.GlobalSettings;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

/**
 * stuff related to getting codebooks
 */
public class RestCalls {
    private static final Logger logger = LogManager.getLogger(RestCalls.class.getName());

    /**
     * checks whether a file exists locally
     * if so, it returns it
     * if not, it downloads it
     * @param uri      where to find the file if it needs to be downloaded
     * @param fileName the file to find
     * @return the file
     */
    public static File getFile(String uri, String fileName){
        File dataFile = new File(fileName);
        try {
            // check whether the file exists and download it if it doesn't
            // this is time-consuming; hence we're saving it locally
            if(!dataFile.exists()){
                downloadFile(uri, fileName);
            }
            logger.log(Level.INFO, "Opening the codebook file: "+dataFile.getName());
            return dataFile;
        } catch (IOException e) {
            throw new RuntimeException("There was an issue downloading the source file... This is fatal...");
        }
    }

    /**
     * downloads a codebook from a URI
     * @param uri       uri where the data will be retrieved
     * @param fileName  name of the file locally
     * @throws IOException exception
     */
    private static void downloadFile(String uri, String fileName) throws IOException{
        logger.log(Level.INFO, "Retrieving a codebook using "+uri);

        // open a url connection
        URL url = new URL(uri);
        URLConnection urlConnection = url.openConnection();

        // set the connection timeout as specified in the global settings
        urlConnection.setConnectTimeout(GlobalSettings.getCodebookConnectionTimeout());
        urlConnection.setReadTimeout(GlobalSettings.getCodebookReadTimeout());

        // get a stream to read data from
        urlConnection.connect();
        ReadableByteChannel readableByteChannel = Channels.newChannel(urlConnection.getInputStream());
        FileOutputStream fileOutputStream = new FileOutputStream(fileName);
        fileOutputStream.getChannel().transferFrom(readableByteChannel, 0, Long.MAX_VALUE);

    }

    /**
     * a general REST call. Returns a String of whatever the output is
     * @param uri the call's URI
     * @return a String of the output
     * @throws IOException issue with the call
     */
    public static String generalCall(String uri) throws IOException {
        try (Scanner scanner = new Scanner(new URL(uri).openStream(), StandardCharsets.UTF_8)){
            scanner.useDelimiter("\\A");
            return scanner.hasNext() ? scanner.next() : "";
        }
    }
}
