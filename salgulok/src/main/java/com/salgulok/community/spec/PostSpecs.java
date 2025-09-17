// spec/PostSpecs.java
package com.salgulok.community.spec;

import com.salgulok.community.entity.Post;
import com.salgulok.region.domain.Region;
import com.salgulok.user.domain.User; // 실제 위치에 맞춰 import
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

public class PostSpecs {

    public static Specification<Post> authorRegionEq(String regionNameOrCode) {
        return (root, query, cb) -> {
            Join<Post, User> author = root.join("author", JoinType.INNER);
            Join<User, Region> region = author.join("region", JoinType.LEFT);
            // Region 엔티티 필드명에 맞게 name/code 중 하나로 비교
            return cb.equal(cb.lower(region.get("name")), regionNameOrCode.toLowerCase());
        };
    }

    public static Specification<Post> topicEq(String topic) {
        return (root, query, cb) -> cb.equal(cb.lower(root.get("topic").as(String.class)), topic.toLowerCase());
    }

    public static Specification<Post> authorIsStaying() {
        return (root, query, cb) -> {
            Join<Post, User> author = root.join("author", JoinType.INNER);
            return cb.isTrue(author.get("isTraveling"));
        };
    }
}
