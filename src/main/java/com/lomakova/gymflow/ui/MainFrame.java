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
    private JButton addGroupBtn;
    private JButton addMemberBtn;

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

        addGroupBtn = new JButton("Добавить группу");
        addMemberBtn = new JButton("Добавить посетителя");

        buttonPanel.add(conductButton);
        buttonPanel.add(absentButton);
        buttonPanel.add(renewButton);

        buttonPanel.add(addGroupBtn);
        buttonPanel.add(addMemberBtn);

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

                // 1. Собираем всех участников из текущего комбобокса
                java.util.List<Member> currentMembers = new java.util.ArrayList<>();
                for (int i = 0; i < memberCombo.getItemCount(); i++) {
                    currentMembers.add(memberCombo.getItemAt(i));
                }

                // 2. Передаем список в сервис
                gymService.conductLesson(currentMembers, allPresent);

                // 3. Обновляем UI (теперь данные из базы подтянутся уже измененными)
                refreshMembers(selectedGroup.getId());
                outputArea.setText("Занятие успешно проведено для группы: " + selectedGroup.getName());
                allPresentBox.setSelected(false);

            } catch (RuntimeException ex) {
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

        addGroupBtn.addActionListener(e -> showAddGroupDialog());
        addMemberBtn.addActionListener(e -> showAddMemberDialog());
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

    private void showAddMemberDialog() {
        JDialog dialog = new JDialog(this, "Добавить посетителя", true);
        dialog.setLayout(new GridLayout(5, 2, 10, 10));

        JTextField nameField = new JTextField();
        JComboBox<Group> dialogGroupCombo = new JComboBox<>();
        groupRepository.findAll().forEach(dialogGroupCombo::addItem);

        // Выбор абонемента через RadioButtons
        JRadioButton rb8 = new JRadioButton("8 занятий", true);
        JRadioButton rb16 = new JRadioButton("16 занятий");
        ButtonGroup bg = new ButtonGroup();
        bg.add(rb8);
        bg.add(rb16);
        JPanel radioPanel = new JPanel();
        radioPanel.add(rb8);
        radioPanel.add(rb16);

        JButton confirmBtn = new JButton("Подтвердить");
        JButton cancelBtn = new JButton("Отменить");

        dialog.add(new JLabel(" Имя:"));
        dialog.add(nameField);
        dialog.add(new JLabel(" Группа:"));
        dialog.add(dialogGroupCombo);
        dialog.add(new JLabel(" Абонемент:"));
        dialog.add(radioPanel);
        dialog.add(confirmBtn);
        dialog.add(cancelBtn);

        confirmBtn.addActionListener(e -> {
            try {
                String name = nameField.getText();
                Group selectedGroup = (Group) dialogGroupCombo.getSelectedItem();
                int visits = rb8.isSelected() ? 8 : 16;

                if (selectedGroup == null) throw new RuntimeException("Выберите группу!");

                gymService.addMember(name, selectedGroup.getId(), visits);

                // Обновляем списки на главном экране
                refreshMembers(selectedGroup.getId());
                dialog.dispose();
                outputArea.setText("Посетитель " + name + " успешно добавлен.");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, ex.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        });

        cancelBtn.addActionListener(e -> dialog.dispose());

        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }
}
