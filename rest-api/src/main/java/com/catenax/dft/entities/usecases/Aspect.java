package com.catenax.dft.entities.usecases;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class Aspect {

    public String uuid;
    public Integer id;
    public String firstName;
    public String lastName;
    public String email;
    public String email2;
    public String profession;
}
