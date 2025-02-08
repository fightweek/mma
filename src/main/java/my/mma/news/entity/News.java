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

    @Enumerated(EnumType.STRING)
    private NewsSource source;

    private String title;

    private String content;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "image_file_id")
    private ImageFile imageFile;

    private int likes;

    @OneToMany(mappedBy = "news",cascade = CascadeType.ALL, orphanRemoval = true)
    private List<NewsReply> newsReplies = new ArrayList<>();

}
