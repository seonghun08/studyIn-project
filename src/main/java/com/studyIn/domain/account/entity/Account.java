package com.studyIn.domain.account.entity;

import com.studyIn.domain.BaseTimeEntity;
import com.studyIn.domain.account.dto.form.SignUpForm;
import com.studyIn.domain.board.Board;
import com.studyIn.domain.comment.Comment;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

import static javax.persistence.CascadeType.ALL;

@Entity
@Table(name = "account")
@Getter @NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Account extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "account_id")
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "profile_id")
    private Profile profile;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "authentication_id")
    private Authentication authentication;

    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL)
    private List<AccountTag> accountTags = new ArrayList<>();

    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL)
    private List<AccountLocation> accountLocations = new ArrayList<>();

    @OneToMany(mappedBy = "writer", cascade = ALL, orphanRemoval = true)
    private List<Board> boards = new ArrayList<>();

    @OneToMany(mappedBy = "writer", cascade = ALL, orphanRemoval = true)
    private List<Comment> comments = new ArrayList<>();


    //== 연관관계 메서드 ==//
    public void setAuthentication(Authentication authentication) {
        this.authentication = authentication;
        authentication.setAccount(this);
    }

    public void setProfile(Profile profile) {
        this.profile = profile;
    }

    public void addBoard(Board board) {
        this.boards.add(board);
    }

    public void addComment(Comment comment) {
        this.comments.add(comment);
    }


    //== 생성 메서드 ==//
    public static Account createUser(SignUpForm signUpForm, Profile profile, Authentication authentication) {
        Account account = new Account();
        account.username = signUpForm.getUsername();
        account.password = signUpForm.getPassword();
        account.setProfile(profile);
        account.setAuthentication(authentication);
        return account;
    }


    //== 수정 메서드 ==//
    public void updateUsername(String newUsername) {
        this.username = newUsername;
    }

    public void updatePassword(PasswordEncoder passwordEncoder, String newPassword) {
        this.password = newPassword;
        this.encodePassword(passwordEncoder);
    }

    public void addAccountTag(AccountTag accountTag) {
        this.accountTags.add(accountTag);
        accountTag.setAccount(this);
    }
    public void removeAccountTag(AccountTag accountTag) {
        this.accountTags.remove(accountTag);
    }

    public void addAccountLocation(AccountLocation accountLocation) {
        this.accountLocations.add(accountLocation);
        accountLocation.setAccount(this);
    }

    /**
     * PasswordEncoder 패스워드 암호화
     */
    public void encodePassword(PasswordEncoder passwordEncoder){
        this.password = passwordEncoder.encode(password);
    }
}
