package org.hospital.ui.view;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import org.hospital.feature.internacion.domain.Sector;

/**
 * View panel for Sector CRUD operations.
 */
public class SectorPanel extends JPanel {
    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField txtIdSector;
    private JTextField txtDescripcion;
    private JButton btnCreate;
    private JButton btnUpdate;
    private JButton btnDelete;
    private JButton btnRefresh;
    private JButton btnClear;
    
    public SectorPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JPanel formPanel = createFormPanel();
        add(formPanel, BorderLayout.NORTH);
        
        JPanel tablePanel = createTablePanel();
        add(tablePanel, BorderLayout.CENTER);
    }
    
    private JPanel createFormPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Sector Information"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // ID Sector
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("ID Sector:"), gbc);
        gbc.gridx = 1;
        txtIdSector = new JTextField(10);
        txtIdSector.setEditable(false);
        panel.add(txtIdSector, gbc);
        
        // Descripcion
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Descripción:"), gbc);
        gbc.gridx = 1;
        txtDescripcion = new JTextField(30);
        panel.add(txtDescripcion, gbc);
        
        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        btnCreate = new JButton("Create");
        btnUpdate = new JButton("Update");
        btnDelete = new JButton("Delete");
        btnClear = new JButton("Clear");
        btnRefresh = new JButton("Refresh");
        
        buttonPanel.add(btnCreate);
        buttonPanel.add(btnUpdate);
        buttonPanel.add(btnDelete);
        buttonPanel.add(btnClear);
        buttonPanel.add(btnRefresh);
        
        gbc.gridx = 0; gbc.gridy = 2;
        gbc.gridwidth = 2;
        panel.add(buttonPanel, gbc);
        
        return panel;
    }
    
    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Sectores"));
        
        String[] columnNames = {"ID", "Descripción"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        JScrollPane scrollPane = new JScrollPane(table);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    // Getters and setters
    public String getIdSector() { return txtIdSector.getText().trim(); }
    public String getDescripcion() { return txtDescripcion.getText().trim(); }
    public void setIdSector(String value) { txtIdSector.setText(value); }
    public void setDescripcion(String value) { txtDescripcion.setText(value); }
    
    public void clearForm() {
        txtIdSector.setText("");
        txtDescripcion.setText("");
    }
    
    public void updateTable(List<Sector> sectores) {
        tableModel.setRowCount(0);
        for (Sector s : sectores) {
            tableModel.addRow(new Object[]{s.getIdSector(), s.getDescripcion()});
        }
    }
    
    public Sector getSelectedSector() {
        int row = table.getSelectedRow();
        if (row == -1) return null;
        
        int id = (Integer) tableModel.getValueAt(row, 0);
        String desc = (String) tableModel.getValueAt(row, 1);
        return new Sector(id, desc);
    }
    
    public void loadSelectedToForm() {
        Sector s = getSelectedSector();
        if (s != null) {
            setIdSector(String.valueOf(s.getIdSector()));
            setDescripcion(s.getDescripcion());
        }
    }
    
    // Button getters
    public JButton getBtnCreate() { return btnCreate; }
    public JButton getBtnUpdate() { return btnUpdate; }
    public JButton getBtnDelete() { return btnDelete; }
    public JButton getBtnRefresh() { return btnRefresh; }
    public JButton getBtnClear() { return btnClear; }
    public JTable getTable() { return table; }
}

