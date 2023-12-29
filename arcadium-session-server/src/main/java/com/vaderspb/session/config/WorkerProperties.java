package com.vaderspb.session.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "arcadium.worker")
public class WorkerProperties {
    private String image;
    private Resources resources;

    public String getImage() {
        return image;
    }

    public void setImage(final String image) {
        this.image = image;
    }

    public Resources getResources() {
        return resources;
    }

    public void setResources(final Resources resources) {
        this.resources = resources;
    }

    public static class Resources {
        private Resource limits;
        private Resource requests;

        public Resource getLimits() {
            return limits;
        }

        public void setLimits(final Resource limits) {
            this.limits = limits;
        }

        public Resource getRequests() {
            return requests;
        }

        public void setRequests(final Resource requests) {
            this.requests = requests;
        }
    }

    public static class Resource {
        private String cpu;
        private String memory;

        public String getCpu() {
            return cpu;
        }

        public void setCpu(final String cpu) {
            this.cpu = cpu;
        }

        public String getMemory() {
            return memory;
        }

        public void setMemory(final String memory) {
            this.memory = memory;
        }
    }
}
