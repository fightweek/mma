package my.mma.news.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Getter
public class News {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "news_id")
    private Long id;

    private String source;

    private String title;

    @Lob
    private String content;

    @OneToMany(mappedBy = "news",cascade = CascadeType.ALL,orphanRemoval = true)
    @Builder.Default
    private List<ImageFile> imageFiles = new ArrayList<>();

    private int likes;

    @OneToMany(mappedBy = "news",cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<NewsReply> newsReplies = new ArrayList<>();


}
