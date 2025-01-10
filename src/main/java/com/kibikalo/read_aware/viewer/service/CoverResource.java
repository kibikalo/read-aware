package com.kibikalo.read_aware.viewer.service;

import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;

public class CoverResource {
    private final Resource resource;
    private final MediaType mediaType;

    public CoverResource(Resource resource, MediaType mediaType) {
        this.resource = resource;
        this.mediaType = mediaType;
    }

    public Resource getResource() {
        return resource;
    }

    public MediaType getMediaType() {
        return mediaType;
    }
}

