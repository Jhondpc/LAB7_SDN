package com.example.wssdn.repository;

import com.example.wssdn.entity.BlackListIP;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface BlackListIpRepository extends JpaRepository<BlackListIP, Integer> {

    @Modifying
    @Query(value = "INSERT INTO blacklist.black_ips (ip) VALUES (?1) RETURNING *", nativeQuery = true)
    @Transactional
    Optional<BlackListIP> agregarIp(String ip);


    @Query(value = "SELECT * FROM blacklist.black_ips WHERE ip = ?1", nativeQuery = true)
    Optional<BlackListIP> obtenerIpPorId(String ip);
}
