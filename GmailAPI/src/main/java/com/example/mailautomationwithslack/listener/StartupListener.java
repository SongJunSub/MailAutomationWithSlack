package com.example.mailautomationwithslack.listener;

import lombok.Getter;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

@Component
public class StartupListener implements ApplicationListener<ContextRefreshedEvent> {

    @Getter
    private static long startUpTime;

    public void onApplicationEvent(ContextRefreshedEvent event) {
        startUpTime = System.currentTimeMillis() / 1000L;
    }

}