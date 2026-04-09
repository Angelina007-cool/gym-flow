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

        conductButton.addActionListener(e -> {
            Group selectedGroup = (Group) groupCombo.getSelectedItem();
            if (selectedGroup == null) {
                outputArea.setText("Ошибка: Группа не выбрана!");
                return;
            }

            try {
                boolean allPresent = allPresentBox.isSelected();

                // Вызываем сервис
                gymService.conductLesson(selectedGroup.getId(), allPresent);

                // Обновляем UI
                refreshMembers(selectedGroup.getId());
                outputArea.setText("Занятие успешно проведено для группы: " + selectedGroup.getName());

                // Сбрасываем чек-бокс
                allPresentBox.setSelected(false);

            } catch (RuntimeException ex) {
                // Вывод ошибки в текстовое поле (согласно 5.1 и 5.2)
                outputArea.setText("ОШИБКА: " + ex.getMessage());
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Внимание", JOptionPane.WARNING_MESSAGE);
            }
        });

        absentButton.addActionListener(e -> {
            Member selectedMember = (Member) memberCombo.getSelectedItem();
            if (selectedMember == null) {
                outputArea.setText("Ошибка: Сначала выберите участника!");
                return;
            }

            // В версии 5.3 тут будет диалоговое окно, а пока используем чекбокс на главном экране
            // Допустим, мы добавили чекбокс 'excusedCheckBox' для пометки уважительной причины
            boolean isExcused = true; // Для 5.2/5.3 по умолчанию считаем, что нажатие кнопки - это отсутствие

            selectedMember.setExcusedAbsence(isExcused);

            outputArea.setText("Участник " + selectedMember.getName() + " отмечен как отсутствующий.\n" +
                    (isExcused ? "Причина: Уважительная" : "Причина: Неуважительная"));
        });

        renewButton.addActionListener(e -> {
            Member selectedMember = (Member) memberCombo.getSelectedItem();
            if (selectedMember == null) return;

            // Вызываем логику из сервиса, которую мы написали ранее
            try {
                // Пополняем до 8 (или 16 в версии 5.2)
                String result = gymService.renewSubscription(selectedMember.getId(), 8);

                // Обновляем данные в ComboBox, чтобы увидеть изменения
                refreshMembers(((Group) groupCombo.getSelectedItem()).getId());

                outputArea.setText(result);
            } catch (Exception ex) {
                outputArea.setText("ОШИБКА: " + ex.getMessage());
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Ошибка пополнения", JOptionPane.ERROR_MESSAGE);
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

    private void showAddGroupDialog() {
        String name = JOptionPane.showInputDialog(this, "Введите название новой группы:");
        if (name != null && !name.isEmpty()) {
            try {
                gymService.addGroup(name);
                refreshGroups();
                outputArea.setText("Группа " + name + " добавлена.");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
