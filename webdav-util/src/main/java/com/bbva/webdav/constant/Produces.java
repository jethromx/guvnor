package com.bbva.webdav.constant;

public class Produces {

    public static final String JSON = "application/json;charset=UTF-8";
    public static final String TEXT = "text/html;charset=UTF-8";

    public static final Produces HTML = new Produces("text/html;charset=UTF-8", "html");
    public static final Produces TXT = new Produces("text/plain", "txt");
    public static final Produces XML = new Produces("application/xml", "xml");
    public static final Produces XSD = new Produces("application/xml", "xsd");
    public static final Produces XLS = new Produces("application/vnd.ms-excel", "xls");
    public static final Produces XLSX = new Produces("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "xlsx");
    public static final Produces PDF = new Produces("application/pdf", "pdf");
    public static final Produces ZIP = new Produces("application/zip", "zip");
    public static final Produces BIN = new Produces("application/octet-stream", "bin");
    public static final Produces PNG = new Produces("image/png", "png");
    public static final Produces JPEG = new Produces("image/jpeg", "jpeg");
    public static final Produces TIIF = new Produces("image/tiff", "tiif");
    public static final Produces SVG = new Produces("application/svg+xml", "svg");
    public static final Produces _JSON = new Produces("application/json;charset=UTF-8", "json");

    private String tipo;
    private String extension;

    public Produces(String tipo, String extension) {
        super();
        this.tipo = tipo;
        this.extension = extension;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getExtension() {
        return extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }

}
