package com.lomakova.gymflow.ui;

import com.lomakova.gymflow.entity.GroupEntity;
import com.lomakova.gymflow.entity.UserEntity;
import com.lomakova.gymflow.enums.Role;
import com.lomakova.gymflow.repository.GroupRepository;
import com.lomakova.gymflow.repository.UserRepository;

import javax.swing.*;
import java.awt.*;

public class AssignMemberDialog extends JDialog {
    private final UserRepository userRepository;
    private final GroupRepository groupRepository;
    private final Runnable onSuccess;

    public AssignMemberDialog(JFrame parent, UserRepository userRepository, GroupRepository groupRepository, Runnable onSuccess) {
        super(parent, "Назначить группу", true);
        this.userRepository = userRepository;
        this.groupRepository = groupRepository;
        this.onSuccess = onSuccess;
        init();
    }

    private void init() {
        setLayout(new GridLayout(4, 2, 10, 10));
        setSize(400, 250);
        setLocationRelativeTo(getOwner());

        JComboBox<UserEntity> userSelector = new JComboBox<>();
        userRepository.findAllByRole(Role.USER).forEach(userSelector::addItem);

        JComboBox<GroupEntity> dialogGroupCombo = new JComboBox<>();
        groupRepository.findAll().forEach(dialogGroupCombo::addItem);

        JButton confirmBtn = new JButton("Подтвердить");
        JButton cancelBtn = new JButton("Отменить");

        add(new JLabel("  Выберите пользователя:"));
        add(userSelector);
        add(new JLabel("  Назначить группу:"));
        add(dialogGroupCombo);
        add(new JLabel(""));
        add(confirmBtn);
        add(new JLabel(""));
        add(cancelBtn);

        confirmBtn.addActionListener(e -> {
            UserEntity selectedUser = (UserEntity) userSelector.getSelectedItem();
            GroupEntity selectedGroup = (GroupEntity) dialogGroupCombo.getSelectedItem();

            if (selectedUser != null && selectedGroup != null) {
                selectedUser.setGroup(selectedGroup);
                userRepository.save(selectedUser);
                onSuccess.run();
                dispose();
            }
        });

        cancelBtn.addActionListener(e -> dispose());
    }
}
