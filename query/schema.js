window.IRISEN_SCHEMA = {
  "date": "2026-09-09",
  "tables": [
    {
      "name": "account_verification",
      "description": "기존 계정의 변경·탈퇴 등 이메일 인증 요청",
      "group": "계정·운영",
      "columns": [
        {
          "name": "request_id",
          "type": "INTEGER",
          "nullable": "아니요",
          "default": "—",
          "key": "PK(1)"
        },
        {
          "name": "uid",
          "type": "varchar(50)",
          "nullable": "아니요",
          "default": "—",
          "key": "논리 → user.(uid)"
        },
        {
          "name": "email",
          "type": "varchar(191)",
          "nullable": "아니요",
          "default": "—",
          "key": "—"
        },
        {
          "name": "action_type",
          "type": "varchar(30)",
          "nullable": "아니요",
          "default": "—",
          "key": "—"
        },
        {
          "name": "password_hash",
          "type": "varchar(255)",
          "nullable": "예",
          "default": "NULL",
          "key": "—"
        },
        {
          "name": "verification_code",
          "type": "varchar(20)",
          "nullable": "아니요",
          "default": "—",
          "key": "—"
        },
        {
          "name": "expires_at",
          "type": "datetime",
          "nullable": "아니요",
          "default": "—",
          "key": "—"
        },
        {
          "name": "created_at",
          "type": "datetime",
          "nullable": "아니요",
          "default": "CURRENT_TIMESTAMP",
          "key": "—"
        }
      ]
    },
    {
      "name": "alarm",
      "description": "사용자 알림과 요청 처리 참조",
      "group": "계정·운영",
      "columns": [
        {
          "name": "alarm_id",
          "type": "INTEGER",
          "nullable": "아니요",
          "default": "—",
          "key": "PK(1)"
        },
        {
          "name": "uid",
          "type": "varchar(50)",
          "nullable": "아니요",
          "default": "—",
          "key": "FK → user.(uid)"
        },
        {
          "name": "alarm_type",
          "type": "varchar(50)",
          "nullable": "아니요",
          "default": "—",
          "key": "—"
        },
        {
          "name": "title",
          "type": "varchar(200)",
          "nullable": "아니요",
          "default": "—",
          "key": "—"
        },
        {
          "name": "content",
          "type": "TEXT",
          "nullable": "예",
          "default": "—",
          "key": "—"
        },
        {
          "name": "ref_type",
          "type": "varchar(50)",
          "nullable": "예",
          "default": "NULL",
          "key": "—"
        },
        {
          "name": "ref_id",
          "type": "varchar(100)",
          "nullable": "예",
          "default": "NULL",
          "key": "—"
        },
        {
          "name": "ref_gall_id",
          "type": "varchar(50)",
          "nullable": "예",
          "default": "NULL",
          "key": "FK → board.(gall_id)"
        },
        {
          "name": "is_read",
          "type": "tinyint(1)",
          "nullable": "아니요",
          "default": "'0'",
          "key": "—"
        },
        {
          "name": "created_at",
          "type": "datetime",
          "nullable": "아니요",
          "default": "CURRENT_TIMESTAMP",
          "key": "—"
        },
        {
          "name": "read_at",
          "type": "datetime",
          "nullable": "예",
          "default": "NULL",
          "key": "—"
        }
      ]
    },
    {
      "name": "app_seed_history",
      "description": "초기 테스트 데이터 생성 이력",
      "group": "계정·운영",
      "columns": [
        {
          "name": "seed_key",
          "type": "varchar(120)",
          "nullable": "아니요",
          "default": "—",
          "key": "PK(1)"
        },
        {
          "name": "post_count",
          "type": "INT",
          "nullable": "아니요",
          "default": "'0'",
          "key": "—"
        },
        {
          "name": "comment_count",
          "type": "INT",
          "nullable": "아니요",
          "default": "'0'",
          "key": "—"
        },
        {
          "name": "note",
          "type": "varchar(255)",
          "nullable": "예",
          "default": "NULL",
          "key": "—"
        },
        {
          "name": "created_at",
          "type": "datetime",
          "nullable": "아니요",
          "default": "CURRENT_TIMESTAMP",
          "key": "—"
        }
      ]
    },
    {
      "name": "board",
      "description": "보드 기본 정보·관리자·주제·활동 상태",
      "group": "보드",
      "columns": [
        {
          "name": "gall_id",
          "type": "varchar(50)",
          "nullable": "아니요",
          "default": "—",
          "key": "PK(1)"
        },
        {
          "name": "gall_name",
          "type": "varchar(100)",
          "nullable": "아니요",
          "default": "—",
          "key": "—"
        },
        {
          "name": "gall_type",
          "type": "varchar(10)",
          "nullable": "예",
          "default": "NULL",
          "key": "—"
        },
        {
          "name": "category",
          "type": "varchar(50)",
          "nullable": "예",
          "default": "NULL",
          "key": "—"
        },
        {
          "name": "manager_uid",
          "type": "varchar(50)",
          "nullable": "예",
          "default": "NULL",
          "key": "FK → user.(uid)"
        },
        {
          "name": "post_count",
          "type": "INT",
          "nullable": "예",
          "default": "'0'",
          "key": "—"
        },
        {
          "name": "status",
          "type": "varchar(20)",
          "nullable": "아니요",
          "default": "'active'",
          "key": "—"
        },
        {
          "name": "last_activity_at",
          "type": "datetime",
          "nullable": "예",
          "default": "NULL",
          "key": "—"
        },
        {
          "name": "dormant_notified_at",
          "type": "datetime",
          "nullable": "예",
          "default": "NULL",
          "key": "—"
        },
        {
          "name": "dormant_at",
          "type": "datetime",
          "nullable": "예",
          "default": "NULL",
          "key": "—"
        },
        {
          "name": "topic_id",
          "type": "varchar(50)",
          "nullable": "예",
          "default": "NULL",
          "key": "논리 → board_topic.(topic_id)"
        }
      ]
    },
    {
      "name": "board_ban",
      "description": "보드별 사용자 또는 IP 차단",
      "group": "보드",
      "columns": [
        {
          "name": "ban_id",
          "type": "INTEGER",
          "nullable": "아니요",
          "default": "—",
          "key": "PK(1)"
        },
        {
          "name": "gall_id",
          "type": "varchar(50)",
          "nullable": "아니요",
          "default": "—",
          "key": "FK → board.(gall_id)"
        },
        {
          "name": "target_uid",
          "type": "varchar(50)",
          "nullable": "예",
          "default": "NULL",
          "key": "FK → user.(uid)"
        },
        {
          "name": "target_ip",
          "type": "varchar(45)",
          "nullable": "예",
          "default": "NULL",
          "key": "—"
        },
        {
          "name": "banned_by",
          "type": "varchar(50)",
          "nullable": "아니요",
          "default": "—",
          "key": "FK → user.(uid)"
        },
        {
          "name": "reason",
          "type": "varchar(255)",
          "nullable": "예",
          "default": "NULL",
          "key": "—"
        },
        {
          "name": "banned_at",
          "type": "datetime",
          "nullable": "아니요",
          "default": "CURRENT_TIMESTAMP",
          "key": "—"
        },
        {
          "name": "expires_at",
          "type": "datetime",
          "nullable": "예",
          "default": "NULL",
          "key": "—"
        }
      ]
    },
    {
      "name": "board_counter",
      "description": "BoardService의 보드별 게시글 번호 발급",
      "group": "보드",
      "columns": [
        {
          "name": "gall_id",
          "type": "varchar(50)",
          "nullable": "아니요",
          "default": "—",
          "key": "PK(1); 논리 → board.(gall_id)"
        },
        {
          "name": "last_post_no",
          "type": "INT",
          "nullable": "아니요",
          "default": "'0'",
          "key": "—"
        }
      ]
    },
    {
      "name": "board_join_request",
      "description": "보드 가입 신청 및 검토",
      "group": "보드",
      "columns": [
        {
          "name": "request_id",
          "type": "INTEGER",
          "nullable": "아니요",
          "default": "—",
          "key": "PK(1)"
        },
        {
          "name": "gall_id",
          "type": "varchar(50)",
          "nullable": "아니요",
          "default": "—",
          "key": "논리 → board.(gall_id)"
        },
        {
          "name": "uid",
          "type": "varchar(50)",
          "nullable": "아니요",
          "default": "—",
          "key": "논리 → user.(uid)"
        },
        {
          "name": "status",
          "type": "varchar(20)",
          "nullable": "아니요",
          "default": "'pending'",
          "key": "—"
        },
        {
          "name": "reason",
          "type": "TEXT",
          "nullable": "예",
          "default": "—",
          "key": "—"
        },
        {
          "name": "reviewed_by",
          "type": "varchar(50)",
          "nullable": "예",
          "default": "NULL",
          "key": "논리 → user.(uid)"
        },
        {
          "name": "reviewed_at",
          "type": "datetime",
          "nullable": "예",
          "default": "NULL",
          "key": "—"
        },
        {
          "name": "created_at",
          "type": "datetime",
          "nullable": "아니요",
          "default": "CURRENT_TIMESTAMP",
          "key": "—"
        }
      ]
    },
    {
      "name": "board_member",
      "description": "보드 가입 사용자와 역할",
      "group": "보드",
      "columns": [
        {
          "name": "gall_id",
          "type": "varchar(50)",
          "nullable": "아니요",
          "default": "—",
          "key": "PK(1); 논리 → board.(gall_id)"
        },
        {
          "name": "uid",
          "type": "varchar(50)",
          "nullable": "아니요",
          "default": "—",
          "key": "PK(2); 논리 → user.(uid)"
        },
        {
          "name": "member_role",
          "type": "varchar(30)",
          "nullable": "아니요",
          "default": "'member'",
          "key": "—"
        },
        {
          "name": "status",
          "type": "varchar(20)",
          "nullable": "아니요",
          "default": "'active'",
          "key": "—"
        },
        {
          "name": "joined_at",
          "type": "datetime",
          "nullable": "아니요",
          "default": "CURRENT_TIMESTAMP",
          "key": "—"
        },
        {
          "name": "approved_by",
          "type": "varchar(50)",
          "nullable": "예",
          "default": "NULL",
          "key": "논리 → user.(uid)"
        }
      ]
    },
    {
      "name": "board_ranking_refresh_state",
      "description": "날짜별 랭킹 갱신 상태",
      "group": "보드",
      "columns": [
        {
          "name": "ranking_date",
          "type": "date",
          "nullable": "아니요",
          "default": "—",
          "key": "PK(1)"
        },
        {
          "name": "refresh_count",
          "type": "INT",
          "nullable": "아니요",
          "default": "'0'",
          "key": "—"
        },
        {
          "name": "last_refreshed_at",
          "type": "datetime",
          "nullable": "예",
          "default": "NULL",
          "key": "—"
        }
      ]
    },
    {
      "name": "board_ranking_snapshot",
      "description": "날짜별 보드 랭킹 결과",
      "group": "보드",
      "columns": [
        {
          "name": "ranking_date",
          "type": "date",
          "nullable": "아니요",
          "default": "—",
          "key": "PK(1); 논리 → board_ranking_refresh_state.(ranking_date)"
        },
        {
          "name": "rank_no",
          "type": "INT",
          "nullable": "아니요",
          "default": "—",
          "key": "PK(2)"
        },
        {
          "name": "gall_id",
          "type": "varchar(50)",
          "nullable": "아니요",
          "default": "—",
          "key": "논리 → board.(gall_id)"
        },
        {
          "name": "gall_name",
          "type": "varchar(100)",
          "nullable": "아니요",
          "default": "—",
          "key": "—"
        },
        {
          "name": "gall_type",
          "type": "varchar(10)",
          "nullable": "예",
          "default": "NULL",
          "key": "—"
        },
        {
          "name": "score",
          "type": "bigint",
          "nullable": "아니요",
          "default": "'0'",
          "key": "—"
        },
        {
          "name": "post_count",
          "type": "INT",
          "nullable": "아니요",
          "default": "'0'",
          "key": "—"
        },
        {
          "name": "recent_post_count",
          "type": "INT",
          "nullable": "아니요",
          "default": "'0'",
          "key": "—"
        },
        {
          "name": "recent_recommend_sum",
          "type": "INT",
          "nullable": "아니요",
          "default": "'0'",
          "key": "—"
        },
        {
          "name": "refreshed_at",
          "type": "datetime",
          "nullable": "아니요",
          "default": "CURRENT_TIMESTAMP",
          "key": "—"
        }
      ]
    },
    {
      "name": "board_request",
      "description": "보드 개설 신청 및 심사",
      "group": "보드",
      "columns": [
        {
          "name": "request_id",
          "type": "INTEGER",
          "nullable": "아니요",
          "default": "—",
          "key": "PK(1)"
        },
        {
          "name": "requester_uid",
          "type": "varchar(50)",
          "nullable": "아니요",
          "default": "—",
          "key": "FK → user.(uid)"
        },
        {
          "name": "gall_id",
          "type": "varchar(50)",
          "nullable": "예",
          "default": "NULL",
          "key": "논리 → board.(gall_id)"
        },
        {
          "name": "gall_name",
          "type": "varchar(100)",
          "nullable": "아니요",
          "default": "—",
          "key": "—"
        },
        {
          "name": "gall_type",
          "type": "varchar(10)",
          "nullable": "아니요",
          "default": "'m'",
          "key": "—"
        },
        {
          "name": "reason",
          "type": "TEXT",
          "nullable": "예",
          "default": "—",
          "key": "—"
        },
        {
          "name": "status",
          "type": "varchar(20)",
          "nullable": "아니요",
          "default": "'pending'",
          "key": "—"
        },
        {
          "name": "reviewed_by",
          "type": "varchar(50)",
          "nullable": "예",
          "default": "NULL",
          "key": "FK → user.(uid)"
        },
        {
          "name": "reviewed_at",
          "type": "datetime",
          "nullable": "예",
          "default": "NULL",
          "key": "—"
        },
        {
          "name": "created_at",
          "type": "datetime",
          "nullable": "아니요",
          "default": "CURRENT_TIMESTAMP",
          "key": "—"
        },
        {
          "name": "topic_id",
          "type": "varchar(50)",
          "nullable": "예",
          "default": "NULL",
          "key": "논리 → board_topic.(topic_id)"
        }
      ]
    },
    {
      "name": "board_submanager",
      "description": "보드 부관리자와 개별 권한",
      "group": "보드",
      "columns": [
        {
          "name": "gall_id",
          "type": "varchar(50)",
          "nullable": "아니요",
          "default": "—",
          "key": "PK(1); 논리 → board.(gall_id)"
        },
        {
          "name": "uid",
          "type": "varchar(50)",
          "nullable": "아니요",
          "default": "—",
          "key": "PK(2); 논리 → user.(uid)"
        },
        {
          "name": "appointed_by",
          "type": "varchar(50)",
          "nullable": "예",
          "default": "NULL",
          "key": "논리 → user.(uid)"
        },
        {
          "name": "status",
          "type": "varchar(20)",
          "nullable": "아니요",
          "default": "'pending'",
          "key": "—"
        },
        {
          "name": "responded_at",
          "type": "datetime",
          "nullable": "예",
          "default": "NULL",
          "key": "—"
        },
        {
          "name": "created_at",
          "type": "datetime",
          "nullable": "아니요",
          "default": "CURRENT_TIMESTAMP",
          "key": "—"
        },
        {
          "name": "can_delete_post",
          "type": "tinyint(1)",
          "nullable": "아니요",
          "default": "'1'",
          "key": "—"
        },
        {
          "name": "can_delete_comment",
          "type": "tinyint(1)",
          "nullable": "아니요",
          "default": "'1'",
          "key": "—"
        },
        {
          "name": "can_manage_write",
          "type": "tinyint(1)",
          "nullable": "아니요",
          "default": "'1'",
          "key": "—"
        },
        {
          "name": "can_manage_guest_penalty",
          "type": "tinyint(1)",
          "nullable": "아니요",
          "default": "'1'",
          "key": "—"
        },
        {
          "name": "can_manage_tags",
          "type": "tinyint(1)",
          "nullable": "아니요",
          "default": "'1'",
          "key": "—"
        },
        {
          "name": "can_manage_images",
          "type": "tinyint(1)",
          "nullable": "아니요",
          "default": "'1'",
          "key": "—"
        },
        {
          "name": "can_manage_notice",
          "type": "tinyint(1)",
          "nullable": "아니요",
          "default": "'1'",
          "key": "—"
        },
        {
          "name": "can_manage_categories",
          "type": "tinyint(1)",
          "nullable": "아니요",
          "default": "'1'",
          "key": "—"
        },
        {
          "name": "can_manage_cover",
          "type": "tinyint(1)",
          "nullable": "아니요",
          "default": "'1'",
          "key": "—"
        },
        {
          "name": "can_ban_user",
          "type": "tinyint(1)",
          "nullable": "아니요",
          "default": "'1'",
          "key": "—"
        },
        {
          "name": "can_manage_forbidden_word",
          "type": "tinyint(1)",
          "nullable": "아니요",
          "default": "'1'",
          "key": "—"
        },
        {
          "name": "can_bump_post",
          "type": "tinyint(1)",
          "nullable": "아니요",
          "default": "'1'",
          "key": "—"
        },
        {
          "name": "can_manage_concept",
          "type": "tinyint(1)",
          "nullable": "아니요",
          "default": "'1'",
          "key": "—"
        },
        {
          "name": "can_manage_concept_cut",
          "type": "tinyint(1)",
          "nullable": "아니요",
          "default": "'1'",
          "key": "—"
        },
        {
          "name": "can_manage_submanager",
          "type": "tinyint(1)",
          "nullable": "아니요",
          "default": "'0'",
          "key": "—"
        }
      ]
    },
    {
      "name": "board_tag",
      "description": "DB에 남아 있는 보드별 태그",
      "group": "보드",
      "columns": [
        {
          "name": "tag_id",
          "type": "INTEGER",
          "nullable": "아니요",
          "default": "—",
          "key": "PK(1)"
        },
        {
          "name": "gall_id",
          "type": "varchar(50)",
          "nullable": "아니요",
          "default": "—",
          "key": "FK → board.(gall_id)"
        },
        {
          "name": "tag_name",
          "type": "varchar(50)",
          "nullable": "아니요",
          "default": "—",
          "key": "—"
        },
        {
          "name": "sort_order",
          "type": "INT",
          "nullable": "아니요",
          "default": "'0'",
          "key": "—"
        },
        {
          "name": "created_at",
          "type": "datetime",
          "nullable": "아니요",
          "default": "CURRENT_TIMESTAMP",
          "key": "—"
        }
      ]
    },
    {
      "name": "board_topic",
      "description": "보드 주제 분류",
      "group": "보드",
      "columns": [
        {
          "name": "topic_id",
          "type": "varchar(50)",
          "nullable": "아니요",
          "default": "—",
          "key": "PK(1)"
        },
        {
          "name": "topic_name",
          "type": "varchar(100)",
          "nullable": "아니요",
          "default": "—",
          "key": "—"
        },
        {
          "name": "description",
          "type": "varchar(255)",
          "nullable": "예",
          "default": "NULL",
          "key": "—"
        },
        {
          "name": "sort_order",
          "type": "INT",
          "nullable": "아니요",
          "default": "'0'",
          "key": "—"
        },
        {
          "name": "is_active",
          "type": "tinyint(1)",
          "nullable": "아니요",
          "default": "'1'",
          "key": "—"
        },
        {
          "name": "created_at",
          "type": "datetime",
          "nullable": "아니요",
          "default": "CURRENT_TIMESTAMP",
          "key": "—"
        }
      ]
    },
    {
      "name": "board_transfer",
      "description": "보드 관리자 양도 요청",
      "group": "보드",
      "columns": [
        {
          "name": "transfer_id",
          "type": "INTEGER",
          "nullable": "아니요",
          "default": "—",
          "key": "PK(1)"
        },
        {
          "name": "gall_id",
          "type": "varchar(50)",
          "nullable": "아니요",
          "default": "—",
          "key": "FK → board.(gall_id)"
        },
        {
          "name": "from_uid",
          "type": "varchar(50)",
          "nullable": "아니요",
          "default": "—",
          "key": "FK → user.(uid)"
        },
        {
          "name": "to_uid",
          "type": "varchar(50)",
          "nullable": "아니요",
          "default": "—",
          "key": "FK → user.(uid)"
        },
        {
          "name": "status",
          "type": "varchar(20)",
          "nullable": "아니요",
          "default": "'pending'",
          "key": "—"
        },
        {
          "name": "responded_at",
          "type": "datetime",
          "nullable": "예",
          "default": "NULL",
          "key": "—"
        },
        {
          "name": "created_at",
          "type": "datetime",
          "nullable": "아니요",
          "default": "CURRENT_TIMESTAMP",
          "key": "—"
        }
      ]
    },
    {
      "name": "comment",
      "description": "댓글 및 계층형 답글",
      "group": "콘텐츠",
      "columns": [
        {
          "name": "id",
          "type": "INTEGER",
          "nullable": "아니요",
          "default": "—",
          "key": "PK(1)"
        },
        {
          "name": "gall_id",
          "type": "varchar(50)",
          "nullable": "아니요",
          "default": "—",
          "key": "FK → board.(gall_id); 논리 → post.(gall_id, post_no)"
        },
        {
          "name": "post_no",
          "type": "INT",
          "nullable": "아니요",
          "default": "—",
          "key": "논리 → post.(gall_id, post_no)"
        },
        {
          "name": "parent_id",
          "type": "INT",
          "nullable": "예",
          "default": "NULL",
          "key": "FK → comment.(id)"
        },
        {
          "name": "writer_uid",
          "type": "varchar(50)",
          "nullable": "예",
          "default": "NULL",
          "key": "FK → user.(uid)"
        },
        {
          "name": "name",
          "type": "varchar(50)",
          "nullable": "아니요",
          "default": "—",
          "key": "—"
        },
        {
          "name": "ip",
          "type": "varchar(45)",
          "nullable": "예",
          "default": "NULL",
          "key": "—"
        },
        {
          "name": "password",
          "type": "varchar(255)",
          "nullable": "예",
          "default": "NULL",
          "key": "—"
        },
        {
          "name": "content",
          "type": "TEXT",
          "nullable": "아니요",
          "default": "—",
          "key": "—"
        },
        {
          "name": "is_deleted",
          "type": "tinyint(1)",
          "nullable": "아니요",
          "default": "'0'",
          "key": "—"
        },
        {
          "name": "created_at",
          "type": "datetime",
          "nullable": "아니요",
          "default": "CURRENT_TIMESTAMP",
          "key": "—"
        },
        {
          "name": "reply_depth",
          "type": "INT",
          "nullable": "아니요",
          "default": "'0'",
          "key": "—"
        },
        {
          "name": "sort_key",
          "type": "varchar(255)",
          "nullable": "예",
          "default": "NULL",
          "key": "—"
        },
        {
          "name": "like_count",
          "type": "INT",
          "nullable": "아니요",
          "default": "'0'",
          "key": "—"
        },
        {
          "name": "report_count",
          "type": "INT",
          "nullable": "아니요",
          "default": "'0'",
          "key": "—"
        },
        {
          "name": "review_status",
          "type": "varchar(20)",
          "nullable": "아니요",
          "default": "'normal'",
          "key": "—"
        }
      ]
    },
    {
      "name": "comment_reaction",
      "description": "댓글 반응",
      "group": "콘텐츠",
      "columns": [
        {
          "name": "comment_id",
          "type": "bigint",
          "nullable": "아니요",
          "default": "—",
          "key": "PK(1); 논리 → comment.(id)"
        },
        {
          "name": "actor_key",
          "type": "varchar(150)",
          "nullable": "아니요",
          "default": "—",
          "key": "PK(2)"
        },
        {
          "name": "reaction_type",
          "type": "varchar(20)",
          "nullable": "아니요",
          "default": "'like'",
          "key": "—"
        },
        {
          "name": "created_at",
          "type": "datetime",
          "nullable": "아니요",
          "default": "CURRENT_TIMESTAMP",
          "key": "—"
        }
      ]
    },
    {
      "name": "comment_report",
      "description": "댓글 신고",
      "group": "콘텐츠",
      "columns": [
        {
          "name": "report_id",
          "type": "INTEGER",
          "nullable": "아니요",
          "default": "—",
          "key": "PK(1)"
        },
        {
          "name": "comment_id",
          "type": "bigint",
          "nullable": "아니요",
          "default": "—",
          "key": "논리 → comment.(id)"
        },
        {
          "name": "reporter_key",
          "type": "varchar(150)",
          "nullable": "아니요",
          "default": "—",
          "key": "—"
        },
        {
          "name": "reason",
          "type": "varchar(255)",
          "nullable": "예",
          "default": "NULL",
          "key": "—"
        },
        {
          "name": "created_at",
          "type": "datetime",
          "nullable": "아니요",
          "default": "CURRENT_TIMESTAMP",
          "key": "—"
        }
      ]
    },
    {
      "name": "forbidden_word",
      "description": "전체 또는 보드별 금칙어",
      "group": "계정·운영",
      "columns": [
        {
          "name": "word_id",
          "type": "INTEGER",
          "nullable": "아니요",
          "default": "—",
          "key": "PK(1)"
        },
        {
          "name": "gall_id",
          "type": "varchar(50)",
          "nullable": "예",
          "default": "NULL",
          "key": "논리 → board.(gall_id)"
        },
        {
          "name": "word",
          "type": "varchar(100)",
          "nullable": "아니요",
          "default": "—",
          "key": "—"
        },
        {
          "name": "action",
          "type": "varchar(20)",
          "nullable": "아니요",
          "default": "'block'",
          "key": "—"
        },
        {
          "name": "created_by",
          "type": "varchar(50)",
          "nullable": "예",
          "default": "NULL",
          "key": "논리 → user.(uid)"
        },
        {
          "name": "created_at",
          "type": "datetime",
          "nullable": "아니요",
          "default": "CURRENT_TIMESTAMP",
          "key": "—"
        }
      ]
    },
    {
      "name": "gallery_counter",
      "description": "PostService의 보드별 게시글 번호 발급",
      "group": "보드",
      "columns": [
        {
          "name": "gall_id",
          "type": "varchar(50)",
          "nullable": "아니요",
          "default": "—",
          "key": "PK(1); 논리 → board.(gall_id)"
        },
        {
          "name": "last_post_no",
          "type": "INT",
          "nullable": "아니요",
          "default": "'0'",
          "key": "—"
        }
      ]
    },
    {
      "name": "gallery_setting",
      "description": "보드 표시·접근·작성·이미지·가입 정책",
      "group": "보드",
      "columns": [
        {
          "name": "gall_id",
          "type": "varchar(50)",
          "nullable": "아니요",
          "default": "—",
          "key": "PK(1); FK → board.(gall_id)"
        },
        {
          "name": "board_notice",
          "type": "TEXT",
          "nullable": "예",
          "default": "—",
          "key": "—"
        },
        {
          "name": "welcome_message",
          "type": "TEXT",
          "nullable": "예",
          "default": "—",
          "key": "—"
        },
        {
          "name": "welcome_image_url",
          "type": "varchar(500)",
          "nullable": "예",
          "default": "NULL",
          "key": "—"
        },
        {
          "name": "theme_color",
          "type": "varchar(20)",
          "nullable": "아니요",
          "default": "'#ff8fab'",
          "key": "—"
        },
        {
          "name": "allow_guest_post",
          "type": "tinyint(1)",
          "nullable": "아니요",
          "default": "'1'",
          "key": "—"
        },
        {
          "name": "allow_guest_comment",
          "type": "tinyint(1)",
          "nullable": "아니요",
          "default": "'1'",
          "key": "—"
        },
        {
          "name": "concept_recommend_threshold",
          "type": "INT",
          "nullable": "아니요",
          "default": "'10'",
          "key": "—"
        },
        {
          "name": "updated_by",
          "type": "varchar(50)",
          "nullable": "예",
          "default": "NULL",
          "key": "FK → user.(uid)"
        },
        {
          "name": "updated_at",
          "type": "datetime",
          "nullable": "아니요",
          "default": "CURRENT_TIMESTAMP",
          "key": "—"
        },
        {
          "name": "category_options",
          "type": "TEXT",
          "nullable": "예",
          "default": "—",
          "key": "—"
        },
        {
          "name": "join_policy",
          "type": "varchar(20)",
          "nullable": "아니요",
          "default": "'free'",
          "key": "—"
        },
        {
          "name": "visibility",
          "type": "varchar(20)",
          "nullable": "아니요",
          "default": "'public'",
          "key": "—"
        },
        {
          "name": "pinned_notice_count",
          "type": "INT",
          "nullable": "아니요",
          "default": "'3'",
          "key": "—"
        },
        {
          "name": "allowed_attachment_types",
          "type": "varchar(255)",
          "nullable": "예",
          "default": "NULL",
          "key": "—"
        },
        {
          "name": "attachment_max_bytes",
          "type": "bigint",
          "nullable": "아니요",
          "default": "'10485760'",
          "key": "—"
        },
        {
          "name": "side_board_approval_policy",
          "type": "varchar(20)",
          "nullable": "아니요",
          "default": "'operator'",
          "key": "—"
        },
        {
          "name": "dormant_after_days",
          "type": "INT",
          "nullable": "아니요",
          "default": "'180'",
          "key": "—"
        },
        {
          "name": "cover_image_url",
          "type": "varchar(500)",
          "nullable": "예",
          "default": "NULL",
          "key": "—"
        },
        {
          "name": "allow_member_image",
          "type": "tinyint(1)",
          "nullable": "아니요",
          "default": "'1'",
          "key": "—"
        },
        {
          "name": "allow_guest_image",
          "type": "tinyint(1)",
          "nullable": "아니요",
          "default": "'0'",
          "key": "—"
        },
        {
          "name": "board_tags",
          "type": "TEXT",
          "nullable": "예",
          "default": "—",
          "key": "—"
        },
        {
          "name": "read_visibility",
          "type": "varchar(20)",
          "nullable": "아니요",
          "default": "'inherit'",
          "key": "—"
        }
      ]
    },
    {
      "name": "moderation_log",
      "description": "운영 조치 이력",
      "group": "계정·운영",
      "columns": [
        {
          "name": "log_id",
          "type": "INTEGER",
          "nullable": "아니요",
          "default": "—",
          "key": "PK(1)"
        },
        {
          "name": "gall_id",
          "type": "varchar(50)",
          "nullable": "예",
          "default": "NULL",
          "key": "논리 → board.(gall_id)"
        },
        {
          "name": "actor_uid",
          "type": "varchar(50)",
          "nullable": "예",
          "default": "NULL",
          "key": "논리 → user.(uid)"
        },
        {
          "name": "target_uid",
          "type": "varchar(50)",
          "nullable": "예",
          "default": "NULL",
          "key": "논리 → user.(uid)"
        },
        {
          "name": "target_ip",
          "type": "varchar(45)",
          "nullable": "예",
          "default": "NULL",
          "key": "—"
        },
        {
          "name": "action_type",
          "type": "varchar(50)",
          "nullable": "아니요",
          "default": "—",
          "key": "—"
        },
        {
          "name": "reason",
          "type": "varchar(255)",
          "nullable": "예",
          "default": "NULL",
          "key": "—"
        },
        {
          "name": "created_at",
          "type": "datetime",
          "nullable": "아니요",
          "default": "CURRENT_TIMESTAMP",
          "key": "—"
        }
      ]
    },
    {
      "name": "permissions",
      "description": "DB의 권한 정의",
      "group": "계정·운영",
      "columns": [
        {
          "name": "perm_id",
          "type": "INTEGER",
          "nullable": "아니요",
          "default": "—",
          "key": "PK(1)"
        },
        {
          "name": "perm_name",
          "type": "varchar(50)",
          "nullable": "아니요",
          "default": "—",
          "key": "—"
        },
        {
          "name": "description",
          "type": "varchar(255)",
          "nullable": "예",
          "default": "NULL",
          "key": "—"
        }
      ]
    },
    {
      "name": "post",
      "description": "게시글 메타데이터와 기존 본문",
      "group": "콘텐츠",
      "columns": [
        {
          "name": "id",
          "type": "INTEGER",
          "nullable": "아니요",
          "default": "—",
          "key": "PK(1)"
        },
        {
          "name": "gall_id",
          "type": "varchar(50)",
          "nullable": "아니요",
          "default": "—",
          "key": "FK → board.(gall_id)"
        },
        {
          "name": "post_no",
          "type": "INT",
          "nullable": "아니요",
          "default": "—",
          "key": "—"
        },
        {
          "name": "tag_id",
          "type": "INT",
          "nullable": "예",
          "default": "NULL",
          "key": "FK → board_tag.(tag_id)"
        },
        {
          "name": "title",
          "type": "varchar(255)",
          "nullable": "아니요",
          "default": "—",
          "key": "—"
        },
        {
          "name": "content",
          "type": "TEXT",
          "nullable": "예",
          "default": "—",
          "key": "—"
        },
        {
          "name": "writer_uid",
          "type": "varchar(50)",
          "nullable": "예",
          "default": "NULL",
          "key": "FK → user.(uid)"
        },
        {
          "name": "name",
          "type": "varchar(50)",
          "nullable": "아니요",
          "default": "—",
          "key": "—"
        },
        {
          "name": "ip",
          "type": "varchar(45)",
          "nullable": "예",
          "default": "NULL",
          "key": "—"
        },
        {
          "name": "password",
          "type": "varchar(255)",
          "nullable": "예",
          "default": "NULL",
          "key": "—"
        },
        {
          "name": "view_count",
          "type": "INT",
          "nullable": "예",
          "default": "'0'",
          "key": "—"
        },
        {
          "name": "comment_count",
          "type": "INT",
          "nullable": "예",
          "default": "'0'",
          "key": "—"
        },
        {
          "name": "recommend_count",
          "type": "INT",
          "nullable": "예",
          "default": "'0'",
          "key": "—"
        },
        {
          "name": "unrecommend_count",
          "type": "INT",
          "nullable": "예",
          "default": "'0'",
          "key": "—"
        },
        {
          "name": "is_concept",
          "type": "tinyint(1)",
          "nullable": "아니요",
          "default": "'0'",
          "key": "—"
        },
        {
          "name": "concept_at",
          "type": "datetime",
          "nullable": "예",
          "default": "NULL",
          "key": "—"
        },
        {
          "name": "concept_cancelled_by",
          "type": "varchar(50)",
          "nullable": "예",
          "default": "NULL",
          "key": "FK → user.(uid)"
        },
        {
          "name": "concept_cancelled_at",
          "type": "datetime",
          "nullable": "예",
          "default": "NULL",
          "key": "—"
        },
        {
          "name": "is_deleted",
          "type": "tinyint(1)",
          "nullable": "아니요",
          "default": "'0'",
          "key": "—"
        },
        {
          "name": "writed_at",
          "type": "datetime",
          "nullable": "예",
          "default": "CURRENT_TIMESTAMP",
          "key": "—"
        },
        {
          "name": "category",
          "type": "varchar(50)",
          "nullable": "예",
          "default": "NULL",
          "key": "—"
        },
        {
          "name": "is_draft",
          "type": "tinyint(1)",
          "nullable": "아니요",
          "default": "'0'",
          "key": "—"
        },
        {
          "name": "is_secret",
          "type": "tinyint(1)",
          "nullable": "아니요",
          "default": "'0'",
          "key": "—"
        },
        {
          "name": "review_status",
          "type": "varchar(20)",
          "nullable": "아니요",
          "default": "'normal'",
          "key": "—"
        },
        {
          "name": "report_count",
          "type": "INT",
          "nullable": "아니요",
          "default": "'0'",
          "key": "—"
        },
        {
          "name": "pinned_at",
          "type": "datetime",
          "nullable": "예",
          "default": "NULL",
          "key": "—"
        },
        {
          "name": "pin_order",
          "type": "INT",
          "nullable": "예",
          "default": "NULL",
          "key": "—"
        },
        {
          "name": "attachment_urls",
          "type": "TEXT",
          "nullable": "예",
          "default": "—",
          "key": "—"
        },
        {
          "name": "is_notice",
          "type": "tinyint(1)",
          "nullable": "아니요",
          "default": "'0'",
          "key": "—"
        },
        {
          "name": "concept_target_count",
          "type": "INT",
          "nullable": "예",
          "default": "NULL",
          "key": "—"
        },
        {
          "name": "concept_manual_state",
          "type": "varchar(20)",
          "nullable": "아니요",
          "default": "'auto'",
          "key": "—"
        },
        {
          "name": "bumped_at",
          "type": "datetime",
          "nullable": "예",
          "default": "NULL",
          "key": "—"
        }
      ]
    },
    {
      "name": "post_attachment",
      "description": "게시글 첨부파일 메타데이터",
      "group": "콘텐츠",
      "columns": [
        {
          "name": "attachment_id",
          "type": "INTEGER",
          "nullable": "아니요",
          "default": "—",
          "key": "PK(1)"
        },
        {
          "name": "gall_id",
          "type": "varchar(50)",
          "nullable": "아니요",
          "default": "—",
          "key": "논리 → post.(gall_id, post_no)"
        },
        {
          "name": "post_no",
          "type": "bigint",
          "nullable": "아니요",
          "default": "—",
          "key": "논리 → post.(gall_id, post_no)"
        },
        {
          "name": "url",
          "type": "varchar(1000)",
          "nullable": "아니요",
          "default": "—",
          "key": "—"
        },
        {
          "name": "file_type",
          "type": "varchar(100)",
          "nullable": "예",
          "default": "NULL",
          "key": "—"
        },
        {
          "name": "file_size",
          "type": "bigint",
          "nullable": "예",
          "default": "NULL",
          "key": "—"
        },
        {
          "name": "created_at",
          "type": "datetime",
          "nullable": "아니요",
          "default": "CURRENT_TIMESTAMP",
          "key": "—"
        }
      ]
    },
    {
      "name": "post_content",
      "description": "분리된 게시글 본문과 렌더링 정책",
      "group": "콘텐츠",
      "columns": [
        {
          "name": "id",
          "type": "INTEGER",
          "nullable": "아니요",
          "default": "—",
          "key": "PK(1)"
        },
        {
          "name": "post_id",
          "type": "bigint",
          "nullable": "아니요",
          "default": "—",
          "key": "논리 → post.(id)"
        },
        {
          "name": "gall_id",
          "type": "varchar(50)",
          "nullable": "아니요",
          "default": "—",
          "key": "논리 → post.(gall_id, post_no)"
        },
        {
          "name": "post_no",
          "type": "bigint",
          "nullable": "아니요",
          "default": "—",
          "key": "논리 → post.(gall_id, post_no)"
        },
        {
          "name": "content",
          "type": "mediumtext",
          "nullable": "예",
          "default": "—",
          "key": "—"
        },
        {
          "name": "content_format",
          "type": "varchar(30)",
          "nullable": "아니요",
          "default": "'html'",
          "key": "—"
        },
        {
          "name": "render_policy",
          "type": "varchar(30)",
          "nullable": "아니요",
          "default": "'trusted_html'",
          "key": "—"
        },
        {
          "name": "allow_images",
          "type": "tinyint(1)",
          "nullable": "아니요",
          "default": "'1'",
          "key": "—"
        },
        {
          "name": "image_count",
          "type": "INT",
          "nullable": "아니요",
          "default": "'0'",
          "key": "—"
        },
        {
          "name": "word_count",
          "type": "INT",
          "nullable": "아니요",
          "default": "'0'",
          "key": "—"
        },
        {
          "name": "updated_at",
          "type": "datetime",
          "nullable": "아니요",
          "default": "CURRENT_TIMESTAMP",
          "key": "—"
        },
        {
          "name": "created_at",
          "type": "datetime",
          "nullable": "아니요",
          "default": "CURRENT_TIMESTAMP",
          "key": "—"
        }
      ]
    },
    {
      "name": "post_report",
      "description": "게시글 신고",
      "group": "콘텐츠",
      "columns": [
        {
          "name": "report_id",
          "type": "INTEGER",
          "nullable": "아니요",
          "default": "—",
          "key": "PK(1)"
        },
        {
          "name": "gall_id",
          "type": "varchar(50)",
          "nullable": "아니요",
          "default": "—",
          "key": "논리 → post.(gall_id, post_no)"
        },
        {
          "name": "post_no",
          "type": "bigint",
          "nullable": "아니요",
          "default": "—",
          "key": "논리 → post.(gall_id, post_no)"
        },
        {
          "name": "reporter_key",
          "type": "varchar(150)",
          "nullable": "아니요",
          "default": "—",
          "key": "—"
        },
        {
          "name": "reason",
          "type": "varchar(255)",
          "nullable": "예",
          "default": "NULL",
          "key": "—"
        },
        {
          "name": "created_at",
          "type": "datetime",
          "nullable": "아니요",
          "default": "CURRENT_TIMESTAMP",
          "key": "—"
        }
      ]
    },
    {
      "name": "post_scrap",
      "description": "사용자의 게시글 스크랩",
      "group": "콘텐츠",
      "columns": [
        {
          "name": "uid",
          "type": "varchar(50)",
          "nullable": "아니요",
          "default": "—",
          "key": "PK(1); 논리 → user.(uid)"
        },
        {
          "name": "gall_id",
          "type": "varchar(50)",
          "nullable": "아니요",
          "default": "—",
          "key": "PK(2); 논리 → post.(gall_id, post_no)"
        },
        {
          "name": "post_no",
          "type": "bigint",
          "nullable": "아니요",
          "default": "—",
          "key": "PK(3); 논리 → post.(gall_id, post_no)"
        },
        {
          "name": "created_at",
          "type": "datetime",
          "nullable": "아니요",
          "default": "CURRENT_TIMESTAMP",
          "key": "—"
        }
      ]
    },
    {
      "name": "post_vote",
      "description": "게시글 추천·비추천",
      "group": "콘텐츠",
      "columns": [
        {
          "name": "id",
          "type": "INTEGER",
          "nullable": "아니요",
          "default": "—",
          "key": "PK(1)"
        },
        {
          "name": "gall_id",
          "type": "varchar(50)",
          "nullable": "아니요",
          "default": "—",
          "key": "논리 → post.(gall_id, post_no)"
        },
        {
          "name": "post_no",
          "type": "bigint",
          "nullable": "아니요",
          "default": "—",
          "key": "논리 → post.(gall_id, post_no)"
        },
        {
          "name": "actor_key",
          "type": "varchar(150)",
          "nullable": "아니요",
          "default": "—",
          "key": "—"
        },
        {
          "name": "vote_type",
          "type": "varchar(20)",
          "nullable": "아니요",
          "default": "—",
          "key": "—"
        },
        {
          "name": "vote_date",
          "type": "date",
          "nullable": "아니요",
          "default": "—",
          "key": "—"
        },
        {
          "name": "created_at",
          "type": "datetime",
          "nullable": "아니요",
          "default": "CURRENT_TIMESTAMP",
          "key": "—"
        }
      ]
    },
    {
      "name": "role_permissions",
      "description": "역할과 권한의 다대다 연결",
      "group": "계정·운영",
      "columns": [
        {
          "name": "role_id",
          "type": "INT",
          "nullable": "아니요",
          "default": "—",
          "key": "PK(1); FK → roles.(role_id)"
        },
        {
          "name": "perm_id",
          "type": "INT",
          "nullable": "아니요",
          "default": "—",
          "key": "PK(2); FK → permissions.(perm_id)"
        }
      ]
    },
    {
      "name": "roles",
      "description": "DB의 역할 정의",
      "group": "계정·운영",
      "columns": [
        {
          "name": "role_id",
          "type": "INTEGER",
          "nullable": "아니요",
          "default": "—",
          "key": "PK(1)"
        },
        {
          "name": "role_name",
          "type": "varchar(50)",
          "nullable": "아니요",
          "default": "—",
          "key": "—"
        },
        {
          "name": "description",
          "type": "varchar(255)",
          "nullable": "예",
          "default": "NULL",
          "key": "—"
        }
      ]
    },
    {
      "name": "signup_verification",
      "description": "가입 완료 전 인증 정보",
      "group": "계정·운영",
      "columns": [
        {
          "name": "request_id",
          "type": "INTEGER",
          "nullable": "아니요",
          "default": "—",
          "key": "PK(1)"
        },
        {
          "name": "uid",
          "type": "varchar(50)",
          "nullable": "아니요",
          "default": "—",
          "key": "—"
        },
        {
          "name": "nick",
          "type": "varchar(100)",
          "nullable": "아니요",
          "default": "—",
          "key": "—"
        },
        {
          "name": "email",
          "type": "varchar(255)",
          "nullable": "아니요",
          "default": "—",
          "key": "—"
        },
        {
          "name": "password_hash",
          "type": "varchar(255)",
          "nullable": "아니요",
          "default": "—",
          "key": "—"
        },
        {
          "name": "verification_code",
          "type": "varchar(20)",
          "nullable": "아니요",
          "default": "—",
          "key": "—"
        },
        {
          "name": "expires_at",
          "type": "datetime",
          "nullable": "아니요",
          "default": "—",
          "key": "—"
        },
        {
          "name": "created_at",
          "type": "datetime",
          "nullable": "아니요",
          "default": "CURRENT_TIMESTAMP",
          "key": "—"
        },
        {
          "name": "nick_type",
          "type": "varchar(20)",
          "nullable": "아니요",
          "default": "'variable'",
          "key": "—"
        }
      ]
    },
    {
      "name": "user",
      "description": "사용자 계정과 서비스 역할",
      "group": "계정·운영",
      "columns": [
        {
          "name": "uid",
          "type": "varchar(50)",
          "nullable": "아니요",
          "default": "—",
          "key": "PK(1)"
        },
        {
          "name": "nick",
          "type": "varchar(50)",
          "nullable": "아니요",
          "default": "—",
          "key": "—"
        },
        {
          "name": "nick_icon_type",
          "type": "varchar(20)",
          "nullable": "예",
          "default": "'default'",
          "key": "—"
        },
        {
          "name": "password_hash",
          "type": "varchar(255)",
          "nullable": "아니요",
          "default": "—",
          "key": "—"
        },
        {
          "name": "email",
          "type": "varchar(100)",
          "nullable": "예",
          "default": "NULL",
          "key": "—"
        },
        {
          "name": "member_division",
          "type": "varchar(20)",
          "nullable": "예",
          "default": "'user'",
          "key": "—"
        },
        {
          "name": "created_at",
          "type": "datetime",
          "nullable": "예",
          "default": "CURRENT_TIMESTAMP",
          "key": "—"
        },
        {
          "name": "nick_type",
          "type": "varchar(20)",
          "nullable": "아니요",
          "default": "'variable'",
          "key": "—"
        }
      ]
    },
    {
      "name": "user_block",
      "description": "사용자 간 차단",
      "group": "계정·운영",
      "columns": [
        {
          "name": "blocker_uid",
          "type": "varchar(50)",
          "nullable": "아니요",
          "default": "—",
          "key": "PK(1); 논리 → user.(uid)"
        },
        {
          "name": "blocked_uid",
          "type": "varchar(50)",
          "nullable": "아니요",
          "default": "—",
          "key": "PK(2); 논리 → user.(uid)"
        },
        {
          "name": "created_at",
          "type": "datetime",
          "nullable": "아니요",
          "default": "CURRENT_TIMESTAMP",
          "key": "—"
        }
      ]
    },
    {
      "name": "user_follow",
      "description": "사용자 간 팔로우",
      "group": "계정·운영",
      "columns": [
        {
          "name": "follower_uid",
          "type": "varchar(50)",
          "nullable": "아니요",
          "default": "—",
          "key": "PK(1); FK → user.(uid)"
        },
        {
          "name": "following_uid",
          "type": "varchar(50)",
          "nullable": "아니요",
          "default": "—",
          "key": "PK(2); FK → user.(uid)"
        },
        {
          "name": "created_at",
          "type": "datetime",
          "nullable": "아니요",
          "default": "CURRENT_TIMESTAMP",
          "key": "—"
        }
      ]
    },
    {
      "name": "user_notification_setting",
      "description": "사용자별 알림 설정",
      "group": "계정·운영",
      "columns": [
        {
          "name": "uid",
          "type": "varchar(50)",
          "nullable": "아니요",
          "default": "—",
          "key": "PK(1); 논리 → user.(uid)"
        },
        {
          "name": "in_app_enabled",
          "type": "tinyint(1)",
          "nullable": "아니요",
          "default": "'1'",
          "key": "—"
        },
        {
          "name": "email_enabled",
          "type": "tinyint(1)",
          "nullable": "아니요",
          "default": "'0'",
          "key": "—"
        },
        {
          "name": "follow_post_enabled",
          "type": "tinyint(1)",
          "nullable": "아니요",
          "default": "'1'",
          "key": "—"
        },
        {
          "name": "comment_enabled",
          "type": "tinyint(1)",
          "nullable": "아니요",
          "default": "'1'",
          "key": "—"
        },
        {
          "name": "updated_at",
          "type": "datetime",
          "nullable": "아니요",
          "default": "CURRENT_TIMESTAMP",
          "key": "—"
        }
      ]
    },
    {
      "name": "user_profile",
      "description": "기존 프로필 정보",
      "group": "계정·운영",
      "columns": [
        {
          "name": "profile_id",
          "type": "INTEGER",
          "nullable": "아니요",
          "default": "—",
          "key": "PK(1)"
        },
        {
          "name": "uid",
          "type": "varchar(50)",
          "nullable": "아니요",
          "default": "—",
          "key": "FK → user.(uid)"
        },
        {
          "name": "profile_image",
          "type": "varchar(500)",
          "nullable": "예",
          "default": "NULL",
          "key": "—"
        },
        {
          "name": "bio",
          "type": "TEXT",
          "nullable": "예",
          "default": "—",
          "key": "—"
        },
        {
          "name": "birthdate",
          "type": "date",
          "nullable": "예",
          "default": "NULL",
          "key": "—"
        },
        {
          "name": "gender",
          "type": "varchar(10)",
          "nullable": "예",
          "default": "NULL",
          "key": "—"
        },
        {
          "name": "website_url",
          "type": "varchar(300)",
          "nullable": "예",
          "default": "NULL",
          "key": "—"
        },
        {
          "name": "sns_instagram",
          "type": "varchar(100)",
          "nullable": "예",
          "default": "NULL",
          "key": "—"
        },
        {
          "name": "sns_x",
          "type": "varchar(100)",
          "nullable": "예",
          "default": "NULL",
          "key": "—"
        },
        {
          "name": "sns_youtube",
          "type": "varchar(200)",
          "nullable": "예",
          "default": "NULL",
          "key": "—"
        },
        {
          "name": "sns_github",
          "type": "varchar(100)",
          "nullable": "예",
          "default": "NULL",
          "key": "—"
        },
        {
          "name": "is_public",
          "type": "tinyint(1)",
          "nullable": "아니요",
          "default": "'1'",
          "key": "—"
        },
        {
          "name": "updated_at",
          "type": "datetime",
          "nullable": "아니요",
          "default": "CURRENT_TIMESTAMP",
          "key": "—"
        }
      ]
    },
    {
      "name": "user_profile_setting",
      "description": "현재 프로필 표시·공개 설정",
      "group": "계정·운영",
      "columns": [
        {
          "name": "uid",
          "type": "varchar(50)",
          "nullable": "아니요",
          "default": "—",
          "key": "PK(1); FK → user.(uid)"
        },
        {
          "name": "status_message",
          "type": "varchar(160)",
          "nullable": "예",
          "default": "NULL",
          "key": "—"
        },
        {
          "name": "bio",
          "type": "TEXT",
          "nullable": "예",
          "default": "—",
          "key": "—"
        },
        {
          "name": "accent_color",
          "type": "varchar(20)",
          "nullable": "아니요",
          "default": "'#ff8fab'",
          "key": "—"
        },
        {
          "name": "show_posts",
          "type": "tinyint(1)",
          "nullable": "아니요",
          "default": "'1'",
          "key": "—"
        },
        {
          "name": "show_comments",
          "type": "tinyint(1)",
          "nullable": "아니요",
          "default": "'1'",
          "key": "—"
        },
        {
          "name": "show_birthdate",
          "type": "tinyint(1)",
          "nullable": "아니요",
          "default": "'1'",
          "key": "—"
        },
        {
          "name": "show_gender",
          "type": "tinyint(1)",
          "nullable": "아니요",
          "default": "'1'",
          "key": "—"
        },
        {
          "name": "show_sns",
          "type": "tinyint(1)",
          "nullable": "아니요",
          "default": "'1'",
          "key": "—"
        },
        {
          "name": "updated_at",
          "type": "datetime",
          "nullable": "아니요",
          "default": "CURRENT_TIMESTAMP",
          "key": "—"
        },
        {
          "name": "avatar_url",
          "type": "varchar(500)",
          "nullable": "예",
          "default": "NULL",
          "key": "—"
        },
        {
          "name": "banner_url",
          "type": "varchar(500)",
          "nullable": "예",
          "default": "NULL",
          "key": "—"
        },
        {
          "name": "show_followers",
          "type": "tinyint(1)",
          "nullable": "아니요",
          "default": "'1'",
          "key": "—"
        },
        {
          "name": "show_following",
          "type": "tinyint(1)",
          "nullable": "아니요",
          "default": "'1'",
          "key": "—"
        },
        {
          "name": "avatar_position",
          "type": "VARCHAR(10)",
          "nullable": "아니요",
          "default": "'center'",
          "key": "—"
        },
        {
          "name": "banner_position",
          "type": "VARCHAR(10)",
          "nullable": "아니요",
          "default": "'center'",
          "key": "—"
        }
      ]
    },
    {
      "name": "user_suspension",
      "description": "사용자 이용 정지",
      "group": "계정·운영",
      "columns": [
        {
          "name": "uid",
          "type": "varchar(50)",
          "nullable": "아니요",
          "default": "—",
          "key": "PK(1); 논리 → user.(uid)"
        },
        {
          "name": "reason",
          "type": "varchar(255)",
          "nullable": "예",
          "default": "NULL",
          "key": "—"
        },
        {
          "name": "suspended_by",
          "type": "varchar(50)",
          "nullable": "예",
          "default": "NULL",
          "key": "논리 → user.(uid)"
        },
        {
          "name": "suspended_at",
          "type": "datetime",
          "nullable": "아니요",
          "default": "CURRENT_TIMESTAMP",
          "key": "—"
        },
        {
          "name": "expires_at",
          "type": "datetime",
          "nullable": "예",
          "default": "NULL",
          "key": "—"
        }
      ]
    }
  ],
  "relations": [
    {
      "source": "account_verification",
      "columns": [
        "uid"
      ],
      "target": "user",
      "targetColumns": [
        "uid"
      ],
      "kind": "논리",
      "note": "코드의 조회·저장 관계"
    },
    {
      "source": "alarm",
      "columns": [
        "ref_gall_id"
      ],
      "target": "board",
      "targetColumns": [
        "gall_id"
      ],
      "kind": "FK",
      "note": "로컬 DB에 선언"
    },
    {
      "source": "alarm",
      "columns": [
        "uid"
      ],
      "target": "user",
      "targetColumns": [
        "uid"
      ],
      "kind": "FK",
      "note": "로컬 DB에 선언"
    },
    {
      "source": "board",
      "columns": [
        "manager_uid"
      ],
      "target": "user",
      "targetColumns": [
        "uid"
      ],
      "kind": "FK",
      "note": "로컬 DB에 선언"
    },
    {
      "source": "board",
      "columns": [
        "topic_id"
      ],
      "target": "board_topic",
      "targetColumns": [
        "topic_id"
      ],
      "kind": "논리",
      "note": "주제 분류"
    },
    {
      "source": "board_ban",
      "columns": [
        "banned_by"
      ],
      "target": "user",
      "targetColumns": [
        "uid"
      ],
      "kind": "FK",
      "note": "로컬 DB에 선언"
    },
    {
      "source": "board_ban",
      "columns": [
        "gall_id"
      ],
      "target": "board",
      "targetColumns": [
        "gall_id"
      ],
      "kind": "FK",
      "note": "로컬 DB에 선언"
    },
    {
      "source": "board_ban",
      "columns": [
        "target_uid"
      ],
      "target": "user",
      "targetColumns": [
        "uid"
      ],
      "kind": "FK",
      "note": "로컬 DB에 선언"
    },
    {
      "source": "board_counter",
      "columns": [
        "gall_id"
      ],
      "target": "board",
      "targetColumns": [
        "gall_id"
      ],
      "kind": "논리",
      "note": "보드 식별자"
    },
    {
      "source": "board_join_request",
      "columns": [
        "gall_id"
      ],
      "target": "board",
      "targetColumns": [
        "gall_id"
      ],
      "kind": "논리",
      "note": "보드 식별자"
    },
    {
      "source": "board_join_request",
      "columns": [
        "reviewed_by"
      ],
      "target": "user",
      "targetColumns": [
        "uid"
      ],
      "kind": "논리",
      "note": "코드의 조회·저장 관계"
    },
    {
      "source": "board_join_request",
      "columns": [
        "uid"
      ],
      "target": "user",
      "targetColumns": [
        "uid"
      ],
      "kind": "논리",
      "note": "코드의 조회·저장 관계"
    },
    {
      "source": "board_member",
      "columns": [
        "approved_by"
      ],
      "target": "user",
      "targetColumns": [
        "uid"
      ],
      "kind": "논리",
      "note": "코드의 조회·저장 관계"
    },
    {
      "source": "board_member",
      "columns": [
        "gall_id"
      ],
      "target": "board",
      "targetColumns": [
        "gall_id"
      ],
      "kind": "논리",
      "note": "보드 식별자"
    },
    {
      "source": "board_member",
      "columns": [
        "uid"
      ],
      "target": "user",
      "targetColumns": [
        "uid"
      ],
      "kind": "논리",
      "note": "코드의 조회·저장 관계"
    },
    {
      "source": "board_ranking_snapshot",
      "columns": [
        "gall_id"
      ],
      "target": "board",
      "targetColumns": [
        "gall_id"
      ],
      "kind": "논리",
      "note": "보드 식별자"
    },
    {
      "source": "board_ranking_snapshot",
      "columns": [
        "ranking_date"
      ],
      "target": "board_ranking_refresh_state",
      "targetColumns": [
        "ranking_date"
      ],
      "kind": "논리",
      "note": "같은 날짜의 집계와 갱신 상태; FK 없는 운영상 연결"
    },
    {
      "source": "board_request",
      "columns": [
        "gall_id"
      ],
      "target": "board",
      "targetColumns": [
        "gall_id"
      ],
      "kind": "논리",
      "note": "개설할 보드 ID; 승인 전에는 대상 보드가 없을 수 있음"
    },
    {
      "source": "board_request",
      "columns": [
        "requester_uid"
      ],
      "target": "user",
      "targetColumns": [
        "uid"
      ],
      "kind": "FK",
      "note": "로컬 DB에 선언"
    },
    {
      "source": "board_request",
      "columns": [
        "reviewed_by"
      ],
      "target": "user",
      "targetColumns": [
        "uid"
      ],
      "kind": "FK",
      "note": "로컬 DB에 선언"
    },
    {
      "source": "board_request",
      "columns": [
        "topic_id"
      ],
      "target": "board_topic",
      "targetColumns": [
        "topic_id"
      ],
      "kind": "논리",
      "note": "주제 분류"
    },
    {
      "source": "board_submanager",
      "columns": [
        "appointed_by"
      ],
      "target": "user",
      "targetColumns": [
        "uid"
      ],
      "kind": "논리",
      "note": "코드의 조회·저장 관계"
    },
    {
      "source": "board_submanager",
      "columns": [
        "gall_id"
      ],
      "target": "board",
      "targetColumns": [
        "gall_id"
      ],
      "kind": "논리",
      "note": "보드 식별자"
    },
    {
      "source": "board_submanager",
      "columns": [
        "uid"
      ],
      "target": "user",
      "targetColumns": [
        "uid"
      ],
      "kind": "논리",
      "note": "코드의 조회·저장 관계"
    },
    {
      "source": "board_tag",
      "columns": [
        "gall_id"
      ],
      "target": "board",
      "targetColumns": [
        "gall_id"
      ],
      "kind": "FK",
      "note": "로컬 DB에 선언"
    },
    {
      "source": "board_transfer",
      "columns": [
        "from_uid"
      ],
      "target": "user",
      "targetColumns": [
        "uid"
      ],
      "kind": "FK",
      "note": "로컬 DB에 선언"
    },
    {
      "source": "board_transfer",
      "columns": [
        "gall_id"
      ],
      "target": "board",
      "targetColumns": [
        "gall_id"
      ],
      "kind": "FK",
      "note": "로컬 DB에 선언"
    },
    {
      "source": "board_transfer",
      "columns": [
        "to_uid"
      ],
      "target": "user",
      "targetColumns": [
        "uid"
      ],
      "kind": "FK",
      "note": "로컬 DB에 선언"
    },
    {
      "source": "comment",
      "columns": [
        "gall_id"
      ],
      "target": "board",
      "targetColumns": [
        "gall_id"
      ],
      "kind": "FK",
      "note": "로컬 DB에 선언"
    },
    {
      "source": "comment",
      "columns": [
        "gall_id",
        "post_no"
      ],
      "target": "post",
      "targetColumns": [
        "gall_id",
        "post_no"
      ],
      "kind": "논리",
      "note": "보드 안에서 게시글 번호를 해석하는 복합 연결"
    },
    {
      "source": "comment",
      "columns": [
        "parent_id"
      ],
      "target": "comment",
      "targetColumns": [
        "id"
      ],
      "kind": "FK",
      "note": "로컬 DB에 선언"
    },
    {
      "source": "comment",
      "columns": [
        "writer_uid"
      ],
      "target": "user",
      "targetColumns": [
        "uid"
      ],
      "kind": "FK",
      "note": "로컬 DB에 선언"
    },
    {
      "source": "comment_reaction",
      "columns": [
        "comment_id"
      ],
      "target": "comment",
      "targetColumns": [
        "id"
      ],
      "kind": "논리",
      "note": "코드의 조회·저장 관계"
    },
    {
      "source": "comment_report",
      "columns": [
        "comment_id"
      ],
      "target": "comment",
      "targetColumns": [
        "id"
      ],
      "kind": "논리",
      "note": "코드의 조회·저장 관계"
    },
    {
      "source": "forbidden_word",
      "columns": [
        "created_by"
      ],
      "target": "user",
      "targetColumns": [
        "uid"
      ],
      "kind": "논리",
      "note": "코드의 조회·저장 관계"
    },
    {
      "source": "forbidden_word",
      "columns": [
        "gall_id"
      ],
      "target": "board",
      "targetColumns": [
        "gall_id"
      ],
      "kind": "논리",
      "note": "보드 범위; NULL이면 전역 범위 가능"
    },
    {
      "source": "gallery_counter",
      "columns": [
        "gall_id"
      ],
      "target": "board",
      "targetColumns": [
        "gall_id"
      ],
      "kind": "논리",
      "note": "보드 식별자"
    },
    {
      "source": "gallery_setting",
      "columns": [
        "gall_id"
      ],
      "target": "board",
      "targetColumns": [
        "gall_id"
      ],
      "kind": "FK",
      "note": "로컬 DB에 선언"
    },
    {
      "source": "gallery_setting",
      "columns": [
        "updated_by"
      ],
      "target": "user",
      "targetColumns": [
        "uid"
      ],
      "kind": "FK",
      "note": "로컬 DB에 선언"
    },
    {
      "source": "moderation_log",
      "columns": [
        "actor_uid"
      ],
      "target": "user",
      "targetColumns": [
        "uid"
      ],
      "kind": "논리",
      "note": "코드의 조회·저장 관계"
    },
    {
      "source": "moderation_log",
      "columns": [
        "gall_id"
      ],
      "target": "board",
      "targetColumns": [
        "gall_id"
      ],
      "kind": "논리",
      "note": "보드 범위; NULL이면 전역 범위 가능"
    },
    {
      "source": "moderation_log",
      "columns": [
        "target_uid"
      ],
      "target": "user",
      "targetColumns": [
        "uid"
      ],
      "kind": "논리",
      "note": "코드의 조회·저장 관계"
    },
    {
      "source": "post",
      "columns": [
        "concept_cancelled_by"
      ],
      "target": "user",
      "targetColumns": [
        "uid"
      ],
      "kind": "FK",
      "note": "로컬 DB에 선언"
    },
    {
      "source": "post",
      "columns": [
        "gall_id"
      ],
      "target": "board",
      "targetColumns": [
        "gall_id"
      ],
      "kind": "FK",
      "note": "로컬 DB에 선언"
    },
    {
      "source": "post",
      "columns": [
        "tag_id"
      ],
      "target": "board_tag",
      "targetColumns": [
        "tag_id"
      ],
      "kind": "FK",
      "note": "로컬 DB에 선언"
    },
    {
      "source": "post",
      "columns": [
        "writer_uid"
      ],
      "target": "user",
      "targetColumns": [
        "uid"
      ],
      "kind": "FK",
      "note": "로컬 DB에 선언"
    },
    {
      "source": "post_attachment",
      "columns": [
        "gall_id",
        "post_no"
      ],
      "target": "post",
      "targetColumns": [
        "gall_id",
        "post_no"
      ],
      "kind": "논리",
      "note": "보드 안에서 게시글 번호를 해석하는 복합 연결"
    },
    {
      "source": "post_content",
      "columns": [
        "gall_id",
        "post_no"
      ],
      "target": "post",
      "targetColumns": [
        "gall_id",
        "post_no"
      ],
      "kind": "논리",
      "note": "보드 안에서 게시글 번호를 해석하는 복합 연결"
    },
    {
      "source": "post_content",
      "columns": [
        "post_id"
      ],
      "target": "post",
      "targetColumns": [
        "id"
      ],
      "kind": "논리",
      "note": "현재 코드의 UNIQUE KEY로 본문 0..1건 의도; 로컬 DB에는 해당 UNIQUE 없음"
    },
    {
      "source": "post_report",
      "columns": [
        "gall_id",
        "post_no"
      ],
      "target": "post",
      "targetColumns": [
        "gall_id",
        "post_no"
      ],
      "kind": "논리",
      "note": "보드 안에서 게시글 번호를 해석하는 복합 연결"
    },
    {
      "source": "post_scrap",
      "columns": [
        "gall_id",
        "post_no"
      ],
      "target": "post",
      "targetColumns": [
        "gall_id",
        "post_no"
      ],
      "kind": "논리",
      "note": "보드 안에서 게시글 번호를 해석하는 복합 연결"
    },
    {
      "source": "post_scrap",
      "columns": [
        "uid"
      ],
      "target": "user",
      "targetColumns": [
        "uid"
      ],
      "kind": "논리",
      "note": "코드의 조회·저장 관계"
    },
    {
      "source": "post_vote",
      "columns": [
        "gall_id",
        "post_no"
      ],
      "target": "post",
      "targetColumns": [
        "gall_id",
        "post_no"
      ],
      "kind": "논리",
      "note": "보드 안에서 게시글 번호를 해석하는 복합 연결"
    },
    {
      "source": "role_permissions",
      "columns": [
        "perm_id"
      ],
      "target": "permissions",
      "targetColumns": [
        "perm_id"
      ],
      "kind": "FK",
      "note": "로컬 DB에 선언"
    },
    {
      "source": "role_permissions",
      "columns": [
        "role_id"
      ],
      "target": "roles",
      "targetColumns": [
        "role_id"
      ],
      "kind": "FK",
      "note": "로컬 DB에 선언"
    },
    {
      "source": "user_block",
      "columns": [
        "blocked_uid"
      ],
      "target": "user",
      "targetColumns": [
        "uid"
      ],
      "kind": "논리",
      "note": "코드의 조회·저장 관계"
    },
    {
      "source": "user_block",
      "columns": [
        "blocker_uid"
      ],
      "target": "user",
      "targetColumns": [
        "uid"
      ],
      "kind": "논리",
      "note": "코드의 조회·저장 관계"
    },
    {
      "source": "user_follow",
      "columns": [
        "follower_uid"
      ],
      "target": "user",
      "targetColumns": [
        "uid"
      ],
      "kind": "FK",
      "note": "로컬 DB에 선언"
    },
    {
      "source": "user_follow",
      "columns": [
        "following_uid"
      ],
      "target": "user",
      "targetColumns": [
        "uid"
      ],
      "kind": "FK",
      "note": "로컬 DB에 선언"
    },
    {
      "source": "user_notification_setting",
      "columns": [
        "uid"
      ],
      "target": "user",
      "targetColumns": [
        "uid"
      ],
      "kind": "논리",
      "note": "코드의 조회·저장 관계"
    },
    {
      "source": "user_profile",
      "columns": [
        "uid"
      ],
      "target": "user",
      "targetColumns": [
        "uid"
      ],
      "kind": "FK",
      "note": "로컬 DB에 선언"
    },
    {
      "source": "user_profile_setting",
      "columns": [
        "uid"
      ],
      "target": "user",
      "targetColumns": [
        "uid"
      ],
      "kind": "FK",
      "note": "로컬 DB에 선언"
    },
    {
      "source": "user_suspension",
      "columns": [
        "suspended_by"
      ],
      "target": "user",
      "targetColumns": [
        "uid"
      ],
      "kind": "논리",
      "note": "코드의 조회·저장 관계"
    },
    {
      "source": "user_suspension",
      "columns": [
        "uid"
      ],
      "target": "user",
      "targetColumns": [
        "uid"
      ],
      "kind": "논리",
      "note": "코드의 조회·저장 관계"
    }
  ]
};
