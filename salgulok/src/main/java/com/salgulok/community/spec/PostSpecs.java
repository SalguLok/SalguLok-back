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

    // 작성자 지역 ID 또는 지역 이름으로 필터링
    /** 작성자 지역(이름 접두 일치) - '서울', '경상' 등 */
    public static Specification<Post> authorRegionLike(String key) {
        return (root, query, cb) -> {
            if (!StringUtils.hasText(key)) return cb.conjunction();
            var region = root.join("author").join("region", JoinType.LEFT);
            // regions.name이 '서울', '경상' 같은 짧은표기라고 가정
            return cb.like(region.get("name"), key + "%");
        };
    }

    /** 작성자 지역ID = 1..14 (숫자로 필터하고 싶을 때) */
    public static Specification<Post> authorRegionIdEq(Long regionId) {
        return (root, query, cb) -> {
            if (regionId == null) return cb.conjunction();
            var region = root.join("author").join("region", JoinType.LEFT);
            // 컬럼명이 region_id → 엔티티 필드가 보통 'regionId' 혹은 'id'일 것. 실제 필드명에 맞춰 조정.
            return cb.equal(region.get("regionId"), regionId);  // 또는 region.get("id")
        };
    }
}
