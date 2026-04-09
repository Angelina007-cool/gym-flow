package com.lomakova.gymflow.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "attendance_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttendanceLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "username")
    private String username;    // Имя атлета

    @Column(name = "group_name")
    private String groupName;   // Группа

    @Column(name = "date")
    private LocalDateTime date; // Дата и время занятия

    @Column(name = "visits_after")
    private int visitsAfter;    // Сколько осталось ПОСЛЕ занятия

    @Column(name = "status")
    private String status;
}
