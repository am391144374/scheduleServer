package com.l.scheduleserver.conf;

import org.quartz.Job;

public abstract class DefaultJobAndFail extends DefaultFail implements Job {

    @Override
    public void fail() {
        super.fail();
    }


}
