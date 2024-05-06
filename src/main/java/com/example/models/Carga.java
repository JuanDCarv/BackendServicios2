package com.example.models;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.util.Date;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.OneToMany;

@Entity
public class Carga {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToMany(mappedBy = "carga", cascade = CascadeType.ALL)
    private List<Remision> remisiones;
    
    @Temporal(TemporalType.TIMESTAMP)
    private Date fecha;
    
    private String propietarioCarga;
    private String origen;
    private String destino;
    private String dimensiones;
    private double peso;
    private double valorAsegurado;
    private String empaque;

    // Constructor vacío (necesario para JPA)
    public Carga() {
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getFecha() {
        return fecha;
    }

    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }

    public String getPropietarioCarga() {
        return propietarioCarga;
    }

    public void setPropietarioCarga(String propietarioCarga) {
        this.propietarioCarga = propietarioCarga;
    }

    public String getOrigen() {
        return origen;
    }

    public void setOrigen(String origen) {
        this.origen = origen;
    }

    public String getDestino() {
        return destino;
    }

    public void setDestino(String destino) {
        this.destino = destino;
    }

    public String getDimensiones() {
        return dimensiones;
    }

    public void setDimensiones(String dimensiones) {
        this.dimensiones = dimensiones;
    }

    public double getPeso() {
        return peso;
    }

    public void setPeso(double peso) {
        this.peso = peso;
    }

    public double getValorAsegurado() {
        return valorAsegurado;
    }

    public void setValorAsegurado(double valorAsegurado) {
        this.valorAsegurado = valorAsegurado;
    }

    public String getEmpaque() {
        return empaque;
    }

    public void setEmpaque(String empaque) {
        this.empaque = empaque;
    }
    
    public List<Remision> getRemisiones() {
        return remisiones;
    }
    public void setRemisiones(List<Remision> remisiones) {
        this.remisiones = remisiones;
    }
}