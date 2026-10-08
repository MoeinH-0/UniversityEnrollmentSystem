package com.example.universityenrollmentsystem.domain.entity;

import lombok.Getter;
import java.io.Serializable;

@Getter
public abstract class BaseEntity<ID extends Serializable> {
    private ID id;
}
