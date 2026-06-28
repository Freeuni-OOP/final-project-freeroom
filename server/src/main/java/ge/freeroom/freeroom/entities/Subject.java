package ge.freeroom.freeroom.entities;

import jakarta.persistence.*;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@Entity
@Table(name = "subject",
        indexes = {
                @Index(columnList = "title"),
                @Index(name = "idx_subject_unique", columnList = "title, type, group_number, lecturer")
        })
public class Subject {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String type;
    
    @Column(name = "group_number")
    private String groupNumber;
    
    private String lecturer;
}
