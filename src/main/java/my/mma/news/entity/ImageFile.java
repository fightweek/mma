package my.mma.news.entity;

import jakarta.persistence.*;
import lombok.*;

import static jakarta.persistence.FetchType.LAZY;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
public class ImageFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "image_file_id")
    private Long id;

    private String storeFileName;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "news_id")
    private News news;

    public void addNews(News news) {
        this.news = news;
        news.getImageFiles().add(this);
    }

}
