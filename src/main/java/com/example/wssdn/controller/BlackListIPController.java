package com.example.wssdn.controller;

import com.example.wssdn.entity.BlackListIP;
import com.example.wssdn.repository.BlackListIpRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/blacklist")
public class BlackListIPController {
    private final BlackListIpRepository blackListIPRepository;

    @Autowired
    public BlackListIPController(BlackListIpRepository blackListIPRepository) {
        this.blackListIPRepository = blackListIPRepository;
    }

    @GetMapping("/listarIps")
    public List<BlackListIP> obtenerIps() {
        return blackListIPRepository.findAll();
    }

    @PostMapping("/{ipOrg}/{portOrg}/{ipDst}/{portDst}")
    public ResponseEntity<String> agregarIp(@PathVariable String ipOrg,
                                            @PathVariable String portOrg,
                                            @PathVariable String ipDst,
                                            @PathVariable String portDst){
        Optional<BlackListIP> blackListIP = blackListIPRepository.agregarIp(ipOrg);

        if(blackListIP.isPresent()){
            return ResponseEntity.ok("IP agregada correctamente a la lista negra");
        }else{
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("No se pudo agregar la IP a la lista negra");
        }
    }

    @DeleteMapping("/borrarIp/{ip}")
    public ResponseEntity<String> eliminarIp(@PathVariable String ip){
        Optional<BlackListIP> blackListIP = blackListIPRepository.obtenerIpPorId(ip);
        if(blackListIP.isPresent()){
            blackListIPRepository.delete(blackListIP.get());
            return ResponseEntity.ok("IP eliminada correctamente de la lista negra");
        }else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("No se encontró la IP en la lista negra");
        }
    }
}