package com.lomakova.gymflow.ui;

import com.lomakova.gymflow.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.awt.*;

@Component
@RequiredArgsConstructor
public class LoginFrame extends JFrame {

    private final UserRepository userRepository;
    private final MainFrame mainFrame;
    private final RegisterFrame registerFrame;

    public void init() {
        setTitle("GymFlow Login");
        setSize(350, 200);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new GridLayout(3, 2, 10, 10));
        setLocationRelativeTo(null);

        JTextField userField = new JTextField();
        JPasswordField passField = new JPasswordField();
        JButton loginBtn = new JButton("Войти");
        JButton goToRegisterBtn = new JButton("Регистрация");

        add(new JLabel("  Логин:"));
        add(userField);
        add(new JLabel("  Пароль:"));
        add(passField);
        add(goToRegisterBtn);
        add(loginBtn);

        loginBtn.addActionListener(e -> {
            String username = userField.getText();
            String password = new String(passField.getPassword());

            userRepository.findByUsername(username).ifPresentOrElse(user -> {
                if (user.getPassword().equals(password)) {
                    mainFrame.applySecurity(user);
                    mainFrame.setVisible(true);
                    this.dispose(); // Закрываем окно логина
                } else {
                    JOptionPane.showMessageDialog(this, "Неверный пароль!", "Ошибка", JOptionPane.ERROR_MESSAGE);
                }
            }, () -> {
                JOptionPane.showMessageDialog(this, "Пользователь не найден!", "Ошибка", JOptionPane.ERROR_MESSAGE);
            });
        });

        goToRegisterBtn.addActionListener(e -> registerFrame.init());

        setVisible(true);
    }
}
