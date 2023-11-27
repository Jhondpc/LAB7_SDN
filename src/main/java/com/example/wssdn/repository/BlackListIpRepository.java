package com.example.wssdn.repository;

import com.example.wssdn.entity.BlackListIP;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface BlackListIpRepository extends JpaRepository<BlackListIP, Integer> {

    @Modifying
    @Query(value = "INSERT INTO black_ips (ip_src, ip_dst, port_src, port_dst) VALUES (?1, ?2, ?3, ?4) RETURNING *", nativeQuery = true)
    @Transactional
    Optional<BlackListIP> agregarIp(String ipSrc, String ipDst, int portSrc, int portDst);


    @Query(value = "SELECT * FROM black_ips WHERE ip_src = ?1", nativeQuery = true)
    Optional<BlackListIP> obtenerIpPorId(String ip);
}
