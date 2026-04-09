package com.lomakova.gymflow.ui;

import com.lomakova.gymflow.entity.Group;
import com.lomakova.gymflow.entity.Member;
import com.lomakova.gymflow.repository.GroupRepository;
import com.lomakova.gymflow.repository.MemberRepository;
import com.lomakova.gymflow.service.GymService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.awt.*;

@Component
@RequiredArgsConstructor
public class MainFrame extends JFrame {

    private final GroupRepository groupRepository;
    private final MemberRepository memberRepository;
    private final GymService gymService;

    private JComboBox<Group> groupCombo;
    private JComboBox<Member> memberCombo;
    private JCheckBox allPresentBox;
    private JTextArea outputArea;
    private JButton conductButton;
    private JButton absentButton;
    private JButton renewButton;

    @PostConstruct
    public void init() {
        setTitle("GymFlow - Система контроля абонементов");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        // Панель управления (Верхняя часть)
        JPanel topPanel = new JPanel(new GridLayout(3, 2, 5, 5));

        groupCombo = new JComboBox<>();
        memberCombo = new JComboBox<>();
        allPresentBox = new JCheckBox("Все пришли");

        topPanel.add(new JLabel("Выберите группу:"));
        topPanel.add(groupCombo);
        topPanel.add(new JLabel("Выберите участника:"));
        topPanel.add(memberCombo);
        topPanel.add(allPresentBox);

        // Поле вывода (Центр)
        outputArea = new JTextArea();
        outputArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(outputArea);

        // Панель кнопок (Низ)
        JPanel buttonPanel = new JPanel();
        conductButton = new JButton("Провести занятие");
        absentButton = new JButton("Отсутствует");
        renewButton = new JButton("Пополнить абонемент");

        buttonPanel.add(conductButton);
        buttonPanel.add(absentButton);
        buttonPanel.add(renewButton);

        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        setupLogic();
        refreshGroups();
    }

    private void setupLogic() {
        // Логика переключения участников при выборе группы (Требование 5.2)
        groupCombo.addActionListener(e -> {
            Group selected = (Group) groupCombo.getSelectedItem();
            if (selected != null) {
                refreshMembers(selected.getId());
            }
        });

        // Логика отображения инфо об абонементе (Требование 5.1)
        memberCombo.addActionListener(e -> {
            Member selected = (Member) memberCombo.getSelectedItem();
            if (selected != null) {
                outputArea.setText("Участник: " + selected.getName() +
                        "\nОсталось занятий: " + selected.getVisitsLeft());
            }
        });
    }

    private void refreshGroups() {
        groupCombo.removeAllItems();
        groupRepository.findAll().forEach(groupCombo::addItem);
    }

    private void refreshMembers(Long groupId) {
        memberCombo.removeAllItems();
        memberRepository.findAllByGroup_Id(groupId).forEach(memberCombo::addItem);
    }
}
