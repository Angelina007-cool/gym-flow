package com.lomakova.gymflow;

import com.lomakova.gymflow.ui.LoginFrame;
import com.lomakova.gymflow.ui.MainFrame;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

import java.awt.*;

@SpringBootApplication
public class GymFlowApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = new SpringApplicationBuilder(GymFlowApplication.class)
                .headless(false)
                .run(args);

        EventQueue.invokeLater(() -> {
            LoginFrame loginFrame = context.getBean(LoginFrame.class);
            loginFrame.init();
        });
    }

}
