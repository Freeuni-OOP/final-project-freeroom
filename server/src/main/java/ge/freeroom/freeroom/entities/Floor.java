package ge.freeroom.freeroom.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "floor")
public class Floor {
    @Id
    private Long id;

    @Column(nullable = false, unique = true)
    private int number; // 1-4

    @com.fasterxml.jackson.annotation.JsonIgnore
    @OneToMany(mappedBy = "floor")
    private List<Room> rooms;
}