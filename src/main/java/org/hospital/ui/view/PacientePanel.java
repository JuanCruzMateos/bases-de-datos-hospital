package org.hospital.ui.view;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;
import org.hospital.paciente.Paciente;

/**
 * View panel for Paciente (Patient) CRUD operations.
 */
public class PacientePanel extends JPanel {
    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField txtTipoDocumento;
    private JTextField txtNroDocumento;
    private JTextField txtNombre;
    private JTextField txtApellido;
    private JTextField txtFechaNacimiento;
    private JComboBox<String> cmbSexo;
    private JButton btnCreate;
    private JButton btnUpdate;
    private JButton btnDelete;
    private JButton btnRefresh;
    private JButton btnClear;
    
    public PacientePanel() {
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
        panel.setBorder(BorderFactory.createTitledBorder("Paciente Information"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Tipo Documento
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Tipo Documento:"), gbc);
        gbc.gridx = 1;
        txtTipoDocumento = new JTextField(10);
        panel.add(txtTipoDocumento, gbc);
        
        // Nro Documento
        gbc.gridx = 2; gbc.gridy = 0;
        panel.add(new JLabel("Nro Documento:"), gbc);
        gbc.gridx = 3;
        txtNroDocumento = new JTextField(15);
        panel.add(txtNroDocumento, gbc);
        
        // Nombre
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Nombre:"), gbc);
        gbc.gridx = 1;
        txtNombre = new JTextField(20);
        panel.add(txtNombre, gbc);
        
        // Apellido
        gbc.gridx = 2; gbc.gridy = 1;
        panel.add(new JLabel("Apellido:"), gbc);
        gbc.gridx = 3;
        txtApellido = new JTextField(20);
        panel.add(txtApellido, gbc);
        
        // Fecha Nacimiento
        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("Fecha Nacimiento (YYYY-MM-DD):"), gbc);
        gbc.gridx = 1;
        txtFechaNacimiento = new JTextField(10);
        panel.add(txtFechaNacimiento, gbc);
        
        // Sexo
        gbc.gridx = 2; gbc.gridy = 2;
        panel.add(new JLabel("Sexo:"), gbc);
        gbc.gridx = 3;
        cmbSexo = new JComboBox<>(new String[]{"M", "F", "X"});
        panel.add(cmbSexo, gbc);
        
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
        
        gbc.gridx = 0; gbc.gridy = 3;
        gbc.gridwidth = 4;
        panel.add(buttonPanel, gbc);
        
        return panel;
    }
    
    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Pacientes"));
        
        String[] columnNames = {"Tipo Doc", "Nro Doc", "Nombre", "Apellido", "Fecha Nac.", "Sexo"};
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
    public String getTipoDocumento() { return txtTipoDocumento.getText().trim(); }
    public String getNroDocumento() { return txtNroDocumento.getText().trim(); }
    public String getNombre() { return txtNombre.getText().trim(); }
    public String getApellido() { return txtApellido.getText().trim(); }
    public String getFechaNacimiento() { return txtFechaNacimiento.getText().trim(); }
    public String getSexo() { return (String) cmbSexo.getSelectedItem(); }
    
    // Setters for form fields
    public void setTipoDocumento(String value) { txtTipoDocumento.setText(value); }
    public void setNroDocumento(String value) { txtNroDocumento.setText(value); }
    public void setNombre(String value) { txtNombre.setText(value); }
    public void setApellido(String value) { txtApellido.setText(value); }
    public void setFechaNacimiento(String value) { txtFechaNacimiento.setText(value); }
    public void setSexo(String value) { cmbSexo.setSelectedItem(value); }
    
    public void clearForm() {
        txtTipoDocumento.setText("");
        txtNroDocumento.setText("");
        txtNombre.setText("");
        txtApellido.setText("");
        txtFechaNacimiento.setText("");
        cmbSexo.setSelectedIndex(0);

        // Modo "alta": se puede editar documento
        setIdentityEditable(true);
    }


    // --- Control de edición de identidad (documento del paciente) ---
    public void setIdentityEditable(boolean editable) {
        txtTipoDocumento.setEditable(editable);
        txtNroDocumento.setEditable(editable);
    }

    
    public void updateTable(List<Paciente> pacientes) {
        tableModel.setRowCount(0);
        for (Paciente p : pacientes) {
            tableModel.addRow(new Object[]{
                p.getTipoDocumento(),
                p.getNroDocumento(),
                p.getNombre(),
                p.getApellido(),
                p.getFechaNacimiento(),
                p.getSexo()
            });
        }
    }
    
    public Paciente getSelectedPaciente() {
        int row = table.getSelectedRow();
        if (row == -1) return null;
        
        String tipoDoc = (String) tableModel.getValueAt(row, 0);
        String nroDoc = (String) tableModel.getValueAt(row, 1);
        String nombre = (String) tableModel.getValueAt(row, 2);
        String apellido = (String) tableModel.getValueAt(row, 3);
        LocalDate fechaNac = LocalDate.parse(tableModel.getValueAt(row, 4).toString());
        char sexo = tableModel.getValueAt(row, 5).toString().charAt(0);
        
        return new Paciente(tipoDoc, nroDoc, nombre, apellido, "PACIENTE", fechaNac, sexo);
    }
    
    public void loadSelectedToForm() {
        Paciente p = getSelectedPaciente();
        if (p != null) {
            setTipoDocumento(p.getTipoDocumento());
            setNroDocumento(p.getNroDocumento());
            setNombre(p.getNombre());
            setApellido(p.getApellido());
            setFechaNacimiento(p.getFechaNacimiento().toString());
            setSexo(String.valueOf(p.getSexo()));

            // Modo "edición": documento bloqueado
            setIdentityEditable(false);
        }
    }

    
    // Button getters for controller
    public JButton getBtnCreate() { return btnCreate; }
    public JButton getBtnUpdate() { return btnUpdate; }
    public JButton getBtnDelete() { return btnDelete; }
    public JButton getBtnRefresh() { return btnRefresh; }
    public JButton getBtnClear() { return btnClear; }
    public JTable getTable() { return table; }
}

