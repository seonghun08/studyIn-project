package com.studyIn.domain.account.service;

import com.studyIn.domain.account.NotificationSettings;
import com.studyIn.domain.account.dto.form.SignUpForm;
import com.studyIn.domain.account.entity.Authentication;
import com.studyIn.domain.account.entity.Profile;
import com.studyIn.domain.account.entity.Account;
import com.studyIn.domain.account.repository.AccountRepository;
import com.studyIn.domain.account.repository.AuthenticationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
@Transactional
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final AuthenticationRepository authenticationRepository;
    private final PasswordEncoder passwordEncoder;
    private final JavaMailSender javaMailSender;
    private final LoginService loginService;

    /**
     * 회원가입
     */
    public Account signUp(SignUpForm form) {
        Account account = createAccount(form);
        sendSignUpConfirmEmail(account);
        return account;
    }

    /**
     * 계정 생성
     */
    private Account createAccount(SignUpForm form) {
        NotificationSettings notificationSettings = new NotificationSettings();
        Authentication authentication = Authentication.createAuthentication(form, notificationSettings);
        Profile profile = Profile.createProfile(form);
        Account account = Account.createUser(form, profile, authentication);
        account.encodePassword(passwordEncoder);
        Account saveAccount = accountRepository.save(account);
        return saveAccount;
    }

    /**
     * 인증 메일 보내기
     */
    private void sendSignUpConfirmEmail(Account account) {
        Authentication authentication = account.getAuthentication();
        authentication.generateEmailToken();

        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setTo(account.getAuthentication().getEmail());
        mailMessage.setSubject("회원가입 인증");
        mailMessage.setText("/check-email-token?"
                + "&email=" + authentication.getEmail()
                + "&token=" + authentication.getEmailCheckToken());
        javaMailSender.send(mailMessage);
    }

    public void resendSignUpConfirmEmail(Long accountId) {
        Account account = accountRepository.findWithAuthenticationById(accountId).orElseThrow();
        sendSignUpConfirmEmail(account);
    }

    /**
     * email 존재 여부 확인 후, 토근이 일치하면 인증 정보 업데이트
     */
    public boolean completeSignUp(String email, String token) {
        Optional<Account> account = accountRepository.findByEmail(email);
        AtomicBoolean check = new AtomicBoolean(false);
        account.ifPresent(a -> {
            if (a.getAuthentication().isValidToken(token)) {
                a.getAuthentication().confirmEmailAuthentication();
                loginService.login(a);
                check.set(true);
            }
        });
        return check.get();
    }
}
