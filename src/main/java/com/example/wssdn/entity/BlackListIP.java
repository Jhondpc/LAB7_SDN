package com.example.wssdn.entity;

import javax.persistence.*;

@Entity
@Table(name = "black_ips")
public class BlackListIP {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column(name = "ip_src")
    private String ipSrc;

    @Column(name = "ip_dst")
    private String ipDst;

    @Column(name = "port_src")
    private int portSrc;

    @Column(name = "port_dst")
    private int portDst;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getIpSrc() {
        return ipSrc;
    }

    public void setIpSrc(String ipSrc) {
        this.ipSrc = ipSrc;
    }

    public String getIpDst() {
        return ipDst;
    }

    public void setIpDst(String ipDst) {
        this.ipDst = ipDst;
    }

    public int getPortSrc() {
        return portSrc;
    }

    public void setPortSrc(int portSrc) {
        this.portSrc = portSrc;
    }

    public int getPortDst() {
        return portDst;
    }

    public void setPortDst(int portDst) {
        this.portDst = portDst;
    }
}

