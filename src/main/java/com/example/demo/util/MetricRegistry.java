package com.example.demo.util;

import com.amazonaws.util.EC2MetadataUtils;
import com.example.demo.config.AWSConfig;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.composite.CompositeMeterRegistry;
import io.micrometer.core.instrument.config.MeterFilter;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Configuration
public class MetricRegistry {

    private MeterRegistry meterRegistry;

    public MetricRegistry(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;

    }

    public MeterRegistry getInstance(){
        this.meterRegistry.config().commonTags("application","webservice");
        //TODO: pick few tags from EC2MetadataUtils
        return this.meterRegistry;
    }


}
