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
        setLayout(new FlowLayout(FlowLayout.CENTER, 10, 10));
        setSize(300, 130);
        setLocationRelativeTo(getOwner());

        JTextField nameField = new JTextField(15);
        JButton saveBtn = new JButton("Сохранить");

        add(new JLabel("Название группы:"));
        add(nameField);
        add(addSeparator());
        add(saveBtn);

        saveBtn.addActionListener(e -> {
            String name = nameField.getText().trim();

            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Пожалуйста, введите название группы",
                        "Пустое поле",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            try {
                gymService.addGroup(name);

                onSuccess.run();
                dispose();

            } catch (RuntimeException ex) {
                JOptionPane.showMessageDialog(
                        this,
                        ex.getMessage(),
                        "Ошибка при создании",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        });
    }

    private Component addSeparator() {
        return Box.createHorizontalStrut(100);
    }
}