package ua.ukrposhta.complexmediabot.controller;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ua.ukrposhta.complexmediabot.bot.BotViber;
import ua.ukrposhta.complexmediabot.bot.TelegramBot;
import ua.ukrposhta.complexmediabot.telegramBot.entityUser.TelegramPersonEntity;
import ua.ukrposhta.complexmediabot.utils.TxtFileScanner;
import ua.ukrposhta.complexmediabot.utils.logger.BotLogger;
import ua.ukrposhta.complexmediabot.utils.type.LoggerType;
import ua.ukrposhta.complexmediabot.viberBot.entityUser.ViberPersonEntity;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * @author Zhurenko Evgeniy created 11.10.2022 inside the package - ua.ukrposhta.complexmediabot.controller
 * @apiNote
 * This is controller for control a count of users that using bot and do not press 'finish work' key. Also it has the
 * opportunity to delete users from cache by force. This controller is also used to add, delete and draw the user_id
 * of a person who has the ability to receive a media request message from the bot.
 */
@Slf4j
@RestController
@RequestMapping ({"/Complex-Media-Bot/cache","/cache"})
@ResponseStatus(HttpStatus.OK)
@Getter
public class PersonsController {

    private BotLogger telegramLogger = BotLogger.getLogger(LoggerType.TELEGRAM);
    private BotLogger viberLogger = BotLogger.getLogger(LoggerType.VIBER);
    private BotLogger consoleLogger = BotLogger.getLogger(LoggerType.CONSOLE);
    private TelegramBot telegram;
    private BotViber viber;
    @Value ("${file.path.telegram}")
    private String pathTelegram;
    @Value ("${file.path.viber}")
    private String pathViber;
    private MainController controller;
    private TxtFileScanner scanner;

    public PersonsController (TelegramBot telegram,
                              BotViber viber,
                              MainController controller,
                              TxtFileScanner scanner) {
        this.telegram = telegram;
        this.viber = viber;
        this.controller = controller;
        this.scanner = scanner;
    }

    @GetMapping (value = {"all-bots"}, produces = "text/plain;charset=UTF-8")
    public ResponseEntity<String> personsCache() {
        consoleLogger.info("start personCache() in PersonsController.class");
        return getResponseEntityAllBots();
    }

    @PostMapping (value = {"telegram/delete"}, produces = "text/plain;charset=UTF-8")
    public ResponseEntity<String> deleteTelegramCacheById(@RequestParam (name = "id", required = false) String id) {
        consoleLogger.info("start deleteTelegramCacheById() in PersonsController.class");
        telegram.getTelegramPersons().remove(id);
        controller.getPersons().remove(id);
        return getResponseEntityAllBots();
    }

    @GetMapping(value = {"telegram/delete-all"}, produces = "text/plain;charset=UTF-8")
    public ResponseEntity<String> deleteAllTelegramCache() {
        consoleLogger.info("start deleteAllTelegramCache() in PersonsController.class");
        for(String key : telegram.getTelegramPersons().keySet()){
            controller.getPersons().remove(key);
        }
        telegram.getTelegramPersons().clear();
        return getResponseEntityAllBots();
    }


    @PostMapping (value = {"viber/delete"}, produces = "text/plain;charset=UTF-8")
    public ResponseEntity<String> deleteViberCacheById(@RequestParam (name = "id", required = false) String id) {
        consoleLogger.info("start deleteViberCacheById() in PersonsController.class");
        viber.getViberPersons().remove(id);
        controller.getPersons().remove(id);
       return getResponseEntityAllBots();
    }

    @GetMapping(value = {"viber/delete-all"}, produces = "text/plain;charset=UTF-8")
    public ResponseEntity<String> deleteAllViberCache() {
        consoleLogger.info("start deleteAllViberCache() in PersonsController.class");
        for(String key : viber.getViberPersons().keySet()){
            controller.getPersons().remove(key);
        }
        viber.getViberPersons().clear();
        return getResponseEntityAllBots();
    }

    @GetMapping(value = {"delete-all-bots"}, produces = "text/plain;charset=UTF-8")
    public ResponseEntity<String> deleteAllCache() {
        consoleLogger.info("start deleteAllCache() in PersonsController.class");
        controller.getPersons().clear();
        telegram.getTelegramPersons().clear();
        viber.getViberPersons().clear();
        return getResponseEntityAllBots();
    }

    @GetMapping(value = {"all-piars"}, produces = "text/plain;charset=UTF-8")
    public ResponseEntity<String> piarsCache() throws IOException {
        consoleLogger.info("start piarsCache() in PersonsController.class");
        return getResponseEntityAllPiars();
    }

    @PostMapping(value = {"add-telegram-piar"}, produces = "text/plain;charset=UTF-8")
    public ResponseEntity<String> addTelegramCachePiar(@RequestParam(value = "new_id", required = false) String id,
                                                       @RequestParam(value = "new_name", required = false) String name) throws IOException {
        consoleLogger.info("start addTelegramCachePiar() in PersonsController.class");
        scanner.addPiarToFile(pathTelegram, id + ":" + name, telegramLogger);
        return getResponseEntityAllPiars();
    }

    @PostMapping(value = {"delete-telegram-piar"}, produces = "text/plain;charset=UTF-8")
    public ResponseEntity<String> deleteTelegramCachePiarsById(@RequestParam (name = "piar_id", required = false) String id) throws IOException {
        consoleLogger.info("start deleteAllCachePiars() in PersonsController.class");
        scanner.deletePiarFromFile(pathTelegram, id, telegramLogger);
        return getResponseEntityAllPiars();
    }


    @PostMapping(value = {"add-viber-piar"}, produces = "text/plain;charset=UTF-8")
    public ResponseEntity<String> addViberCachePiar(@RequestParam(value = "new_id", required = false) String id,
                                                    @RequestParam(value = "new_name", required = false) String name) throws IOException {
        consoleLogger.info("start addViberCachePiar() in PersonsController.class");
        scanner.addPiarToFile(pathViber, id + ":" + name, viberLogger);
        return getResponseEntityAllPiars();
    }

    @PostMapping(value = {"delete-viber-piar"}, produces = "text/plain;charset=UTF-8")
    public ResponseEntity<String> deleteViberCachePiarsById(@RequestParam (name = "piar_id", required = false) String id) throws IOException {
        consoleLogger.info("start deleteViberCachePiarsById() in PersonsController.class");
        scanner.deletePiarFromFile(pathViber, id, viberLogger);
        return getResponseEntityAllPiars();
    }

    @GetMapping(value = {"delete-all-piars"}, produces = "text/plain;charset=UTF-8")
    public ResponseEntity<String> deleteAllCachePiars() throws IOException {
        consoleLogger.info("start deleteAllCachePiars() in PersonsController.class");
        for(String keyTelegram : scanner.getPiarToHashMap(pathTelegram, telegramLogger).keySet()) {
            scanner.deletePiarFromFile(pathTelegram, keyTelegram, telegramLogger);
        }
        for(String keyViber : scanner.getPiarToHashMap(pathViber, viberLogger).keySet()) {
            scanner.deletePiarFromFile(pathViber, keyViber, viberLogger);
        }
        return getResponseEntityAllPiars();
    }

    @NotNull
    private ResponseEntity<String> getResponseEntityAllBots () {
        StringBuilder builder = new StringBuilder("<center>Список користувачів, які зараз використовують ботів : ")
                .append("<ol><li>в ТЕЛЕГРАМ боті - ")
                .append(telegram.getTelegramPersons().size())
                .append(";</li><li>в ВАЙБЕР боті - ")
                .append(viber.getViberPersons().size())
                .append(";</li></ol></center>");
        telegramLogger.info("PersonsController.class getResponseEntityAllBots() users cache size is " + telegram.getTelegramPersons().size());
        viberLogger.info("PersonsController.class getResponseEntityAllBots() users cache size is " + viber.getViberPersons().size());
        Map<String, TelegramPersonEntity> telegramPersons = telegram.getTelegramPersons();
        Map<String, ViberPersonEntity> viberPersons = viber.getViberPersons();
        return getUsersHTML(telegramPersons, viberPersons, builder);
    }

    @NotNull
    private ResponseEntity<String> getResponseEntityAllPiars() throws IOException {
        StringBuilder builder = new StringBuilder("<center>Список співробітників, які отримують сповіщення від ботів для ЗМІ : ")
                .append("<ol><li>в ТЕЛЕГРАМ боті - ")
                .append(scanner.getPiarToHashMap(pathTelegram, telegramLogger).size())
                .append(";</li><li>в ВАЙБЕР боті - ")
                .append(scanner.getPiarToHashMap(pathViber, viberLogger).size())
                .append(";</li></ol></center>");
        telegramLogger.info("PersonsController.class getResponseEntityAllPiars() piars cache size is " +
                scanner.getPiarToHashMap(pathTelegram, telegramLogger).size());
        viberLogger.info("PersonsController.class getResponseEntityAllPiars() piars cache size is " +
                scanner.getPiarToHashMap(pathViber, viberLogger).size());
        return getPiarsHTML(scanner.getPiarToHashMap(pathTelegram, telegramLogger),
                scanner.getPiarToHashMap(pathViber, viberLogger), builder);
    }

    private ResponseEntity<String> getUsersHTML (Map<String, TelegramPersonEntity> telegramPersons,
                                                 Map<String, ViberPersonEntity> viberPersons, StringBuilder builder) {

        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set("Content-Type","text/html;charset=UTF-8");
        List<String> keysTelegram = telegramPersons.entrySet().stream().sorted(Comparator.comparing(Entry::getKey))
                .map(Entry::getKey).collect(Collectors.toList());
        builder.append("<hr style='width:50%;text-align:left;margin-left:0'><center>ТЕЛЕГРАММ БОТ перелік юзерів : </center>");
        for(String key : keysTelegram){
            telegramLogger.info("PersonsController.class getUsersHTML() method telegram_key : " + key);
            builder.append("<form id='del_telegram_").append(telegramPersons.get(key).getIncomTelegramMessage().getChat_id())
                    .append("' action='/Complex-Media-Bot/cache/telegram/delete' method='post'><p><input name='id' form='del_telegram_")
                    .append(telegramPersons.get(key).getIncomTelegramMessage().getChat_id()).append("' value=")
                    .append(telegramPersons.get(key).getIncomTelegramMessage().getChat_id()).append( "></p>")
                    .append("<p><input type='submit' form='del_telegram_").append(telegramPersons.get(key).getIncomTelegramMessage().getChat_id())
                    .append("' value='DELETE'></p></form>")
                    .append("<p>name_surname : ")
                    .append(telegramPersons.get(key).getName_surname())
                    .append( ";</p>")
                    .append("<p>media : ")
                    .append(telegramPersons.get(key).getMediaName())
                    .append( ";</p>")
                    .append("<p>phone : ").append(telegramPersons.get(key).getPhone()).append( ";</p>")
                    .append("<p>email : ").append(telegramPersons.get(key).getEmail()).append( ";</p>")
                    .append("<p>subject : ")
                    .append(telegramPersons.get(key).getSubject())
                    .append( ";</p>");
        }
        if(telegramPersons.size() != 0) {
            builder.append("<form id='del_telegram' action='/Complex-Media-Bot/cache/telegram/delete-all' method='get'>")
                    .append("<p><input type='submit' form='del_telegram' value='DELETE ALL TELEGRAM'></p></form>");
        }

        List<String> keysViber = viberPersons.entrySet().stream().sorted(Comparator.comparing(Entry::getKey))
                .map(Entry::getKey).collect(Collectors.toList());
        builder.append("<hr style='width:50%;text-align:left;margin-left:0'><center>ВАЙБЕР БОТ перелік юзерів : </center>");
        for(String key : keysViber){
            viberLogger.info("PersonsController.class getUsersHTML() method viber_key : " + key);
            builder.append("<form id='del_viber_").append(viberPersons.get(key).getViberSender().getId())
                    .append("' action='/Complex-Media-Bot/cache/viber/delete' method='post'><p><input name='id' form='del_viber_")
                    .append(viberPersons.get(key).getViberSender().getId()).append("' value=")
                    .append(viberPersons.get(key).getViberSender().getId()).append( "></p>")
                    .append("<p><input type='submit' form='del_viber_").append(viberPersons.get(key).getViberSender().getId())
                    .append("' value='DELETE'></p></form>")
                    .append("<p>name_surname : ")
                    .append(viberPersons.get(key).getName_surname())
                    .append( ";</p>")
                    .append("<p>media : ")
                    .append(viberPersons.get(key).getMediaName())
                    .append( ";</p>")
                    .append("<p>phone : ").append(viberPersons.get(key).getPhone()).append( ";</p>")
                    .append("<p>email : ").append(viberPersons.get(key).getEmail()).append( ";</p>")
                    .append("<p>subject : ")
                    .append(viberPersons.get(key).getSubject())
                    .append( ";</p>");
        }
        if(viberPersons.size() != 0) {
            builder.append("<form id='del_viber' action='/Complex-Media-Bot/cache/viber/delete-all' method='get'>")
                    .append("<p><input type='submit' form='del_viber' value='DELETE ALL VIBER'></p></form>");
        }
        if(telegramPersons.size() != 0 && viberPersons.size() != 0){
            builder.append("<hr style='width:50%;text-align:left;margin-left:0'>")
                    .append("<form id='del_all_bots' action='/Complex-Media-Bot/cache/delete-all-bots' method='get'>")
                    .append("<p><input type='submit' form='del_all_bots' value='DELETE ALL BOTS'></p></form>");
        }
        return ResponseEntity.ok()
                .headers(responseHeaders)
                .body(builder.toString());
    }

    private ResponseEntity<String> getPiarsHTML (Map<String, String> telegramPiars,
                                                 Map<String, String> viberPiars, StringBuilder builder){
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set("Content-Type","text/html;charset=UTF-8");
        List<String> keysTelegram = telegramPiars.entrySet().stream().sorted(Comparator.comparing(Entry::getKey))
                .map(Entry::getKey).collect(Collectors.toList());
        for(String key : keysTelegram){
            telegramLogger.info("PersonsController.class getPiarsHTML() method telegram_piar_key : " + key);
            builder.append("<form id='telegram_piar_" + key + "' action='/Complex-Media-Bot/cache/delete-telegram-piar' method='post'>")
                    .append("<p><label for=piar_id'>ID співробітника отримуючого сповіщення від боту для ЗМІ : </label>")
                    .append("<br/><input id='piar_id' name='piar_id' form='telegram_piar_" + key + "' value=")
                    .append(key).append( "></p>")
                    .append("<p>ПІБ співробітника : ")
                    .append(telegramPiars.get(key))
                    .append( ";</p>")
                    .append("<p><input type='submit' form='telegram_piar_" + key + "' value='ВИДАЛИТИ'></p></form>");
        }
        builder.append("<hr style='width:50%;text-align:left;margin-left:0'>")
                .append("<center>Додати співробітника, які отримаують сповіщення від ТЕЛЕГРАМ БОТУ для ЗМІ</center>")
                .append("<form id='add_telegram_piar' action='/Complex-Media-Bot/cache/add-telegram-piar' method='post'>")
                .append("<p><label for='new_telegram_id'>Введіть ID телеграмм співробітника для отримання сповіщення від ТЕЛЕГРАМ боту для ЗМІ : </label>")
                .append("<br/><input id='new_telegram_id' name='new_id' form='add_telegram_piar'></p>")
                .append("<p><label for='new_telegram_name'>Введіть ім'я та прізвище співробітника : </label>")
                .append("<br/><input id='new_telegram_name' name='new_name' form='add_telegram_piar'></p>")
                .append("<p><input type='submit' form='add_telegram_piar' value='ДОДАТИ'></p></form>");

        List<String> keysViber = viberPiars.entrySet().stream().sorted(Comparator.comparing(Entry::getKey))
                .map(Entry::getKey).collect(Collectors.toList());
        for(String key : keysViber){
            viberLogger.info("PersonsController.class getPiarsHTML() method viber_piar_key : " + key);
            builder.append("<form id='viber_piar_" + key + "' action='/Complex-Media-Bot/cache/delete-viber-piar' method='post'>")
                    .append("<p><label for='piar_id'>ID співробітника отримуючого сповіщення від ВАЙБЕР боту для ЗМІ : </label>")
                    .append("<br/><input id='piar_id' name='piar_id' form='viber_piar_" + key + "' value=")
                    .append(key).append( "></p>")
                    .append("<p>ПІБ співробітника : ")
                    .append(viberPiars.get(key))
                    .append( ";</p>")
                    .append("<p><input type='submit' form='viber_piar_" + key + "' value='ВИДАЛИТИ'></p></form>");
        }
        builder.append("<hr style='width:50%;text-align:left;margin-left:0'>")
                .append("<center>Додати співробітника, які отримаують сповіщення від ВАЙБЕР БОТУ для ЗМІ</center>")
                .append("<form id='add_viber_piar' action='/Complex-Media-Bot/cache/add-viber-piar' method='post'>")
                .append("<p><label for='new_viber_id'>Введіть ID телеграмм співробітника для отримання сповіщення від боту для ЗМІ : </label>")
                .append("<br/><input id='new_viber_id' name='new_id' form='add_viber_piar'></p>")
                .append("<p><label for='new_viber_name'>Введіть ім'я та прізвище співробітника : </label>")
                .append("<br/><input id='new_viber_name' name='new_name' form='add_viber_piar'></p>")
                .append("<p><input type='submit' form='add_viber_piar' value='ДОДАТИ'></p></form>");

        if(telegramPiars.size() != 0 && viberPiars.size() != 0) {
                builder.append("<hr style='width:50%;text-align:left;margin-left:0'>")
                    .append("<center>Видалити всіх співробітників, які отримаують сповіщення від ТЕЛЕГРАМ та ВАЙБЕР БОТІВ для ЗМІ</center>")
                    .append("<form id='del_all_bots' action='/Complex-Media-Bot/cache/delete-all-piars' method='get'>")
                    .append("<p><input type='submit' form='del_all_piars' value='ВИДАЛИТИ ВСІХ'></p></form>");
        }
        return ResponseEntity.ok()
                .headers(responseHeaders)
                .body(builder.toString());
    }
}
