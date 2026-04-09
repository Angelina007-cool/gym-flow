package com.lomakova.gymflow.config;

import com.lomakova.gymflow.entity.Group;
import com.lomakova.gymflow.entity.Member;
import com.lomakova.gymflow.repository.GroupRepository;
import com.lomakova.gymflow.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final GroupRepository groupRepository;
    private final MemberRepository memberRepository;

    @Override
    public void run(String... args) throws Exception {

        // Проверяем, если база пуста, наполняем её
        if (groupRepository.count() == 0) {
            System.out.println(">>> Наполнение базы тестовыми данными...");

            // 1. Создаем две группы (согласно условию 5.2)
            Group groupA = Group.builder()
                    .name("Йога Утро")
                    .build();
            Group groupB = Group.builder()
                    .name("Кроссфит Вечер")
                    .build();

            groupRepository.saveAll(List.of(groupA, groupB));

            // 2. Добавляем по 6 человек в каждую группу (согласно условию 5.1/5.2)
            // У всех новые абонементы по 8 занятий
            String[] namesA = {"Иван", "Мария", "Алексей", "Елена", "Дмитрий", "Ольга"};
            for (String name : namesA) {
                memberRepository.save(Member.builder()
                        .name(name)
                        .group(groupA)
                        .visitsLeft(8)
                        .maxVisits(8)
                        .build());
            }

            String[] namesB = {"Петр", "Анна", "Сергей", "Наталья", "Артем", "Светлана"};
            for (String name : namesB) {
                memberRepository.save(Member.builder()
                        .name(name)
                        .group(groupB)
                        .visitsLeft(8)
                        .maxVisits(8)
                        .build());
            }

            System.out.println(">>> Данные успешно загружены: 2 группы, 12 участников.");
        }
    }
}
