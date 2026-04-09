package com.lomakova.gymflow.ui;

import com.lomakova.gymflow.service.GymService;

import javax.swing.*;
import java.awt.*;

public class AddGroupDialog extends JDialog {
    private final GymService gymService;
    private final Runnable onSuccess;

    public AddGroupDialog(JFrame parent, GymService gymService, Runnable onSuccess) {
        super(parent, "Добавить группу", true);
        this.gymService = gymService;
        this.onSuccess = onSuccess;
        init();
    }

    private void init() {
        setLayout(new FlowLayout());
        setSize(300, 120);
        setLocationRelativeTo(getOwner());

        JTextField nameField = new JTextField(15);
        JButton saveBtn = new JButton("Сохранить");

        add(new JLabel("Название:"));
        add(nameField);
        add(saveBtn);

        saveBtn.addActionListener(e -> {
            String name = nameField.getText().trim();
            if (!name.isEmpty()) {
                gymService.addGroup(name);
                onSuccess.run();
                dispose();
            }
        });
    }
}