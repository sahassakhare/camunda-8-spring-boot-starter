package org.maverick.devtools.worker.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WorkerMetrics {

    private final MeterRegistry meterRegistry;

    public void recordJobSuccess(String jobType) {
        Counter.builder("camunda.job.success")
                .tag("type", jobType)
                .register(meterRegistry)
                .increment();
    }

    public void recordJobFailure(String jobType, String errorType) {
        Counter.builder("camunda.job.failure")
                .tag("type", jobType)
                .tag("error", errorType)
                .register(meterRegistry)
                .increment();
    }

    public Timer.Sample startTimer() {
        return Timer.start(meterRegistry);
    }

    public void recordJobDuration(Timer.Sample sample, String jobType, String status) {
        sample.stop(Timer.builder("camunda.job.duration")
                .tag("type", jobType)
                .tag("status", status)
                .register(meterRegistry));
    }
}
