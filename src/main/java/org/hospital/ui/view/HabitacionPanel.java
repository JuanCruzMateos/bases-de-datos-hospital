package org.hospital.ui.view;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import org.hospital.internacion.Habitacion;

/**
 * View panel for Habitacion (Room) CRUD operations.
 */
public class HabitacionPanel extends JPanel {
    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField txtNroHabitacion;
    private JTextField txtPiso;
    private JComboBox<String> cmbOrientacion;
    private JTextField txtIdSector;
    private JButton btnCreate;
    private JButton btnUpdate;
    private JButton btnDelete;
    private JButton btnRefresh;
    private JButton btnClear;
    
    public HabitacionPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        add(createFormPanel(), BorderLayout.NORTH);
        add(createTablePanel(), BorderLayout.CENTER);
    }
    
    private JPanel createFormPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Habitación Information"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Nro Habitacion
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Nro Habitación:"), gbc);
        gbc.gridx = 1;
        txtNroHabitacion = new JTextField(10);
        txtNroHabitacion.setEditable(false);
        panel.add(txtNroHabitacion, gbc);
        
        // Piso
        gbc.gridx = 2; gbc.gridy = 0;
        panel.add(new JLabel("Piso:"), gbc);
        gbc.gridx = 3;
        txtPiso = new JTextField(10);
        panel.add(txtPiso, gbc);
        
        // Orientacion
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Orientación:"), gbc);
        gbc.gridx = 1;
        cmbOrientacion = new JComboBox<>(new String[]{"NORTE", "SUR", "ESTE", "OESTE"});
        panel.add(cmbOrientacion, gbc);
        
        // ID Sector
        gbc.gridx = 2; gbc.gridy = 1;
        panel.add(new JLabel("ID Sector:"), gbc);
        gbc.gridx = 3;
        txtIdSector = new JTextField(10);
        panel.add(txtIdSector, gbc);
        
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
        gbc.gridwidth = 4;
        panel.add(buttonPanel, gbc);
        
        return panel;
    }
    
    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Habitaciones"));
        
        String[] columnNames = {"Nro", "Piso", "Orientación", "Sector"};
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
    public String getNroHabitacion() { return txtNroHabitacion.getText().trim(); }
    public String getPiso() { return txtPiso.getText().trim(); }
    public String getOrientacion() { return (String) cmbOrientacion.getSelectedItem(); }
    public String getIdSector() { return txtIdSector.getText().trim(); }
    
    public void setNroHabitacion(String value) { txtNroHabitacion.setText(value); }
    public void setPiso(String value) { txtPiso.setText(value); }
    public void setOrientacion(String value) { cmbOrientacion.setSelectedItem(value); }
    public void setIdSector(String value) { txtIdSector.setText(value); }
    
    public void clearForm() {
        txtNroHabitacion.setText("");
        txtPiso.setText("");
        cmbOrientacion.setSelectedIndex(0);
        txtIdSector.setText("");
    }
    
    public void updateTable(List<Habitacion> habitaciones) {
        tableModel.setRowCount(0);
        for (Habitacion h : habitaciones) {
            tableModel.addRow(new Object[]{
                h.getNroHabitacion(), h.getPiso(), h.getOrientacion(), h.getIdSector()
            });
        }
    }
    
    public void loadSelectedToForm() {
        int row = table.getSelectedRow();
        if (row == -1) return;
        
        setNroHabitacion(tableModel.getValueAt(row, 0).toString());
        setPiso(tableModel.getValueAt(row, 1).toString());
        setOrientacion((String) tableModel.getValueAt(row, 2));
        setIdSector(tableModel.getValueAt(row, 3).toString());
    }
    
    // Button getters
    public JButton getBtnCreate() { return btnCreate; }
    public JButton getBtnUpdate() { return btnUpdate; }
    public JButton getBtnDelete() { return btnDelete; }
    public JButton getBtnRefresh() { return btnRefresh; }
    public JButton getBtnClear() { return btnClear; }
    public JTable getTable() { return table; }
}

