package com.bbva.webdav.client;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.io.LineIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bbva.webdav.drools.GuvnorRepository;
import com.bbva.webdav.drools.GuvnorRepositoryException;
import com.bbva.webdav.drools.GuvnorRest;
import com.bbva.webdav.util.ArchivoUtil;
import com.bbva.webdav.util.UtilException;
import com.bbva.webdav.util.WebDAVItem;

public class GuvnorClientWebDav implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = LoggerFactory.getLogger(GuvnorClientWebDav.class);
    private GuvnorRepository guvnorRepository;
    private GuvnorRest guvnorRest;
    private String server;
    
    public GuvnorClientWebDav(PropertyClientWebDav propertyClientWebDav) throws MalformedURLException {
        guvnorRepository = new GuvnorRepository(propertyClientWebDav.getHost(), propertyClientWebDav.getUser(), propertyClientWebDav.getPwd());
        guvnorRest = new GuvnorRest(propertyClientWebDav.getHost(), propertyClientWebDav.getUser(), propertyClientWebDav.getPwd());
        server = ArchivoUtil.completeFileSeparator(propertyClientWebDav.getHost());
    }

    private void checkStatus(HttpMethod method, String errMessage, int statusCode) throws UtilException {
        try {
            guvnorRepository.getHttpClient().executeMethod(method);
            if (method.getStatusCode() != statusCode) {
                throw new UtilException(errMessage + method.getStatusLine());
            }
        } catch (Exception e) {
            throw new UtilException(e);
        }
    }
    
    public InputStream getResource(String resource) throws UtilException {
        InputStream stream = null;
        GetMethod method = null;
        try {
            method = new GetMethod(server + resource);
            checkStatus(method, "Error al descargar el recurso: ", 200);
            stream = method.getResponseBodyAsStream();
        } catch (Exception e) {
            throw new UtilException(e);
        }

        return stream;
    }

    public List<WebDAVItem> listResource() throws UtilException {
        List<WebDAVItem> resources = new ArrayList<WebDAVItem>();
        String path = "org.drools.guvnor.Guvnor/webdav/packages";
        LOGGER.info("Lista de recursos de '{}'", server + path);
        try {
            BufferedReader stringReader = new BufferedReader(new InputStreamReader(guvnorRest.packagesAllText()));
            LineIterator lineIterator = new LineIterator(stringReader);
            int i = 0;
            BufferedReader stringReaderAsset = null;
            LineIterator lineIteratorAsset = null;
            int j = 0;
            String href1;
            String href2;
            while(lineIterator.hasNext()) {
                href1 = server + path + "/" + lineIterator.nextLine();
                if(i > 0) {
                    resources.add(new WebDAVItem(href1, true));
                    LOGGER.info("'{}'", href1);
                    
                    stringReaderAsset = new BufferedReader(new InputStreamReader(guvnorRest.assetsAllText(href1.replace(server + path + "/", ""))));
                    lineIteratorAsset = new LineIterator(stringReaderAsset);
                    j = 0;
                    while(lineIteratorAsset.hasNext()) {
                        href2 = server + path + "/" + href1 + "/" + lineIteratorAsset.nextLine();
                        if(j > 0) {
                            resources.add(new WebDAVItem(href2, false));
                            LOGGER.info("'{}'", href2);
                        }
                        j++;
                    }
                }
                i++;
            }
        } catch(Exception e) {
            throw new UtilException(e);
        }

        return resources;
    }

    public void updateResource(String packageName, String resource, byte[] bin) throws UtilException {
        try {
            guvnorRepository.updateAsset(packageName, resource, bin);
        } catch (Exception e1) {
        	e1.printStackTrace();
            throw new UtilException(e1);
        }
    }
    
    public void putResource(String packageName, String resource, byte[] bin) throws UtilException {
        try {
            guvnorRepository.deployAsset(packageName, resource, bin);
        } catch (GuvnorRepositoryException e) {
        	LOGGER.debug("", e);
        	updateResource(packageName, resource, bin);
        } catch (Exception e) {
            throw new UtilException(e);
        }
    }

    public void createPackage(String packageName) throws UtilException {
        try {
            guvnorRepository.createPackage(packageName);
        } catch (Exception e) {
            throw new UtilException(e);
        }
    }
    
    public void deletePackage(String packageName) throws UtilException {
        try {
            guvnorRepository.deletePackage(packageName);
        } catch (Exception e) {
            throw new UtilException(e);
        }
    }
}
