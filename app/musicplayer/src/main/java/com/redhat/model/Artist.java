package com.redhat.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "artists")
@JsonInclude(Include.NON_NULL)
public class Artist extends PanacheEntity {

    @NotBlank
    private String name;

    @OneToMany(mappedBy = "artist", cascade = CascadeType.ALL)
    public List<Song> songs;

    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }

    public List<Song> getSongs() {
        return songs;
    }

    public void setSongs(List<Song> songs) {
        this.songs = songs;
    }
}
