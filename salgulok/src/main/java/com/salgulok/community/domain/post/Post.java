//package com.salgulok.community.domain.post;
//
//import com.salgulok.community.domain.comment.Comment;
//import com.salgulok.community.domain.user.User;
//import com.salgulok.community.entity.BaseEntity;
//import com.salgulok.community.entity.Status;
//import jakarta.persistence.*;
//import lombok.*;
//
//import java.util.ArrayList;
//import java.util.List;
//
//@Entity
//@Getter
//@NoArgsConstructor(access = AccessLevel.PROTECTED)
//@AllArgsConstructor
//@Builder
//
//public class Post extends BaseEntity{
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    @Column(nullable = false, length = 100)
//    private String title;
//
//    @Column(nullable = false, columnDefinition = "TEXT")
//    private String content;
//
//    @Column(nullable = false, length = 50)
//    private String region;
//
//    @Enumerated(EnumType.STRING)
//    @Column(nullable = false, length = 20)
//    private Topic topic;
//
//    @Enumerated(EnumType.STRING)
//    @Column(nullable = false, length = 20)
//    private Status status; // STAYING / FINISHED
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "author_id", nullable = false)
//    private User author;
//
//    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
//    private List<Comment> comments = new ArrayList<>();
//
//    //== 편의 메서드 ==//
//    public void addComment(Comment comment) {
//        comments.add(comment);
//        comment.setPost(this);
//    }
//
//    public void updatePost(String title, String content, Topic topic, String region) {
//        this.title = title;
//        this.content = content;
//        this.topic = topic;
//        this.region = region;
//    }
//
//}
//
//
