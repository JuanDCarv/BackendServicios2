package com.example.models;

import java.util.Date;

public class CargaDTO {

    private Long id;
    private Date fecha;
    private String propietarioCarga;
    private String origen;
    private String destino;
    private String dimensiones;
    private double peso;
    private double valorAsegurado;
    private String empaque;
    private String estado;
    private String ruta;
    private String mensajeConductor;
    private boolean remitida;
    private boolean entregada;
    private Long vehiculoAsignadoId;

    // Constructor vacío
    public CargaDTO() {
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

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getRuta() {
        return ruta;
    }

    public void setRuta(String ruta) {
        this.ruta = ruta;
    }

    public String getMensajeConductor() {
        return mensajeConductor;
    }

    public void setMensajeConductor(String mensajeConductor) {
        this.mensajeConductor = mensajeConductor;
    }

    public boolean isRemitida() {
        return remitida;
    }

    public void setRemitida(boolean remitida) {
        this.remitida = remitida;
    }

    public boolean isEntregada() {
        return entregada;
    }

    public void setEntregada(boolean entregada) {
        this.entregada = entregada;
    }

    public Long getVehiculoAsignadoId() {
        return vehiculoAsignadoId;
    }

    public void setVehiculoAsignadoId(Long vehiculoAsignadoId) {
        this.vehiculoAsignadoId = vehiculoAsignadoId;
    }
}