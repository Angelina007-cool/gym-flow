package com.lomakova.gymflow.service;

import com.lomakova.gymflow.entity.Group;
import com.lomakova.gymflow.entity.Member;
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
    public void conductLesson(Long groupId, boolean allPresent) {
        List<Member> members = memberRepository.findAllByGroup_Id(groupId);

        for (Member member : members) {
            if (member.getVisitsLeft() <= 0 && (allPresent || !member.isExcursedAbsence())) {
                System.out.println("Ошибка: у " + member.getName() + " пустой абонемент");
                continue;
            }
            member.decrementVisit(allPresent);
        }
        memberRepository.saveAll(members);
    }

    // Пополнение абонемента
    public String renewSubscription(Long memberId, int newType) {
        Member member = memberRepository.findById(memberId)
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
    public Group createGroup(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new RuntimeException("Имя группы пустое");
        }
        return groupRepository.save(Group.builder()
                .name(name)
                .build());
    }
}
