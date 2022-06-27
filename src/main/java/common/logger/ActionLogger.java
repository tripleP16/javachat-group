package common.logger;

import common.logger.actor.Actor;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class ActionLogger {
    private final Logger logger = Logger.getLogger("defaultLogger");

    public ActionLogger(String filePrefix) {
        FileHandler fileHandler = null;
        LocalDateTime today = LocalDateTime.now();
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyyMMdd");

        try {
            fileHandler = new FileHandler(filePrefix + dateFormatter.format(today) + ".log");
        } catch (IOException e) {
            e.printStackTrace();
        }
        assert fileHandler != null;
        SimpleFormatter formatter = new SimpleFormatter();
        fileHandler.setFormatter(formatter);
        logger.addHandler(fileHandler);
    }
    public void log(String actionName, Actor actor, int quantity) {
        logger.info("Actor:[" + actor.getName() + "] -> Accion:[" + actionName + "]" + "[" + quantity + "]");
    }

    public void log(String actionName, Actor actor) {
        logger.info("Actor:[" + actor.getName() + "] -> Accion:[" + actionName + "]");
    }

    public void log(String actionName) {
        logger.info("Actor:[" + "] -> Accion:[" + actionName + "]");
    }

    public void log(String actionName, Actor... actors) {
        for (Actor actor : actors) {
            logger.info("Actor:[" + actor.getName() + "] -> Accion:[" + actionName + "]");
        }
    }
}
