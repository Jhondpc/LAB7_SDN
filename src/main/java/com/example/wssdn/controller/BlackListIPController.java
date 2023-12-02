package com.example.wssdn.controller;

import com.example.wssdn.entity.BlackListIP;
import com.example.wssdn.repository.BlackListIpRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.json.JsonArray;
import jakarta.transaction.Transactional;
import org.apache.http.Header;
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
import org.springframework.http.HttpEntity;

import java.io.InputStream;
import java.io.OutputStream;
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
        try {
            //Consultar la API REST del Floodlight para obtener información sobre los dispositivos conectados
            List<String> dispositivosConectados = obtenerDispositivosConectadosDesdeFloodlight();

            //Devolver la lista de dispositivos conectados
            return dispositivosConectados;
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    @PostMapping("/agregarIp")
    public ResponseEntity<String> saveIp(@RequestParam("ipSrc") String ipSrc,
                                         @RequestParam("portSrc") int portSrc,
                                         @RequestParam("ipDst") String ipDst,
                                         @RequestParam("portDst") int portDst) {
        try {
            //Guardar en la base de datos local
            BlackListIP blackListIP = new BlackListIP();
            blackListIP.setIpSrc(ipSrc);
            blackListIP.setIpDst(ipDst);
            blackListIP.setPortSrc(portSrc);
            blackListIP.setPortDst(portDst);
            blackListIPRepository.save(blackListIP);

            //Enviar la información al controlador Floodlight
            enviarIpAControladorFloodlight(ipSrc, ipDst, portSrc, portDst);

            //Devolver una respuesta de éxito
            return ResponseEntity.ok("IP "+ipSrc+" agregada correctamente a la lista negra y enviada a Floodlight");
        } catch (Exception e) {
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

        //Configura la solicitud HTTP GET
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .GET()
                .build();

        //Enviar la solicitud
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());


        int statusCode = response.statusCode();
        String responseBody = response.body();

        List<String> dispositivosConectados = new ArrayList<>();

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(responseBody);

        for (JsonNode deviceNode : jsonNode) {
            String macAddress = deviceNode.path("mac").get(0).asText();
            dispositivosConectados.add(macAddress);
        }

        return dispositivosConectados;
    }

    private void enviarIpAControladorFloodlight(String ipSrc, String ipDst, int portSrc, int portDst) {
        try {
            String url = "http://192.168.200.200:8080/wm/staticflowpusher/json";

            String jsonBody = String.format("switch\":\"00:00:f2:20:f9:45:4c:4e\", \"name\":\"flow-mod-1\", \"cookie\":\"0\", \"priority\":\"32768\", \"ipv4_dst\":\"10.0.0.2\", \"eth_type\":\"0x0800\", \"active\":\"true\", \"actions\":\"drop\"", ipSrc);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<String> requestEntity = new HttpEntity<>(jsonBody, headers);

            RestTemplate restTemplate = new RestTemplate();

            ResponseEntity<String> responseEntity = restTemplate.postForEntity(url, requestEntity, String.class);

            if (responseEntity.getStatusCode() == HttpStatus.OK) {
                System.out.println("Reglas de bloqueo de tráfico hacia " + ipSrc + " agregadas correctamente.");
            } else {
                System.err.println("Error al agregar las reglas: " + responseEntity.getBody());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}