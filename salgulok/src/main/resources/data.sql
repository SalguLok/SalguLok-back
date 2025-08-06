INSERT INTO regions (name, created_at)
VALUES
    ('서울', NOW()),
    ('부산', NOW()),
    ('제주도', NOW());

INSERT INTO logs (
    user_id, region_id, title, img_url, one_review, view,
    start_date, end_date, is_public, created_at
)
VALUES
    (1, 1, '서울 여행기', 'https://example.com/img1.jpg', '즐거운 서울 여행', 100,
     '2025-01-01', '2025-01-05', true, NOW()),

    (1, 2, '부산 맛집 탐방', 'https://example.com/img2.jpg', '회가 정말 맛있었어요', 150,
     '2025-03-10', '2025-03-15', true, NOW()),

    (1, 3, '제주도 힐링 여행', 'https://example.com/img3.jpg', '바다를 보며 힐링했어요', 200,
     '2025-05-01', '2025-05-07', false, NOW());
