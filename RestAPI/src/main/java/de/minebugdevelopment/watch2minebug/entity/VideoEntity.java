package de.minebugdevelopment.watch2minebug.entity;

import jdk.jfr.StackTrace;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "video")
public class VideoEntity implements IEntity {


    @Id
    @Type(type = "uuid-char")
    private UUID uuid = UUID.randomUUID();

    @Setter
    private String title;

    @Setter
    private String contentPath;

    @Setter
    @ManyToOne(cascade = CascadeType.PERSIST)
    private UserEntity uploader;

}
