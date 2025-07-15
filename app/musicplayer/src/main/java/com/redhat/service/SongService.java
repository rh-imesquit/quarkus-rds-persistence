package com.redhat.service;

import java.util.List;
import java.util.Optional;

import com.redhat.model.Song;
import com.redhat.repository.SongRepository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class SongService {

    @Inject
    private SongRepository repository;

    public List<Song> listAll() {
        return repository.listAll();
    }

    public Optional<Song> findById(Long id) {
        return Optional.ofNullable(repository.findById(id));
    }

    @Transactional
    public Song create(Song song) {
        repository.persist(song);
        return song;
    }

    @Transactional
    public boolean delete(Long id) {
        return repository.deleteById(id);
    }
}