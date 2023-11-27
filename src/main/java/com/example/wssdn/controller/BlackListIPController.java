package com.example.wssdn.controller;

import com.example.wssdn.entity.BlackListIP;
import com.example.wssdn.repository.BlackListIpRepository;
import jakarta.transaction.Transactional;
import org.hibernate.TransactionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
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

    @Transactional
    @PostMapping("/{ipSrc}/{portSrc}/{ipDst}/{portDst}")
    public ResponseEntity<String> saveIp(@PathVariable String ipSrc,
                                         @PathVariable int portSrc,
                                         @PathVariable String ipDst,
                                         @PathVariable int portDst) {
        try {
            blackListIPRepository.agregarIp(ipSrc, ipDst, portSrc, portDst);
            return ResponseEntity.ok("IP agregada correctamente a la lista negra");
        } catch (DataAccessException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error de acceso a datos al agregar la IP" + ipSrc + " " + ipDst + " " + portSrc + " " + portDst);
        } catch (TransactionException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error de transacción al agregar la IP" + ipSrc + " " + ipDst + " " + portSrc + " " + portDst);
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
