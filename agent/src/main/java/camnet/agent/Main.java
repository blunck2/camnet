package camnet.agent;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@SpringBootApplication
public class Main {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        logger.trace("starting application...");
        SpringApplication.run(Main.class, args);
        logger.trace("application started.");
    }

}
