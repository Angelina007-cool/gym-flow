package com.lomakova.gymflow.service;

import com.lomakova.gymflow.entity.UserEntity;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.util.List;

@Service
public class ExportService {
    public void exportToTextFile(File file, String groupName, List<UserEntity> members) throws IOException {
        try (PrintWriter writer = new PrintWriter(file)) {
            writer.println("ОТЧЕТ ПО ГРУППЕ: " + groupName.toUpperCase());
            writer.println("Дата генерации: " + LocalDate.now());
            writer.println("==========================================");
            writer.printf("%-20s | %-15s | %-10s%n", "Имя спортсмена", "Остаток", "Всего");
            writer.println("------------------------------------------");

            for (UserEntity m : members) {
                writer.printf("%-20s | %-15d | %-10d%n",
                        m.getUsername(),
                        m.getVisitsLeft(),
                        m.getMaxVisits());
            }

            writer.println("==========================================");
            writer.println("Сгенерировано в системе GymFlow");
        }
    }
}
