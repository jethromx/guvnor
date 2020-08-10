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


public class EliminarApp {
	
	private static SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
	private static final Logger LOGGER = Logger.getLogger(EliminarApp.class);
	
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
		PropertyClientWebDav propertyClientWebDav = new PropertyClientWebDav();
		
		//TEST
		propertyClientWebDav.setHost("http://localh8080/guvnor");
		propertyClientWebDav.setUser("admin");
		propertyClientWebDav.setPwd("admin");
		
		GuvnorClientWebDav clientWebDav = new GuvnorClientWebDav(propertyClientWebDav);
		
		String[] paquetes=new String[]{
				"pe.com.bbva.cotiza.base.v7.P001LimpiarSesion"
				
		};
		
		for(String paquete : paquetes) {
			try {
				LOGGER.info("Eliminando paquete: "+paquete);
				clientWebDav.deletePackage(paquete);
			} catch(Exception e) {
				LOGGER.warn("Error en el paquete:" + paquete+" "+e.getMessage());
			}
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
