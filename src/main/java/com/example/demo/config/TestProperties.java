package com.example.demo.config;

import java.net.URI;
import java.net.URISyntaxException;

public class TestProperties {

    public enum SecurityConfigAuthType {
        BASIC
    }

    private String url;

    private URI uri;

    private Integer maxConnections;

    private String userAgent;

    private Integer forceRetryAttempts;

    private TimeoutProperties timeout;

    private SecurityConfig security;

    public static class TimeoutProperties {

        private Integer read;

        private Integer connect;

        private Integer connectionRequest;

        private Long connectionIdle;

        private Long connectionTimeToLive;

        public Integer getRead() {
            return read;
        }

        public void setRead(Integer read) {
            this.read = read;
        }

        public Integer getConnect() {
            return connect;
        }

        public void setConnect(Integer connect) {
            this.connect = connect;
        }

        public Integer getConnectionRequest() {
            return connectionRequest;
        }

        public void setConnectionRequest(Integer connectionRequest) {
            this.connectionRequest = connectionRequest;
        }

        public Long getConnectionIdle() {
            return connectionIdle;
        }

        public void setConnectionIdle(Long connectionIdle) {
            this.connectionIdle = connectionIdle;
        }

        public Long getConnectionTimeToLive() {
            return connectionTimeToLive;
        }

        public void setConnectionTimeToLive(Long connectionTimeToLive) {
            this.connectionTimeToLive = connectionTimeToLive;
        }
    }

    public static class SecurityConfig {

        private SecurityConfigAuthType authType;

        private BasicSecurityConfig basic;

        public BasicSecurityConfig getBasic() {
            return basic;
        }

        public void setBasic(BasicSecurityConfig basic) {
            this.basic = basic;
        }

        public SecurityConfigAuthType getAuthType() {
            return authType;
        }

        public void setAuthType(SecurityConfigAuthType authType) {
            this.authType = authType;
        }

    }

    public static class BasicSecurityConfig {

        private String username;

        private String password;

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }

    public SecurityConfig getSecurity() {
        return security;
    }

    public void setSecurity(SecurityConfig security) {
        this.security = security;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) throws URISyntaxException {
        this.url = url;
        uri = new URI(url);
    }

    public TimeoutProperties getTimeout() {
        return timeout;
    }

    public void setTimeout(TimeoutProperties timeout) {
        this.timeout = timeout;
    }

    public String getHostname() {
        return uri.getHost();
    }

    public Integer getMaxConnections() {
        return maxConnections;
    }

    public void setMaxConnections(Integer maxConnections) {
        this.maxConnections = maxConnections;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public Integer getForceRetryAttempts() {
        return forceRetryAttempts;
    }

    public void setForceRetryAttempts(Integer forceRetryAttempts) {
        this.forceRetryAttempts = forceRetryAttempts;
    }

}
