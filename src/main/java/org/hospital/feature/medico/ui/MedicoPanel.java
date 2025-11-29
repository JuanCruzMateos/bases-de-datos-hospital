package org.hospital.feature.medico.ui;

import org.hospital.feature.medico.ui.MedicoPanel;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.imageio.ImageIO;
import org.hospital.feature.medico.domain.Especialidad;
import org.hospital.feature.medico.domain.Medico;

/**
 * View panel for Medico (Doctor) CRUD operations with Especialidad management.
 */
public class MedicoPanel extends JPanel {
    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField txtMatricula;
    private JTextField txtTipoDocumento;
    private JTextField txtNroDocumento;
    private JTextField txtNombre;
    private JTextField txtApellido;
    private JTextField txtCuilCuit;
    private JTextField txtFechaIngreso;
    private JTextField txtMaxCantGuardia;
    private JLabel lblFoto;
    private JButton btnLoadFoto;
    private JButton btnClearFoto;
    private byte[] fotoBytes;
    
    // Especialidades management
    private JList<String> listEspecialidades;
    private DefaultListModel<String> especialidadesModel;
    private JComboBox<EspecialidadComboItem> cmbAvailableEspecialidades;
    private JButton btnAddEspecialidad;
    private JButton btnRemoveEspecialidad;
    
    // CRUD buttons
    private JButton btnCreate;
    private JButton btnUpdate;
    private JButton btnDelete;
    private JButton btnRefresh;
    private JButton btnClear;
    
    // Medico especialidades (cod_especialidad -> descripcion)
    private Set<Especialidad> currentEspecialidades = new HashSet<>();
    
    public MedicoPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Create main split pane
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setResizeWeight(0.5);
        
        // Top: Form + Especialidades
        JPanel topPanel = new JPanel(new BorderLayout(5, 5));
        topPanel.add(createFotoPanel(), BorderLayout.WEST);
        topPanel.add(createFormPanel(), BorderLayout.CENTER);
        topPanel.add(createEspecialidadesPanel(), BorderLayout.EAST);
        
        // Bottom: Table
        JPanel tablePanel = createTablePanel();
        
        splitPane.setTopComponent(topPanel);
        splitPane.setBottomComponent(tablePanel);
        
        add(splitPane, BorderLayout.CENTER);
    }
    
    private JPanel createFormPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Medico Information"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        int row = 0;
        
        // Matricula
        gbc.gridx = 0; gbc.gridy = row;
        panel.add(new JLabel("Matrícula:"), gbc);
        gbc.gridx = 1;
        txtMatricula = new JTextField(15);
        panel.add(txtMatricula, gbc);
        
        // Tipo Documento
        gbc.gridx = 2; gbc.gridy = row;
        panel.add(new JLabel("Tipo Doc:"), gbc);
        gbc.gridx = 3;
        txtTipoDocumento = new JTextField(10);
        panel.add(txtTipoDocumento, gbc);
        
        row++;
        
        // Nro Documento
        gbc.gridx = 0; gbc.gridy = row;
        panel.add(new JLabel("Nro Doc:"), gbc);
        gbc.gridx = 1;
        txtNroDocumento = new JTextField(15);
        panel.add(txtNroDocumento, gbc);
        
        // CUIL/CUIT
        gbc.gridx = 2; gbc.gridy = row;
        panel.add(new JLabel("CUIL/CUIT:"), gbc);
        gbc.gridx = 3;
        txtCuilCuit = new JTextField(15);
        panel.add(txtCuilCuit, gbc);
        
        row++;
        
        // Nombre
        gbc.gridx = 0; gbc.gridy = row;
        panel.add(new JLabel("Nombre:"), gbc);
        gbc.gridx = 1;
        txtNombre = new JTextField(20);
        panel.add(txtNombre, gbc);
        
        // Apellido
        gbc.gridx = 2; gbc.gridy = row;
        panel.add(new JLabel("Apellido:"), gbc);
        gbc.gridx = 3;
        txtApellido = new JTextField(20);
        panel.add(txtApellido, gbc);
        
        row++;
        
        // Fecha Ingreso
        gbc.gridx = 0; gbc.gridy = row;
        panel.add(new JLabel("Fecha Ingreso (YYYY-MM-DD):"), gbc);
        gbc.gridx = 1;
        txtFechaIngreso = new JTextField(12);
        panel.add(txtFechaIngreso, gbc);
        
        // Max Cant Guardia
        gbc.gridx = 2; gbc.gridy = row;
        panel.add(new JLabel("Max Guardias:"), gbc);
        gbc.gridx = 3;
        txtMaxCantGuardia = new JTextField(5);
        panel.add(txtMaxCantGuardia, gbc);
        
        row++;
        
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
        
        gbc.gridx = 0; gbc.gridy = row;
        gbc.gridwidth = 4;
        panel.add(buttonPanel, gbc);
        
        return panel;
    }
    
    private JPanel createEspecialidadesPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createTitledBorder("Especialidades"));
        panel.setPreferredSize(new Dimension(300, 0));
        
        // Current especialidades list
        especialidadesModel = new DefaultListModel<>();
        listEspecialidades = new JList<>(especialidadesModel);
        listEspecialidades.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(listEspecialidades);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // Add/Remove panel
        JPanel managePanel = new JPanel(new BorderLayout(5, 5));
        
        // Combo box for available especialidades
        cmbAvailableEspecialidades = new JComboBox<>();
        managePanel.add(cmbAvailableEspecialidades, BorderLayout.NORTH);
        
        // Buttons
        JPanel buttonsPanel = new JPanel(new GridLayout(2, 1, 5, 5));
        btnAddEspecialidad = new JButton("Add Especialidad");
        btnRemoveEspecialidad = new JButton("Remove Selected");
        buttonsPanel.add(btnAddEspecialidad);
        buttonsPanel.add(btnRemoveEspecialidad);
        managePanel.add(buttonsPanel, BorderLayout.CENTER);
        
        panel.add(managePanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Medicos"));
        
        String[] columnNames = {"Matrícula", "Nombre", "Apellido", "CUIL/CUIT", "Fecha Ingreso", "Especialidades"};
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
    public String getMatricula() { return txtMatricula.getText().trim(); }
    public String getTipoDocumento() { return txtTipoDocumento.getText().trim(); }
    public String getNroDocumento() { return txtNroDocumento.getText().trim(); }
    public String getNombre() { return txtNombre.getText().trim(); }
    public String getApellido() { return txtApellido.getText().trim(); }
    public String getCuilCuit() { return txtCuilCuit.getText().trim(); }
    public String getFechaIngreso() { return txtFechaIngreso.getText().trim(); }
    public String getMaxCantGuardia() { return txtMaxCantGuardia.getText().trim(); }
    public byte[] getFotoBytes() { return fotoBytes; }
    
    // Setters for form fields
    public void setMatricula(String value) { txtMatricula.setText(value); }
    public void setTipoDocumento(String value) { txtTipoDocumento.setText(value); }
    public void setNroDocumento(String value) { txtNroDocumento.setText(value); }
    public void setNombre(String value) { txtNombre.setText(value); }
    public void setApellido(String value) { txtApellido.setText(value); }
    public void setCuilCuit(String value) { txtCuilCuit.setText(value); }
    public void setFechaIngreso(String value) { txtFechaIngreso.setText(value); }
    public void setMaxCantGuardia(String value) { txtMaxCantGuardia.setText(value); }
    public void setFotoBytes(byte[] bytes) { 
        this.fotoBytes = bytes; 
        updateFotoPreview(); 
    }
    
    public void clearForm() {
        txtMatricula.setText("");
        txtTipoDocumento.setText("");
        txtNroDocumento.setText("");
        txtNombre.setText("");
        txtApellido.setText("");
        txtCuilCuit.setText("");
        txtFechaIngreso.setText("");
        txtMaxCantGuardia.setText("");
        especialidadesModel.clear();
        currentEspecialidades.clear();
        setFotoBytes(null);

        // Modo "alta": se puede editar identidad
        setIdentityEditable(true);
    }


    // --- Control de edición de identidad (matrícula + documento) ---
    public void setIdentityEditable(boolean editable) {
        txtMatricula.setEditable(editable);
        txtTipoDocumento.setEditable(editable);
        txtNroDocumento.setEditable(editable);
    }

    
    public void updateTable(List<Medico> medicos) {
        tableModel.setRowCount(0);
        for (Medico m : medicos) {
            String especialidadesStr = m.getEspecialidades().stream()
                    .map(Especialidad::getDescripcion)
                    .reduce((a, b) -> a + ", " + b)
                    .orElse("");
            
            tableModel.addRow(new Object[]{
                m.getMatricula(),
                m.getNombre(),
                m.getApellido(),
                m.getCuilCuit(),
                m.getFechaIngreso(),
                especialidadesStr
            });
        }
    }
    
    public Medico getSelectedMedico() {
        int row = table.getSelectedRow();
        if (row == -1) return null;
        
        long matricula = Long.parseLong(tableModel.getValueAt(row, 0).toString());
        String nombre = (String) tableModel.getValueAt(row, 1);
        String apellido = (String) tableModel.getValueAt(row, 2);
        String cuilCuit = (String) tableModel.getValueAt(row, 3);
        LocalDate fechaIngreso = LocalDate.parse(tableModel.getValueAt(row, 4).toString());
        
        // Note: This returns a partial Medico. Controller should fetch full one from service.
        Medico medico = new Medico();
        medico.setMatricula(matricula);
        medico.setNombre(nombre);
        medico.setApellido(apellido);
        medico.setCuilCuit(cuilCuit);
        medico.setFechaIngreso(fechaIngreso);
        
        return medico;
    }
    
    public void loadSelectedToForm() {
        // This will be implemented by controller
    }
    
    // Especialidades management
    public void setCurrentEspecialidades(Set<Especialidad> especialidades) {
        this.currentEspecialidades = new HashSet<>(especialidades);
        especialidadesModel.clear();
        for (Especialidad esp : especialidades) {
            especialidadesModel.addElement(esp.getCodEspecialidad() + " - " + esp.getDescripcion());
        }
    }
    
    public Set<Especialidad> getCurrentEspecialidades() {
        return new HashSet<>(currentEspecialidades);
    }
    
    public void loadAvailableEspecialidades(List<Especialidad> especialidades) {
        cmbAvailableEspecialidades.removeAllItems();
        for (Especialidad esp : especialidades) {
            cmbAvailableEspecialidades.addItem(new EspecialidadComboItem(esp));
        }
    }
    
    public EspecialidadComboItem getSelectedAvailableEspecialidad() {
        return (EspecialidadComboItem) cmbAvailableEspecialidades.getSelectedItem();
    }
    
    public String getSelectedEspecialidadInList() {
        String selected = listEspecialidades.getSelectedValue();
        if (selected == null) return null;
        // Extract code from "CODE - Description" format
        return selected.split(" - ")[0];
    }
    
    // Button getters for controller
    public JButton getBtnCreate() { return btnCreate; }
    public JButton getBtnUpdate() { return btnUpdate; }
    public JButton getBtnDelete() { return btnDelete; }
    public JButton getBtnRefresh() { return btnRefresh; }
    public JButton getBtnClear() { return btnClear; }
    public JButton getBtnAddEspecialidad() { return btnAddEspecialidad; }
    public JButton getBtnRemoveEspecialidad() { return btnRemoveEspecialidad; }
    public JButton getBtnLoadFoto() { return btnLoadFoto; }
    public JButton getBtnClearFoto() { return btnClearFoto; }
    public JTable getTable() { return table; }
    
    // Helper class for combo box items
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

    private JPanel createFotoPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createTitledBorder("Foto"));
        panel.setPreferredSize(new Dimension(170, 0));

        lblFoto = new JLabel("Sin foto", SwingConstants.CENTER);
        lblFoto.setPreferredSize(new Dimension(150, 150));
        lblFoto.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        panel.add(lblFoto, BorderLayout.CENTER);

        JPanel buttons = new JPanel(new GridLayout(1, 2, 5, 5));
        btnLoadFoto = new JButton("Cargar");
        btnClearFoto = new JButton("Borrar");
        buttons.add(btnLoadFoto);
        buttons.add(btnClearFoto);
        panel.add(buttons, BorderLayout.SOUTH);

        return panel;
    }

    public void clearFoto() {
        setFotoBytes(null);
    }

    private void updateFotoPreview() {
        if (fotoBytes == null || fotoBytes.length == 0) {
            lblFoto.setIcon(null);
            lblFoto.setText("Sin foto");
            return;
        }
        try {
            BufferedImage img = ImageIO.read(new ByteArrayInputStream(fotoBytes));
            if (img != null) {
                Image scaled = img.getScaledInstance(150, 150, Image.SCALE_SMOOTH);
                lblFoto.setIcon(new ImageIcon(scaled));
                lblFoto.setText("");
            } else {
                lblFoto.setIcon(null);
                lblFoto.setText("Sin foto");
            }
        } catch (IOException e) {
            lblFoto.setIcon(null);
            lblFoto.setText("Sin foto");
        }
    }
}
