package com.example.jwttest.domain.match.domain;

import com.example.jwttest.domain.match.converter.MapToJsonConverter;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.Map;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Match {
    @Id
    @Column(name = "match_id")
    String id;

    @Lob
    @Convert(converter = MapToJsonConverter.class)
    @Column(nullable = false, unique = true)
    Map<String, Object> json;
}
