package com.expediagroup.library.drshadow.springboot;

import org.slf4j.MDC;
import org.springframework.core.task.TaskDecorator;

import java.util.Map;

/**
 * Ensure that MDC logging is passed to Dr Shadow async threads
 */
public class ShadowTrafficLoggingTaskDecorator implements TaskDecorator {

    @Override
    public Runnable decorate(Runnable task) {
        Map<String, String> context = MDC.getCopyOfContextMap();
        return () -> {
            try {
                if (context != null) {
                    MDC.setContextMap(context);
                }
                task.run();
            } finally {
                MDC.clear();
            }
        };
    }

}
