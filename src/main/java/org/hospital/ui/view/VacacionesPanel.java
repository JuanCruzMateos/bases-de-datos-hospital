package org.hospital.ui.view;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.hospital.medico.Vacaciones;
import org.hospital.medico.Medico;

/**
 * View panel for Vacaciones (Vacations) CRUD operations.
 * Allows creating and editing vacation periods for medicos.
 */
public class VacacionesPanel extends JPanel {
    private JTable table;
    private DefaultTableModel tableModel;
    private JComboBox<MedicoComboItem> cmbMedico;
    private JTextField txtFechaInicio;
    private JTextField txtFechaFin;
    private JLabel lblDiasDuration;
    private JButton btnCreate;
    private JButton btnUpdate;
    private JButton btnDelete;
    private JButton btnRefresh;
    private JButton btnClear;
    
    // Store original values when editing
    private Vacaciones selectedVacaciones;
    
    public VacacionesPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Create form panel
        JPanel formPanel = createFormPanel();
        add(formPanel, BorderLayout.NORTH);
        
        // Create table panel
        JPanel tablePanel = createTablePanel();
        add(tablePanel, BorderLayout.CENTER);
    }
    
    private JPanel createFormPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Vacation Information"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Medico
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Medico:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 3;
        cmbMedico = new JComboBox<>();
        cmbMedico.setPreferredSize(new Dimension(400, 25));
        panel.add(cmbMedico, gbc);
        gbc.gridwidth = 1;
        
        // Fecha Inicio
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Fecha Inicio (YYYY-MM-DD):"), gbc);
        gbc.gridx = 1;
        txtFechaInicio = new JTextField(15);
        txtFechaInicio.setToolTipText("Format: 2024-12-20");
        panel.add(txtFechaInicio, gbc);
        
        // Add listener to calculate duration
        txtFechaInicio.addActionListener(e -> updateDuration());
        txtFechaInicio.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                updateDuration();
            }
        });
        
        // Fecha Fin
        gbc.gridx = 2; gbc.gridy = 1;
        panel.add(new JLabel("Fecha Fin (YYYY-MM-DD):"), gbc);
        gbc.gridx = 3;
        txtFechaFin = new JTextField(15);
        txtFechaFin.setToolTipText("Format: 2024-12-31");
        panel.add(txtFechaFin, gbc);
        
        // Add listener to calculate duration
        txtFechaFin.addActionListener(e -> updateDuration());
        txtFechaFin.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                updateDuration();
            }
        });
        
        // Duration label
        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("Duration:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 3;
        lblDiasDuration = new JLabel("-- days");
        lblDiasDuration.setFont(new Font("Arial", Font.BOLD, 12));
        lblDiasDuration.setForeground(new Color(41, 128, 185));
        panel.add(lblDiasDuration, gbc);
        gbc.gridwidth = 1;
        
        // Info panel with business rules
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 4;
        JPanel infoPanel = createInfoPanel();
        panel.add(infoPanel, gbc);
        gbc.gridwidth = 1;
        
        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        btnCreate = new JButton("Create Vacation");
        btnUpdate = new JButton("Update Vacation");
        btnDelete = new JButton("Delete");
        btnClear = new JButton("Clear");
        btnRefresh = new JButton("Refresh");
        
        buttonPanel.add(btnCreate);
        buttonPanel.add(btnUpdate);
        buttonPanel.add(btnDelete);
        buttonPanel.add(btnClear);
        buttonPanel.add(btnRefresh);
        
        gbc.gridx = 0; gbc.gridy = 4;
        gbc.gridwidth = 4;
        panel.add(buttonPanel, gbc);
        
        return panel;
    }
    
    private JPanel createInfoPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createLineBorder(new Color(41, 128, 185), 1));
        panel.setBackground(new Color(236, 240, 241));
        return panel;
    }
    
    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Vacaciones List"));
        
        String[] columnNames = {"Matricula", "Medico", "Fecha Inicio", "Fecha Fin", "Duration (Days)"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getColumnModel().getColumn(0).setPreferredWidth(80);
        table.getColumnModel().getColumn(1).setPreferredWidth(200);
        table.getColumnModel().getColumn(2).setPreferredWidth(120);
        table.getColumnModel().getColumn(3).setPreferredWidth(120);
        table.getColumnModel().getColumn(4).setPreferredWidth(120);
        
        JScrollPane scrollPane = new JScrollPane(table);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    // Getters for form fields
    public MedicoComboItem getSelectedMedico() { 
        return (MedicoComboItem) cmbMedico.getSelectedItem(); 
    }
    
    public String getFechaInicio() { 
        return txtFechaInicio.getText().trim(); 
    }
    
    public String getFechaFin() { 
        return txtFechaFin.getText().trim(); 
    }
    
    public Vacaciones getSelectedVacaciones() {
        return selectedVacaciones;
    }
    
    // Setters for form fields
    public void setFechaInicio(String value) { 
        txtFechaInicio.setText(value);
        updateDuration();
    }
    
    public void setFechaFin(String value) { 
        txtFechaFin.setText(value);
        updateDuration();
    }
    
    public void clearForm() {
        txtFechaInicio.setText("");
        txtFechaFin.setText("");
        lblDiasDuration.setText("-- days");
        if (cmbMedico.getItemCount() > 0) cmbMedico.setSelectedIndex(0);
        selectedVacaciones = null;
    }
    
    private void updateDuration() {
        try {
            if (!txtFechaInicio.getText().trim().isEmpty() && 
                !txtFechaFin.getText().trim().isEmpty()) {
                LocalDate inicio = LocalDate.parse(txtFechaInicio.getText().trim());
                LocalDate fin = LocalDate.parse(txtFechaFin.getText().trim());
                long days = ChronoUnit.DAYS.between(inicio, fin) + 1;
                
                if (days >= 0) {
                    lblDiasDuration.setText(days + " days");
                    lblDiasDuration.setForeground(new Color(46, 204, 113));
                } else {
                    lblDiasDuration.setText("Invalid range!");
                    lblDiasDuration.setForeground(new Color(231, 76, 60));
                }
            } else {
                lblDiasDuration.setText("-- days");
                lblDiasDuration.setForeground(new Color(41, 128, 185));
            }
        } catch (Exception e) {
            lblDiasDuration.setText("Invalid date format");
            lblDiasDuration.setForeground(new Color(231, 76, 60));
        }
    }
    
    public void updateTable(List<Vacaciones> vacacionesList, List<Medico> medicos) {
        tableModel.setRowCount(0);
        for (Vacaciones v : vacacionesList) {
            // Find medico name
            String medicoName = medicos.stream()
                    .filter(m -> m.getMatricula() == v.getMatricula())
                    .map(m -> m.getNombre() + " " + m.getApellido())
                    .findFirst()
                    .orElse("Unknown");
            
            long days = ChronoUnit.DAYS.between(v.getFechaInicio(), v.getFechaFin()) + 1;
            
            tableModel.addRow(new Object[]{
                v.getMatricula(),
                medicoName,
                v.getFechaInicio(),
                v.getFechaFin(),
                days
            });
        }
    }
    
    public void loadSelectedToForm() {
        int row = table.getSelectedRow();
        if (row == -1) return;

        long matricula = Long.parseLong(tableModel.getValueAt(row, 0).toString());
        String fechaInicio = tableModel.getValueAt(row, 2).toString();
        String fechaFin = tableModel.getValueAt(row, 3).toString();
        
        // Store the original selected vacaciones
        selectedVacaciones = new Vacaciones(
            matricula,
            LocalDate.parse(fechaInicio),
            LocalDate.parse(fechaFin)
        );
        
        // Set form fields
        setFechaInicio(fechaInicio);
        setFechaFin(fechaFin);

        // Select medico in combo box
        for (int i = 0; i < cmbMedico.getItemCount(); i++) {
            MedicoComboItem item = cmbMedico.getItemAt(i);
            if (item.getMedico().getMatricula() == matricula) {
                cmbMedico.setSelectedIndex(i);
                break;
            }
        }
    }
    
    // Load combo box data
    public void loadMedicos(List<Medico> medicos) {
        cmbMedico.removeAllItems();
        for (Medico m : medicos) {
            cmbMedico.addItem(new MedicoComboItem(m));
        }
    }
    
    // Button getters for controller
    public JButton getBtnCreate() { return btnCreate; }
    public JButton getBtnUpdate() { return btnUpdate; }
    public JButton getBtnDelete() { return btnDelete; }
    public JButton getBtnRefresh() { return btnRefresh; }
    public JButton getBtnClear() { return btnClear; }
    public JTable getTable() { return table; }
    
    // Helper class for combo box items
    public static class MedicoComboItem {
        private Medico medico;
        
        public MedicoComboItem(Medico medico) {
            this.medico = medico;
        }
        
        public Medico getMedico() {
            return medico;
        }
        
        @Override
        public String toString() {
            return medico.getMatricula() + " - " + medico.getNombre() + " " + medico.getApellido();
        }
    }
}

