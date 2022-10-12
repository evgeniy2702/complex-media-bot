package ua.ukrposhta.complexmediabot.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ua.ukrposhta.complexmediabot.bot.BotViber;
import ua.ukrposhta.complexmediabot.bot.TelegramBot;
import ua.ukrposhta.complexmediabot.controller.MainController;
import ua.ukrposhta.complexmediabot.utils.logger.BotLogger;
import ua.ukrposhta.complexmediabot.utils.type.LoggerType;

/**
 * @author Zhurenko Evgeniy created 30.08.2022 inside the package - ua.ukrposhta.mediabot.telegram.service
 * @apiNote
 * This is class for cleaning cache of users in view of Map<K, V>
 */

@Service
@EnableScheduling
@PropertySource("classpath:application.properties")
public class CacheUsersCleanerSchedule {

    @Value("${cron}")
    protected String cron;

    private BotLogger telegramLogger = BotLogger.getLogger(LoggerType.TELEGRAM);
    private BotLogger viberLogger = BotLogger.getLogger(LoggerType.VIBER);
    private BotLogger consoleLogger = BotLogger.getLogger(LoggerType.CONSOLE);
    private TelegramBot telegramBot;
    private BotViber viberBot;
    private MainController controller;

    public CacheUsersCleanerSchedule (TelegramBot telegramBot,
                                      BotViber viberBot,
                                      MainController controller) {
        this.telegramBot = telegramBot;
        this.viberBot = viberBot;
        this.controller = controller;
    }

    @Scheduled(cron = "0 0 1 ? * SUN")
    public void cleaner(){
        consoleLogger.info("start cleaner() in CacheUsersCleanerSchedule.class");
        telegramLogger.info("CacheUsersCleanerSchedule.class cleaner() cron =" + cron);
        telegramLogger.info("CacheUsersCleanerSchedule.class before cleaner() users cache size = " + telegramBot.getTelegramPersons().size());
        telegramBot.getTelegramPersons().clear();
        telegramLogger.info("CacheUsersCleanerSchedule.class after cleaner() users cache size = " + telegramBot.getTelegramPersons().size());
        viberLogger.info("CacheUsersCleanerSchedule.class cleaner() cron =" + cron);
        viberLogger.info("CacheUsersCleanerSchedule.class before cleaner() users cache size = " + telegramBot.getTelegramPersons().size());
        viberBot.getViberPersons().clear();
        controller.getPersons().clear();

    }
}