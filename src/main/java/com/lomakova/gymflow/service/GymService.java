package com.lomakova.gymflow.service;

import com.lomakova.gymflow.entity.AttendanceLog;
import com.lomakova.gymflow.entity.GroupEntity;
import com.lomakova.gymflow.entity.UserEntity;
import com.lomakova.gymflow.repository.AttendanceLogRepository;
import com.lomakova.gymflow.repository.GroupRepository;
import com.lomakova.gymflow.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GymService {

    private final GroupRepository groupRepository;
    private final AttendanceLogRepository logRepository;
    private final UserRepository userRepository;

    @Transactional
    public void conductLesson(List<UserEntity> members, boolean allPresent) {
        for (UserEntity member : members) {

            if (member.getVisitsLeft() <= 0) {
                continue;
            }

            boolean shouldDecrement = allPresent || !member.isExcusedAbsence();

            if (shouldDecrement) {
                member.setVisitsLeft(Math.max(0, member.getVisitsLeft() - 1));
            }

            String status;
            if (allPresent) {
                status = "ПОСЕЩЕНИЕ";
            } else if (member.isExcusedAbsence()) {
                status = "БОЛЕЗНЬ";
            } else {
                status = "ПРОГУЛ";
            }

            AttendanceLog log = AttendanceLog.builder()
                    .username(member.getUsername())
                    .groupName(member.getGroup().getName())
                    .date(LocalDateTime.now())
                    .visitsAfter(member.getVisitsLeft())
                    .status(status)
                    .build();

            logRepository.save(log);

            member.setExcusedAbsence(false);
            userRepository.save(member);
        }
    }

    public String renewSubscription(Long memberId, int newType) {
        UserEntity user = userRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("Посетитель не найден"));

        if (user.getVisitsLeft() >= 3) {
            return "Ошибка: У вас еще достаточно занятий (" + user.getVisitsLeft() + ")";
        }

        user.setVisitsLeft(newType);
        user.setMaxVisits(newType);

        user.setExcusedAbsence(false);

        userRepository.save(user);

        return "Абонемент успешно обновлен до " + newType + " занятий.";
    }

    public String addGroup(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new RuntimeException("Имя группы не может быть пустым!");
        }

        String trimmedName = name.trim();

        if (groupRepository.existsByName(trimmedName)) {
            throw new RuntimeException("Группа с названием '" + trimmedName + "' уже существует!");
        }

        GroupEntity newGroup = GroupEntity.builder()
                .name(trimmedName)
                .build();

        groupRepository.save(newGroup);

        return "Группа '" + trimmedName + "' успешно создана";
    }
}
