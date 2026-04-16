package com.lomakova.gymflow.ui;

import com.lomakova.gymflow.repository.AttendanceLogRepository;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class StatisticsDialog extends JDialog {
    public StatisticsDialog(JFrame parent, AttendanceLogRepository repository) {
        super(parent, "История посещений и операций", true);
        setSize(700, 450);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());

        String[] columns = {"Дата и время", "Атлет", "Группа", "Действие", "Остаток"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Запрещаем редактирование ячеек истории
            }
        };

        repository.findAllByOrderByDateDesc().forEach(log -> {
            model.addRow(new Object[]{
                    log.getDate().toString().replace("T", " ").substring(0, 16),
                    log.getUsername(),
                    log.getGroupName(),
                    log.getStatus(),
                    log.getVisitsAfter()
            });
        });

        JTable table = new JTable(model);
        table.getColumnModel().getColumn(0).setPreferredWidth(120);
        table.getColumnModel().getColumn(3).setPreferredWidth(150);

        add(new JScrollPane(table), BorderLayout.CENTER);

        JButton closeBtn = new JButton("Закрыть");
        closeBtn.addActionListener(e -> dispose());
        JPanel southPanel = new JPanel();
        southPanel.add(closeBtn);
        add(southPanel, BorderLayout.SOUTH);
    }
}