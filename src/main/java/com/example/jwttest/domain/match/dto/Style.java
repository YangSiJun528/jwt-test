package com.example.jwttest.domain.match.dto;

import java.util.ArrayList;

public record Style(
        String description,
        ArrayList<Selection> selections,
        int style
) {}



