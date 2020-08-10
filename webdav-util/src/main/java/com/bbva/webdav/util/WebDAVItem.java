package com.bbva.webdav.util;

public class WebDAVItem {

    private String name;
    private boolean directory;
    private String jarName;

    public WebDAVItem(String name, boolean directory) {
        super();
        this.name = name;
        this.directory = directory;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isDirectory() {
        return directory;
    }

    public void setDirectory(boolean directory) {
        this.directory = directory;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("WebDAVItem [name=");
        builder.append(name);
        builder.append(", directory=");
        builder.append(directory);
        builder.append("]");
        return builder.toString();
    }

    public String getJarName() {
        return jarName;
    }

    public void setJarName(String jarName) {
        this.jarName = jarName;
    }

}
