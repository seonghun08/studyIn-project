package com.studyIn.domain.notification;

import com.studyIn.domain.account.AccountInfo;
import com.studyIn.domain.account.CurrentAccount;
import com.studyIn.domain.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final NotificationRepository notificationRepository;

    @GetMapping("/notifications")
    public String viewNotifications(@CurrentAccount AccountInfo AccountInfo, Model model) {
        List<Notification> unreadNotifications = notificationRepository.findNotificationsByChecked(AccountInfo.getAccountId(), false);
        int readNotificationsCount = notificationRepository.countByAccountAndChecked(AccountInfo.getAccountId(), true);

        model.addAttribute("AccountInfo", AccountInfo);
        model.addAttribute("isNew", true);
        putCategorizedNotificationList(unreadNotifications, readNotificationsCount, unreadNotifications.size(), model);
        notificationService.readChecked(unreadNotifications);
        return "notification/list";
    }

    @GetMapping("/notifications/old")
    public String viewOldNotifications(@CurrentAccount AccountInfo AccountInfo, Model model) {
        List<Notification> readNotifications = notificationRepository.findNotificationsByChecked(AccountInfo.getAccountId(), true);
        int unreadNotificationsCount = notificationRepository.countByAccountAndChecked(AccountInfo.getAccountId(), false);

        model.addAttribute("AccountInfo", AccountInfo);
        model.addAttribute("isNew", false);
        putCategorizedNotificationList(readNotifications, unreadNotificationsCount, readNotifications.size(), model);
        return "notification/list";
    }

    @DeleteMapping("/notifications")
    public String deleteNotifications(@CurrentAccount AccountInfo AccountInfo) {
        notificationRepository.deleteByAccountAndChecked(AccountInfo.getAccountId(), true);
        return "redirect:/notifications";
    }

    private void putCategorizedNotificationList(List<Notification> notificationList, int numberOfChecked, int numberOfNotChecked, Model model) {
        List<Notification> STUDY_CREATED = new ArrayList<>();
        List<Notification> STUDY_UPDATED = new ArrayList<>();
        List<Notification> EVENT_ENROLLMENT = new ArrayList<>();

        for (var n : notificationList) {
            switch (n.getNotificationType()) {
                case STUDY_CREATED:
                    STUDY_CREATED.add(n);
                    break;
                case STUDY_UPDATED:
                    STUDY_UPDATED.add(n);
                    break;
                case EVENT_ENROLLMENT:
                    EVENT_ENROLLMENT.add(n);
                    break;
            }
        }

        model.addAttribute("notificationList", notificationList);
        model.addAttribute("numberOfNotChecked", numberOfNotChecked);
        model.addAttribute("numberOfChecked", numberOfChecked);
        model.addAttribute("STUDY_CREATED", STUDY_CREATED);
        model.addAttribute("STUDY_UPDATED", STUDY_UPDATED);
        model.addAttribute("EVENT_ENROLLMENT", EVENT_ENROLLMENT);
    }
}
