package com.bbva.webdav.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.apache.any23.encoding.TikaEncodingDetector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@code ArchivoUtil}, contiene metodos genericos para manejo de archivos
 *
 * @author jquedena
 * @version 1.0
 */
public class ArchivoUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(ArchivoUtil.class);
    private static final int BUFFER_SIZE = 1024;

    public ArchivoUtil() {
        super();
    }

    private static void cerrar(Closeable recurso, File archivo) {
        if (recurso != null) {
            try {
                recurso.close();
            } catch (IOException e) {
                LOGGER.error("Archivo:cerrar:" + archivo.getPath());
                LOGGER.error("Archivo:cerrar", e);
            }
        }
    }

    /**
     * Crea un directorio
     *
     * @param archivo {@link File}
     * @throws IOException
     */
    public static void crearDirectorio(File archivo) throws IOException {
        File parentFile = archivo.getParentFile();
        if (!parentFile.exists() && !parentFile.mkdirs()) {
            throw new IOException("Creacion de directorios " + parentFile.getPath() + " fallida");
        }
    }

    /**
     * Copia un archivo
     *
     * @param origen {@link File}
     * @param destino {@link File}
     * @param reescribir {@link Boolean}, {@code true} indica que se sobreescribira el archivo
     * @throws IOException
     */
    public static void copiar(File origen, File destino, boolean reescribir) throws IOException {
        if (destino.exists() && !reescribir) {
            throw new IOException("La copia del archivo " + origen.getPath() + " a " + destino.getPath() + " no se puede realizar, el archivo de destino existe.");
        }

        crearDirectorio(destino);
        BufferedInputStream input = null;
        BufferedOutputStream output = null;

        try {
            input = new BufferedInputStream(new FileInputStream(origen));
            output = new BufferedOutputStream(new FileOutputStream(destino));
            int data = -1;
            while ((data = input.read()) != -1) {
                output.write(data);
            }
        } catch (Exception e) {
            LOGGER.error("Archivo:copiar", e);
        } finally {
            cerrar(input, origen);
            cerrar(output, destino);
        }
    }

    /**
     * Mueve un archivo
     *
     * @param origen {@link String}
     * @param destino {@link String}
     * @param sobreescribir {@link Boolean}, {@code true} indica que se sobreescribira el archivo
     * @throws IOException
     */
    public static void mover(String origen, String destino, boolean sobreescribir) throws IOException {
        mover(new File(origen), new File(destino), sobreescribir);
    }

    /**
     * Mueve un archivo
     *
     * @param origen {@link File}
     * @param destino {@link File}
     * @param sobreescribir {@link Boolean}, {@code true} indica que se sobreescribira el archivo
     * @throws IOException
     */
    public static void mover(File origen, File destino, boolean sobreescribir) throws IOException {
        if (destino.exists()) {
            if (sobreescribir) {
                destino.delete();
            } else {
                throw new IOException("El archivo " + origen.getPath() + " no se puede mover a " + destino.getPath() + ", el archivo de destino existe.");
            }
        }

        if (origen.exists()) {
            crearDirectorio(destino);
            if (!origen.renameTo(destino)) {
                throw new IOException("Error al mover el archivo de " + origen.getPath() + " a " + destino.getPath() + ".");
            }
        } else {
            throw new FileNotFoundException("El archivo " + origen.getName() + " no existe.");
        }
    }

    /**
     * Renombra un archivo
     *
     * @param origen {@link File}
     * @param destino {@link File}
     * @throws IOException
     */
    public static void renombrar(File origen, File destino) throws IOException {
        if (origen.exists()) {
            if (origen.renameTo(destino)) {
                LOGGER.error("Se renombro con exito el archivo.");
            } else {
                throw new IOException("No se pudo renombrar el archivo.");
            }
        } else {
            throw new FileNotFoundException("El archivo " + origen.getName() + " no existe.");
        }
    }

    /**
     * Devuelve un arrego con los archivos de un directorio dado
     *
     * @param archivo {@link File}
     * @return {@link File}[], Si archivo no es un directorio retorna {@code null}
     * @throws IOException
     */
    public static File[] listarArchivos(File archivo) throws IOException {
        if (archivo != null && archivo.isDirectory()) {
            return archivo.listFiles();
        } else {
            return null;
        }
    }

    private static void addZipEntry(File file, ZipOutputStream zipos, boolean pathComplete) throws IOException {
        byte[] buf = new byte[BUFFER_SIZE];
        int len;
        ZipEntry zipEntry = new ZipEntry(pathComplete ? file.getAbsolutePath() : file.getName());

        FileInputStream fin = new FileInputStream(file);
        BufferedInputStream in = new BufferedInputStream(fin);
        zipos.putNextEntry(zipEntry);

        while ((len = in.read(buf)) >= 0) {
            zipos.write(buf, 0, len);
        }
        zipos.flush();

        in.close();
        zipos.closeEntry();
    }

    private static void recurseFiles(File file, ZipOutputStream zipos) throws IOException {
        if (file.isDirectory()) {
            String[] fileNames = file.list();
            if (fileNames != null) {
                for (int i = 0; i < fileNames.length; i++) {
                    recurseFiles(new File(file, fileNames[i]), zipos);
                }
            }
        } else {
            addZipEntry(file, zipos, true);
        }
    }

    /**
     * Comprime un archivo
     *
     * @param pFile {@link String}
     * @param pZipFile {@link String}
     * @throws Exception
     */
    public static void zip(String pFile, String pZipFile) throws UtilException {
        FileOutputStream fos = null;
        ZipOutputStream zipos = null;

        try {
            fos = new FileOutputStream(pZipFile);
            zipos = new ZipOutputStream(fos);
            recurseFiles(new File(pFile), zipos);
        } catch (Exception e) {
            throw new UtilException(e);
        } finally {
            if(zipos != null) {
                try {
                    zipos.close();
                } catch (IOException e) {
                    throw new UtilException(e);
                }
            }
            if(fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    throw new UtilException(e);
                }
            }
        }
    }

    /**
     * Comprime un archivo
     *
     * @param pFile {@link String}
     * @param pZipFile {@link String}
     * @throws Exception
     */
    public static void zip(List<String> listFile, String pZipFile) throws UtilException {
        FileOutputStream fos = null;
        ZipOutputStream zipos = null;

        try {
            fos = new FileOutputStream(pZipFile);
            zipos = new ZipOutputStream(fos);

            for (String f : listFile) {
                addZipEntry(new File(f), zipos, false);
            }
        } catch (Exception e) {
            throw new UtilException(e);
        } finally {
            if(zipos != null) {
                try {
                    zipos.close();
                } catch (IOException e) {
                    throw new UtilException(e);
                }
            }
            if(fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    throw new UtilException(e);
                }
            }
        }
    }

    /**
     * Descomprimir
     * 
     * @param zipFile archivo de entrada
     * @param output directorio de salida
     */
    public static List<String> unzip(String zipFile, String outputFolder) throws IOException {
        return unzip(new FileInputStream(zipFile), outputFolder);
    }

    /**
     * Descomprimir
     * 
     * @param zipFile archivo de entrada
     * @param output directorio de salida
     */
    public static List<String> unzip(InputStream zipFile, String outputFolder) throws IOException {
        List<String> files = new ArrayList<String>();
        byte[] buffer = new byte[1024];

        // create output directory is not exists
        File folder = new File(outputFolder);
        if (!folder.exists()) {
            folder.mkdir();
        }

        // get the zip file content
        ZipInputStream zis = new ZipInputStream(zipFile);
        // get the zipped file list entry
        ZipEntry ze = zis.getNextEntry();

        while (ze != null) {

            String fileName = ze.getName();
            File newFile = new File(outputFolder + File.separator + fileName);
            files.add(fileName);

            // create all non exists folders
            // else you will hit FileNotFoundException for compressed folder
            new File(newFile.getParent()).mkdirs();

            if(!ze.isDirectory()) {
                FileOutputStream fos = new FileOutputStream(newFile);

                int len;
                while ((len = zis.read(buffer)) > 0) {
                    fos.write(buffer, 0, len);
                }

                fos.close();
            }
           
            ze = zis.getNextEntry();
        }

        zis.closeEntry();
        zis.close();

        return files;
    }
    
    /**
     * Escribe un archivo a partir de un arreglo de bytes
     *
     * @param archivo {@link String}
     * @param valor {@code byte[]}
     * @return {@link File}, retorna el archivo creado
     * @throws IOException
     */
    public static File escribirArchivo(String archivo, byte[] valor) throws IOException {
        File file = new File(archivo);
        BufferedOutputStream output = null;

        if (file.exists()) {
            if (file.isDirectory()) {
                throw new IOException(file.getAbsolutePath() + " es un directorio.");
            }
        } else {
            crearDirectorio(file);
        }

        try {
            ByteArrayInputStream in = new ByteArrayInputStream(valor);

            output = new BufferedOutputStream(new FileOutputStream(file));
            int read = 0;
            int i = 0;
            byte[] buffer = new byte[1024];
            while ((read = in.read(buffer)) >= 0) {
                output.write(buffer, 0, read);
                i++;
                if ((i % 4) == 0) {
                    output.flush();
                }
            }
        } catch (Exception e) {
            LOGGER.error("ArchivoUtil:escribirArchivo", e);
        } finally {
            cerrar(output, file);
        }

        return file;
    }

    /**
     * Escribe un archivo a partir de una cadena
     *
     * @param archivo {@link String}
     * @param valor {@link String}
     * @return {@link File}, retorna el archivo creado
     * @throws IOException
     */
    public static File escribirArchivo(String archivo, String valor) throws IOException {
        return escribirArchivo(archivo, valor.getBytes());
    }

    /**
     * Obtiene el {@link InputStream} de un archivo
     *
     * @param archivo {@link File}
     * @return {@link InputStream}
     * @throws FileNotFoundException
     */
    public static InputStream obtenerArchivo(File archivo) throws FileNotFoundException {
        InputStream input = null;

        if (archivo.exists()) {
            input = new FileInputStream(archivo);
        } else {
            throw new FileNotFoundException();
        }

        return input;
    }

    /**
     * Obtiene el {@link InputStream} de un archivo
     *
     * @param archivo {@link String}
     * @return {@link InputStream}
     * @throws FileNotFoundException
     */
    public static InputStream obtenerArchivo(String archivo) throws FileNotFoundException {
        return obtenerArchivo(new File(archivo));
    }

    public static String completeFileSeparator(String directorio) {
        String result = directorio.replace("\\", "/");
        if (!result.endsWith("/")) {
            result += "/";
        }

        return result;
    }

    public static String deleteStartFileSeparator(String directorio) {
        String result = directorio.replace("\\", "/");
        if (result.startsWith("/")) {
            result = result.replaceFirst("/", "");
        }

        return result;
    }
    
    /**
     * Retorna una cadena con los nombres de los archivos separados por la
     * cadena como parametro
     * 
     * @param file
     * @param separator
     * @return
     */
    public String listarArchivosConSeparador(File file, String separator) {
        String cadena = "";
        if(file != null && file.listFiles() != null) {
            for (File f : file.listFiles()) {
                if ("".equals(cadena)) {
                    cadena = f.getName();
                } else {
                    cadena += separator + file.getName();
                }
            }
        }
        return cadena;
    }

    public static Charset obtainCharset(InputStream is) throws IOException {
        return Charset.forName(new TikaEncodingDetector().guessEncoding(is));
    }

    public static Properties obtainProperties(String file) throws UtilException {
        try {
            InputStream is = new FileInputStream(new File(file));
            return obtainProperties(is);
        } catch (IOException e) {
            throw new UtilException(e);
        }
    }

    public static Properties obtainProperties(InputStream input) throws UtilException {
        Properties properties = new Properties();
        try {
            properties.load(input);
        } catch (IOException e) {
            throw new UtilException(e);
        }
        return properties;
    }
}
