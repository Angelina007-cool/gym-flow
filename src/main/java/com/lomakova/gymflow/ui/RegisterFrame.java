package com.lomakova.gymflow.ui;

import com.lomakova.gymflow.entity.UserEntity;
import com.lomakova.gymflow.enums.Role;
import com.lomakova.gymflow.repository.GroupRepository;
import com.lomakova.gymflow.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.awt.*;

@Component
@RequiredArgsConstructor
public class RegisterFrame extends JFrame {

    private final UserRepository userRepository;
    private final GroupRepository groupRepository;

    public void init() {
        setTitle("GymFlow - Регистрация");
        setSize(400, 320);
        setLayout(new GridLayout(5, 2, 10, 10));
        setLocationRelativeTo(null);

        JTextField userField = new JTextField();
        JPasswordField passField = new JPasswordField();
        JComboBox<Role> roleCombo = new JComboBox<>(Role.values());

        JLabel subscriptionLabel = new JLabel("  Абонемент:");

        JRadioButton rb8 = new JRadioButton("8 зан.", true);
        JRadioButton rb16 = new JRadioButton("16 зан.");
        ButtonGroup bg = new ButtonGroup();
        bg.add(rb8); bg.add(rb16);

        JPanel radioPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        radioPanel.add(rb8);
        radioPanel.add(rb16);

        JButton registerBtn = new JButton("Создать аккаунт");

        roleCombo.addActionListener(e -> {
            boolean isUser = roleCombo.getSelectedItem() == Role.USER;
            subscriptionLabel.setVisible(isUser);
            radioPanel.setVisible(isUser);
        });

        add(new JLabel("  Логин:"));
        add(userField);
        add(new JLabel("  Пароль:"));
        add(passField);
        add(new JLabel("  Кто вы:"));
        add(roleCombo);
        add(subscriptionLabel);
        add(radioPanel);
        add(new JLabel(""));
        add(registerBtn);

        registerBtn.addActionListener(e -> {
            String username = userField.getText().trim();
            String password = new String(passField.getPassword()).trim();
            Role role = (Role) roleCombo.getSelectedItem();
            int selectedMax = rb8.isSelected() ? 8 : 16;

            if (username.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Заполните все поля!");
                return;
            }

            if (userRepository.existsByUsername(username)) {
                JOptionPane.showMessageDialog(this, "Логин занят!");
                return;
            }

            UserEntity.UserEntityBuilder userBuilder = UserEntity.builder()
                    .username(username)
                    .password(password)
                    .role(role);

            if (role == Role.USER) {
                userBuilder.maxVisits(selectedMax);
                userBuilder.visitsLeft(selectedMax);
            }

            userRepository.save(userBuilder.build());

            JOptionPane.showMessageDialog(this, "Регистрация успешна!");
            this.dispose();
        });

        setVisible(true);
    }
}
