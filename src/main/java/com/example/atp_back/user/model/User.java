package com.example.atp_back.user.model;

import com.example.atp_back.portfolio.model.entity.Portfolio;
import com.example.atp_back.user.model.follow.response.FollowResp;
import com.example.atp_back.user.model.follow.UserFollow;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Entity
public class User implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idx;
    @Schema(description="닉네임, 필수(문자열)")
    @Column(nullable = false)
    private String name;
    @Schema(description="이메일, 가입에 필수(문자열)")
    @Column(unique = true, nullable = false)
    private String email;
    @Schema(description="암호화된 비밀번호, 가입에 필수(문자열)")
    @Column(nullable = false)
    private String password;
    @Schema(description="가입일(LocalDate)")
    @Column(nullable = false)
    private LocalDate createdAt;
    @Schema(description="최신 정보 갱신일(LocalDate)")
    @Column(nullable = false)
    private LocalDate updatedAt;
    @Schema(description="프로파일 이미지 값(문자열): 유저가 업로드 or 서버 내 파일 옵션으로 선택")
    private String profileImage;
    @Schema(description="권한(문자열): role은 따로 부여하지 않고 가입시 전체 ROLE_USER로 설정, 관리자는 따로 두지 않는다.")
    @Column(nullable = false)
    private String role;


    @Schema(description="나를 팔로우한 사람 수")
    @Column(nullable = false)
    private Integer followCount;

    @Schema(description="내가 팔로우하는 사람 수")
    @Column(nullable = false)
    private Integer followingCount;

    public void addFollower(){
        followCount++;
    }
    public void addFollowing(){
        followingCount++;
    }
    public void removeFollower(){
        followCount--;
    }
    public void removeFollowing(){
        followingCount--;
    }

    // ------------Version--------------
//    @Version
//    @ColumnDefault(value="0")
//    private Long version;

    // ------------Relation-------------

    @Schema(description="사용자 티어 관계")
    @ManyToOne
    @JoinColumn(name = "usertier_idx")
    private UserTier tierGrade;

    @Schema(description="소유 포트폴리오 외래키")
    @OneToMany(mappedBy = "user")
    private List<Portfolio> portfolios;

    @Schema(description="내가 팔로우하고 있는 사람")
    @OneToMany(mappedBy = "follower")
    private List<UserFollow> followers;

    @Schema(description="나를 팔로우 중인 사람들")
    @OneToMany(mappedBy = "followee")
    private List<UserFollow> followees;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Collection<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority(role));
        return authorities;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    public FollowResp toFollowResp() {
        return FollowResp.builder().name(name).image(profileImage).tier(tierGrade.getGrade()).build();
    }
}
