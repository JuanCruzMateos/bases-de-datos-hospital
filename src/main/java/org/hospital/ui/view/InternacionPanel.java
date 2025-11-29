package org.hospital.ui.view;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import org.hospital.feature.internacion.domain.Internacion;

/**
 * View panel for Internacion (Hospitalization) CRUD operations.
 */
public class InternacionPanel extends JPanel {
    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField txtNroInternacion;
    private JTextField txtFechaInicio;
    private JTextField txtFechaFin;
    private JTextField txtTipoDocumento;
    private JTextField txtNroDocumento;
    private JTextField txtMatricula;
    private JCheckBox chkActiva;
    private JButton btnCreate;
    private JButton btnUpdate;
    private JButton btnDelete;
    private JButton btnRefresh;
    private JButton btnClear;
    private JButton btnFilterActivas;
    private JTextField txtNroHabitacion;
    private JTextField txtNroCama;

    
    public InternacionPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        add(createFormPanel(), BorderLayout.NORTH);
        add(createTablePanel(), BorderLayout.CENTER);
    }
    
    private JPanel createFormPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Internación Information"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Nro Internacion
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Nro Internación:"), gbc);
        gbc.gridx = 1;
        txtNroInternacion = new JTextField(10);
        txtNroInternacion.setEditable(false);
        panel.add(txtNroInternacion, gbc);
        
        // Fecha Inicio
        gbc.gridx = 2; gbc.gridy = 0;
        panel.add(new JLabel("Fecha Inicio (YYYY-MM-DD):"), gbc);
        gbc.gridx = 3;
        txtFechaInicio = new JTextField(10);
        panel.add(txtFechaInicio, gbc);
        
        // Fecha Fin
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Fecha Fin (YYYY-MM-DD):"), gbc);
        gbc.gridx = 1;
        txtFechaFin = new JTextField(10);
        panel.add(txtFechaFin, gbc);
        
        // Activa checkbox
        gbc.gridx = 2; gbc.gridy = 1;
        chkActiva = new JCheckBox("En curso (sin fecha fin)");
        chkActiva.addActionListener(e -> {
            txtFechaFin.setEnabled(!chkActiva.isSelected());
            if (chkActiva.isSelected()) {
                txtFechaFin.setText("");
            }
        });
        gbc.gridwidth = 2;
        panel.add(chkActiva, gbc);
        gbc.gridwidth = 1;
        
        // Tipo Documento
        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("Tipo Documento:"), gbc);
        gbc.gridx = 1;
        txtTipoDocumento = new JTextField(10);
        panel.add(txtTipoDocumento, gbc);
        
        // Nro Documento
        gbc.gridx = 2; gbc.gridy = 2;
        panel.add(new JLabel("Nro Documento:"), gbc);
        gbc.gridx = 3;
        txtNroDocumento = new JTextField(15);
        panel.add(txtNroDocumento, gbc);

        // Nro Habitación
        gbc.gridx = 0; gbc.gridy = 3;
        panel.add(new JLabel("Nro Habitación:"), gbc);
        gbc.gridx = 1;
        txtNroHabitacion = new JTextField(10);
        panel.add(txtNroHabitacion, gbc);

        // Nro Cama
        gbc.gridx = 2; gbc.gridy = 3;
        panel.add(new JLabel("Nro Cama:"), gbc);
        gbc.gridx = 3;
        txtNroCama = new JTextField(10);
        panel.add(txtNroCama, gbc);

        // Matricula
        gbc.gridx = 0; gbc.gridy = 4;
        panel.add(new JLabel("Matrícula Médico:"), gbc);
        gbc.gridx = 1;
        txtMatricula = new JTextField(10);
        panel.add(txtMatricula, gbc);
        
        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        btnCreate = new JButton("Create");
        btnUpdate = new JButton("Update");
        btnDelete = new JButton("Delete");
        btnClear = new JButton("Clear");
        btnRefresh = new JButton("Refresh All");
        btnFilterActivas = new JButton("Show Active Only");
        
        buttonPanel.add(btnCreate);
        buttonPanel.add(btnUpdate);
        buttonPanel.add(btnDelete);
        buttonPanel.add(btnClear);
        buttonPanel.add(btnRefresh);
        buttonPanel.add(btnFilterActivas);
        
        gbc.gridx = 0; gbc.gridy = 5;
        gbc.gridwidth = 4;
        panel.add(buttonPanel, gbc);
        
        return panel;
    }
    
    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Internaciones"));
        
        String[] columnNames = {"Nro", "Inicio", "Fin", "Tipo Doc", "Nro Doc", "Matrícula"};
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
    
    // Getters
    public String getNroInternacion() { return txtNroInternacion.getText().trim(); }
    public String getFechaInicio() { return txtFechaInicio.getText().trim(); }
    public String getFechaFin() { return txtFechaFin.getText().trim(); }
    public String getTipoDocumento() { return txtTipoDocumento.getText().trim(); }
    public String getNroDocumento() { return txtNroDocumento.getText().trim(); }
    public String getMatricula() { return txtMatricula.getText().trim(); }
    public boolean isActiva() { return chkActiva.isSelected(); }
    public String getNroHabitacion() {return txtNroHabitacion.getText().trim();}
    public String getNroCama() {return txtNroCama.getText().trim();}
    
    // Setters
    public void setNroInternacion(String value) { txtNroInternacion.setText(value); }
    public void setFechaInicio(String value) { txtFechaInicio.setText(value); }
    public void setFechaFin(String value) { txtFechaFin.setText(value); }
    public void setTipoDocumento(String value) { txtTipoDocumento.setText(value); }
    public void setNroDocumento(String value) { txtNroDocumento.setText(value); }
    public void setMatricula(String value) { txtMatricula.setText(value); }
    public void setNroHabitacion(String nroHabitacion) {txtNroHabitacion.setText(nroHabitacion);}
    public void setNroCama(String nroCama) {txtNroCama.setText(nroCama);}

    public void clearForm() {
        txtNroInternacion.setText("");
        txtFechaInicio.setText("");
        txtFechaFin.setText("");
        txtTipoDocumento.setText("");
        txtNroDocumento.setText("");
        txtMatricula.setText("");
        chkActiva.setSelected(false);
        txtFechaFin.setEnabled(true);
        txtNroHabitacion.setText("");
        txtNroCama.setText("");
    }
    
    public void updateTable(List<Internacion> internaciones) {
        tableModel.setRowCount(0);
        for (Internacion i : internaciones) {
            tableModel.addRow(new Object[]{
                i.getNroInternacion(),
                i.getFechaInicio(),
                i.getFechaFin() != null ? i.getFechaFin() : "En curso",
                i.getTipoDocumento(),
                i.getNroDocumento(),
                i.getMatricula()
            });
        }
    }
    
    public void loadSelectedToForm() {
        int row = table.getSelectedRow();
        if (row == -1) return;
        
        setNroInternacion(tableModel.getValueAt(row, 0).toString());
        setFechaInicio(tableModel.getValueAt(row, 1).toString());
        
        Object fechaFin = tableModel.getValueAt(row, 2);
        if ("En curso".equals(fechaFin)) {
            setFechaFin("");
            chkActiva.setSelected(true);
            txtFechaFin.setEnabled(false);
        } else {
            setFechaFin(fechaFin.toString());
            chkActiva.setSelected(false);
            txtFechaFin.setEnabled(true);
        }
        
        setTipoDocumento((String) tableModel.getValueAt(row, 3));
        setNroDocumento((String) tableModel.getValueAt(row, 4));
        setMatricula(tableModel.getValueAt(row, 5).toString());
    }
    
    // Button getters
    public JButton getBtnCreate() { return btnCreate; }
    public JButton getBtnUpdate() { return btnUpdate; }
    public JButton getBtnDelete() { return btnDelete; }
    public JButton getBtnRefresh() { return btnRefresh; }
    public JButton getBtnClear() { return btnClear; }
    public JButton getBtnFilterActivas() { return btnFilterActivas; }
    public JTable getTable() { return table; }
}

