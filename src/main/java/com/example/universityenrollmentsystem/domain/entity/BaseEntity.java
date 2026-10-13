package com.example.universityenrollmentsystem.domain.entity;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;

import java.io.Serializable;
import java.util.Objects;

@Getter
@MappedSuperclass
public abstract class BaseEntity<ID extends Serializable> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private ID id;

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;

        if (o == null)
            return false;

        if (!(o instanceof BaseEntity<?> that))
            return false;

        if (getClass().isInstance(o) || o.getClass().isInstance(this))
            return getId() != null && Objects.equals(getId(), that.getId());

        return false;
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
