package com.l.scheduleserver.conf.initQuartz;

import com.l.scheduleserver.quartz.QuartzExcutors;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class conf {

    @Bean
    public QuartzExcutors quartzExcutors(){
        QuartzExcutors quartzExcutors = QuartzExcutors.getInstance();
        return quartzExcutors;
    }

}
