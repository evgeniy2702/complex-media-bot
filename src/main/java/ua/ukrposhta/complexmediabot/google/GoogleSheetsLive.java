package ua.ukrposhta.complexmediabot.google;

import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.services.sheets.v4.model.BatchGetValuesResponse;
import com.google.api.services.sheets.v4.model.BatchUpdateValuesRequest;
import com.google.api.services.sheets.v4.model.BatchUpdateValuesResponse;
import com.google.api.services.sheets.v4.model.ValueRange;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ua.ukrposhta.complexmediabot.bot.BotContext;
import ua.ukrposhta.complexmediabot.model.user.PersonEntity;
import ua.ukrposhta.complexmediabot.telegramBot.entityUser.TelegramPersonEntity;
import ua.ukrposhta.complexmediabot.utils.logger.BotLogger;
import ua.ukrposhta.complexmediabot.utils.logger.ConsoleLogger;
import ua.ukrposhta.complexmediabot.utils.type.BotType;
import ua.ukrposhta.complexmediabot.utils.type.LoggerType;
import ua.ukrposhta.complexmediabot.viberBot.entityUser.ViberPersonEntity;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author Zhurenko Evgeniy
 *
 * @apiNote
 * This class for work with google sheets. It can to write data to sheets and read from it
 */

@Component
public class GoogleSheetsLive {


    private BotLogger consoleLogger = ConsoleLogger.getLogger(LoggerType.CONSOLE);

    private SheetsServiceUtil sheetsServiceUtil;

    @Autowired
    public void setSheetsServiceUtil(SheetsServiceUtil sheetsServiceUtil) {
        this.sheetsServiceUtil = sheetsServiceUtil;
    }

    @Value("${google.spreadsheet.id}")
    private String SPREADSHEET_ID ;


    public Integer writeDataInExcelSheet(BotContext context,
                                         Integer numberOfCellExcelSheetForMediaRequest,
                                         Integer numberOfRowsForMediaRequest) throws IOException, GeneralSecurityException {

        BotLogger botLogger = BotLogger.getLogger(LoggerType.valueOf(context.getTypeBot().name()));
        consoleLogger.info("START writeDataInExcelSheet method in GoogleSheetsLive.class");

        List<ValueRange> data = new ArrayList<>();

        BotType type = context.getTypeBot();
        ValueRange requestMediaInfo = new ValueRange();

        if(type.equals(BotType.TELEGRAM)) {
            TelegramPersonEntity telegramPerson = context.getTelegramPerson();

            getListValueRangeAccodingBotType(data, telegramPerson, requestMediaInfo, numberOfCellExcelSheetForMediaRequest, numberOfRowsForMediaRequest);
        }

        if(type.equals(BotType.VIBER)){

           ViberPersonEntity viberPerson = context.getViberPerson();

           getListValueRangeAccodingBotType(data, viberPerson, requestMediaInfo, numberOfCellExcelSheetForMediaRequest, numberOfRowsForMediaRequest);
        }

        ValueRange numberOfCellExcel = new ValueRange()
                .setRange("X994")
                .setValues(Collections.singletonList(
                        Collections.singletonList(numberOfCellExcelSheetForMediaRequest + 1)));

        data.add(numberOfCellExcel);

        botLogger.info("requestMediaInfo for write to excel sheet request from media : " + requestMediaInfo.toString());
        botLogger.info("write number of cell excel in cache as sheet : " + numberOfCellExcel.toString());

        try {

            BatchUpdateValuesRequest batchBody = new BatchUpdateValuesRequest()
                    .setValueInputOption("USER_ENTERED")
                    .setData(data);

            BatchUpdateValuesResponse batchResult = sheetsServiceUtil.getSheetsService(type).spreadsheets().values()
                    .batchUpdate(SPREADSHEET_ID, batchBody)
                    .execute();

        }catch (GoogleJsonResponseException google){
            consoleLogger.error("ERROR writeDataInExcelSheet method in GoogleSheetsLive.class : " + google.getMessage());
            consoleLogger.error("CAUSE writeDataInExcelSheet method in GoogleSheetsLive.class : " + google.getCause());
            botLogger.error("ERROR writeDataInExcelSheet method in GoogleSheetsLive.class : " + google.getMessage());
            botLogger.error("CAUSE writeDataInExcelSheet method in GoogleSheetsLive.class : " + google.getCause());
            google.getStackTrace();
        }

        return numberOfCellExcelSheetForMediaRequest + 1;
    }


    public Integer readNumberOfCellExcelSheetFromExcelSheet(BotContext context, int numberOfCellExcelSheet) throws IOException, GeneralSecurityException {

        BotLogger botLogger = BotLogger.getLogger(LoggerType.valueOf(context.getTypeBot().name()));
        consoleLogger.info("start readNumberOfCellExcelSheetFromExcelSheet method in GoogleSheetsLive.class");

        BotType type = context.getTypeBot();

        int numberOfRowsForMediaRequest = 100;

        try {
            List<String> ranges = Arrays.asList("X994","X992");
            BatchGetValuesResponse readResult = sheetsServiceUtil.getSheetsService(type).spreadsheets().values()
                    .batchGet(SPREADSHEET_ID)
                    .setRanges(ranges)
                    .execute();

            numberOfCellExcelSheet = Integer.valueOf(readResult.getValueRanges().get(0)
                                            .getValues().get(0).get(0).toString());

            numberOfRowsForMediaRequest = Integer.valueOf(readResult.getValueRanges().get(1)
                                            .getValues().get(0).get(0).toString());

            botLogger.info("body for write to excel sheet numberOfCellExcelSheet : " + numberOfCellExcelSheet + " ; numberOfRowsForMediaRequest : " + numberOfRowsForMediaRequest);
        }catch (GoogleJsonResponseException google){
            consoleLogger.error("ERROR readNumberOfCellExcelSheetFromExcelSheet method in GoogleSheetsLive.class : " + google.getMessage());
            consoleLogger.error("CAUSE readNumberOfCellExcelSheetFromExcelSheet method in GoogleSheetsLive.class : " + google.getCause());
            botLogger.error("ERROR readNumberOfCellExcelSheetFromExcelSheet method in GoogleSheetsLive.class : " + google.getMessage());
            botLogger.error("CAUSE readNumberOfCellExcelSheetFromExcelSheet method in GoogleSheetsLive.class : " + google.getCause());
            google.getStackTrace();
        }

        if(numberOfCellExcelSheet == 0)
            numberOfCellExcelSheet = 2;

        return writeDataInExcelSheet(context, numberOfCellExcelSheet, numberOfRowsForMediaRequest);
    }

    private void getListValueRangeAccodingBotType(List<ValueRange> data, PersonEntity personEntity, ValueRange requestMediaInfo,
                                                              Integer numberOfCellExcelSheetForMediaRequest,
                                                              Integer numberOfRowsForMediaRequest){

            if (numberOfCellExcelSheetForMediaRequest == numberOfRowsForMediaRequest)
                numberOfCellExcelSheetForMediaRequest = 2;

        requestMediaInfo
                    .setRange("A" + numberOfCellExcelSheetForMediaRequest)
                    .setValues(Collections.singletonList(
                            Arrays.asList(LocalDate.now().toString(), personEntity.getMediaName(), personEntity.getName_surname(), personEntity.getSubject(),
                                    personEntity.getPhone(), personEntity.getEmail())));
            data.add(requestMediaInfo);
    }
}
