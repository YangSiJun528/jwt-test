package com.example.jwttest.global.batch;

import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;

public class ResetCacheJobListener implements JobExecutionListener {

    public ResetCacheJobListener() {}

    @Override
    public void beforeJob(JobExecution jobExecution) {
        InMemCache.getInstance().clear();
        InMemCacheLeague.getInstance().clear();
        InMemCacheStatistics.getInstance().clear();
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        InMemCache.getInstance().clear();
        InMemCacheLeague.getInstance().clear();
        InMemCacheStatistics.getInstance().clear();
    }
}
