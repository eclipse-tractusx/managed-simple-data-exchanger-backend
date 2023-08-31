package org.eclipse.tractusx.sde.agent.entity;

import jakarta.persistence.*;
import lombok.Data;

@Table(name = "ftps_config")
@Entity
@Data
public class SftpConfigEntity {

    public final static String SFTP_CONFIG_ID = "755FCA09-884B-4A05-91C9-EB2D3E7A45FF";

    @Id
    @Column(name = "uuid")
    private String uuid;

    @Column(name = "type")
    private String type;

    @Column(name = "content")
    private String content;
}
