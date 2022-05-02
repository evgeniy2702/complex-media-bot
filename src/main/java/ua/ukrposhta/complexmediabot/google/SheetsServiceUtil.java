package ua.ukrposhta.complexmediabot.google;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;
import ua.ukrposhta.complexmediabot.utils.logger.BotLogger;
import ua.ukrposhta.complexmediabot.utils.logger.ConsoleLogger;
import ua.ukrposhta.complexmediabot.utils.logger.TelegramLogger;
import ua.ukrposhta.complexmediabot.utils.type.LoggerType;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

/**
 * @author Zhurenko Evgeniy
 *
 * @apiNote
 * This class is service for work with google sheets. It organise access to google sheet by APPLICATION NAME and
 * CREDENTIALS, which was got in Google Cloud when created application with according APPLICATION NAME
 */

@Component
@PropertySource("classpath:properties/google.properties")
public class SheetsServiceUtil {

    private BotLogger consoleLogger = ConsoleLogger.getLogger(LoggerType.CONSOLE);
    private static BotLogger telegramLogger = TelegramLogger.getLogger(LoggerType.TELEGRAM);

    @Value("${google.application.name}")
    private String APPLICATION_NAME = "MediaBot";
    @Value("${google.credentials.file.path}")
    private static String credentials_file_path = "/google-sheets-client-secret.json";


    public Sheets getSheetsService() throws IOException {

        consoleLogger.info("START getSheetsService method in SheetsServiceUtil.class");

        InputStream in = SheetsServiceUtil.class.getResourceAsStream(credentials_file_path);
        if (in == null) {

            telegramLogger.error("Resource not found: " + credentials_file_path);

            throw new FileNotFoundException("Resource not found: " + credentials_file_path);
        }

        List<String> scopes = Arrays.asList(SheetsScopes.SPREADSHEETS, SheetsScopes.DRIVE, SheetsScopes.DRIVE_FILE);

        Sheets sheets = null;

        try {
            GoogleCredentials googleCredentials = GoogleCredentials.fromStream(in).createScoped(scopes);
            sheets = new Sheets.Builder(
                    GoogleNetHttpTransport.newTrustedTransport(),
                    JacksonFactory.getDefaultInstance(),
                    new HttpCredentialsAdapter(googleCredentials))
                    .setApplicationName(APPLICATION_NAME)
                    .build();

        } catch (Exception e){

            consoleLogger.error("ERROR in authorize method in GoogleAuthorizeUtil.class : Sheets not builder .");
            telegramLogger.error("ERROR in authorize method in GoogleAuthorizeUtil.class : Sheets not builder .");
        }

        return sheets;

    }
}
