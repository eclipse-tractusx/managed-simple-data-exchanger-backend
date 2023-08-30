package org.eclipse.tractusx.sde.agent.entity;

import jakarta.persistence.*;
import lombok.Data;

@Table(name = "ftps_config")
@Entity
@Data
public class SftpConfigEntity {

    @Id
    @Column(name = "uuid")
    private String uuid;

    @Column(name = "type")
    private String type;

    @Column(name = "content")
    private String content;
}
