package com.example.studyhub.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;

@Document(collection = "volume_estudo")
@CompoundIndex(name = "user_date_vol_idx", def = "{'usuarioId': 1, 'data': 1}", unique = true)
public class VolumeEstudo {
    @Id
    private String id;
    private String usuarioId;
    private LocalDate data;
    private int quantidade; // Total de questões resolvidas no dia

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(String usuarioId) {
        this.usuarioId = usuarioId;
    }

    public LocalDate getData() {
        return data;
    }

    public void setData(LocalDate data) {
        this.data = data;
    }

    public int getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(int quantidade) {
        this.quantidade = quantidade;
    }
}