package com.lomakova.gymflow.ui;

import com.lomakova.gymflow.entity.UserEntity;
import com.lomakova.gymflow.enums.Role;
import com.lomakova.gymflow.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.awt.*;

@Component
@RequiredArgsConstructor
public class RegisterFrame extends JFrame {

    private final UserRepository userRepository;

    public void init() {
        setTitle("Регистрация нового пользователя");
        setSize(350, 250);
        setLayout(new GridLayout(4, 2, 10, 10));
        setLocationRelativeTo(null);

        JTextField userField = new JTextField();
        JPasswordField passField = new JPasswordField();
        JComboBox<Role> roleCombo = new JComboBox<>(Role.values());
        JButton registerBtn = new JButton("Зарегистрироваться");

        add(new JLabel("  Новый логин:")); add(userField);
        add(new JLabel("  Новый пароль:")); add(passField);
        add(new JLabel("  Роль:")); add(roleCombo);
        add(new JLabel("")); add(registerBtn);

        registerBtn.addActionListener(e -> {
            String username = userField.getText();
            String password = new String(passField.getPassword());
            Role role = (Role) roleCombo.getSelectedItem();

            if (username.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Заполните все поля!");
                return;
            }

            if (userRepository.existsByUsername(username)) {
                JOptionPane.showMessageDialog(this, "Пользователь с таким именем уже существует!");
            } else {
                userRepository.save(UserEntity.builder()
                        .username(username)
                        .password(password)
                        .role(role)
                        .build());
                JOptionPane.showMessageDialog(this, "Регистрация успешна!");
                this.dispose();
            }
        });
        setVisible(true);
    }
}
