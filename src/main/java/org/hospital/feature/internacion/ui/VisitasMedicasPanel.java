package org.hospital.feature.internacion.ui;

import org.hospital.feature.internacion.ui.VisitasMedicasPanel;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import org.hospital.feature.internacion.domain.InternacionPaciente;
import org.hospital.feature.internacion.domain.ComentarioVisita;

/**
 * View panel for Visitas Medicas reports.
 * Shows patient internations and visit comments.
 */
public class VisitasMedicasPanel extends JPanel {
    private JTable tableInternaciones;
    private DefaultTableModel tableModelInternaciones;
    private JTable tableComentarios;
    private DefaultTableModel tableModelComentarios;
    private JComboBox<String> cmbTipoDocumento;
    private JTextField txtNroDocumento;
    private JButton btnLoadInternaciones;
    private JButton btnLoadComentarios;
    private JButton btnClear;
    
    public VisitasMedicasPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JPanel topPanel = createTopPanel();
        add(topPanel, BorderLayout.NORTH);
        
        JPanel centerPanel = createCenterPanel();
        add(centerPanel, BorderLayout.CENTER);
    }
    
    private JPanel createTopPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        // Info panel
        JPanel infoPanel = new JPanel();
        infoPanel.setBorder(BorderFactory.createTitledBorder("Seguimiento de Visitas Médicas"));
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        
        JLabel lblInfo1 = new JLabel("• Internaciones: Muestra las internaciones de un paciente específico");
        JLabel lblInfo2 = new JLabel("• Comentarios: Muestra los comentarios de las visitas médicas de una internación");
        
        lblInfo1.setFont(new Font("Arial", Font.PLAIN, 12));
        lblInfo2.setFont(new Font("Arial", Font.PLAIN, 12));
        
        infoPanel.add(lblInfo1);
        infoPanel.add(Box.createVerticalStrut(5));
        infoPanel.add(lblInfo2);
        
        panel.add(infoPanel, BorderLayout.CENTER);
        
        // Control panel
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        controlPanel.setBorder(BorderFactory.createTitledBorder("Búsqueda de Paciente"));
        
        controlPanel.add(new JLabel("Tipo Doc:"));
        cmbTipoDocumento = new JComboBox<>(new String[]{"DNI", "LC", "LE", "CI", "PASAPORTE"});
        controlPanel.add(cmbTipoDocumento);
        
        controlPanel.add(new JLabel("Nro. Documento:"));
        txtNroDocumento = new JTextField(15);
        controlPanel.add(txtNroDocumento);
        
        btnLoadInternaciones = new JButton("Buscar Internaciones");
        btnLoadComentarios = new JButton("Ver Comentarios");
        btnClear = new JButton("Limpiar");
        
        controlPanel.add(btnLoadInternaciones);
        controlPanel.add(btnLoadComentarios);
        controlPanel.add(btnClear);
        
        panel.add(controlPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createCenterPanel() {
        JPanel panel = new JPanel(new GridLayout(2, 1, 5, 5));
        
        // Internaciones table
        JPanel internacionesPanel = createInternacionesTablePanel();
        panel.add(internacionesPanel);
        
        // Comentarios table
        JPanel comentariosPanel = createComentariosTablePanel();
        panel.add(comentariosPanel);
        
        return panel;
    }
    
    private JPanel createInternacionesTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Internaciones del Paciente"));
        
        String[] columnNames = {"Nro. Internación", "Fecha Inicio", "Fecha Fin", "Estado"};
        tableModelInternaciones = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        tableInternaciones = new JTable(tableModelInternaciones);
        tableInternaciones.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        JScrollPane scrollPane = new JScrollPane(tableInternaciones);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createComentariosTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Comentarios de Visitas Médicas"));
        
        String[] columnNames = {"Nro. Int.", "Paciente", "Médico", "Fecha", "Hora Inicio", "Hora Fin", "Comentario"};
        tableModelComentarios = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        tableComentarios = new JTable(tableModelComentarios);
        tableComentarios.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        // Set wider column for comments
        tableComentarios.getColumnModel().getColumn(6).setPreferredWidth(300);
        
        JScrollPane scrollPane = new JScrollPane(tableComentarios);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    // Getters
    public String getTipoDocumento() { 
        return (String) cmbTipoDocumento.getSelectedItem(); 
    }
    
    public String getNroDocumento() { 
        return txtNroDocumento.getText().trim(); 
    }
    
    public void setTipoDocumento(String value) { 
        cmbTipoDocumento.setSelectedItem(value); 
    }
    
    public void setNroDocumento(String value) { 
        txtNroDocumento.setText(value); 
    }
    
    public void clearForm() {
        txtNroDocumento.setText("");
        tableModelComentarios.setRowCount(0);
    }
    
    public void clearAll() {
        txtNroDocumento.setText("");
        cmbTipoDocumento.setSelectedIndex(0);
        tableModelInternaciones.setRowCount(0);
        tableModelComentarios.setRowCount(0);
    }
    
    public void updateInternacionesTable(List<InternacionPaciente> internaciones) {
        tableModelInternaciones.setRowCount(0);
        for (InternacionPaciente i : internaciones) {
            tableModelInternaciones.addRow(new Object[]{
                i.getNroInternacion(), 
                i.getFechaInicio(), 
                i.getFechaFin() != null ? i.getFechaFin() : "En curso",
                i.getEstado()
            });
        }
    }
    
    public void updateComentariosTable(List<ComentarioVisita> comentarios) {
        tableModelComentarios.setRowCount(0);
        for (ComentarioVisita c : comentarios) {
            tableModelComentarios.addRow(new Object[]{
                c.getNroInternacion(),
                c.getPaciente(),
                c.getMedico(),
                c.getFechaRecorrido(),
                c.getHoraInicio(),
                c.getHoraFin(),
                c.getComentario()
            });
        }
    }
    
    public Integer getSelectedInternacion() {
        int row = tableInternaciones.getSelectedRow();
        if (row == -1) return null;
        return (Integer) tableModelInternaciones.getValueAt(row, 0);
    }
    
    // Button getters
    public JButton getBtnLoadInternaciones() { return btnLoadInternaciones; }
    public JButton getBtnLoadComentarios() { return btnLoadComentarios; }
    public JButton getBtnClear() { return btnClear; }
    public JTable getTableInternaciones() { return tableInternaciones; }
}

