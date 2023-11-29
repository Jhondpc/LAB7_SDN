package com.example.wssdn.controller;

import com.example.wssdn.entity.BlackListIP;
import com.example.wssdn.repository.BlackListIpRepository;
import jakarta.json.JsonArray;
import jakarta.transaction.Transactional;
import org.apache.http.HttpEntity;  // Importación correcta
import org.apache.http.entity.StringEntity;  // Importación correcta

import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClients;
import org.hibernate.TransactionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import org.springframework.http.HttpHeaders;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;


@RestController
@RequestMapping("/blacklist")
public class BlackListIPController {
    private final BlackListIpRepository blackListIPRepository;

    @Autowired
    public BlackListIPController(BlackListIpRepository blackListIPRepository) {
        this.blackListIPRepository = blackListIPRepository;
    }

    @GetMapping("/listarIps")
    public List<String> obtenerIps() {
        // Obtener las IPs desde la base de datos local
        List<BlackListIP> blackListIPs = blackListIPRepository.findAll();
        //return blackListIPs;

        try {
            // Consultar la API REST del Floodlight para obtener información sobre los dispositivos conectados
            List<String> dispositivosConectados = obtenerDispositivosConectadosDesdeFloodlight();

            // Aquí puedes hacer lo que necesites con la información de los dispositivos conectados
            // ...

            // Devolver la lista de IPs desde la base de datos local
            return dispositivosConectados;
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            // Manejar la excepción según tus necesidades
            // Puedes lanzar una excepción personalizada, loggear el error, etc.
            return Collections.emptyList(); // Otra opción podría ser devolver una lista vacía en caso de error
        }
    }

    @PostMapping("/agregarIp")
    public ResponseEntity<String> saveIp(@RequestParam("ipSrc") String ipSrc,
                                         @RequestParam("portSrc") int portSrc,
                                         @RequestParam("ipDst") String ipDst,
                                         @RequestParam("portDst") int portDst) {
        try {
            // Guardar en la base de datos local
            BlackListIP blackListIP = new BlackListIP();
            blackListIP.setIpSrc(ipSrc);
            blackListIP.setIpDst(ipDst);
            blackListIP.setPortSrc(portSrc);
            blackListIP.setPortDst(portDst);
            blackListIPRepository.save(blackListIP);

            // Enviar la información al controlador Floodlight
            enviarIpAControladorFloodlight(ipSrc, ipDst, portSrc, portDst);

            // Devolver una respuesta de éxito
            return ResponseEntity.ok("IP agregada correctamente a la lista negra y enviada a Floodlight");
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al enviar la IP a la lista negra en Floodlight: " + e.getMessage());
        }
    }

    @DeleteMapping("/borrarIp/{ip}")
    public ResponseEntity<String> eliminarIp(@PathVariable String ip) {
        Optional<BlackListIP> blackListIP = blackListIPRepository.obtenerIpPorId(ip);
        if (blackListIP.isPresent()) {
            blackListIPRepository.delete(blackListIP.get());
            return ResponseEntity.ok("IP eliminada correctamente de la lista negra");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("No se encontró la IP en la lista negra");
        }
    }

    private List<String> obtenerDispositivosConectadosDesdeFloodlight() throws IOException, InterruptedException {
        String url = "http://192.168.201.200:8080/wm/device/";

        // Configurar la solicitud HTTP GET
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .GET()
                .build();

        // Enviar la solicitud
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // Verificar si la solicitud fue exitosa (código de estado 200)
        if (response.statusCode() == 200) {
            // Parsear la respuesta JSON para obtener la lista de dispositivos
            List<String> dispositivosConectados = parsearRespuestaFloodlight(response.body());

            // Devolver la lista de dispositivos
            return dispositivosConectados;
        } else {
            // Manejar el caso en el que la solicitud no fue exitosa
            System.out.println("La solicitud al Floodlight no fue exitosa. Código de estado: " + response.statusCode());
            return Collections.emptyList();
        }
    }

    private List<String> parsearRespuestaFloodlight(String responseBody) {
        // Aquí deberías implementar la lógica para parsear la respuesta JSON del Floodlight
        // y extraer la información necesaria. La estructura exacta dependerá de la respuesta real.

        // Por ahora, solo devolver una lista vacía como ejemplo
        return Collections.emptyList();
    }

    private void enviarIpAControladorFloodlight(String ipSrc, String ipDst, int portSrc, int portDst) throws IOException {
        String url = "http://10.20.12.215:8080/wm/staticflowentrypusher/json";

        String jsonBody = String.format(
                "{ \"name\":\"flow-mod-ip-%s-%s\", \"cookie\":\"0\", \"priority\":\"32768\", " +
                        "\"active\":\"true\", \"actions\":\"drop\", " +
                        "\"match\":\"ipv4_src=%s,ipv4_dst=%s,tcp_src=%d,tcp_dst=%d\" }",
                ipSrc, ipDst, ipSrc, ipDst, portSrc, portDst);

        // Configurar la solicitud HTTP POST
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        try {
            // Enviar la solicitud
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            // Manejar la respuesta si es necesario
            int statusCode = response.statusCode();
            String responseBody = response.body();
            System.out.println("Código de estado: " + statusCode);
            System.out.println("Cuerpo de la respuesta: " + responseBody);

            // Puedes revisar el código de estado en statusCode
            // No olvides manejar excepciones si algo sale mal durante la solicitud
        } catch (IOException e) {
            e.printStackTrace();
            // Manejar la excepción según tus necesidades
            throw e; // Asegúrate de relanzar la excepción para que sea capturada por el bloque catch en el método saveIp
        } catch (InterruptedException e) {
            e.printStackTrace();
            // Manejar la excepción según tus necesidades
        }
    }
}
