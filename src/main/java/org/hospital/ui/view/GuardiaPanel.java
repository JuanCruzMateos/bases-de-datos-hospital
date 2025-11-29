package org.hospital.ui.view;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDateTime;
import java.util.List;
import org.hospital.guardia.Guardia;
import org.hospital.guardia.Turno;
import org.hospital.medico.Especialidad;
import org.hospital.medico.Medico;

/**
 * View panel for Guardia (Guard Shifts) CRUD operations.
 */
public class GuardiaPanel extends JPanel {
    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField txtNroGuardia;
    private JTextField txtFechaHora;
    private JComboBox<MedicoComboItem> cmbMedico;
    private JComboBox<EspecialidadComboItem> cmbEspecialidad;
    private JComboBox<TurnoComboItem> cmbTurno;
    private JButton btnCreate;
    private JButton btnUpdate;
    private JButton btnDelete;
    private JButton btnRefresh;
    private JButton btnClear;
    
    public GuardiaPanel() {
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
        panel.setBorder(BorderFactory.createTitledBorder("Guardia Information"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Nro Guardia
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Nro Guardia:"), gbc);
        gbc.gridx = 1;
        txtNroGuardia = new JTextField(10);
        // Igual que en Sector, Habitación e Internación: PK solo lectura
        txtNroGuardia.setEditable(false);
        txtNroGuardia.setToolTipText("Generado automáticamente por la base de datos");
        panel.add(txtNroGuardia, gbc);
        
        // Fecha Hora
        gbc.gridx = 2; gbc.gridy = 0;
        panel.add(new JLabel("Fecha Hora (YYYY-MM-DDTHH:MM):"), gbc);
        gbc.gridx = 3;
        txtFechaHora = new JTextField(20);
        txtFechaHora.setToolTipText("Format: 2024-12-25T08:00");
        panel.add(txtFechaHora, gbc);
        
        // Medico
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Medico:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 3;
        cmbMedico = new JComboBox<>();
        panel.add(cmbMedico, gbc);
        gbc.gridwidth = 1;
        
        // Especialidad
        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("Especialidad:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 3;
        cmbEspecialidad = new JComboBox<>();
        panel.add(cmbEspecialidad, gbc);
        gbc.gridwidth = 1;
        
        // Turno
        gbc.gridx = 0; gbc.gridy = 3;
        panel.add(new JLabel("Turno:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 3;
        cmbTurno = new JComboBox<>();
        panel.add(cmbTurno, gbc);
        gbc.gridwidth = 1;
        
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
        
        gbc.gridx = 0; gbc.gridy = 4;
        gbc.gridwidth = 4;
        panel.add(buttonPanel, gbc);
        
        return panel;
    }
    
    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Guardias"));
        
        String[] columnNames = {"Nro Guardia", "Fecha Hora", "Matricula", "Medico", "Especialidad", "Turno"};
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
    
    // Getters for form fields
    public String getNroGuardia() { return txtNroGuardia.getText().trim(); }
    public String getFechaHora() { return txtFechaHora.getText().trim(); }
    public MedicoComboItem getSelectedMedico() { return (MedicoComboItem) cmbMedico.getSelectedItem(); }
    public EspecialidadComboItem getSelectedEspecialidad() { return (EspecialidadComboItem) cmbEspecialidad.getSelectedItem(); }
    public TurnoComboItem getSelectedTurno() { return (TurnoComboItem) cmbTurno.getSelectedItem(); }
    
    // Setters for form fields
    public void setNroGuardia(String value) { txtNroGuardia.setText(value); }
    public void setFechaHora(String value) { txtFechaHora.setText(value); }
    
    public void clearForm() {
        txtNroGuardia.setText("");
        txtFechaHora.setText("");
        if (cmbMedico.getItemCount() > 0) cmbMedico.setSelectedIndex(0);
        if (cmbEspecialidad.getItemCount() > 0) cmbEspecialidad.setSelectedIndex(0);
        if (cmbTurno.getItemCount() > 0) cmbTurno.setSelectedIndex(0);
    }
    
    public void updateTable(List<Guardia> guardias, List<Medico> medicos, List<Especialidad> especialidades, List<Turno> turnos) {
        tableModel.setRowCount(0);
        for (Guardia g : guardias) {
            // Find medico name
            String medicoName = medicos.stream()
                    .filter(m -> m.getMatricula() == g.getMatricula())
                    .map(m -> m.getNombre() + " " + m.getApellido())
                    .findFirst()
                    .orElse("Unknown");
            
            // Find especialidad description
            String especialidadDesc = especialidades.stream()
                    .filter(e -> e.getCodEspecialidad() == g.getCodEspecialidad())
                    .map(Especialidad::getDescripcion)
                    .findFirst()
                    .orElse("Unknown");
            
            // Find turno horario
            String turnoHorario = turnos.stream()
                    .filter(t -> t.getIdTurno() == g.getIdTurno())
                    .map(Turno::getHorario)
                    .findFirst()
                    .orElse("Unknown");
            
            tableModel.addRow(new Object[]{
                g.getNroGuardia(),
                g.getFechaHora(),
                g.getMatricula(),
                medicoName,
                especialidadDesc,
                turnoHorario
            });
        }
    }
    
    public Guardia getSelectedGuardia() {
        int row = table.getSelectedRow();
        if (row == -1) return null;
        
        int nroGuardia = Integer.parseInt(tableModel.getValueAt(row, 0).toString());
        LocalDateTime fechaHora = LocalDateTime.parse(tableModel.getValueAt(row, 1).toString());
        long matricula = Long.parseLong(tableModel.getValueAt(row, 2).toString());
        
        Guardia guardia = new Guardia();
        guardia.setNroGuardia(nroGuardia);
        guardia.setFechaHora(fechaHora);
        guardia.setMatricula(matricula);
        
        return guardia;
    }
    
    public void loadSelectedToForm() {
        int row = table.getSelectedRow();
        if (row == -1) return;

        // Campos simples
        setNroGuardia(tableModel.getValueAt(row, 0).toString());
        setFechaHora(tableModel.getValueAt(row, 1).toString());

        long matricula = Long.parseLong(tableModel.getValueAt(row, 2).toString());

        // Médico
        for (int i = 0; i < cmbMedico.getItemCount(); i++) {
            MedicoComboItem item = cmbMedico.getItemAt(i);
            if (item.getMedico().getMatricula() == matricula) {
                cmbMedico.setSelectedIndex(i);
                break;
            }
        }

        // Especialidad (columna 4)
        String especialidadDesc = tableModel.getValueAt(row, 4).toString();
        for (int i = 0; i < cmbEspecialidad.getItemCount(); i++) {
            EspecialidadComboItem item = cmbEspecialidad.getItemAt(i);
            if (item.getEspecialidad().getDescripcion().equals(especialidadDesc)) {
                cmbEspecialidad.setSelectedIndex(i);
                break;
            }
        }

        // Turno (columna 5: solo el horario, sin el "id -")
        String turnoHorario = tableModel.getValueAt(row, 5).toString();
        for (int i = 0; i < cmbTurno.getItemCount(); i++) {
            TurnoComboItem item = cmbTurno.getItemAt(i);
            if (item.getTurno().getHorario().equals(turnoHorario)) {
                cmbTurno.setSelectedIndex(i);
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
    
    public void loadEspecialidades(List<Especialidad> especialidades) {
        cmbEspecialidad.removeAllItems();
        for (Especialidad e : especialidades) {
            cmbEspecialidad.addItem(new EspecialidadComboItem(e));
        }
    }
    
    public void loadTurnos(List<Turno> turnos) {
        cmbTurno.removeAllItems();
        for (Turno t : turnos) {
            cmbTurno.addItem(new TurnoComboItem(t));
        }
    }
    
    // Button getters for controller
    public JButton getBtnCreate() { return btnCreate; }
    public JButton getBtnUpdate() { return btnUpdate; }
    public JButton getBtnDelete() { return btnDelete; }
    public JButton getBtnRefresh() { return btnRefresh; }
    public JButton getBtnClear() { return btnClear; }
    public JTable getTable() { return table; }
    
    // Helper classes for combo box items
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
    
    public static class EspecialidadComboItem {
        private Especialidad especialidad;
        
        public EspecialidadComboItem(Especialidad especialidad) {
            this.especialidad = especialidad;
        }
        
        public Especialidad getEspecialidad() {
            return especialidad;
        }
        
        @Override
        public String toString() {
            return especialidad.getCodEspecialidad() + " - " + especialidad.getDescripcion();
        }
    }
    
    public static class TurnoComboItem {
        private Turno turno;
        
        public TurnoComboItem(Turno turno) {
            this.turno = turno;
        }
        
        public Turno getTurno() {
            return turno;
        }
        
        @Override
        public String toString() {
            return turno.getIdTurno() + " - " + turno.getHorario();
        }
    }
}

