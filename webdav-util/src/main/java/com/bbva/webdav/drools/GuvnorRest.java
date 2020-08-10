package com.bbva.webdav.drools;

import java.io.BufferedOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpRetryException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bbva.webdav.constant.Produces;

public class GuvnorRest {

    private static final Logger LOGGER = LoggerFactory.getLogger(GuvnorRest.class);
    private static final String GET = "GET";
    private static final String POST = "POST";
    private String path;
    private String user;
    private String password;

    public GuvnorRest(String path, String user, String password) {
        super();
        this.path = path;
        this.user = user;
        this.password = password;
    }

    public InputStream packagesAllText() throws DroolsException {
        return executeGet(path + "/org.drools.guvnor.Guvnor/webdav/packages", Produces.TXT);
    }
    
    public InputStream assetsAllText(String packageName) throws DroolsException {
        return executeGet(path + "/org.drools.guvnor.Guvnor/webdav/packages/" + packageName, Produces.TXT);
    }
    
    public InputStream packagesText(String packageName) throws DroolsException {
        return executeGet(path + "/rest/packages/" + packageName + "/source", Produces.TXT);
    }

    public InputStream packagesAllJson() throws DroolsException {
        return executeGet(path + "/rest/packages", Produces._JSON);
    }
    
    protected InputStream execute(URL url, String method, Produces format, byte[] bin) throws DroolsException {
        LOGGER.info("URL: {}", url.toString());
        InputStream io;
        try {
            String authorization = "Basic " + Base64.encodeBase64String((user + ":" + password).getBytes());

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setRequestMethod(method);
            connection.setRequestProperty("Authorization", authorization);
            connection.setRequestProperty("Accept", format.getTipo());
            connection.setRequestProperty("Content-Type", "application/octet-stream");

            if (bin != null) {
                OutputStream os = connection.getOutputStream();
                BufferedOutputStream writer = new BufferedOutputStream(os, 10240);
                writer.write(bin);
                writer.flush();
                writer.close();
                os.close();
            }

            connection.setUseCaches(false);
            connection.connect();

            if (connection.getResponseCode() != 200) {
                throw new HttpRetryException("Bad response code: " + connection.getResponseCode(), connection.getResponseCode(), "");
            }

            io = connection.getInputStream();
        } catch (Exception e) {
            throw new DroolsException(e);
        }
        return io;
    }

    protected InputStream execute(URL url, String method, Produces format) throws DroolsException {
        return execute(url, method, format, null);
    }

    protected InputStream execute(String url, String method, Produces format, byte[] bin) throws DroolsException {
        URL uri;
        try {
            uri = new URL(url);
        } catch (MalformedURLException e) {
            throw new DroolsException(e);
        }
        return execute(uri, method, format, bin);
    }

    protected InputStream execute(String url, String method, Produces format) throws DroolsException {
        return execute(url, method, format, null);
    }

    protected InputStream executePost(String url, Produces format) throws DroolsException {
        return execute(url, POST, format);
    }

    protected InputStream executeGet(String url, Produces format) throws DroolsException {
        return execute(url, GET, format);
    }

    protected InputStream executePost(String url, Produces format, byte[] bin) throws DroolsException {
        return execute(url, POST, format, bin);
    }
}
