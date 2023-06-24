import java.io.Serializable;

public class Libro  implements Serializable{
    private String url;
        private long palabras;
        public Libro(String direccion, long palabras){
            this.url = direccion;
            this.palabras = palabras;
        }

        public long getPalabras(){
            return this.palabras;
        }

        public String getURL(){
            return this.url;
        }
}
