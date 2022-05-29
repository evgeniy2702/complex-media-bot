package ua.ukrposhta.complexmediabot.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping({"/Complex-Media-Bot/","/"})
public class DebugController {

    @GetMapping("")
    public String getDebugMessageGet(){
        return "<center><br>HELLO DEBUG! COMPLEX_MEDIA_BOT is WORKING!</br></center>";
    }

}
