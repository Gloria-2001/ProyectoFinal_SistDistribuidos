/*
    Briseño Lira Andrés          4CM14
    Cabrera García Luis Ángel    4CM11
    Olivares Ménez Gloria Oliva  4CM12
*/
package com.mycompany.app;  
import java.util.Map;

public class FrontendSearchResponse {
        private String nombre;
        private String libro;
        private double puntuacion;
        private String palabras;
        

public FrontendSearchResponse() {
           

        }
        public FrontendSearchResponse(String nombre,double puntuacion,String palabras) {
            this.libro = nombre;
            this.puntuacion = puntuacion;
            this.palabras = palabras;
            String[] s = nombre.split("\\.");
            int n = s.length;
            s = s[n-2].split("\\[");
            this.nombre = s[0].replace("_", " ");
        }

        public String getnombre() {
            return nombre;
        }
        
        public String getlibro() {
            return libro;
        }
        
        public double getpuntuacion() {
            return puntuacion;
        }
        
        public String getpalabras() {
            return palabras;
        }

   
}
