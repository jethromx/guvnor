package com.bbva;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import com.bbva.webdav.client.GuvnorClientWebDav;
import com.bbva.webdav.client.PropertyClientWebDav;
import com.bbva.webdav.util.UtilException;


public class App {
	
	private static SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
	private static final Logger LOGGER = Logger.getLogger(App.class);
	
	public static void main(String[] args) {
		try {
			LOGGER.info("Iniciando carga de reglas...");
			cargarReglas();
			LOGGER.info("Carga terminada !");
		} catch (UtilException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} 
	}
	
	public static void cargarReglas() throws UtilException, IOException {
		File dir = new File("/home/jethro/Descargas/reglas");
		PropertyClientWebDav propertyClientWebDav = new PropertyClientWebDav();
		propertyClientWebDav.setHost("http://localhost:8080/guvnor");
		propertyClientWebDav.setUser("admin");
		propertyClientWebDav.setPwd("admin");		

		
		GuvnorClientWebDav clientWebDav = new GuvnorClientWebDav(propertyClientWebDav);
		
		for(File f : dir.listFiles()) {
			try {
				clientWebDav.createPackage(f.getName());
			} catch(Exception e) {
				LOGGER.warn("Ya existe el paquete:" + f.getName());
			}
			int i = 1;
			boolean reiniciar=false;
			/*for(File s : f.listFiles()) {
				LOGGER.info(i++ + "\t" + f.getName() + "--"+ s.getName());
				clientWebDav.putResource(f.getName(), s.getName(), obtenerBytes(s));
				LOGGER.info("\t\tcargado en el servidor !!");
				LOGGER.info(s.delete()?"\t\tBORRADO de la carpeta origen !!":"\t\tNo se pudo borrar el archivo");				
			}	*/
			
			List<File> files = new ArrayList<File>(Arrays.asList(f.listFiles()));
			Iterator<File> iterator = files.iterator();
			while(iterator.hasNext()){
			    File s = iterator.next();
			    if(i==2){reiniciar=true;break;}
			    LOGGER.info(i++ + "\t" + f.getName() + "--"+ s.getName());
				clientWebDav.putResource(f.getName(), s.getName(), obtenerBytes(s));				
				LOGGER.info("\t\t cargado en el servidor !!");
				LOGGER.info(s.delete()?"\t\tBORRADO de la carpeta origen !!":"\t\tNo se pudo borrar el archivo");	
			}	
			if(reiniciar){
				cargarReglas();
				return;
			}
			
			f.delete();
		}
	}
	
	public static byte[] obtenerBytes(File ruleFile) {
        byte[] result = null;
        InputStream input = null;
        ByteArrayOutputStream output=null;
        try {
            input = new FileInputStream(ruleFile);
            output = new ByteArrayOutputStream();
            IOUtils.copy(input, output);

            result = output.toByteArray();
        } catch (Exception e) {
        	LOGGER.error("No se pudo obtener la regla");
        } finally {
        	try {
        		output.close();
				input.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
        }

        return result;
    }
}
