package com.lomakova.gymflow.service;

import com.lomakova.gymflow.entity.GroupEntity;
import com.lomakova.gymflow.entity.MemberEntity;
import com.lomakova.gymflow.repository.GroupRepository;
import com.lomakova.gymflow.repository.MemberRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GymService {

    private final GroupRepository groupRepository;
    private final MemberRepository memberRepository;

    // Проведение занятия для группы
    @Transactional
    public void conductLesson(List<MemberEntity> members, boolean allPresent) {
        if (members == null || members.isEmpty()) {
            throw new RuntimeException("В группе нет участников!");
        }

        for (MemberEntity member : members) {
            // Вызываем логику списания (ту самую с проверкой visitsLeft > 0)
            if (allPresent || !member.isExcusedAbsence()) {
                if (member.getVisitsLeft() <= 0) {
                    throw new RuntimeException("У пользователя " + member.getName() + " закончился абонемент!");
                }
            }
            member.decrementVisit(allPresent);
        }
        // Сохраняем измененные объекты обратно в базу
        memberRepository.saveAll(members);
    }

    // Пополнение абонемента
    public String renewSubscription(Long memberId, int newType) {
        MemberEntity member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("Посетитель не найден"));

        if (member.getVisitsLeft() >= 3) {
            return "Ошибка: Пополнение невозможно, осталось более 2 занятий!";
        }

        member.setVisitsLeft(newType);
        member.setMaxVisits(newType);
        memberRepository.save(member);
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
    public MemberEntity addMember(String name, Long groupId, int visits) {
        GroupEntity group = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Группа не найдена"));

        if (group.isFull()) {
            throw new RuntimeException("Группа переполнена! Максимум 8 человек.");
        }

        MemberEntity member = MemberEntity.builder()
                .name(name)
                .group(group)
                .visitsLeft(visits)
                .maxVisits(visits)
                .build();

        return memberRepository.save(member);
    }
}
