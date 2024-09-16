package study.data_jpa.repository;

import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import study.data_jpa.dto.MemberDto;
import study.data_jpa.entity.Member;

import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long>, MemberRepositoryCustom {

    //** 쿼리 메소드
    List<Member> findByUsernameAndAgeGreaterThan(String username, int age);

    //** 익명 네임드 쿼리
    @Query("select m from Member m where m.username = :username and m.age = :age")
    List<Member> findUser(@Param("username") String username, @Param("age") int age);

    @Query("select m.username from Member m")
    List<String> findUsernameList();

    //** dto 변환
    @Query("select new study.data_jpa.dto.MemberDto(m.id, m.username, t.name) from Member m join m.team t")
    List<MemberDto> findMemberDto();

    //* 익명 네임드 쿼리 in절 사용
    @Query("select m from Member m where m.username in :names")
    List<Member> findByNames(@Param("names") List<String> names);

    List<Member> findListByUsername(String username);
    Member findMemberByUsername(String username);
    Optional<Member> findOptionalByUsername(String username);

    /**
    스프링 데이터 jpa 페이징 방법
    1. 파라미터 Pageable 리턴타입 Page 인 함수를 만든다
    2. Pageable(PageRequest) 생성하면서 page, size, sort 전달
    3. Page 객체 사용
     **/
    // 카운트 쿼리 분리 가능
    @Query(value = "select m from Member m left join m.team t", countQuery = "select count(m.username) from Member m")
    //Pageable 을 넘기고 Page 로 리턴받으면 끝
    Page<Member> findByAge(int age, Pageable pageable);

    //** 벌크 업데이트 쿼리(영속성컨텍스트를 무시하고 바로 db에 업데이트 하여 문제가 생길수 있음)
    @Modifying(clearAutomatically = true) // 필수로 넣어야 executeUpdate() 호출함
    @Query("update Member m set m.age = m.age + 1 where m.age >= :age")
    int bulkAgePlus(@Param("age") int age);

    @Query("select m from Member m left join fetch m.team t")
    List<Member> findMemberFetchJoin();

    //** @EntityGraph - 내부적으로 fetch join 을 이용하여 가져옴
    // 간단하면 @EntityGraph
    // 복잡하면 jpql fetch join
    @Override
    @EntityGraph(attributePaths = "team")
    List<Member> findAll();

    @EntityGraph(attributePaths = "team")
    List<Member> findByUsername(@Param("username") String username);

    //** 쿼리 힌트 이용하여 readOnly 설정
    @QueryHints(value = @QueryHint(name = "org.hibernate.readOnly", value = "true"))
    Member findReadOnlyByUsername(String username);

    //** select lock
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<Member> findLockByUsername(String username);
}
