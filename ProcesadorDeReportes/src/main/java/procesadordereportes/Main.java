package procesadordereportes;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.util.Iterator;

public class Main {

    static int totalProcesos = 0;
    static int procesosCompletos = 0;
    static int procesosPendientes = 0;
    static int recursosTipoHerramienta = 0;
    static double sumaEficiencia = 0;
    static int cuentaEficiencia = 0;

    static long fechaMasAntigua = Long.MAX_VALUE;
    static JsonNode procesoMasAntiguo = null;

    public static void main(String[] args) throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();

        
        HttpRequest requestGenerador = HttpRequest.newBuilder()
                .uri(URI.create("https://58o1y6qyic.execute-api.us-east-1.amazonaws.com/default/taskReport"))
                .GET()
                .build();

        HttpResponse<String> responseGenerador = client.send(requestGenerador, HttpResponse.BodyHandlers.ofString());

        String jsonOriginal = responseGenerador.body();
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(jsonOriginal);

       
        processNode(root.get("procesos"));

        double eficienciaPromedio = cuentaEficiencia == 0 ? 0 : sumaEficiencia / cuentaEficiencia;

        
        mapper.createObjectNode();


ObjectNode resultadoBusqueda = mapper.createObjectNode();
resultadoBusqueda.put("totalProcesos", totalProcesos);
resultadoBusqueda.put("procesosCompletos", procesosCompletos);
resultadoBusqueda.put("procesosPendientes", procesosPendientes);
resultadoBusqueda.put("recursosTipoHerramienta", recursosTipoHerramienta);
resultadoBusqueda.put("eficienciaPromedio", eficienciaPromedio);

ObjectNode procesoAntiguo = mapper.createObjectNode();
procesoAntiguo.put("id", procesoMasAntiguo.get("id").asInt());
procesoAntiguo.put("nombre", procesoMasAntiguo.get("nombre").asText());
procesoAntiguo.put("fechaInicio", procesoMasAntiguo.get("fechaInicio").asText());

resultadoBusqueda.set("procesoMasAntiguo", procesoAntiguo);

ObjectNode finalJson = mapper.createObjectNode();
finalJson.put("nombre", "Cesar David Morales Castellanos");
finalJson.put("carnet", "5190188869");
finalJson.put("seccion", "A");
finalJson.set("resultadoBusqueda", resultadoBusqueda);


finalJson.set("payload", root);


String jsonResultado = mapper.writeValueAsString(finalJson);

        
        HttpRequest requestEvaluador = HttpRequest.newBuilder()
                .uri(URI.create("https://t199qr74fg.execute-api.us-east-1.amazonaws.com/default/taskReportVerification"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonResultado))
                .build();

        HttpResponse<String> responseEvaluador = client.send(requestEvaluador, HttpResponse.BodyHandlers.ofString());

        System.out.println("Respuesta de la API Evaluadora:");
        System.out.println(responseEvaluador.body());
    }

    
    public static void processNode(JsonNode procesos) {
        if (procesos == null || !procesos.isArray()) return;

        for (JsonNode proceso : procesos) {
            totalProcesos++;

            String estado = proceso.get("estado").asText();
            if (estado.equalsIgnoreCase("completo")) procesosCompletos++;
            else procesosPendientes++;

            if (proceso.has("recursos") && proceso.get("recursos").isArray()) {
                for (JsonNode recurso : proceso.get("recursos")) {
                    if (recurso.get("tipo").asText().equalsIgnoreCase("herramienta")) {
                        recursosTipoHerramienta++;
                    }
                }
            }

            if (proceso.has("eficiencia")) {
                sumaEficiencia += proceso.get("eficiencia").asDouble();
                cuentaEficiencia++;
            }

            if (proceso.has("fechaInicio")) {
                String fecha = proceso.get("fechaInicio").asText();
                long tiempo = Instant.parse(fecha).toEpochMilli();
                if (tiempo < fechaMasAntigua) {
                    fechaMasAntigua = tiempo;
                    procesoMasAntiguo = proceso;
                }
            }

            
            if (proceso.has("subprocesos")) {
                processNode(proceso.get("subprocesos"));
            }
        }
    }
}
