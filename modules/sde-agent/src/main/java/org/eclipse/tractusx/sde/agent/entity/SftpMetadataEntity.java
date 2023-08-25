package org.eclipse.tractusx.sde.agent.entity;

import jakarta.persistence.*;
import lombok.Data;

@Table(name = "ftpsConfig")
@Entity
@Data
public class SftpMetadataEntity {

    @Id
    @Column(name = "uuid")
    private String uuid;

    @Column(name = "content")
    private String content;
}
