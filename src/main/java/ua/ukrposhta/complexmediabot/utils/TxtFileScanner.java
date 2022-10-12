package ua.ukrposhta.complexmediabot.utils;

import lombok.Getter;
import lombok.Setter;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import ua.ukrposhta.complexmediabot.utils.logger.BotLogger;
import ua.ukrposhta.complexmediabot.utils.type.LoggerType;

import java.io.*;
import java.util.HashMap;

/**
 * @author Zhurenko Evgeniy created 11.10.2022 inside the package - ua.ukrposhta.complexmediabot.utils
 * @apiNote
 * This is a .txt files scanner for reading, deleting and writing lines.
 */

@Component
@Getter
@Setter
public class TxtFileScanner {

    private final BotLogger consoleLogger = BotLogger.getLogger(LoggerType.CONSOLE);

    public HashMap<String, String> getPiarToHashMap(String path, BotLogger logger) throws IOException {
        consoleLogger.info("start getPiarToHashMap() in TxtFileScaner.class");
        logger.info("getPiarToHashMap : " + path);
        File file = new ClassPathResource(path).getFile();
        String line;
        HashMap<String, String> map = new HashMap<>();
        try(BufferedReader reader =
                    new BufferedReader(
                            new FileReader(file))){
            while((line = reader.readLine()) != null){
                String[] keyValuePair = line.split(":", 2);
                if(keyValuePair.length > 1) {
                    String key = keyValuePair[0];
                    String value = keyValuePair[1];
                    logger.info("getPiarToHashMap : " + key + ":" + value);
                    map.put(key, value);
                } else {
                    logger.warn("No Key:Value found in line, ignoring: " + line);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            for(StackTraceElement element : e.getStackTrace()){
                logger.error(element.toString());
            }
        }

        return map;
    }

    public HashMap<String, String> addPiarToFile(String path, String keyValue, BotLogger logger) throws IOException {
        consoleLogger.info("start addPiarToFile() in TxtFileScaner.class");
        logger.info("addPiarToFile : " + path);
        File file = new ClassPathResource(path).getFile();
        try (Writer write = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(file, true), "UTF-8"))){
            write.write("\n" + keyValue);
            write.flush();
        } catch (IOException e) {
            e.printStackTrace();
            for(StackTraceElement element : e.getStackTrace()){
                logger.error(element.toString());
            }
        }

        return getPiarToHashMap(path, logger);

    }

    public HashMap<String, String> deletePiarFromFile(String path, String keyNew, BotLogger logger) throws IOException {
        consoleLogger.info("start deletePiarFromFile() in TxtFileScaner.class");
        logger.info("deletePiarFromFile : " + path);
        String line;
        File file = new ClassPathResource(path).getFile();
        StringBuilder str = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))){
            while((line = reader.readLine()) != null){
                String[] keyValuePair = line.split(":", 2);
                if(keyValuePair.length > 1) {
                    String key = keyValuePair[0];
                    String value = keyValuePair[1];
                    if(!key.equals(keyNew)) {
                        str = str.append(key).append(":").append(value).append("\n");
                    }
                } else {
                    System.out.println("No Key:Value found in line, ignoring: " + line);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
            for (StackTraceElement element : e.getStackTrace()) {
                logger.error(element.toString());
            }
        }

        try (Writer write = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(file), "UTF-8"))){
            write.write(str.toString());
            write.flush();
        } catch (IOException e) {
            e.printStackTrace();
            for (StackTraceElement element : e.getStackTrace()) {
                logger.error(element.toString());
            }
        }
        return getPiarToHashMap(path, logger);

    }

}
