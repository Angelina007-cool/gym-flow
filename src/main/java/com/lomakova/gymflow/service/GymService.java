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

    // Проведение занятия для группы
    @Transactional
    public void conductLesson(List<UserEntity> members, boolean allPresent) {
        for (UserEntity member : members) {
            // Определяем: нужно ли списывать занятие?
            // Списываем если:
            // 1. Пришли все
            // 2. Или если этот конкретный человек не имеет уважительной причины (прогул)
            boolean shouldDecrement = allPresent || !member.isExcusedAbsence();

            if (shouldDecrement) {
                member.setVisitsLeft(Math.max(0, member.getVisitsLeft() - 1));
            }

            // Формируем статус для лога
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

            // Сбрасываем флаг для следующего занятия
            member.setExcusedAbsence(false);
            userRepository.save(member);
        }
    }

    // Пополнение абонемента
    public String renewSubscription(Long memberId, int newType) {
        UserEntity user = userRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("Посетитель не найден"));

        if (user.getVisitsLeft() >= 3) {
            return "Ошибка: Пополнение невозможно, осталось более 2 занятий!";
        }

        user.setVisitsLeft(newType);
        user.setMaxVisits(newType);
        userRepository.save(user);
        return "Абонемент успешно обновлен до " + newType + " занятий.";
    }

    // Создание новой группы (проверка на уникальность имени)
    public GroupEntity createGroup(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new RuntimeException("Имя группы пустое");
        }
        return groupRepository.save(GroupEntity.builder()
                .name(name)
                .build());
    }

    public GroupEntity addGroup(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new RuntimeException("Имя группы не может быть пустым!");
        }
        return groupRepository.save(GroupEntity.builder().name(name).build());
    }

    @Transactional
    public UserEntity addMember(String name, Long groupId, int visits) {
        GroupEntity group = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Группа не найдена"));

        if (group.isFull()) {
            throw new RuntimeException("Группа переполнена! Максимум 8 человек.");
        }

        UserEntity user = UserEntity.builder()
                .username(name)
                .group(group)
                .visitsLeft(visits)
                .maxVisits(visits)
                .build();

        return userRepository.save(user);
    }
}
