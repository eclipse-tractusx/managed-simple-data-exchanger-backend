package com.catenax.dft.entities.database;

import lombok.Data;
import org.springframework.lang.Nullable;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
@Data
public class AspectEntity {

    public Integer id;
    public String firstName;
    public String lastName;
    public String email;
    public String email2;
    public String profession;
    @Id
    private String uuid;
}
