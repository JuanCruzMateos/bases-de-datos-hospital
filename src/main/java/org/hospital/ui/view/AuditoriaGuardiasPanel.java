package org.hospital.ui.view;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import org.hospital.internacion.AuditoriaGuardia;
import java.text.SimpleDateFormat;

/**
 * View panel for Auditoria Guardias reports.
 * Shows audit trail of guard assignments.
 */
public class AuditoriaGuardiasPanel extends JPanel {
    private JTable tableAuditoria;
    private DefaultTableModel tableModel;
    private JButton btnActualizar;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    
    public AuditoriaGuardiasPanel() {
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
        infoPanel.setBorder(BorderFactory.createTitledBorder("Auditoría de Guardias"));
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        
        JLabel lblInfo1 = new JLabel("• Listado completo de auditoría sobre cambios en guardias médicas");
        JLabel lblInfo2 = new JLabel("• Muestra operaciones INSERT, UPDATE, DELETE con detalles de cambios");
        JLabel lblInfo3 = new JLabel("• Haga clic en 'Actualizar' para recargar los datos");
        
        lblInfo1.setFont(new Font("Arial", Font.PLAIN, 12));
        lblInfo2.setFont(new Font("Arial", Font.PLAIN, 12));
        lblInfo3.setFont(new Font("Arial", Font.PLAIN, 12));
        
        infoPanel.add(lblInfo1);
        infoPanel.add(Box.createVerticalStrut(5));
        infoPanel.add(lblInfo2);
        infoPanel.add(Box.createVerticalStrut(5));
        infoPanel.add(lblInfo3);
        
        panel.add(infoPanel, BorderLayout.CENTER);
        
        // Control panel with just the Actualizar button
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        btnActualizar = new JButton("Actualizar");
        controlPanel.add(btnActualizar);
        
        panel.add(controlPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createCenterPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Registros de Auditoría"));
        
        String[] columnNames = {
            "ID Auditoría", "Fecha/Hora Registro", "Usuario BD", "Operación", 
            "Nro. Guardia", "Fecha/Hora Guardia", "Matrícula", "Especialidad", 
            "Turno", "Detalle Anterior", "Detalle Nuevo"
        };
        
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        tableAuditoria = new JTable(tableModel);
        tableAuditoria.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tableAuditoria.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        
        // Set column widths for better visibility
        tableAuditoria.getColumnModel().getColumn(0).setPreferredWidth(90);   // ID
        tableAuditoria.getColumnModel().getColumn(1).setPreferredWidth(150);  // Fecha Reg
        tableAuditoria.getColumnModel().getColumn(2).setPreferredWidth(100);  // Usuario
        tableAuditoria.getColumnModel().getColumn(3).setPreferredWidth(80);   // Operación
        tableAuditoria.getColumnModel().getColumn(4).setPreferredWidth(90);   // Nro Guardia
        tableAuditoria.getColumnModel().getColumn(5).setPreferredWidth(150);  // Fecha Guardia
        tableAuditoria.getColumnModel().getColumn(6).setPreferredWidth(80);   // Matrícula
        tableAuditoria.getColumnModel().getColumn(7).setPreferredWidth(90);   // Especialidad
        tableAuditoria.getColumnModel().getColumn(8).setPreferredWidth(60);   // Turno
        tableAuditoria.getColumnModel().getColumn(9).setPreferredWidth(200);  // Detalle Old
        tableAuditoria.getColumnModel().getColumn(10).setPreferredWidth(200); // Detalle New
        
        JScrollPane scrollPane = new JScrollPane(tableAuditoria);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    public void updateTable(List<AuditoriaGuardia> auditorias) {
        tableModel.setRowCount(0);
        for (AuditoriaGuardia a : auditorias) {
            tableModel.addRow(new Object[]{
                a.getIdAuditoria(),
                a.getFechaHoraReg() != null ? dateFormat.format(a.getFechaHoraReg()) : "",
                a.getUsuarioBd(),
                a.getOperacion(),
                a.getNroGuardia(),
                a.getFechaHoraGuard() != null ? dateFormat.format(a.getFechaHoraGuard()) : "",
                a.getMatricula(),
                a.getCodEspecialidad(),
                a.getIdTurno(),
                a.getDetalleOld(),
                a.getDetalleNew()
            });
        }
    }
    
    // Button getter
    public JButton getBtnActualizar() { return btnActualizar; }
}

