package org.hospital.feature.internacion.ui;

import org.hospital.feature.internacion.ui.CamaDisponiblePanel;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import org.hospital.feature.internacion.domain.CamaDisponibleResumen;
import org.hospital.feature.internacion.domain.CamaDisponibleDetalle;

/**
 * View panel for Camas Disponibles reports.
 * Shows summary and detail of available beds by sector.
 */
public class CamaDisponiblePanel extends JPanel {
    private JTable tableResumen;
    private DefaultTableModel tableModelResumen;
    private JTable tableDetalle;
    private DefaultTableModel tableModelDetalle;
    private JTextField txtIdSector;
    private JButton btnLoadResumen;
    private JButton btnLoadDetalle;
    private JButton btnClear;
    
    public CamaDisponiblePanel() {
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
        infoPanel.setBorder(BorderFactory.createTitledBorder("Reportes de Camas Disponibles"));
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        
        JLabel lblInfo1 = new JLabel("• Resumen: Muestra la cantidad de camas libres agrupadas por sector");
        JLabel lblInfo2 = new JLabel("• Detalle: Muestra el listado detallado de camas libres de un sector específico");
        
        lblInfo1.setFont(new Font("Arial", Font.PLAIN, 12));
        lblInfo2.setFont(new Font("Arial", Font.PLAIN, 12));
        
        infoPanel.add(lblInfo1);
        infoPanel.add(Box.createVerticalStrut(5));
        infoPanel.add(lblInfo2);
        
        panel.add(infoPanel, BorderLayout.CENTER);
        
        // Control panel
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        controlPanel.setBorder(BorderFactory.createTitledBorder("Controles"));
        
        controlPanel.add(new JLabel("ID Sector (para detalle):"));
        txtIdSector = new JTextField(10);
        controlPanel.add(txtIdSector);
        
        btnLoadResumen = new JButton("Cargar Resumen");
        btnLoadDetalle = new JButton("Cargar Detalle");
        btnClear = new JButton("Limpiar");
        
        controlPanel.add(btnLoadResumen);
        controlPanel.add(btnLoadDetalle);
        controlPanel.add(btnClear);
        
        panel.add(controlPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createCenterPanel() {
        JPanel panel = new JPanel(new GridLayout(2, 1, 5, 5));
        
        // Resumen table
        JPanel resumenPanel = createResumenTablePanel();
        panel.add(resumenPanel);
        
        // Detalle table
        JPanel detallePanel = createDetalleTablePanel();
        panel.add(detallePanel);
        
        return panel;
    }
    
    private JPanel createResumenTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Resumen: Camas Libres por Sector"));
        
        String[] columnNames = {"ID Sector", "Descripción", "Camas Libres"};
        tableModelResumen = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        tableResumen = new JTable(tableModelResumen);
        tableResumen.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        JScrollPane scrollPane = new JScrollPane(tableResumen);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createDetalleTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Detalle: Camas Libres del Sector Seleccionado"));
        
        String[] columnNames = {"ID Sector", "Descripción", "Habitación", "Piso", "Orientación", "Nro Cama", "Estado"};
        tableModelDetalle = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        tableDetalle = new JTable(tableModelDetalle);
        tableDetalle.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        JScrollPane scrollPane = new JScrollPane(tableDetalle);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    // Getters
    public String getIdSector() { 
        return txtIdSector.getText().trim(); 
    }
    
    public void setIdSector(String value) { 
        txtIdSector.setText(value); 
    }
    
    public void clearForm() {
        txtIdSector.setText("");
        tableModelDetalle.setRowCount(0);
    }
    
    public void clearAll() {
        txtIdSector.setText("");
        tableModelResumen.setRowCount(0);
        tableModelDetalle.setRowCount(0);
    }
    
    public void updateResumenTable(List<CamaDisponibleResumen> resumenes) {
        tableModelResumen.setRowCount(0);
        for (CamaDisponibleResumen r : resumenes) {
            tableModelResumen.addRow(new Object[]{
                r.getIdSector(), 
                r.getDescripcion(), 
                r.getCamasLibres()
            });
        }
    }
    
    public void updateDetalleTable(List<CamaDisponibleDetalle> detalles) {
        tableModelDetalle.setRowCount(0);
        for (CamaDisponibleDetalle d : detalles) {
            tableModelDetalle.addRow(new Object[]{
                d.getIdSector(),
                d.getDescripcion(),
                d.getNroHabitacion(),
                d.getPiso(),
                d.getOrientacion(),
                d.getNroCama(),
                d.getEstado()
            });
        }
    }
    
    public Integer getSelectedSectorFromResumen() {
        int row = tableResumen.getSelectedRow();
        if (row == -1) return null;
        return (Integer) tableModelResumen.getValueAt(row, 0);
    }
    
    // Button getters
    public JButton getBtnLoadResumen() { return btnLoadResumen; }
    public JButton getBtnLoadDetalle() { return btnLoadDetalle; }
    public JButton getBtnClear() { return btnClear; }
    public JTable getTableResumen() { return tableResumen; }
}

