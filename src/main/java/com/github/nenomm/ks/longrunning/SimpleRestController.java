package com.github.nenomm.ks.longrunning;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Profile("longRunning")
@RestController
@RequestMapping("punctuator")
public class SimpleRestController {
    private static final Logger logger = LoggerFactory.getLogger(SimpleRestController.class);
    private static int DEFAULT_DURATION = 10;

    @Autowired
    private LongRunningExample longRunningExample;

    @GetMapping("/pause")
    public void pause(@RequestParam String sleepDuration) {
        logger.info("Attempting to pause punctuator...");
        longRunningExample.pausePunctuator(parseAndValidateDuration(sleepDuration));
    }

    int parseAndValidateDuration(String sleepDuration) {
        int duration = DEFAULT_DURATION;

        try {
            duration = Integer.parseInt(sleepDuration);
        } catch (NumberFormatException | NullPointerException e) {
            logger.warn("value [{}] for sleepDuration is not parsable. Using default duration {}", sleepDuration, duration);
        }

        return duration;
    }

}
