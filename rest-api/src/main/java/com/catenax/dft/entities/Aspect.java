package com.catenax.dft.entities;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Aspect {

    public Integer id;
    public String firstName;
    public String lastName;
    public String email;
    public String email2;
    public String profession;
}
