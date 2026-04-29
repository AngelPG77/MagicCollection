package pga.magiccollectionspring.card.domain;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "mtg_sets", indexes = {
        @Index(name = "idx_set_code", columnList = "code")
})
public class MtgSet {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 10)
    private String code;

    @Column(nullable = false)
    private String name;

    @Column(name = "release_date")
    private LocalDate releaseDate;

    public MtgSet() {}

    public MtgSet(String code, String name, LocalDate releaseDate) {
        this.code = code;
        this.name = name;
        this.releaseDate = releaseDate;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public LocalDate getReleaseDate() { return releaseDate; }
    public void setReleaseDate(LocalDate releaseDate) { this.releaseDate = releaseDate; }
}
