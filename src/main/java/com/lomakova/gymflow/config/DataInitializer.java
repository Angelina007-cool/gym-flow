package com.lomakova.gymflow.config;

import com.lomakova.gymflow.entity.GroupEntity;
import com.lomakova.gymflow.entity.MemberEntity;
import com.lomakova.gymflow.entity.UserEntity;
import com.lomakova.gymflow.enums.Role;
import com.lomakova.gymflow.repository.GroupRepository;
import com.lomakova.gymflow.repository.MemberRepository;
import com.lomakova.gymflow.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final GroupRepository groupRepository;
    private final MemberRepository memberRepository;
    private final UserRepository userRepository;

    @Override
    public void run(String... args) throws Exception {

        // Проверяем, если база пуста, наполняем её
        if (groupRepository.count() == 0) {
            System.out.println(">>> Наполнение базы тестовыми данными...");

            // 1. Создаем две группы (согласно условию 5.2)
            GroupEntity groupA = GroupEntity.builder()
                    .name("Йога Утро")
                    .build();
            GroupEntity groupB = GroupEntity.builder()
                    .name("Кроссфит Вечер")
                    .build();

            groupRepository.saveAll(List.of(groupA, groupB));

            // 2. Добавляем по 6 человек в каждую группу (согласно условию 5.1/5.2)
            // У всех новые абонементы по 8 занятий
            String[] namesA = {"Иван", "Мария", "Алексей", "Елена", "Дмитрий", "Ольга"};
            for (String name : namesA) {
                memberRepository.save(MemberEntity.builder()
                        .name(name)
                        .group(groupA)
                        .visitsLeft(8)
                        .maxVisits(8)
                        .build());
            }

            String[] namesB = {"Петр", "Анна", "Сергей", "Наталья", "Артем", "Светлана"};
            for (String name : namesB) {
                memberRepository.save(MemberEntity.builder()
                        .name(name)
                        .group(groupB)
                        .visitsLeft(8)
                        .maxVisits(8)
                        .build());
            }

            System.out.println(">>> Данные успешно загружены: 2 группы, 12 участников.");

            if (userRepository.count() == 0) {
                System.out.println(">>> Создание учетных записей...");

                // Создаем администратора
                userRepository.save(UserEntity.builder()
                        .username("admin")
                        .password("1234") // В учебном проекте храним просто строкой
                        .role(Role.ADMIN)
                        .build());

                // Создаем обычного пользователя (тренера)
                userRepository.save(UserEntity.builder()
                        .username("user")
                        .password("1111")
                        .role(Role.USER)
                        .build());

                System.out.println(">>> Аккаунты созданы: admin/1234 и user/1111");
            }
        }
    }
}
