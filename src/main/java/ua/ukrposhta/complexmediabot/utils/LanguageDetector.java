package ua.ukrposhta.complexmediabot.utils;

import org.springframework.stereotype.Component;
import com.cybozu.labs.langdetect.Detector;
import com.cybozu.labs.langdetect.DetectorFactory;
import com.cybozu.labs.langdetect.LangDetectException;
import com.cybozu.labs.langdetect.Language;
import ua.ukrposhta.complexmediabot.utils.logger.BotLogger;

import java.io.File;
import java.net.URISyntaxException;
import java.util.ArrayList;

/**
 * @author Zhurenko Evgeniy
 *
 * @apiNote
 * This is class, which can detect language of users according them message typing to bot.
 */

@Component
public class LanguageDetector {

    private void init() throws LangDetectException, URISyntaxException {
        DetectorFactory.loadProfile(new File(Detector.class.getResource("/profiles").toURI()));
    }

    private String detect(String text) throws LangDetectException {
        Detector detector = DetectorFactory.create();
        detector.append(text);
        return detector.detect();
    }

    private ArrayList<Language> detectLangs(String text) throws LangDetectException {
        Detector detector = DetectorFactory.create();
        detector.append(text);
        return detector.getProbabilities();
    }

    public String getLanguage(String text, BotLogger log) {
        String s = "uk";
        try {
            init();
            s = detect(text);
        } catch (Exception e) {
            log.warn("Couldn't detect language");
            log.warn(e.getMessage());
        }
        DetectorFactory.clear();
        return s;
    }

    public ArrayList<Language> getLanguages(String text, BotLogger log){
        ArrayList<Language> languages = new ArrayList<>();
        try {
            init();
            languages = detectLangs(text);
        } catch (Exception e){
            log.warn("Couldn't detect languages");
            log.warn(e.getMessage());
        }
        DetectorFactory.clear();
        return languages;
    }
}
