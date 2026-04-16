package com.lomakova.gymflow.ui;

import com.lomakova.gymflow.entity.GroupEntity;
import com.lomakova.gymflow.entity.UserEntity;
import com.lomakova.gymflow.enums.Role;
import com.lomakova.gymflow.repository.AttendanceLogRepository;
import com.lomakova.gymflow.repository.GroupRepository;
import com.lomakova.gymflow.repository.UserRepository;
import com.lomakova.gymflow.service.ExportService;
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
    private final UserRepository userRepository;
    private final GymService gymService;
    private final ExportService exportService;
    private final AttendanceLogRepository attendanceLogRepository;

    private UserEntity currentUser;

    private JComboBox<GroupEntity> groupCombo;
    private JComboBox<UserEntity> memberCombo;
    private JCheckBox allPresentBox;
    private JTextArea outputArea;
    private JButton conductButton;
    private JButton absentButton;
    private JButton renewButton;
    private JButton addGroupBtn;
    private JButton addMemberBtn;
    private JButton exportBtn;
    private JButton statBtn;

    private JPanel topPanel;

    @PostConstruct
    public void init() {
        setTitle("GymFlow - Система контроля абонементов");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        topPanel = new JPanel(new GridLayout(3, 2, 5, 5));

        groupCombo = new JComboBox<>();
        memberCombo = new JComboBox<>();
        allPresentBox = new JCheckBox("Все пришли");

        topPanel.add(new JLabel("Выберите группу:"));
        topPanel.add(groupCombo);
        topPanel.add(new JLabel("Выберите участника:"));
        topPanel.add(memberCombo);
        topPanel.add(allPresentBox);

        outputArea = new JTextArea();
        outputArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(outputArea);

        JPanel buttonPanel = new JPanel();
        conductButton = new JButton("Провести занятие");
        absentButton = new JButton("Отсутствует");
        renewButton = new JButton("Пополнить абонемент");

        addGroupBtn = new JButton("Добавить группу");
        addMemberBtn = new JButton("Добавить посетителя");

        exportBtn = new JButton("Экспорт отчета");
        statBtn = new JButton("Cтатистика");

        buttonPanel.add(conductButton);
        buttonPanel.add(absentButton);
        buttonPanel.add(renewButton);

        buttonPanel.add(addGroupBtn);
        buttonPanel.add(addMemberBtn);

        buttonPanel.add(exportBtn);
        buttonPanel.add(statBtn);

        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        setupLogic();
        refreshGroups();
    }

    private void setupLogic() {
        groupCombo.addActionListener(e -> {
            GroupEntity selected = (GroupEntity) groupCombo.getSelectedItem();
            if (selected != null) {
                refreshMembers(selected.getId());
            }
        });

        memberCombo.addActionListener(e -> {
            UserEntity selected = (UserEntity) memberCombo.getSelectedItem();
            if (selected != null) {
                outputArea.setText("Участник: " + selected.getUsername() +
                        "\nОсталось занятий: " + selected.getVisitsLeft());
            }
        });

        conductButton.addActionListener(e -> {
            GroupEntity selectedGroup = (GroupEntity) groupCombo.getSelectedItem();
            if (selectedGroup == null) {
                outputArea.setText("Ошибка: Группа не выбрана!");
                return;
            }

            try {
                boolean allPresent = allPresentBox.isSelected();

                java.util.List<UserEntity> currentMembers = new java.util.ArrayList<>();
                for (int i = 0; i < memberCombo.getItemCount(); i++) {
                    currentMembers.add(memberCombo.getItemAt(i));
                }

                gymService.conductLesson(currentMembers, allPresent);

                refreshMembers(selectedGroup.getId());
                outputArea.setText("Занятие успешно проведено для группы: " + selectedGroup.getName());
                allPresentBox.setSelected(false);

            } catch (RuntimeException ex) {
                outputArea.setText("ОШИБКА: " + ex.getMessage());
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Внимание", JOptionPane.WARNING_MESSAGE);
            }
        });

        absentButton.addActionListener(e -> {
            UserEntity selectedMember = (UserEntity) memberCombo.getSelectedItem();
            if (selectedMember == null) {
                outputArea.setText("Ошибка: Сначала выберите участника!");
                return;
            }

            boolean isExcused = true;

            selectedMember.setExcusedAbsence(isExcused);

            outputArea.setText("Участник " + selectedMember.getUsername() + " отмечен как отсутствующий.\n" +
                    (isExcused ? "Причина: Уважительная" : "Причина: Неуважительная"));
        });

        renewButton.addActionListener(e -> {
            UserEntity target = (currentUser.getRole() == Role.USER)
                    ? currentUser
                    : (UserEntity) memberCombo.getSelectedItem();

            if (target == null) return;

            try {
                int topUpAmount = target.getMaxVisits() > 0 ? target.getMaxVisits() : 8;
                String result = gymService.renewSubscription(target.getId(), topUpAmount);

                //target.setVisitsLeft(target.getVisitsLeft() + topUpAmount);
                UserEntity updated = userRepository.findById(target.getId())
                        .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

                if (currentUser.getRole() == Role.USER) {
                    this.currentUser = updated;
                    applySecurity(this.currentUser);
                } else {
                    refreshMembers(((GroupEntity) groupCombo.getSelectedItem()).getId());
                }

                JOptionPane.showMessageDialog(this, result);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Ошибка: " + ex.getMessage());
            }
        });

        exportBtn.addActionListener(e -> exportGroupReport());
        statBtn.addActionListener(e -> {
            new StatisticsDialog(this, attendanceLogRepository).setVisible(true);
        });
        addGroupBtn.addActionListener(e -> {
            new AddGroupDialog(this, gymService, this::refreshGroups).setVisible(true);
        });
        addMemberBtn.addActionListener(e -> {
            new AssignMemberDialog(this, userRepository, groupRepository, () -> {
                GroupEntity g = (GroupEntity) groupCombo.getSelectedItem();
                if (g != null) refreshMembers(g.getId());
            }).setVisible(true);
        });
    }

    private void refreshGroups() {
        groupCombo.removeAllItems();
        groupRepository.findAll().forEach(groupCombo::addItem);
    }

    private void refreshMembers(Long groupId) {
        memberCombo.removeAllItems();
        userRepository.findAllByRoleAndGroup_Id(Role.USER, groupId)
                .forEach(memberCombo::addItem);
    }

    public void applySecurity(UserEntity user) {
        this.currentUser = user;
        Role role = currentUser.getRole();

        if (role == Role.USER) {
            topPanel.setVisible(false);

            groupCombo.setVisible(false);
            memberCombo.setVisible(false);
            conductButton.setVisible(false);
            absentButton.setVisible(false);
            addGroupBtn.setVisible(false);
            addMemberBtn.setVisible(false);
            exportBtn.setVisible(false);
            statBtn.setVisible(false);

            renewButton.setVisible(true);
            renewButton.setText("Пополнить абонемент");

            String groupName = (currentUser.getGroup() != null)
                    ? currentUser.getGroup().getName()
                    : "Группа не назначена (обратитесь к админу)";

            StringBuilder profileInfo = new StringBuilder();
            profileInfo.append("  ЛИЧНЫЙ КАБИНЕТ АТЛЕТА\n");
            profileInfo.append("  ------------------------------------------\n");
            profileInfo.append("  Имя: ").append(currentUser.getUsername()).append("\n");
            profileInfo.append("  Группа: ").append(groupName).append("\n");
            profileInfo.append("  Доступно занятий: ").append(currentUser.getVisitsLeft()).append("/").append(currentUser.getMaxVisits()).append("\n");
            profileInfo.append("  ------------------------------------------\n");
            profileInfo.append("  Статус: ").append(currentUser.getVisitsLeft() > 0 ? "Активен" : "Нужно пополнить");

            outputArea.setFont(new Font("Monospaced", Font.BOLD, 14));
            outputArea.setText(profileInfo.toString());
            outputArea.setEditable(false);

            setTitle("GymFlow - Личный кабинет: " + currentUser.getUsername());
        } else {
            addGroupBtn.setEnabled(true);
            addMemberBtn.setEnabled(true);
            renewButton.setVisible(false);

            outputArea.setText("Авторизован как: АДМИНИСТРАТОР\n" +
                    "Доступ: Полный контроль системы.");
        }
    }

    private void exportGroupReport() {
        GroupEntity selectedGroup = (GroupEntity) groupCombo.getSelectedItem();
        if (selectedGroup == null) {
            JOptionPane.showMessageDialog(this, "Сначала выберите группу!");
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Сохранить отчет");
        fileChooser.setSelectedFile(new java.io.File(selectedGroup.getName() + "_отчет.txt"));

        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                java.util.List<UserEntity> members = new java.util.ArrayList<>();
                for (int i = 0; i < memberCombo.getItemCount(); i++) {
                    members.add(memberCombo.getItemAt(i));
                }

                exportService.exportToTextFile(
                        fileChooser.getSelectedFile(),
                        selectedGroup.getName(),
                        members
                );

                outputArea.setText("Отчет успешно сохранен в: " + fileChooser.getSelectedFile().getName());

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Ошибка экспорта: " + ex.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
