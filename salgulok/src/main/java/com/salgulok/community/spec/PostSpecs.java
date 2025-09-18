// spec/PostSpecs.java
package com.salgulok.community.spec;

import com.salgulok.community.entity.Post;
import com.salgulok.region.domain.Region;
import com.salgulok.user.domain.User; // 실제 위치에 맞춰 import
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

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

    /** 게시글에 저장된 regionId로 필터링 */
    public static Specification<Post> postRegionIdEq(Long regionId) {
        return (root, query, cb) -> {
            if (regionId == null) {
                return cb.conjunction();
            }
            // `Post` 엔티티의 `regionId` 필드를 직접 참조합니다.
            return cb.equal(root.get("regionId"), regionId);
        };
    }
}
