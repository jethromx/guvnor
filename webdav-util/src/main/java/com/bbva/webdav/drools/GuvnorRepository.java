package com.bbva.webdav.drools;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpConnectionManager;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.InputStreamRequestEntity;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import org.apache.jackrabbit.webdav.DavConstants;
import org.apache.jackrabbit.webdav.DavException;
import org.apache.jackrabbit.webdav.MultiStatusResponse;
import org.apache.jackrabbit.webdav.client.methods.DavMethod;
import org.apache.jackrabbit.webdav.client.methods.DeleteMethod;
import org.apache.jackrabbit.webdav.client.methods.PropFindMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GuvnorRepository {
    private static final Logger LOGGER = LoggerFactory.getLogger(GuvnorRepository.class);
    private static String WEBDAV_URI = "org.drools.guvnor.Guvnor/webdav";
    private static String REST_API_URI = "org.drools.guvnor.Guvnor/api";
    private static String PACKAGE_API_URI = "rest/packages";

    private static int CONNECTIONS_PER_HOST = 2000;

    private URL url;
    private String username;
    private String password;

    private HttpClient httpClient;

    public GuvnorRepository(final String url, final String username, final String password) throws MalformedURLException {
        this.url = new URL(url);
        this.username = username;
        this.password = password;
        initHttpClient();
    }

    public HttpClient getHttpClient() {
        return httpClient;
    }

    private void initHttpClient() {
        LOGGER.info("Initializing Guvnor HTTPClient");

        HttpConnectionManagerParams params = new HttpConnectionManagerParams();
        params.setDefaultMaxConnectionsPerHost(CONNECTIONS_PER_HOST);

        HttpConnectionManager connMan = new MultiThreadedHttpConnectionManager();
        connMan.setParams(params);

        UsernamePasswordCredentials creds = new UsernamePasswordCredentials(username, password);
        HostConfiguration hostConfig = new HostConfiguration();
        hostConfig.setHost(url.getHost());

        httpClient = new HttpClient(connMan);
        httpClient.getState().setCredentials(AuthScope.ANY, creds);
        httpClient.setHostConfiguration(hostConfig);
    }

    void checkStatus(HttpMethod method, int expectedStatus) throws GuvnorRepositoryException {
        if (method.getStatusCode() != expectedStatus) {
            throw new GuvnorRepositoryException(method, expectedStatus);
        }
    }

    private List<String> getWebDavChildren(String url, Pattern pattern) throws IOException, GuvnorRepositoryException, DavException {
        List<String> children = new ArrayList<String>();

        DavMethod method = new PropFindMethod(url, DavConstants.PROPFIND_ALL_PROP, 1);
        httpClient.executeMethod(method);
        checkStatus(method, 207);

        for (MultiStatusResponse resp : method.getResponseBodyAsMultiStatus().getResponses()) {
            String href = resp.getHref();
            Matcher m = pattern.matcher(href);
            if (m.find()) {
                children.add(m.group(1));
            }
        }
        return children;
    }

    public List<String> getPackageNames() throws IOException, GuvnorRepositoryException, DavException {
        LOGGER.info("Retrieving package names from Guvnor at '{}'", this.url);
        String methodUrl = this.url + "/" + WEBDAV_URI + "/packages";
        Pattern pattern = Pattern.compile(this.url.getPath() + "/" + WEBDAV_URI + "/packages/([^/]*)/$");
        return getWebDavChildren(methodUrl, pattern);
    }

    public boolean packageExists(String packageName) throws IOException, GuvnorRepositoryException, DavException {
        return getPackageNames().contains(packageName);
    }

    public List<String> getPackageAssetNames(String packageName) throws IOException, GuvnorRepositoryException, DavException {
        LOGGER.info("Retrieving package names from Guvnor at '{}'", this.url);
        String methodUrl = this.url + "/" + WEBDAV_URI + "/packages/" + packageName;
        Pattern pattern = Pattern.compile(this.url.getPath() + "/" + WEBDAV_URI + "/packages/" + packageName + "/([^/]+)$");
        return getWebDavChildren(methodUrl, pattern);
    }

    public void createPackage(String packageName) throws HttpException, IOException, GuvnorRepositoryException {
        LOGGER.info("Creating new package '{}'", packageName);
        String methodUrl = this.url + "/" + REST_API_URI + "/packages/" + packageName + "/.package";
        PostMethod method = new PostMethod(methodUrl);
        httpClient.executeMethod(method);
        checkStatus(method, 200);
    }

    public void deletePackage(String packageName) throws HttpException, IOException, GuvnorRepositoryException {
        LOGGER.info("Deleting package '{}'", packageName);
        String methodUrl = this.url + "/" + WEBDAV_URI + "/packages/" + packageName;
        DeleteMethod method = new DeleteMethod(methodUrl);
        httpClient.executeMethod(method);
        checkStatus(method, 204);
    }

    public void deployAsset(File assetFile, String packageName) throws GuvnorRepositoryException, HttpException, IOException {
        LOGGER.info("Deploying file '{}' to package '{}'", assetFile.getName(), packageName);
        String methodUrl = this.url + "/" + REST_API_URI + "/packages/" + packageName + "/" + assetFile.getName() + "/source";
        PostMethod method = new PostMethod(methodUrl);
        FileInputStream is = new FileInputStream(assetFile);
        method.setRequestEntity(new InputStreamRequestEntity(is));
        httpClient.executeMethod(method);
        checkStatus(method, 200);
    }

    public void deployAsset(String packageName, String assetFileName, byte[] assetFileBin) throws GuvnorRepositoryException, HttpException, IOException {
        LOGGER.info("Deploying file '{}' to package '{}'", assetFileName, packageName);
        String methodUrl = this.url + "/" + REST_API_URI + "/packages/" + packageName + "/" + java.net.URLEncoder.encode(assetFileName,"UTF-8") + "/source";
        PostMethod method = new PostMethod(methodUrl);
        ByteArrayInputStream is = new ByteArrayInputStream(assetFileBin);
        method.setRequestEntity(new InputStreamRequestEntity(is));
        httpClient.executeMethod(method);
        checkStatus(method, 200);
    }

    public void updateAsset(File assetFile, String packageName) throws GuvnorRepositoryException, HttpException, IOException {
        LOGGER.info("Updating file '{}' in package '{}'", assetFile.getName(), packageName);
        String methodUrl = this.url + "/" + REST_API_URI + "/packages/" + packageName + "/" + assetFile.getName() + "/source";
        PutMethod method = new PutMethod(methodUrl);
        FileInputStream is = new FileInputStream(assetFile);
        method.setRequestEntity(new InputStreamRequestEntity(is));
        httpClient.executeMethod(method);
        checkStatus(method, 200);
    }

    public void updateAsset(String packageName, String assetFileName, byte[] assetFileBin) throws GuvnorRepositoryException, HttpException, IOException {
        LOGGER.info("Updating file '{}' in package '{}'", assetFileName, packageName);
        String methodUrl = this.url + "/" + REST_API_URI + "/packages/" + packageName + "/" + java.net.URLEncoder.encode(assetFileName,"UTF-8") + "/source";
        PutMethod method = new PutMethod(methodUrl);
        ByteArrayInputStream is = new ByteArrayInputStream(assetFileBin);
        method.setRequestEntity(new InputStreamRequestEntity(is));
        httpClient.executeMethod(method);
        checkStatus(method, 200);
    }

    public void deleteAsset(String assetFile, String packageName) throws HttpException, IOException, GuvnorRepositoryException {
        LOGGER.info("Deleting file '{}' from package '{}'", assetFile, packageName);
        String methodUrl = this.url + "/" + REST_API_URI + "/packages" + packageName + "/" + assetFile;
        DeleteMethod method = new DeleteMethod(methodUrl);
        httpClient.executeMethod(method);
        checkStatus(method, 204);
    }

    public void updatePackageAssets(String packageName, List<File> assetFiles) throws IOException, GuvnorRepositoryException, DavException {
        Set<String> remoteAssetNames = new HashSet<String>(getPackageAssetNames(packageName));

        Map<String, File> localFileMap = new HashMap<String, File>();
        for (File f : assetFiles) {
            localFileMap.put(f.getName(), f);
        }

        Set<String> localAssetNames = localFileMap.keySet();

        Set<String> toDelete = new HashSet<String>(remoteAssetNames);
        toDelete.removeAll(localAssetNames);

        for (String fileToDelete : toDelete) {
            deleteAsset(fileToDelete, packageName);
        }

        for (String filename : localAssetNames) {
            File file = localFileMap.get(filename);
            if (remoteAssetNames.contains(filename)) {
                updateAsset(file, packageName);
            } else {
                deployAsset(file, packageName);
            }
        }
    }

    public void createPackageSnapshot(String packageName) throws HttpException, IOException, GuvnorRepositoryException {
        LOGGER.info("Creating package snapshot for package '{}'", packageName);
        String methodUrl = this.url + "/" + PACKAGE_API_URI + "/" + packageName + "/snapshot/deploy" + System.currentTimeMillis();
        PostMethod method = new PostMethod(methodUrl);
        httpClient.executeMethod(method);
        checkStatus(method, 204);
    }
}
