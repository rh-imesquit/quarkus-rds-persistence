package com.redhat.repository;

import com.redhat.model.Song;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class SongRepository implements PanacheRepository<Song> {
    
    public Song findById(final Long id) {
        return findById(id);
    }
}
