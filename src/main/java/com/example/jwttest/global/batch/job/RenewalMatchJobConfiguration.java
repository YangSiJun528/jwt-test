package com.example.jwttest.global.batch.job;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class RenewalMatchJobConfiguration {

    private int CHUNK_SIZE = 1;
    private final String JOB_NAME = "renewMatchJob";

    private final JobParameter jobParameter;

    @Getter
    @AllArgsConstructor
    public class JobParameter {
        @NonNull
        private final LocalDateTime standardDateTime;

        // @NonNull을 필드에 지정하면 롬복이 생성한 메서드에만 null check 들어감
        // 내가 직접 allArg 생성자를 만든다고 해도, 그거까지 null check를 해주지 않는다는 소리임
    }

    @Bean(JOB_NAME + "jobParameter")
    @JobScope
    public JobParameter jobParameter(
            @Value("#{jobParameters[date]}") LocalDateTime date
    ) {
        return new JobParameter(date);
    }



}
