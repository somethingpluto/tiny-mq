package org.tiny.mq.model;

public class ConfigModel {
    /**
     * MQ相关数据文件存放文件夹
     */
    private String basePath;

    public String getBasePath() {
        return basePath;
    }

    public void setBasePath(String basePath) {
        this.basePath = basePath;
    }

    @Override
    public String toString() {
        return "ConfigModel{" +
                "basePath='" + basePath + '\'' +
                '}';
    }
}
