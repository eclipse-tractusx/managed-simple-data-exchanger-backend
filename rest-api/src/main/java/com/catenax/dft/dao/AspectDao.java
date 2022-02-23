package com.catenax.dft.dao;


import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
@Data
public class AspectDao {


    @Id
    private String uuid;
    public String firstName;
    public String lastName;
    public String email;
    public String email2;
    public String profession;
}
